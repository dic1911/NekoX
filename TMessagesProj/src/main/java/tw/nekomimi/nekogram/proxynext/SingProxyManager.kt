package tw.nekomimi.nekogram.proxynext

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.FileLog
import org.telegram.messenger.SharedConfig
import tw.nekomimi.nekogram.proxy.GuardedProcessPool
import tw.nekomimi.nekogram.proxy.ProxyManager
import java.io.File

class SingProxyManager {

    companion object {
        val mainInstance = SingProxyManager()
        val testInstance = SingProxyManager()
    }

    val proxies = mutableListOf<SharedConfig.SingProxyInfo>()
    private val allocatedPorts = mutableSetOf<Int>()

    val isSingExist: Boolean by lazy {
        resolveProviders(ApplicationLoader.applicationContext).size == 1
    }

    val singPath: String? by lazy {
        val providers = resolveProviders(ApplicationLoader.applicationContext)
        val provider = providers.single().providerInfo
        provider?.metaData?.getString("nekox.messenger.sing.executable_path")
                ?.let { relativePath ->
                    File(provider.applicationInfo.nativeLibraryDir).resolve(relativePath).apply {
                    }.absolutePath
                }
    }

    private val singRunner: GuardedProcessPool = GuardedProcessPool {
        FileLog.e(it.toString())
    }

    @Volatile
    var isStarted: Boolean = false

    fun start() {
        synchronized(this) {
            if (isStarted) return
            isStarted = true
        }
    }

    fun stop() {

    }

    fun restart() {

    }

    private fun resolveProviders(context: Context): List<ResolveInfo> {
        val uri = Uri.Builder()
                .scheme("plugin")
                .authority("nekox.messenger.sing")
                .path("/sing-box")
                .build()
        val flags = PackageManager.GET_META_DATA
        return context.packageManager.queryIntentContentProviders(
                Intent("nekox.messenger.sing.ACTION_SING_PLUGIN", uri), flags
        ).filter { it.providerInfo.exported }
    }

    fun setProxyRemarks(proxy: ProxyConfig.SingProxyBean, remarks: String) {

    }

    fun registerProxy(proxyBing: ProxyConfig.SingProxyBean): SharedConfig.SingProxyInfo {
        var port = ProxyManager.mkPort()
        while (allocatedPorts.contains(port)) port = ProxyManager.mkPort()
        allocatedPorts.add(port)
        val proxy = SharedConfig.SingProxyInfo(port, proxyBing)
        proxies.add(proxy)
        return proxy;
    }

    fun unregister(info: SharedConfig.SingProxyInfo) {
        val reload = proxies.remove(info)
        if (proxies.size == 0)
            stop()
        else
            TODO()
    }

    fun clearProxies() {
        check(!isStarted)
        this.proxies.clear()
    }



}