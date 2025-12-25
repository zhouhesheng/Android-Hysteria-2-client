package us.leaf3stones.hy2droid.proxy

import android.util.Log


/**
 * Manages the tun2socks process that handles VPN traffic
 */
class TProxyService() : Tun2SocksControl {
    companion object {
        private val TAG = TProxyService::class.java.simpleName.toString()

        @JvmStatic
        @Suppress("FunctionName")
        private external fun TProxyStartService(configPath: String, fd: Int)

        @JvmStatic
        @Suppress("FunctionName")
        private external fun TProxyStopService()

        @JvmStatic
        @Suppress("FunctionName")
        private external fun TProxyGetStats(): LongArray?

        init {
            System.loadLibrary("hev-socks5-tunnel")
        }
    }

    /**
     * Starts the tun2socks process with the appropriate parameters.
     */
    override fun start(configPath: String, fd: Int) {
        Log.i(TAG, "Starting HevSocks5Tunnel via JNI")

        try {
            TProxyStartService(configPath, fd)
            Log.d(TAG, TProxyGetStats().contentToString())
        } catch (e: Exception) {
            Log.e(TAG, "HevSocks5Tunnel exception: ${e.message}")
        }
    }

    /**
     * Stops the tun2socks process
     */
    override fun stop() {
        try {
            Log.i(TAG, "TProxyStopService...")
            TProxyStopService()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop hev-socks5-tunnel", e)
        }
    }
}
