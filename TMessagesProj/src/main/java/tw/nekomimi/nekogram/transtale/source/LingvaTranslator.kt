package tw.nekomimi.nekogram.transtale.source

import android.util.Log
import cn.hutool.core.util.XmlUtil
import cn.hutool.http.HttpUtil
import org.json.JSONObject
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.w3c.dom.Node
import tw.nekomimi.nekogram.NekoConfig
import tw.nekomimi.nekogram.transtale.Translator
import java.net.URLEncoder

object LingvaTranslator : Translator {

    var randomPathMap = hashMapOf<String, String>()

    override suspend fun doTranslate(from: String, to: String, query: String): String {
        val processedQuery = URLEncoder.encode(query, "utf-8").replace("+", " ")
        val instance = NekoConfig.customLingvaInstance.String().trim('/')
        if (instance.isNullOrBlank()) error(LocaleController.getString(R.string.LingvaInstanceNotConfigured))

        if (!randomPathMap.containsKey(instance)) {
            val get = HttpUtil.createGet(instance)
            val res = get.execute()
            if (!res.isOk) {
                error("ERROR ${res.status}: ${res.body()}")
            }
            try {
                val path = res.body().split("/_buildManifest.js")[0].split("/").last()
                randomPathMap[instance] = path
            } catch (ex: Exception) {
                Log.e("030-lv", "error getting path for translation api", ex)
                error(ex)
            }
        }

        val path = randomPathMap[instance]

        var req = HttpUtil.createGet("$instance/_next/data/$path/$from/$to/${processedQuery}.json")

        val res = req.execute()

        if (res.status != 200) {
            Log.d("030-lv-url", "$instance/_next/data/$path/$from/$to/${processedQuery}.json")
            Log.e("030-lv-err", "HTTP ${res.status} : ${res.body()}")
            error("HTTP ${res.status} : ${res.body()}")
        }

        val respObj = JSONObject(res.body())

        if (respObj.optString("error", "").isNotBlank()) {
            Log.d("030-lv", respObj.toString(2))
            error(respObj.toString(4))
        }

        return respObj.getJSONObject("pageProps").getString("translation")

    }

}