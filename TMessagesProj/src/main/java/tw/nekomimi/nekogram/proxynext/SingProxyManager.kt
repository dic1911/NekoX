package tw.nekomimi.nekogram.proxynext

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.FileLog
import tw.nekomimi.nekogram.proxy.GuardedProcessPool
import tw.nekomimi.nekogram.proxy.ProxyManager
import tw.nekomimi.nekogram.utils.ProxyUtil
import java.io.File

class SingProxyManager private constructor() {

    companion object {
        @JvmField
        val INSTANCE = SingProxyManager()

        @JvmField
        val TEST_INSTANCE = SingProxyManager()
    }

    val proxies = mutableListOf<ProxyConfig.BoxProxy>()
    private val allocatedPorts = mutableSetOf<Int>()

    val isSingExist: Boolean by lazy {
        resolveProviders(ApplicationLoader.applicationContext).size == 1
    }

    private val singPath: String? by lazy {
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

    fun setProxyRemarks(proxy: ProxyConfig.BoxProxy, remarks: String) {

    }

    fun allocatePort(proxy: ProxyConfig.BoxProxy): Int {
        var port = ProxyManager.mkPort()
        while (allocatedPorts.contains(port)) port = ProxyManager.mkPort()
        allocatedPorts.add(port)
        proxy.socks5Port = port
        return port
    }

    fun addProxy(boxProxy: ProxyConfig.BoxProxy) {
        this.proxies.add(boxProxy)
    }

    fun clearProxies() {
        check(!isStarted)
        this.proxies.clear()
    }

}