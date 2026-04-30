package com.answufeng.image

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities

/**
 * 网络状态监控器。
 *
 * 通过 [ConnectivityManager.NetworkCallback] 实时监听网络变化，
 * 缓存连接状态到 `@Volatile` 变量，[isConnected] 读取为 O(1) 操作。
 *
 * 线程约束：
 * - `ConnectivityManager.NetworkCallback` 的回调在主线程执行
 * - `isConnected` 可在任意线程调用（仅读取 `@Volatile` 变量）
 * - `ensureRegistered` 使用 double-checked locking 确保单次注册
 *
 * 生命周期：回调通过 `registerDefaultNetworkCallback` 注册，
 * 随 ApplicationContext 生命周期存在，不会泄漏。
 *
 * 首次调用 [isConnected] 时自动注册回调，后续调用直接读取缓存值。
 */
internal object ImageNetworkMonitor {

    @Volatile
    internal var isStrictNetworkForOffline: Boolean = true

    @Volatile
    private var connected: Boolean = false

    @Volatile
    private var registered = false

    private val connectivityListeners =
        java.util.concurrent.CopyOnWriteArrayList<(Boolean) -> Unit>()

    fun addOnConnectivityChangedListener(listener: (Boolean) -> Unit) {
        connectivityListeners.add(listener)
    }

    fun removeOnConnectivityChangedListener(listener: (Boolean) -> Unit) {
        connectivityListeners.remove(listener)
    }

    private fun hasUsableInternet(caps: NetworkCapabilities): Boolean {
        if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) return false
        return if (isStrictNetworkForOffline) {
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            true
        }
    }

    /**
     * 检查当前是否联网。
     *
     * 首次调用时注册 [ConnectivityManager.NetworkCallback]，
     * 后续调用直接读取 `@Volatile` 缓存值，无系统调用开销。
     */
    fun isConnected(context: Context): Boolean {
        ensureRegistered(context)
        return connected
    }

    private fun ensureRegistered(context: Context) {
        if (registered) return
        synchronized(this) {
            if (registered) return
            val appContext = context.applicationContext
            val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE)
                as? ConnectivityManager ?: return
            connected = queryCurrentState(cm)
            cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    val caps = cm.getNetworkCapabilities(network)
                    val nowConnected = caps != null && hasUsableInternet(caps)
                    connected = nowConnected
                    if (nowConnected) {
                        for (l in connectivityListeners) l.invoke(true)
                    }
                }

                override fun onLost(network: Network) {
                    connected = false
                    for (l in connectivityListeners) l.invoke(false)
                }

                override fun onCapabilitiesChanged(
                    network: Network, caps: NetworkCapabilities
                ) {
                    val nowConnected = hasUsableInternet(caps)
                    connected = nowConnected
                    for (l in connectivityListeners) l.invoke(nowConnected)
                }
            })
            registered = true
        }
    }

    private fun queryCurrentState(cm: ConnectivityManager): Boolean {
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return hasUsableInternet(caps)
    }
}
