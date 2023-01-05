package pt.isec.agileMath.models

import kotlinx.coroutines.Job
import java.io.*
import java.net.ServerSocket
import java.net.Socket


class MultiplayerConnection
{
    var socket: Socket
        private set

    var socketIn: BufferedReader
        private set

    var socketOut: PrintWriter
        private set


    constructor(socket: Socket, messageReaderCoroutine: Thread? = null) {
        this.socket = socket

        this.socketIn = socket.getInputStream().bufferedReader()
        this.socketOut = PrintWriter(socket.getOutputStream());
    }
}
