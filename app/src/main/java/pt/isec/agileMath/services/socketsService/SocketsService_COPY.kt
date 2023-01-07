package pt.isec.agileMath.services.socketsService

import android.util.Log
import java.net.Socket
import java.net.ServerSocket
import pt.isec.agileMath.constants.GameState
import pt.isec.agileMath.models.MultiplayerConnection
import pt.isec.agileMath.models.messagePayloads.ServerMessagePayload
import pt.isec.agileMath.models.messagePayloads.ClientMessagePayload
import pt.isec.agileMath.viewModels.gameViewModel.MultiplayerPlayerViewModel
import java.io.IOException
import kotlin.concurrent.thread

class SocketsService {
    private var serverSocket: ServerSocket? = null

    private var viewModel: MultiplayerPlayerViewModel
    private var serverPort = 8081

    private val clientsConnectionList = ArrayList<MultiplayerConnection>()

    private var connectionWithServer: MultiplayerConnection? = null

    constructor(viewModel: MultiplayerPlayerViewModel) {
        this.viewModel = viewModel
    }

    fun startServer() {
        thread {
            try {
                serverSocket = ServerSocket(serverPort)
                waitForConnections()
            } catch (_e: Exception) {
                _e.message?.let { Log.e("Connection error", it) }
                viewModel.setGameState(GameState.CONNECTION_ERROR)
                stopServer()
            }
        }
    }

    fun stopServer(){
        serverSocket?.close()
        serverSocket = null
    }

    fun connectToServer(hostName: String) {
        thread {
            try {
                val socket = Socket(hostName, serverPort)
                connectionWithServer = MultiplayerConnection(socket)

                serverMessagesReaderCoroutine(connectionWithServer!!)

                viewModel.setGameState(GameState.CONNECTION_TO_SERVER_ESTABLISHED)
                Log.e("CONNECTION", "Connected to server")
            }catch (e: Exception) {
                Log.e("connectToServer", "Error connecting to server: $e")
                viewModel.setGameState(GameState.CONNECTION_TO_SERVER_ERROR)
            }
        }
    }

    fun replyToServer(messagePayload: ClientMessagePayload) {
        thread {
            try {
                Log.e("replyToServer", messagePayload.gameState.toString())

                connectionWithServer?.socketOut?.println(messagePayload.toJson())
                connectionWithServer?.socketOut?.flush()
            } catch (e: Exception) {
                Log.e("replyToServer", "ERROR")
                viewModel.setGameState(GameState.SOCKET_ERROR)
            }
        }
    }

    // TODO use the payload model
    fun replyToAll() {}

    fun closeAll() {
        for (item in clientsConnectionList) {
            item.socket.close()
        }

        connectionWithServer?.close()

        clientsConnectionList.clear()
    }

    private fun clientMessagesReaderCoroutine(
        socketConnection: MultiplayerConnection
    ) {
        thread {

            var clientUUID: String? = null

            while (true) {
                try {
                    val messageString = socketConnection.socketIn.readLine()
                    val messagePayload = ClientMessagePayload.fromString(messageString)

                    clientUUID = messagePayload.playerResult.player.uuid

                    viewModel.onMessageReceived(socketConnection, messagePayload)
                } catch (e: IOException) {
                    viewModel.onConnectionLost(socketConnection, clientUUID)
                    break
                } catch (e: ClassCastException) { }
                catch (e: Exception) {
                    e.printStackTrace()
                    break
                }
            }
        }
    }

    private fun serverMessagesReaderCoroutine(
        socketConnection: MultiplayerConnection
    ) {
        thread {
            while (true) {
                try {
                    val messageString = socketConnection.socketIn.readLine()
                    val messagePayload = ServerMessagePayload.fromString(messageString)

                    viewModel.onMessageReceived(socketConnection, messagePayload)
                } catch (e: IOException) {
                    viewModel.onConnectionLost(socketConnection)
                    clientsConnectionList.remove(socketConnection)
                    break
                } catch (e: ClassCastException) { }
                catch (e: Exception) {
                    e.printStackTrace()
                    break
                }
            }
        }
    }

    private fun waitForConnections() {
       if (serverSocket == null) {
           return
       }

        while (true) {
            val clientSocket = serverSocket!!.accept()

            val clientConnection = MultiplayerConnection(clientSocket)

            clientMessagesReaderCoroutine(clientConnection)

            clientsConnectionList.add(clientConnection)

            viewModel.setGameState(GameState.CONNECTION_ESTABLISHED)
        }
    }
}