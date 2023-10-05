package tw.nekomimi.nekogram.proxynext

import cn.hutool.core.codec.Base64
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONObject
import tw.nekomimi.nekogram.proxynext.Utils.parseInt

data class ShadowsocksRBean(
        var host: String = "",
        var port: Int = 443,
        var method: String = "aes-256-cfb",
        var password: String = "",
        var protocol: String = "origin",
        var protocol_param: String = "",
        var obfs: String = "plain",
        var obfs_param: String = ""
) : ProxyConfig.SingProxyBean() {

    override fun parseFromLink(link: String): ProxyConfig.SingProxyBean {
        val params = Base64.decodeStr(link.substringAfter(ProxyConfig.SSR_PROTOCOL)).split(":")

        host = params[0]
        port = params[1].parseInt()
        protocol = params[2]
        method = params[3]
        obfs = params[4]
        password = Base64.decodeStr(params[5].substringBefore("/"))

        val httpUrl = ("https://localhost" + params[5].substringAfter("/")).toHttpUrl()

        runCatching {
            obfs_param = Base64.decodeStr(httpUrl.queryParameter("obfsparam")!!)
        }
        runCatching {
            protocol_param = Base64.decodeStr(httpUrl.queryParameter("protoparam")!!)
        }
        runCatching {
            val remarks = httpUrl.queryParameter("remarks")
            if (remarks?.isNotBlank() == true) {
                this.remarks = Base64.decodeStr(remarks)
            }
        }
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

    companion object {

        val methods = arrayOf(

                "none",
                "table",
                "rc4",
                "rc4-md5",
                "rc4-md5-6",
                "aes-128-cfb",
                "aes-192-cfb",
                "aes-256-cfb",
                "aes-128-ctr",
                "aes-192-ctr",
                "aes-256-ctr",
                "bf-cfb",
                "camellia-128-cfb",
                "camellia-192-cfb",
                "camellia-256-cfb",
                "salsa20",
                "chacha20",
                "chacha20-ietf"

        )

        val protocols = arrayOf(
                "origin",
                "verify_simple",
                "verify_sha1",
                "auth_sha1",
                "auth_sha1_v2",
                "auth_sha1_v4",
                "auth_aes128_sha1",
                "auth_aes128_md5",
                "auth_chain_a",
                "auth_chain_b"
        )

        val obfses = arrayOf(
                "plain",
                "http_simple",
                "http_post",
                "tls_simple",
                "tls1.2_ticket_auth"
        )

    }
}
