package tw.nekomimi.nekogram.proxynext

import android.annotation.SuppressLint
import cn.hutool.core.codec.Base64
import com.github.shadowsocks.plugin.PluginConfiguration
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject

data class ShadowsocksBean(
        var host: String = "",
        var port: Int = 443,
        var password: String = "",
        var method: String = "aes-256-cfb",
        var plugin: String = ""
) : ProxyConfig.SingProxyBean() {
    private val pluginOptions: MutableMap<String, String> = HashMap()
    companion object {
        val methods = arrayOf(
                "none",
                "rc4-md5",
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
                "chacha20-ietf",
                "aes-128-gcm",
                "aes-192-gcm",
                "aes-256-gcm",
                "chacha20-ietf-poly1305",
                "xchacha20-ietf-poly1305"

        )
    }

    @SuppressLint("NewApi")
    override fun parseFromLink(link: String): ProxyConfig.SingProxyBean {
        if (link.contains("@")) {
            // ss-android style
            val link = link.replace(ProxyConfig.SS_PROTOCOL, "https://").toHttpUrlOrNull()
                    ?: error("invalid ss-android link $link")

            if (link.password.isNotBlank()) {
                host = link.host
                port = link.port
                password = link.password
                method = link.username
                plugin = link.queryParameter("plugin") ?: ""
                remarks = link.fragment ?: ""
            } else {
                val methodAndPswd = Base64.decodeStr(link.username)
                host = link.host
                port = link.port
                password = methodAndPswd.substringAfter(":")
                method = methodAndPswd.substringBefore(":")
                plugin = link.queryParameter("plugin") ?: ""
                remarks = link.fragment ?: ""
            }
        } else {
            // v2rayNG style
            var v2Url = link
            if (v2Url.contains("#")) v2Url = v2Url.substringBefore("#")
            val link = ("https://" + Base64.decodeStr(v2Url.substringAfter(ProxyConfig.SS_PROTOCOL))).toHttpUrlOrNull()
                    ?: error("invalid v2rayNG link $link")
            host = link.host
            port = link.port
            password = link.password
            method = link.username
            plugin = ""
            remarks = link.fragment ?: ""
        }
        // init
        if (method == "plain") method = "none"
        // resole plugin
        val pl = PluginConfiguration(plugin)

        if (pl.selected.contains("v2ray") && pl.selected != "v2ray-plugin") {
            // v2ray plugin
//            pl.pluginsOptions["v2ray-plugin"] = pl.getOptions().apply { id = "v2ray-plugin" }
//            pl.pluginsOptions.remove(pl.selected)
            this.plugin = "v2ray-plugin"
            pl.pluginsOptions["v2ray-plugin"] = pl.getOptions().apply { id = "v2ray-plugin" }
            pl.getOptions().forEach { key, value ->
                run {
                    if (value != null)
                        this.pluginOptions[key] = value
                }
            }
        } else if (pl.selected == "obfs") {
            this.plugin = "obfs-local"
            pl.pluginsOptions["obfs-local"] = pl.getOptions().apply { id = "obfs-local" }
            pl.getOptions().forEach { key, value ->
                run {
                    if (value != null)
                        this.pluginOptions[key] = value
                }
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
}