package tw.nekomimi.nekogram.transtale

import org.dizitart.no2.filters.FluentFilter
import org.dizitart.no2.repository.ObjectRepository
import org.telegram.messenger.LocaleController
import tw.nekomimi.nekogram.NekoConfig
import tw.nekomimi.nekogram.database.mkDatabase
import tw.nekomimi.nekogram.utils.UIUtil
import java.util.*
import kotlin.collections.HashMap

class TranslateDb(val code: String) {

    var conn: ObjectRepository<TransItem> = db.getRepository(TransItem::class.java, code)

    companion object {

        val db = mkDatabase("translate_caches")

        val repo = HashMap<Locale, TranslateDb>()
        val chat = db.getRepository(ChatLanguage::class.java, "chat")
        val ccTarget = db.getRepository(ChatCCTarget::class.java, "opencc")

        @JvmStatic fun getChatLanguage(chatId: Long, default: Locale): Locale {

            val cursor = chat.find(FluentFilter.where("chatId").eq(chatId))
            cursor.forEach { return it.language.code2Locale }
            return default
//            return if (cursor.isEmpty) default else cursor.first().language.code2Locale

        }

        @JvmStatic
        fun saveChatLanguage(chatId: Long, locale: Locale) = UIUtil.runOnIoDispatcher {

            chat.update(ChatLanguage(chatId, locale.locale2code), true)

        }

        @JvmStatic
        fun getChatCCTarget(chatId: Long, default: String?): String? {

            val cursor = ccTarget.find(FluentFilter.where("chatId").eq(chatId))
            cursor.forEach { return it as String? }
            return default
//            return ccTarget.find(FluentFilter.where("chatId").eq(chatId)).firstOrDefault()?.ccTarget
//                    ?: default

        }

        @JvmStatic
        fun saveChatCCTarget(chatId: Long, target: String) = UIUtil.runOnIoDispatcher {

            ccTarget.update(ChatCCTarget(chatId, target), true)

        }

        @JvmStatic
        fun currentTarget() = NekoConfig.translateToLang.String()?.transDbByCode
                ?: LocaleController.getInstance().currentLocale.transDb

        @JvmStatic
        fun forLocale(locale: Locale) = locale.transDb

        @JvmStatic
        fun currentInputTarget() = NekoConfig.translateInputLang.String().transDbByCode

        @JvmStatic
        fun clearAll() {

            db.listRepositories()
                    .filter { it  != "chat" }
                    .map { db.getCollection(it) }
                    .forEach { it.drop() }

            repo.clear()

        }

    }

    fun clear() = synchronized(this) {

        conn.drop()

    }

    fun contains(text: String): Boolean = synchronized(this) { return conn.find(FluentFilter.where("text").eq(text)).count() > 0 }

    fun save(text: String, trans: String) = synchronized<Unit>(this) {

        conn.update(TransItem(text, trans), true)

    }

    fun query(text: String): String? = synchronized(this) {

        val cursor = conn.find(FluentFilter.where("text").eq(text))
        cursor.forEach { return it.trans }
        return null // conn.find(FluentFilter.where("text").eq(text)).firstOrDefault()?.trans

    }

}