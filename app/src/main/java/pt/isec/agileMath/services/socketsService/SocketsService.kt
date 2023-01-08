package pt.isec.agileMath.services.socketsService

import pt.isec.agileMath.models.MultiplayerConnection
import pt.isec.agileMath.models.messagePayloads.JsonParserInterface
import pt.isec.agileMath.models.messagePayloads.ServerMessagePayload

interface SocketsService {
    fun initServer()

    fun initServer(port: Int)

    fun connect(hostname: String, port: Int)

    fun <T> sendMessage(messagePayload: T) where T: JsonParserInterface

    fun <T> sendMessage(messagePayload: T, destinationConnection: MultiplayerConnection) where T: JsonParserInterface

    fun sendToAll(messagePayload: ServerMessagePayload)

    fun messagesReaderRoutine(socketConnection: MultiplayerConnection)

    fun close()
}