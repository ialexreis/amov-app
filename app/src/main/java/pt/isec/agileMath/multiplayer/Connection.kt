package pt.isec.agileMath.multiplayer

import android.util.Log
import java.net.Socket
import java.net.ServerSocket
import kotlin.concurrent.thread
import java.net.InetSocketAddress
import pt.isec.agileMath.constants.GameState
import pt.isec.agileMath.models.Game
import pt.isec.agileMath.viewModels.gameViewModel.GameViewModel
import java.io.BufferedReader

class Connection {
    private val sockProps : SocketProperties = SocketProperties
    private lateinit var viewModel: GameViewModel
    private var SERVER_PORT= 9999

    constructor(vm: GameViewModel) {
        this.viewModel = vm
    }

    fun startServer() {
        if (isCreatingSocket())
            return

        this.viewModel.setGameState(GameState.SERVER_CONNECTING)

        thread {
            this.sockProps.serverSocket = ServerSocket(SERVER_PORT)
            this.sockProps.serverSocket?.run {
                try {
                    val clientSocket = sockProps.serverSocket!!.accept()
                    startComms(clientSocket)
                } catch (_e: Exception) {
                    _e.message?.let { Log.e("Connection error", it) }
                    viewModel.setGameState(GameState.CONNECTION_ERROR)
                } finally {
                    sockProps.serverSocket?.close()
                    sockProps.serverSocket = null
                }
            }
        }
    }

    fun stopServer(){
        this.sockProps.serverSocket?.close()
        this.viewModel.setGameState(GameState.CONNECTION_ENDED)
        this.sockProps.serverSocket = null
    }

    private fun isCreatingSocket() : Boolean {
        return this.sockProps.serverSocket != null
                || sockProps.socket != null
                || this.viewModel.gameStateObserver.value == GameState.SETTING_PARAMETERS
    }

    private fun startComms(skt: Socket) {
        if(this.sockProps.threadCom != null)
            return

        this.sockProps.socket = skt
        this.sockProps.threadCom = thread {
            try {
                if(this.sockProps.socketIn == null)
                    return@thread

                this.viewModel.setGameState(GameState.CONNECTION_ESTABLISHED)
                val inputBuffer: BufferedReader = this.sockProps.socketIn!!.bufferedReader()
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
    }


}