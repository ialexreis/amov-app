package pt.isec.agileMath.multiplayer

import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket


object SocketProperties {
    var socket: Socket? = null
    val socketIn: InputStream?
        get() = this.socket?.getInputStream()
    val socketOut: OutputStream?
        get() = this.socket?.getOutputStream()
    var serverSocket: ServerSocket? = null
    var threadCom: Thread? = null
}