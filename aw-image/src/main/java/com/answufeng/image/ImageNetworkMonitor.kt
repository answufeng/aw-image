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
