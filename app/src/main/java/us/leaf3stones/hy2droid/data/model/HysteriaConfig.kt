package us.leaf3stones.hy2droid.data.model

import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable

enum class ProxyType {
    HYSTERIA_2, HYSTERIA_GO, TUIC
}

data class HysteriaConfig(
    val proxyType: ProxyType = ProxyType.HYSTERIA_2,
    val userId: String = "",
    val server: String = "",
    val password: String = "",
    val sni: String = ""
) : Serializable {

     fun getJsonConfig(): String {
        val jsonConfig = JSONObject()
        val inbound = JSONObject()
        inbound.put("listen_addr", "127.0.0.1")
        inbound.put("listen_port", 1080)
        inbound.put("username", "")
        inbound.put("password", "")
        jsonConfig.put("inbound", inbound)

        val outbound = JSONObject()
        val serverParts = server.split(":")
        val serverAddr = serverParts.getOrElse(0) { "" }
        val serverPortOrPorts = serverParts.getOrElse(1) { "" }

        outbound.put("server_addr", serverAddr)

        when (proxyType) {
            ProxyType.HYSTERIA_GO -> {
                val serverPortsArray = JSONArray()
                serverPortOrPorts.split(",").forEach { port ->
                    serverPortsArray.put(port.trim().replace("-", ":"))
                }
                outbound.put("server_ports", serverPortsArray)
            }
            ProxyType.TUIC -> {
                outbound.put("server_port", serverPortOrPorts.toIntOrNull() ?: 0)
            }
            else -> {
                outbound.put("server_port", serverPortOrPorts.toIntOrNull() ?: 0)
            }
        }

        outbound.put("hop_interval", "30s")
        outbound.put("up_mbps", 100)
        outbound.put("down_mbps", 100)
        outbound.put("congestion_control", "bbr")
        outbound.put("token", password)
        outbound.put("uuid", userId)

        val alpnArray = JSONArray()
        alpnArray.put("h3")
        outbound.put("alpn", alpnArray)

        outbound.put("sni", sni)
        outbound.put("allow_insecure", false)
        jsonConfig.put("outbound", outbound)

        return jsonConfig.toString(2)
    }

    fun getYamlConfig(): String {
        val mapper = mapOf(SERVER_ADDRESS_PLACEHOLDER to server, PASSWORD_PLACEHOLDER to password)
        var resultingConf = HYSTERIA_CONFIG_TEXT_DATA
        for (m in mapper) {
            resultingConf = resultingConf.replace(m.key, m.value)
        }
        val sniData = if (sni.isBlank()) "" else getSniData(sni)
        return resultingConf.replace(SNI_PLACEHOLDER, sniData)
    }

    private fun getSniData(sni: String): String {
        return """
tls:
    sni: $sni
        """.trimIndent()
    }

    companion object {
        private const val SERVER_ADDRESS_PLACEHOLDER = "__SERVER_ADDRESS_PLACEHOLDER__"
        private const val PASSWORD_PLACEHOLDER = "__PASSWORD_PLACEHOLDER__"
        private const val SNI_PLACEHOLDER = "__SNI_PLACEHOLDER__"
        private const val HYSTERIA_CONFIG_TEXT_DATA = """
server: $SERVER_ADDRESS_PLACEHOLDER

auth: $PASSWORD_PLACEHOLDER

$SNI_PLACEHOLDER

bandwidth:
  up: 10 mbps
  down: 10 mbps

socks5:
  listen: 127.0.0.1:1080

transport:
  type: udp 
  udp:
    hopInterval: 30s
  
http:
  listen: 127.0.0.1:1081
  
quic:
  maxIdleTimeout: 30s 
  keepAlivePeriod: 20s 
  
"""
    }
}