package tw.nekomimi.nekogram.transtale

import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import cn.hutool.core.util.ArrayUtil
import cn.hutool.core.util.StrUtil
import org.apache.commons.lang3.LocaleUtils
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController
import org.telegram.messenger.MessageObject
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.R
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ChatActivity
import org.telegram.ui.Components.Bulletin
import org.telegram.ui.Components.TranslateAlert2
import tw.nekomimi.nekogram.NekoConfig
import tw.nekomimi.nekogram.cc.CCConverter
import tw.nekomimi.nekogram.cc.CCTarget
import tw.nekomimi.nekogram.transtale.source.*
import tw.nekomimi.nekogram.ui.PopupBuilder
import tw.nekomimi.nekogram.utils.UIUtil
import tw.nekomimi.nekogram.utils.receive
import tw.nekomimi.nekogram.utils.receiveLazy
import java.util.*


val String.code2Locale: Locale by receiveLazy<String, Locale> {
    var ret: Locale
    if (this == null || this.isBlank()) {
        ret = LocaleController.getInstance().currentLocale
    } else {
        val args = replace('-', '_').split('_')

        if (args.size == 1) {
            ret = Locale(args[0])
        } else {
            ret = Locale(args[0], args[1])
        }
    }
    ret
}

val Locale.locale2code by receiveLazy<Locale, String> {

    if (StrUtil.isBlank(country)) {
        language
    } else {
        "$language-$country"
    }

}

val LocaleController.LocaleInfo.locale by receiveLazy<LocaleController.LocaleInfo, Locale> { pluralLangCode.code2Locale }

val Locale.transDb by receive<Locale, TranslateDb> {

    TranslateDb.repo[this] ?: TranslateDb(locale2code).also {

        TranslateDb.repo[this] = it

    }

}

val String.transDbByCode by receive<String, TranslateDb> { code2Locale.transDb }

interface Translator {

    suspend fun doTranslate(from: String, to: String, query: String): String

    companion object {

        @Throws(Exception::class)
        suspend fun translate(query: String) = translate(
            NekoConfig.translateToLang.String()?.code2Locale
                ?: LocaleController.getInstance().currentLocale, query)

        const val providerGoogle = 1
        const val providerGoogleCN = 2
        const val providerYandex = 3
        const val providerLingo = 4
        const val providerMicrosoft = 5
        const val providerYouDao = 6
        const val providerDeepL = 7
        const val providerTelegram = 8

        @Throws(Exception::class)
        suspend fun translate(to: Locale, query: String): String {

            var language = to.language
            var country = to.country

            if (language == "in") language = "id"
            if (country.lowercase() == "duang") country = "CN"

            val provider = NekoConfig.translationProvider.Int()
            when (provider) {
                providerYouDao -> if (language == "zh") {
                    language = "zh-CHS"
                }
                providerDeepL -> language = language.toUpperCase()
                providerMicrosoft,
                providerGoogle,
                providerGoogleCN -> if (language == "zh") {
                    val countryUpperCase = country.toUpperCase()
                    if (countryUpperCase == "CN" || countryUpperCase == "DUANG") {
                        language = if (provider == providerMicrosoft) "zh-Hans" else "zh-CN"
                    } else if (countryUpperCase == "TW" || countryUpperCase == "HK") {
                        language = if (provider == providerMicrosoft) "zh-HanT" else "zh-TW"
                    }
                }

            }
            val translator = when (provider) {
                providerGoogle, providerGoogleCN -> GoogleAppTranslator
                providerYandex -> YandexTranslator
                providerLingo -> LingoTranslator
                providerMicrosoft -> MicrosoftTranslator
                providerYouDao -> YouDaoTranslator
                providerDeepL -> DeepLTranslator
                providerTelegram -> TelegramAPITranslator
                else -> throw IllegalArgumentException()
            }

            // FileLog.d("[Trans] use provider ${translator.javaClass.simpleName}, toLang: $toLang, query: $query")

            val result =  translator.doTranslate("auto", language, query).also {

                to.transDb.save(query, it)

            }

            if (language == "zh") {
                val countryUpperCase = country.toUpperCase()
                if (countryUpperCase == "CN") {
                    return CCConverter.get(CCTarget.SP).convert(result)
                } else if (countryUpperCase == "TW") {
                    return CCConverter.get(CCTarget.TT).convert(result)
                }
            }

            return result

        }

        @JvmStatic
        @JvmOverloads
        fun showTargetLangSelect(anchor: View, input: Boolean = false, full: Boolean = false, callback: (Locale) -> Unit) {

            val builder = PopupBuilder(anchor)

            var locales = (if (full) LocaleUtils.availableLocaleList()
                    .filter { it.variant.isBlank() } else LocaleController.getInstance()
                    .languages
                    .map { it.pluralLangCode }
                    .toSet()
                    .filter { !it.lowercase().contains("duang") }
                    .filter { NekoConfig.preferredTranslateTargetLangList.isEmpty() ||
                                NekoConfig.preferredTranslateTargetLangList.contains(it.lowercase()) }
                    .map { it.code2Locale })
                    .toTypedArray()

            val currLocale = LocaleController.getInstance().currentLocale

            for (i in locales.indices) {

                val defLang = if (!input) currLocale else Locale.ENGLISH

                if (locales[i] == defLang) {

                    locales = ArrayUtil.remove(locales, i)
                    locales = ArrayUtil.insert(locales, 0, defLang)

                    break

                }

            }

            val localeNames = arrayOfNulls<String>(if (full) locales.size else locales.size + 1)

            for (i in locales.indices) {

                localeNames[i] = if (!full && i == 0) {

                    LocaleController.getString("Default", R.string.Default) + " ( " + locales[i].getDisplayName(currLocale) + " )"

                } else {

                    locales[i].getDisplayName(currLocale)

                }

            }

            if (!full) {

                localeNames[localeNames.size - 1] = LocaleController.getString("More", R.string.More)

            }

            builder.setItems(localeNames.filterIsInstance<CharSequence>().toTypedArray()) { index: Int, _ ->

                if (index == locales.size) {

                    showTargetLangSelect(anchor, input, true, callback)

                } else {

                    callback(locales[index])

                }

            }

            builder.show()

        }

        @JvmStatic
        @JvmOverloads
        fun showCCTargetSelect(anchor: View, input: Boolean = true, callback: (String) -> Unit) {

            val builder = PopupBuilder(anchor)

            builder.setItems(arrayOf(
                    if (!input) LocaleController.getString("CCNo", R.string.CCNo) else null,
                    LocaleController.getString("CCSC", R.string.CCSC),
                    LocaleController.getString("CCSP", R.string.CCSP),
                    LocaleController.getString("CCTC", R.string.CCTC),
                    LocaleController.getString("CCHK", R.string.CCHK),
                    LocaleController.getString("CCTT", R.string.CCTT),
                    LocaleController.getString("CCJP", R.string.CCJP)
            )) { index: Int, _ ->
                callback(when (index) {
                    1 -> CCTarget.SC.name
                    2 -> CCTarget.SP.name
                    3 -> CCTarget.TC.name
                    4 -> CCTarget.HK.name
                    5 -> CCTarget.TT.name
                    6 -> CCTarget.JP.name
                    else -> ""
                })
            }

            builder.show()

        }

        @JvmStatic
        @JvmOverloads
        fun translate(to: Locale = NekoConfig.translateToLang.String()?.code2Locale
                ?: LocaleController.getInstance().currentLocale, query: String, translateCallBack: TranslateCallBack) {

            UIUtil.runOnIoDispatcher {

                runCatching {

                    val result = translate(to, query)

                    UIUtil.runOnUIThread(Runnable {

                        translateCallBack.onSuccess(result)

                    })

                }.onFailure {

                    translateCallBack.onFailed(it is UnsupportedOperationException, it.message
                            ?: it.javaClass.simpleName)

                }

            }

        }

        interface TranslateCallBack {

            fun onSuccess(translation: String)
            fun onFailed(unsupported: Boolean, message: String)

        }

        var transReqId: Int? = null

        @JvmStatic
        fun translateMessageBeforeSent(currentAccount: Int, text: CharSequence?) {
            translateMessageBeforeSent(currentAccount, text, "en")
        }

        @JvmStatic
        fun translateMessageBeforeSent(currentAccount: Int, text: CharSequence?, targetLang: String) {
            translateMessageBeforeSent(currentAccount, text, "en", true, null)
        }

        @JvmStatic
        fun translateMessageBeforeSent(currentAccount: Int, text: CharSequence?, targetLang: String, isSelfOutgoingMessage: Boolean, maybeChatActivity: ChatActivity?) {
            if (transReqId != null) {
                ConnectionsManager.getInstance(currentAccount).cancelRequest(transReqId!!, true)
                transReqId = null
            }

            var msgCount = 0
            var localIsSelfOutgoing = isSelfOutgoingMessage
            if (!isSelfOutgoingMessage) {
                val chatActivity = maybeChatActivity!!
                if (chatActivity.messagePreviewParams == null) {
                    Log.d("030-tx", "not forwarding, fix state")
                    localIsSelfOutgoing = true
                } else {
                    msgCount = chatActivity.messagePreviewParams.forwardMessages.messages.size

                    val target = ArrayList<MessageObject>()
                    chatActivity.messagePreviewParams.forwardMessages.getSelectedMessages(target)
                    target.forEach {
                        var isMsgText = false
                        var text = it.caption
                        if (text == null || text.trim().isBlank()) {
                            isMsgText = true
                            text = it.messageText
                        }

                        if (text.isNullOrBlank()) return@forEach

                        doTranslateWithOfficialApi(currentAccount, text, targetLang, { result ->
                            // Log.d("030-tx", "fwd: $text -> $result")
                            --msgCount
                            if (isMsgText) it.messageText = result
                            else it.caption = result
                        }, {
                            // Log.d("030-tx", "fwd: $text -> <FAILED>")
                            --msgCount
                        })
                    }
                }
            }

            val notiType = if (localIsSelfOutgoing) NotificationCenter.outgoingMessageTranslated
                            else NotificationCenter.forwardingMessageTranslated

            var result = "" as CharSequence

            val t = Thread {
                // wait until everything is translated
                while (msgCount > 0) {
                    // Log.d("030-tx", "wait...")
                    Thread.sleep(100)
                }
                // Log.d("030-tx", "ok")
                AndroidUtilities.runOnUIThread {
                    NotificationCenter.getInstance(currentAccount).postNotificationName(notiType, result)
                }
            }

            if (text.isNullOrBlank()) {
                t.start()
                return
            }

            doTranslateWithOfficialApi(currentAccount, text, targetLang, {
                result = it
                t.start()
            }, {
                if (localIsSelfOutgoing) {
                    Log.d("030-tx", "showing err toast")
                    AndroidUtilities.runOnUIThread {
                        NotificationCenter.getGlobalInstance().postNotificationName(
                            NotificationCenter.showBulletin,
                            Bulletin.TYPE_ERROR_SUBTITLE,
                            LocaleController.getString(
                                "TranslationFailedAlert2",
                                R.string.TranslationFailedAlert2
                            ), it.replaceFirst(Regex(" "), "")
                        )
                    }
                }
                result = it
                t.start()
            })

        }

        @JvmStatic
        fun doTranslateWithOfficialApi(currentAccount: Int, text: CharSequence?, targetLang: String, onSuccess: ((CharSequence) -> Unit)?, onFailure: ((CharSequence) -> Unit)?) {
            if (text.isNullOrBlank()) return
            val req: TLRPC.TL_messages_translateText = TLRPC.TL_messages_translateText()
            val textWithEntities: TLRPC.TL_textWithEntities = TLRPC.TL_textWithEntities()
            textWithEntities.text = text.toString()
            req.flags = req.flags or 2
            req.text.add(textWithEntities)
            var lang = "en" // 030: tmp
            if (lang != null) {
                lang = lang.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            }
            if ("nb" == lang) {
                lang = "no"
            }
            req.to_lang = targetLang
            transReqId = ConnectionsManager.getInstance(currentAccount).sendRequest(
                req
            ) { res: TLObject?, err: TLRPC.TL_error? ->
                AndroidUtilities.runOnUIThread {
                    transReqId = null
                    if (res != null && (res is TLRPC.TL_messages_translateResult &&
                                !(res as TLRPC.TL_messages_translateResult).result.isEmpty()) &&
                        (res as TLRPC.TL_messages_translateResult).result.get(0) != null &&
                        ((res as TLRPC.TL_messages_translateResult).result.get(0).text != null)
                    ) {
                        val processedText: TLRPC.TL_textWithEntities = TranslateAlert2.preprocess(
                            textWithEntities, (res as TLRPC.TL_messages_translateResult).result.get(0))
                        val translated: CharSequence? = SpannableStringBuilder.valueOf(processedText.text)
                        MessageObject.addEntitiesToText(
                            translated,
                            processedText.entities,
                            false,
                            true,
                            false,
                            false
                        )

                        if (translated != null) {
                            if (onSuccess != null) {
                                onSuccess(translated)
                            }
                        } else {
                            throw NullPointerException("null translated string")
                        }
                    } else {
                        Log.d("030-tx", "response exists? ${res != null}")
                        var errMsg = " NULL"
                        if (err != null) {
                            errMsg = " ${err.code} - ${err.text}"
                            Log.e("030-tx", errMsg)
                        }

                        if (onFailure != null) {
                            onFailure(errMsg)
                        }
                    }
                }
            }
        }
    }

}