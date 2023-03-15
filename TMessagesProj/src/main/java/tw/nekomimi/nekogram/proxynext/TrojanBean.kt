package tw.nekomimi.nekogram.proxynext

import com.google.gson.JsonObject
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject

data class TrojanBean(var address: String = "",
                      var port: Int = 0,
                      var id: String = "",
                      var requestHost: String = "",
                      var remarks: String = "") : ProxyConfig.BoxProxy() {

    override fun parseFromLink(link: String) {

        val parsed = link.replace(ProxyConfig.TROJAN_PROTOCOL, "https://")
                .toHttpUrlOrNull()
                ?: error("invalid trojan link $link")

        address = parsed.host
        port = parsed.port
        id = parsed.username

        if (parsed.password.isNotBlank()) {
            // https://github.com/trojan-gfw/igniter/issues/318
            id += ":" + parsed.password
        }

        requestHost = parsed.queryParameter("sni") ?: requestHost
        remarks = parsed.fragment ?: ""
    }

    override fun parseFromBoxConf(json: JSONObject) {
        TODO("Not yet implemented")
    }

    override fun generateBoxConf(): JSONObject {
        TODO("Not yet implemented")
    }

    override fun generateLink(): String {
        TODO("Not yet implemented")
    }

}
