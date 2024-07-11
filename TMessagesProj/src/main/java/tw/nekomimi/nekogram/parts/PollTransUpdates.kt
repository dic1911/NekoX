package tw.nekomimi.nekogram.parts

import org.telegram.tgnet.TLRPC
import tw.nekomimi.nekogram.NekoConfig
import tw.nekomimi.nekogram.transtale.TranslateDb
import tw.nekomimi.nekogram.transtale.code2Locale

fun postPollTrans(media: TLRPC.TL_messageMediaPoll, poll: TLRPC.TL_poll) {
    poll.translatedQuestion = media.poll.translatedQuestion
    poll.answers.forEach { answer ->
        val ans = media.poll.answers.find { it != null && it.text == answer.text }
        if (ans != null && answer != null)
            answer.translatedText = ans.translatedText
        else if (answer != null) {
            // workaround for null stuff
            val db = TranslateDb.forLocale(NekoConfig.translateToLang.String().code2Locale)
            answer.translatedText = db.query(answer.text.text) + " | " + answer.text.text
        }
    }
}