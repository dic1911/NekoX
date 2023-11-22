package tw.nekomimi.nekogram.proxynext

import android.widget.Toast
import org.json.JSONObject
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.FileLog
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.messenger.SharedConfig
import org.telegram.messenger.Utilities
import java.lang.Exception

object ProxyConfig {
    @JvmStatic
    fun parseSingBoxConfig(url: String): SingProxyBean? {
        try {
            if (url.startsWith(VMESS_PROTOCOL) || url.startsWith(VMESS1_PROTOCOL)) {
                return VMessBean().parseFromLink(url)
            } else if (url.startsWith(SS_PROTOCOL)) {
                return ShadowsocksBean().parseFromLink(url)
            } else if (url.startsWith(SSR_PROTOCOL)) {
                return ShadowsocksRBean().parseFromLink(url)
            } else if (url.startsWith(TROJAN_PROTOCOL)) {
                return TrojanBean().parseFromLink(url)
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

    @JvmStatic
    fun parseSingBoxConfig(outbound: JSONObject): SingProxyBean? {
        try {
            val boxConfig = when (outbound.opt("type")) {
                "shadowsocks" -> ShadowsocksBean().parseFromStorage(outbound)
                "shadowsocksr" -> ShadowsocksRBean().parseFromStorage(outbound)
                "vmess" -> VMessBean().parseFromStorage(outbound)
                "trojan" -> TrojanBean().parseFromStorage(outbound)
                else -> null
            }
            return boxConfig
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

    abstract class SingProxyBean {
        var remarks = ""
        abstract fun parseFromLink(link: String): SingProxyBean
        abstract fun parseFromStorage(json: JSONObject): SingProxyBean
        abstract fun generateStorageJson(): String
        abstract fun generateSingConfig() : JSONObject
        abstract fun generateLink(): String

        fun getHash(): String {
            val json = generateStorageJson().toString()
            return Utilities.MD5(json)
        }

        fun generateDummyProxyInfo(): SharedConfig.SingProxyInfo {
            val port = 11451;
            return SharedConfig.SingProxyInfo(port, this)
        }
    }
}