package pers.zhc.frpss

object Configs {
    var SS_PORT = 15000.toUShort()
    var SS_ENCRYPTION = "chacha20-ietf-poly1305"
    var SS_PASSWORD = "password"
    var FRP_SERVER_ADDRESS = "x.x.x.x"
    var FRP_SERVER_PORT = 6666.toUShort()
    var FRP_REMOTE_PORT = 6000.toUShort()

    fun generateFrpConfig(): String {
        return """[common]
server_addr = $FRP_SERVER_ADDRESS
server_port = $FRP_SERVER_PORT

[frp-ss]
type = tcp
local_ip = 127.0.0.1
local_port = $SS_PORT
remote_port = $FRP_REMOTE_PORT
"""
    }
}
