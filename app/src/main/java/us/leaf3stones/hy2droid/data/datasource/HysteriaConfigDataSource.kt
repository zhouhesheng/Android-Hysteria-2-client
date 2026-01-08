package us.leaf3stones.hy2droid.data.datasource

import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import us.leaf3stones.hy2droid.App
import us.leaf3stones.hy2droid.data.KEY_IS_VPN_CONFIG_READY
import us.leaf3stones.hy2droid.data.KEY_VPN_CONFIG_PATH
import us.leaf3stones.hy2droid.data.MASTER_CONFIG_JSON_FILE_NAME
import us.leaf3stones.hy2droid.data.MASTER_HYSTERIA_CONFIG_FILE_NAME
import us.leaf3stones.hy2droid.data.model.HysteriaConfig
import us.leaf3stones.hy2droid.data.model.ProxyType
import us.leaf3stones.hy2droid.data.vpnPrefDataStore
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.charset.StandardCharsets

class HysteriaConfigDataSource() {

    suspend fun saveConfig(config: HysteriaConfig) = withContext(Dispatchers.IO) {
        val context = App.appContext!!

        if (config.proxyType != ProxyType.HYSTERIA_2) {
            val jsonConfigFile = File(context.filesDir, MASTER_CONFIG_JSON_FILE_NAME)
            FileOutputStream(jsonConfigFile).use {
                it.write(config.getJsonConfig().toByteArray(StandardCharsets.UTF_8))
            }
            context.vpnPrefDataStore.edit {
                it[KEY_IS_VPN_CONFIG_READY] = true
                it[KEY_VPN_CONFIG_PATH] = jsonConfigFile.absolutePath
            }
        } else {
            val vpnConfigFile = File(context.filesDir, MASTER_HYSTERIA_CONFIG_FILE_NAME)
            FileOutputStream(vpnConfigFile).use {
                it.write(config.getYamlConfig().toByteArray(StandardCharsets.UTF_8))
            }
            context.vpnPrefDataStore.edit {
                it[KEY_IS_VPN_CONFIG_READY] = true
                it[KEY_VPN_CONFIG_PATH] = vpnConfigFile.absolutePath
            }
        }

        val cachedFile = File(context.filesDir, MASTER_HYSTERIA_CONFIG_FILE_NAME + "_cached.binary")
        ObjectOutputStream(FileOutputStream(cachedFile)).use {
            it.writeObject(config)
        }
    }

    suspend fun loadConfig() : HysteriaConfig = withContext(Dispatchers.IO) {
        val context = App.appContext!!
        val vpnConfigFile = File(context.filesDir, MASTER_HYSTERIA_CONFIG_FILE_NAME)
        val cachedFile = File(vpnConfigFile.absolutePath + "_cached.binary")

        try {
            ObjectInputStream(FileInputStream(cachedFile)).use {
                val config = it.readObject() as? HysteriaConfig
                return@withContext config ?: HysteriaConfig() // Return a new instance if null
            }
        } catch (ignored: Exception) {
            return@withContext HysteriaConfig()
        }
    }
}