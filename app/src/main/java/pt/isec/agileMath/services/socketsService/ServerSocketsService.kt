package pt.isec.agileMath.services.socketsService

import android.util.Log
import pt.isec.agileMath.constants.GameState
import pt.isec.agileMath.models.MultiplayerConnection
import pt.isec.agileMath.models.messagePayloads.ClientMessagePayload
import pt.isec.agileMath.models.messagePayloads.JsonParserInterface
import pt.isec.agileMath.models.messagePayloads.ServerMessagePayload
import pt.isec.agileMath.viewModels.gameViewModel.MultiplayerPlayerViewModel
import java.io.IOException
import java.net.ServerSocket
import kotlin.concurrent.thread

class ServerSocketsService: SocketsService {
    private var viewModel: MultiplayerPlayerViewModel

    private var serverSocket: ServerSocket? = null

    private val clientsConnectionsList = ArrayList<MultiplayerConnection>()

    constructor(viewModel: MultiplayerPlayerViewModel) {
        this.viewModel = viewModel
    }

    override fun initServer() {
        initServer(port)
    }

    override fun initServer(port: Int) {
        thread {
            try {
                serverSocket = ServerSocket(port)

                // TODO block connections when the game starts
                while (true) {
                    val clientSocket = serverSocket!!.accept()

                    val clientConnection = MultiplayerConnection(clientSocket)

                    messagesReaderRoutine(clientConnection)

                    clientsConnectionsList.add(clientConnection)

                    viewModel.setGameState(GameState.CONNECTION_ESTABLISHED)
                }

            } catch (e: Exception) {
                Log.e("Connection error", e.toString())

                viewModel.setGameState(GameState.CONNECTION_ERROR)
                this.close()
            }
        }
    }

    override fun connect(hostname: String, port: Int) {
        throw IOException("Not implemented in the server service")
    }

    override fun sendToAll(messagePayload: ServerMessagePayload) {
        for (connection in clientsConnectionsList) {
            sendMessage(messagePayload, connection)
        }
    }

    override fun <T> sendMessage(messagePayload: T, destinationConnection: MultiplayerConnection) where T: JsonParserInterface {
        thread {
            try {
                destinationConnection?.socketOut?.println(messagePayload.toJson())
                destinationConnection?.socketOut?.flush()
            } catch (e: Exception) {
                Log.e("replyToClient", "ERROR")
                viewModel.setGameState(GameState.SOCKET_ERROR)
            }
        }
    }

    override fun <T> sendMessage(messagePayload: T) where T: JsonParserInterface {}

    override fun messagesReaderRoutine(socketConnection: MultiplayerConnection) {
        thread {

            var clientUUID: String? = null

            while (true) {
                try {
                    val messageString = socketConnection.socketIn.readLine()

                    if (messageString.isNullOrEmpty() || messageString.isNullOrBlank()) {
                        throw IOException()
                    }

                    val messagePayload = ClientMessagePayload.fromString(messageString)
                    clientUUID = messagePayload.playerResult.player.uuid

                    viewModel.onClientMessageReceived(socketConnection, messagePayload)
                } catch (e: IOException) {
                    viewModel.onConnectionLost(socketConnection, clientUUID)
                    clientsConnectionsList.remove(socketConnection)
                    break
                }
                catch (e: Exception) {
                    e.printStackTrace()
                    break
                }
            }
        }
    }

    override fun close() {
        for (connection in clientsConnectionsList) {
            connection.socket.close()
        }

        serverSocket?.close()

        clientsConnectionsList.clear()
    }

    companion object {
        const val port = 8081
    }
}