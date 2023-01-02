package pt.isec.agileMath.models

import kotlinx.coroutines.Job
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket


data class MultiplayerConnection(
    val socket: Socket,
    var messageReaderCoroutine: Job? = null
)
{
    val socketIn: InputStream?
        get() = socket?.getInputStream()
    val socketOut: OutputStream?
        get() = socket?.getOutputStream()
}
