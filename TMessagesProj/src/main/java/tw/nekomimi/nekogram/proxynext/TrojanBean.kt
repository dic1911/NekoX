package tw.nekomimi.nekogram.proxynext

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject

data class TrojanBean(var address: String = "",
                      var port: Int = 0,
                      var id: String = "",
                      var requestHost: String = "")
    : ProxyConfig.SingProxyBean() {

    override fun parseFromLink(link: String): ProxyConfig.SingProxyBean {
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
        return this
    }

    override fun parseFromStorage(json: JSONObject): ProxyConfig.SingProxyBean {
        return this
    }

    override fun generateStorageJson(): String {
        TODO("Not yet implemented")
    }

    override fun generateLink(): String {
        TODO("Not yet implemented")
    }

}
