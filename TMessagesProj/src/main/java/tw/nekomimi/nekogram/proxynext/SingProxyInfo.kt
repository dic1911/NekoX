package tw.nekomimi.nekogram.proxynext

import org.telegram.messenger.SharedConfig

class SingProxyInfo(internalPort: Int, val proxyBean: ProxyConfig.SingProxyBean)
    : SharedConfig.ProxyInfo("127.0.0.1", internalPort, "", "", "") {

    override fun getHash(): String {
        return proxyBean.getHash()
    }

    override fun getLink(): String {
        return proxyBean.generateLink()
    }

    override fun ensureStarted() {
        super.ensureStarted()
    }

    override fun stop() {
        super.stop()
    }

    final override fun getProxyType(): Int {
        return SharedConfig.PROXY_TYPE_SING
    }
}