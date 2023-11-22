package tw.nekomimi.nekogram.parts

import android.os.SystemClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.SharedConfig
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.ui.ProxyListActivity
import tw.nekomimi.nekogram.utils.UIUtil
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean

fun ProxyListActivity.checkProxyList(force: Boolean, context: ExecutorService) {
    GlobalScope.launch(Dispatchers.IO) {
        SharedConfig.proxyList.toList().forEach {
            if (it.checking || SystemClock.elapsedRealtime() - it.availableCheckTime < 2 * 60 * 1000L && !force) {
                return@forEach
            }
            it.checking = true
            runCatching {
                context.execute {
                    runCatching {
                        val lock = AtomicBoolean()
                        val startAt = SystemClock.elapsedRealtime()
                        UIUtil.runOnUIThread { NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.proxyCheckDone, it) }
                        checkSingleProxy(it, if (it.proxyType != SharedConfig.PROXY_TYPE_ORIGINAL) 3 else 1) {
                            AndroidUtilities.runOnUIThread {
                                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.proxyCheckDone, it)
                            }
                            lock.set(true)
                        }
                        while (!lock.get() && SystemClock.elapsedRealtime() - startAt < 4000L) Thread.sleep(100L)
                        if (!lock.get()) {
                            it.availableCheckTime = SystemClock.elapsedRealtime()
                            it.checking = false
                            it.available = false
                            it.ping = 0
                            it.stop()
                            AndroidUtilities.runOnUIThread {
                                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.proxyCheckDone, it)
                            }
                        }
                    }
                }
            }.onFailure {
                return@launch
            }
        }
    }

}