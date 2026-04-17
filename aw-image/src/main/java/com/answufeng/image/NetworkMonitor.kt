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
 * 首次调用 [isConnected] 时自动注册回调，后续调用直接读取缓存值。
 */
internal object NetworkMonitor {

    @Volatile
    private var connected: Boolean = false

    @Volatile
    private var registered = false

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
                    connected = true
                }

                override fun onLost(network: Network) {
                    connected = false
                }

                override fun onCapabilitiesChanged(
                    network: Network, caps: NetworkCapabilities
                ) {
                    connected = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                }
            })
            registered = true
        }
    }

    private fun queryCurrentState(cm: ConnectivityManager): Boolean {
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
