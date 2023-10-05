package tw.nekomimi.nekogram.proxynext

import cn.hutool.core.codec.Base64
import com.google.gson.Gson
import org.json.JSONObject
import tw.nekomimi.nekogram.proxynext.sagernet.StandardV2RayBean
import tw.nekomimi.nekogram.proxynext.sagernet.parseV2Ray

/*
    VMess / Trojan
 */

data class VMessQRCode(var v: String = "",
                       var ps: String = "",
                       var add: String = "",
                       var port: String = "",
                       var id: String = "",
                       var aid: String = "",
                       var net: String = "",
                       var type: String = "",
                       var host: String = "",
                       var path: String = "",
                       var tls: String = "") {}

class VMessBean() : ProxyConfig.SingProxyBean() {
    var standardV2RayBean = StandardV2RayBean()

    override fun parseFromLink(link: String): ProxyConfig.SingProxyBean {
        standardV2RayBean = parseV2Ray(link)
        return this
    }

    override fun parseFromStorage(json: JSONObject): ProxyConfig.SingProxyBean {
        standardV2RayBean = Gson().fromJson(json.toString(), StandardV2RayBean::class.java)
        return this
    }

    override fun generateStorageJson(): String {

    }

    override fun generateLink(): String {
        val qr = VMessQRCode().also {
            it.v = configVersion.toString()
            it.ps = remarks
            it.add = address
            it.port = port.toString()
            it.id = id
            it.aid = alterId.toString()
            it.net = network
            it.type = headerType
            it.host = requestHost
            it.path = path
            it.tls = streamSecurity
        }
        return ProxyConfig.VMESS_PROTOCOL + Base64.encode(Gson().toJson(qr))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VMessBean

        if (address != other.address) return false
        if (port != other.port) return false
        if (id != other.id) return false
        if (alterId != other.alterId) return false
        if (security != other.security) return false
        if (network != other.network) return false
        if (headerType != other.headerType) return false
        if (requestHost != other.requestHost) return false
        if (path != other.path) return false
        if (streamSecurity != other.streamSecurity) return false
        if (remarks != other.remarks) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + port
        result = 31 * result + id.hashCode()
        result = 31 * result + alterId
        result = 31 * result + security.hashCode()
        result = 31 * result + network.hashCode()
        result = 31 * result + headerType.hashCode()
        result = 31 * result + requestHost.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + streamSecurity.hashCode()
        result = 31 * result + remarks.hashCode()
        return result
    }

}