package pt.isec.agileMath.viewModels.gameViewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import pt.isec.agileMath.activities.EditProfileActivity
import pt.isec.agileMath.constants.Constants
import pt.isec.agileMath.constants.GameState
import pt.isec.agileMath.models.Game
import pt.isec.agileMath.models.MultiplayerConnection
import pt.isec.agileMath.models.Player
import pt.isec.agileMath.models.messagePayloads.ClientMessagePayload
import pt.isec.agileMath.models.Result
import pt.isec.agileMath.models.messagePayloads.ServerMessagePayload
import pt.isec.agileMath.services.SocketsService
import java.util.UUID

class MultiplayerPlayerViewModel: GameViewModel() {
    lateinit var player: Player

    var isHost: Boolean = false

    val playersConnected = arrayListOf<Result>()

    private var wasAlreadyInitialized = false

    private val multiplayerSocketService = SocketsService(this)

    fun initGame(context: Context, isHost: Boolean) {
        this.isHost = isHost
        this.player = EditProfileActivity.getProfilePlayer(context)

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

    fun replyToServer(messagePayload: ClientMessagePayload) {
        multiplayerSocketService.replyToServer(messagePayload)
    }


    fun onConnectionLost(playerConnection: MultiplayerConnection, clientUUID: String?) {
        playersConnected.filter { !it.player.uuid.equals(clientUUID) }

        onConnectionLost(playerConnection)
    }

    fun onConnectionLost(playerConnection: MultiplayerConnection) {
        Log.e("onConnectionLost", "CONNECTION LOSS WITH CLIENT")
        setGameState(GameState.SOCKET_ERROR)
    }

    fun onMultiplayerGameStateChange(state: GameState) {
        when(state) {
            GameState.CONNECTION_TO_SERVER_ESTABLISHED ->
                replyToServer(ClientMessagePayload(Result(player), GameState.CONNECT_CLIENT))
            else -> {}
        }

        Log.i("onMultiplayerGameStateChange", state.toString())
    }

    fun onMessageReceived(socketConnection: MultiplayerConnection, messagePayload: ClientMessagePayload) {
        when(messagePayload.gameState) {
            GameState.CONNECT_CLIENT -> {
                playersConnected.add(messagePayload.playerResult.copy())
                setGameState(GameState.REFRESH_PLAYERS_LIST)
            }
            else -> {}
        }

        Log.e("onMessageReceived", messagePayload.gameState.toString())
    }

    fun onMessageReceived(socketConnection: MultiplayerConnection, messagePayload: ServerMessagePayload) {
        when(messagePayload.gameState) {
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

    override fun onCleared() {
        super.onCleared()

        if (isHost) {
            multiplayerSocketService.stopServer()
        }

        multiplayerSocketService.closeAll()
    }
}