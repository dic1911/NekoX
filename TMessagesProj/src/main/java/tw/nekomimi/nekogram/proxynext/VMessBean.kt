package tw.nekomimi.nekogram.proxynext

import cn.hutool.core.codec.Base64
import com.google.gson.Gson
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONObject
import tw.nekomimi.nekogram.proxynext.Utils.parseInt
import tw.nekomimi.nekogram.proxynext.Utils.urlDecode

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

data class VMessBean(var uuid: String = "123456",
                     var address: String = "",
                     var port: Int = 443,
                     var id: String = "",
                     var alterId: Int = 0,
                     var security: String = "auto",
                     var network: String = "tcp",
                     var headerType: String = "none",
                     var requestHost: String = "",
                     var path: String = "",
                     var streamSecurity: String = "",
                     var configVersion: Int = 2,
                     var testResult: String = "") : ProxyConfig.SingProxyBean() {

    override fun parseFromLink(link: String): ProxyConfig.SingProxyBean {
        check(link.startsWith("vmess://") || link.startsWith("vmess1://"))
        try {
            if (link.isBlank()) error("empty link")
            if (link.startsWith(ProxyConfig.VMESS_PROTOCOL)) {
                val indexSplit = link.indexOf("?")
                if (indexSplit > 0) {
                    resolveSimpleVmess1(link)
                } else {
                    var result = link.replace(ProxyConfig.VMESS_PROTOCOL, "")
                    result = Base64.decodeStr(result)
                    if (result.isBlank()) {
                        error("invalid url format")
                    }

                    if (result.contains("= vmess")) {
                         resolveSomeIOSAppShitCsvLink(result)
                    } else {
                        val vmessQRCode = Gson().fromJson(result, VMessQRCode::class.java)
                        if (vmessQRCode.add.isBlank() || vmessQRCode.port.isBlank()
                                || vmessQRCode.id.isBlank() || vmessQRCode.aid.isBlank()
                                || vmessQRCode.net.isBlank())
                            error("invalid vmess protocol")

                        security = "auto"
                        network = "tcp"
                        headerType = "none"

                        configVersion = vmessQRCode.v.parseInt()
                        remarks = vmessQRCode.ps
                        address = vmessQRCode.add
                        port = vmessQRCode.port.parseInt()
                        id = vmessQRCode.id
                        alterId = vmessQRCode.aid.parseInt()
                        network = vmessQRCode.net
                        headerType = vmessQRCode.type
                        requestHost = vmessQRCode.host
                        path = vmessQRCode.path
                        streamSecurity = vmessQRCode.tls
                    }
                }
                upgradeServerVersion()
            } else if (link.startsWith(ProxyConfig.VMESS1_PROTOCOL)) {
                parseVmess1Link(link)
            } else {
                error("invalid protocol")
            }
        } catch (e: Exception) {
            throw IllegalArgumentException(e)
        }
        return this
    }

    private fun resolveSimpleVmess1(link: String) {
        var result = link.replace(ProxyConfig.VMESS_PROTOCOL, "")
        val indexSplit = result.indexOf("?")
        if (indexSplit > 0) {
            result = result.substring(0, indexSplit)
        }
        result = Base64.decodeStr(result)

        val arr1 = result.split('@')
        if (arr1.count() != 2) {
            error("unexpected VMess1 link format")
        }
        val arr21 = arr1[0].split(':')
        val arr22 = arr1[1].split(':')
        if (arr21.count() != 2 || arr21.count() != 2) {
            error("unexpected VMess1 link format")
        }

        address = arr22[0]
        port = arr22[1].parseInt()
        security = arr21[0]
        id = arr21[1]
        security = "chacha20-poly1305"
        network = "tcp"
        headerType = "none"
        remarks = ""
        alterId = 0
    }

    private fun parseVmess1Link(link: String) {
        val lnk = ("https://" + link.substringAfter(ProxyConfig.VMESS1_PROTOCOL)).toHttpUrl()

        address = lnk.host
        port = lnk.port
        id = lnk.username
        remarks = lnk.fragment ?: ""

        lnk.queryParameterNames.forEach {
            when (it) {
                "tls" -> if (lnk.queryParameter(it) == "true") streamSecurity = "tls"
                "network" -> {
                    network = lnk.queryParameter(it)!!
                    if (network in arrayOf("http", "ws")) {
                        path = lnk.encodedPath.urlDecode()
                    }
                }
                "header" -> {
                    headerType = lnk.queryParameter(it)!!
                }
            }
        }
    }

    private fun resolveSomeIOSAppShitCsvLink(csv: String) {

        val args = csv.split(",")
        address = args[1]
        port = args[2].toInt()
        security = args[3]
        id = args[4].replace("\"", "")
        args.subList(5, args.size).forEach {
            when {
                it == "over-tls=true" -> {
                    streamSecurity = "tls"
                }
                it.startsWith("tls-host=") -> {
                    requestHost = it.substringAfter("=")
                }
                it.startsWith("obfs=") -> {
                    network = it.substringAfter("=")
                }

                it.startsWith("obfs-path=") || it.contains("Host:") -> {
                    runCatching {
                        path = it
                                .substringAfter("obfs-path=\"")
                                .substringBefore("\"obfs")
                    }
                    runCatching {
                        requestHost = it
                                .substringAfter("Host:")
                                .substringBefore("[")
                    }
                }
            }
        }
    }

    private fun upgradeServerVersion(): Int {
        try {
            if (configVersion == 2) {
                return 0
            }

            when (network) {
                "ws" -> {
                    var path = ""
                    var host = ""
                    val lstParameter = requestHost.split(";")
                    if (lstParameter.isNotEmpty()) {
                        path = lstParameter[0].trim()
                    }
                    if (lstParameter.size > 1) {
                        path = lstParameter[0].trim()
                        host = lstParameter[1].trim()
                    }
                    this.path = path
                    this.requestHost = host
                }
                "h2" -> {
                    var path = ""
                    var host = ""
                    val lstParameter = requestHost.split(";")
                    if (lstParameter.isNotEmpty()) {
                        path = lstParameter[0].trim()
                    }
                    if (lstParameter.size > 1) {
                        path = lstParameter[0].trim()
                        host = lstParameter[1].trim()
                    }
                    this.path = path
                    this.requestHost = host
                }
            }
            configVersion = 2
            return 0
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }
    }

    override fun parseFromBoxConf(json: JSONObject): ProxyConfig.SingProxyBean {
        return this
    }

    override fun generateBoxConf(): JSONObject {
        TODO("Not yet implemented")
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