package pt.isec.agileMath.services.socketsService

import android.util.Log
import pt.isec.agileMath.constants.GameState
import pt.isec.agileMath.models.MultiplayerConnection
import pt.isec.agileMath.models.messagePayloads.JsonParserInterface
import pt.isec.agileMath.models.messagePayloads.ServerMessagePayload
import pt.isec.agileMath.viewModels.gameViewModel.MultiplayerPlayerViewModel
import java.io.IOException
import java.net.Socket
import kotlin.concurrent.thread

class ClientSocketsService: SocketsService {
    private var viewModel: MultiplayerPlayerViewModel

    private var serverConnection: MultiplayerConnection? = null

    constructor(viewModel: MultiplayerPlayerViewModel) {
        this.viewModel = viewModel
    }

    override fun initServer() {}

    override fun initServer(port: Int) {}

    override fun connect(hostname: String, port: Int) {
        thread {
            try {
                val socket = Socket(hostname, port)
                val serverConnection = MultiplayerConnection(socket)

                this.serverConnection = serverConnection

                messagesReaderRoutine(serverConnection)

                viewModel.setGameState(GameState.CONNECTION_TO_SERVER_ESTABLISHED)

                Log.e("CONNECTION", "Connected to server")
            }catch (e: Exception) {
                Log.e("connectToServer", "Error connecting to server: $e")
                viewModel.setGameState(GameState.CONNECTION_TO_SERVER_ERROR)
            }
        }
    }

    override fun <T> sendMessage(messagePayload: T) where T: JsonParserInterface{
        thread {
            try {
                serverConnection?.socketOut?.println(messagePayload.toJson())
                serverConnection?.socketOut?.flush()

            } catch (e: Exception) {
                Log.e("replyToServer", "ERROR")
                viewModel.setGameState(GameState.SOCKET_ERROR)
            }
        }
    }

    override fun <T : JsonParserInterface> sendMessage(
        messagePayload: T,
        destinationConnection: MultiplayerConnection
    ) {}

    override fun messagesReaderRoutine(socketConnection: MultiplayerConnection) {
        thread {

            var clientUUID: String? = null

            while (true) {
                try {
                    val messageString = socketConnection.socketIn.readLine()

                    if (messageString.isNullOrEmpty() || messageString.isNullOrBlank()) {
                        throw IOException()
                    }

                    val messagePayload = ServerMessagePayload.fromString(messageString)


                    viewModel.onMessageReceived(socketConnection, messagePayload)
                } catch (e: IOException) {
                    viewModel.onConnectionLost(socketConnection, clientUUID)
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
        serverConnection?.socket?.close()
    }
}