package tw.nekomimi.nekogram.proxynext

import android.widget.Toast
import org.json.JSONObject
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.FileLog
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import java.lang.Exception

object ProxyConfig {
    @JvmStatic
    fun parseSingBoxConfig(url: String): BoxProxy? {
        try {
            if (url.startsWith(VMESS_PROTOCOL) || url.startsWith(VMESS1_PROTOCOL)) {
                return VMessBean().also { it.parseFromLink(url) }
            } else if (url.startsWith(SS_PROTOCOL)) {
                return ShadowsocksBean().also { it.parseFromLink(url) }
            } else if (url.startsWith(SSR_PROTOCOL)) {
                return ShadowsocksRBean().also { it.parseFromLink(url) }
            } else if (url.startsWith(TROJAN_PROTOCOL)) {
                return TrojanBean().also { it.parseFromLink(url) }
            }
            return null
        } catch (ex: Exception) {
            FileLog.e(ex);
            Toast.makeText(ApplicationLoader.applicationContext,
                    LocaleController.getString("UnsupportedProxy", R.string.UnsupportedProxy),
                    Toast.LENGTH_LONG).show()
            return null
        }
    }

    const val VMESS_PROTOCOL: String = "vmess://"
    const val VMESS1_PROTOCOL = "vmess1://"
    const val SS_PROTOCOL: String = "ss://"
    const val SSR_PROTOCOL: String = "ssr://"
    const val TROJAN_PROTOCOL: String = "trojan://"
    const val WS_PROTOCOL: String = "ws://"
    const val WSS_PROTOCOL: String = "wss://"

    val SUPPORTED_PROTOCOLS = listOf(VMESS_PROTOCOL, VMESS1_PROTOCOL, SS_PROTOCOL, SSR_PROTOCOL, TROJAN_PROTOCOL)

    abstract class BoxProxy {
        var socks5Port: Int = 1080

        abstract fun parseFromLink(link: String)
        abstract fun parseFromBoxConf(json: JSONObject)
        abstract fun generateBoxConf(): JSONObject
        abstract fun generateLink(): String
    }
}