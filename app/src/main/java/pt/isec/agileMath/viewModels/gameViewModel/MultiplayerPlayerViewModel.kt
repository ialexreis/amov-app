package pt.isec.agileMath.viewModels.gameViewModel

import android.util.Log
import pt.isec.agileMath.constants.Constants
import pt.isec.agileMath.constants.GameState
import pt.isec.agileMath.models.Game
import pt.isec.agileMath.models.MultiplayerConnection
import pt.isec.agileMath.models.SocketMessagePayload
import pt.isec.agileMath.models.Result
import pt.isec.agileMath.services.multiplayerSockets.MultiplayerSocketsService

class MultiplayerPlayerViewModel: GameViewModel() {
    var isHost: Boolean = false

    val playersConnected = arrayListOf<Result>()

    private var wasAlreadyInitialized = false

    private val multiplayerSocketService = MultiplayerSocketsService(this)

    fun initGame(isHost: Boolean) {
        this.isHost = isHost
        wasAlreadyInitialized = true

        if (isHost) {
            setGameState(GameState.START_AS_HOST)
            return
        }

        setGameState(GameState.START_AS_CLIENT)
    }

    fun endGame() {
        if (isHost) {
            multiplayerSocketService.stopServer()
        }

        multiplayerSocketService.closeAll()

        setGameState(GameState.CONNECTION_ENDED)
    }

    fun startServer() {
        multiplayerSocketService.startServer()
    }

    fun connectToServer(hostname: String) {
        multiplayerSocketService.connectToServer(hostname)
    }

    fun replyToServer(messagePayload: SocketMessagePayload) {
        multiplayerSocketService.replyToServer(messagePayload)
    }

    fun onConnectionLost(playerConnection: MultiplayerConnection) {
        setGameState(GameState.SOCKET_ERROR)
    }

    fun onMultiplayerGameStateChange(state: GameState) {
        when(state) {
            GameState.CONNECTION_TO_SERVER_ESTABLISHED -> replyToServer(SocketMessagePayload(Game(), Result(), GameState.CONNECT_CLIENT))
            else -> {}
        }

        Log.i("onMultiplayerGameStateChange", state.toString())
    }

    fun onMessageReceived(socketConnection: MultiplayerConnection, messagePayload: SocketMessagePayload) {
        when(messagePayload.gameState) {
            GameState.CONNECT_CLIENT -> {
                playersConnected.add(messagePayload.playerResult.copy())
                setGameState(GameState.REFRESH_PLAYERS_LIST)
            }
            else -> {}
        }

        Log.e("onMessageReceived", messagePayload.gameState.toString())
    }

    override fun executeMove(positionFromTouch: Constants.BOARD_POSITION) {
        val gameState = game.executeMove(positionFromTouch, result)

        setGameState(gameState)
    }

    override suspend fun nextLevelCountdownRoutine() {

    }
}