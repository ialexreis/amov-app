package pt.isec.agileMath.models

import java.io.*
import java.net.Socket


class MultiplayerConnection
{
    var socket: Socket
        private set

    var socketIn: BufferedReader
        private set

    var socketOut: PrintWriter
        private set


    constructor(socket: Socket) {
        this.socket = socket

        this.socketIn = socket.getInputStream().bufferedReader()
        this.socketOut = PrintWriter(socket.getOutputStream());
    }

    fun close() {
        socket.close()
    }
}
