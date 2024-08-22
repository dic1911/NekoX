package tw.nekomimi.nekogram.transtale.source

import android.os.Build
import cn.hutool.core.lang.UUID
import cn.hutool.http.HttpUtil
import org.json.JSONObject
import tw.nekomimi.nekogram.transtale.Translator
import tw.nekomimi.nekogram.utils.applyUserAgent
import java.net.URLEncoder

object YandexTranslator : Translator {

    val uuid = UUID.fastUUID().toString(true)

    override suspend fun doTranslate(from: String, to: String, query: String): String {

        val uuid2 = UUID.fastUUID().toString(true)

        var req = HttpUtil.createPost("https://translate.yandex.net/api/v1/tr.json/translate?srv=android&uuid=$uuid&id=$uuid2-9-0")
            .applyUserAgent()

        req = if (Build.VERSION.SDK_INT > 25) {
            req
                .form("text", query)
                .form("lang", if (from == "auto") to else "$from-$to")
        } else {
            // .form would cause NoClassDefFoundError in ConverterRegistry
            req.body("lang=${if (from == "auto") to else "$from-$to"}&text=${URLEncoder.encode(query)}")
        }

        val response = req.execute()

        if (response.status != 200) {

            error("HTTP ${response.status} : ${response.body()}")

        }

        val respObj = JSONObject(response.body())

        if (respObj.optInt("code", -1) != 200) error(respObj.toString(4))

        return respObj.getJSONArray("text").getString(0)

    }

}