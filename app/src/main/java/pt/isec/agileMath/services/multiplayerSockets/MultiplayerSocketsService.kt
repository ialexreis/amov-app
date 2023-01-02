package pt.isec.agileMath.services.multiplayerSockets

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.net.Socket
import java.net.ServerSocket
import pt.isec.agileMath.constants.GameState
import pt.isec.agileMath.models.MultiplayerConnection
import pt.isec.agileMath.models.SocketMessagePayload
import pt.isec.agileMath.viewModels.gameViewModel.MultiplayerPlayerViewModel
import kotlin.concurrent.thread

class MultiplayerSocketsService {
    private var serverSocket: ServerSocket? = null

    private var viewModel: MultiplayerPlayerViewModel
    private var serverPort = 8081

    private val clientsConnectionList = ArrayList<MultiplayerConnection>()

    private var connectionWithServer: MultiplayerConnection? = null

    constructor(viewModel: MultiplayerPlayerViewModel) {
        this.viewModel = viewModel
    }

    fun startServer() {
        viewModel.viewModelScope.launch {
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
                val connection = MultiplayerConnection(socket)

                connection.messageReaderCoroutine = messageReaderCoroutine(connection)

                connectionWithServer = connection

                viewModel.setGameState(GameState.CONNECTION_ESTABLISHED)
                Log.e("CONNECTION", "Connected to server")
            }catch (e: Exception) {
                Log.e("connectToServer", "Error connecting to server: $e")
                viewModel.setGameState(GameState.CONNECTION_TO_SERVER_ERROR)
            }
        }
    }

    fun replyToServer(messagePayload: SocketMessagePayload) {
        connectionWithServer?.socketOut?.write(SocketMessagePayload.toJSONByteArray(messagePayload))
    }

    // TODO use the payload model
    fun replyToAll() {}

    fun closeAll() {
        for (item in clientsConnectionList) {
            item.socket.close()
            item.messageReaderCoroutine?.cancel()
        }

        clientsConnectionList.clear()
    }

    private fun startClientConnection(socket: Socket) {
        /*
        MultiplayerConnection.socket = skt
        MultiplayerConnection.threadCom = thread {
            try {
                if(MultiplayerConnection.socketIn == null)
                    return@thread

                this.viewModel.setGameState(GameState.CONNECTION_ESTABLISHED)
                val inputBuffer: BufferedReader = MultiplayerConnection.socketIn!!.bufferedReader()
                while (this.viewModel.gameStateObserver.value !in arrayOf(GameState.GAME_OVER)){
                    val message = inputBuffer.readLine()
                    if(message.toString().isNotEmpty()){
                        // communication between games
                    }
                }
            } catch (_e: Exception) {
                var err = _e.message
            } finally {
                // stop game
            }
        }
        */
    }

    private fun messageReaderCoroutine(
        socketConnection: MultiplayerConnection
    ) = viewModel.viewModelScope.launch {
        while (true) {
            try {
                val messageBytes = socketConnection.socketIn?.readBytes()

                if (messageBytes?.isEmpty() == true) {
                    continue
                }

                val messagePayload = SocketMessagePayload.fromByteArray(messageBytes!!)

                Log.e("READER_ROUTINE", "State: ${messagePayload.gameState}")

                viewModel.onMessageReceived(socketConnection, messagePayload)
            } catch (e: Exception) {
                viewModel.onConnectionLost(socketConnection)
                break
            }

        }
    }

    private fun waitForConnections() {
       if (serverSocket == null) {
           return
       }

        while (true) {
            // Wait for client connection
            Log.e("SERVER", "Waiting Connections")
            val clientSocket = serverSocket!!.accept()
            Log.e("SERVER", "Connection Established")

            val clientConnection = MultiplayerConnection(clientSocket)
            clientConnection.messageReaderCoroutine = messageReaderCoroutine(clientConnection)

            clientsConnectionList.add(clientConnection)
        }
    }
}