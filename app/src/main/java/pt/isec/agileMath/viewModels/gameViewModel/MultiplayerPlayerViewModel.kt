package pt.isec.agileMath.viewModels.gameViewModel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.isec.agileMath.constants.Constants
import pt.isec.agileMath.constants.GameState
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

    }

    fun onMultiplayerGameStateChange(state: GameState) {
        when(state) {
            else -> {}
        }
    }

    fun onMessageReceived(socketConnection: MultiplayerConnection, messagePayload: SocketMessagePayload) {
        when(messagePayload.gameState) {
            GameState.CLIENT_CONNECTED -> {
                playersConnected.add(messagePayload.playerResult.copy())
                setGameState(GameState.REFRESH_PLAYERS_LIST)
            }
            else -> {}
        }
    }

    override fun executeMove(positionFromTouch: Constants.BOARD_POSITION) {
        val gameState = game.executeMove(positionFromTouch, result)

        setGameState(gameState)
    }

    override suspend fun nextLevelCountdownRoutine() {

    }
}