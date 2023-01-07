package pt.isec.agileMath.viewModels.gameViewModel

import android.content.Context
import android.util.Log
import pt.isec.agileMath.activities.EditProfileActivity
import pt.isec.agileMath.constants.Constants
import pt.isec.agileMath.constants.GameState
import pt.isec.agileMath.models.MultiplayerConnection
import pt.isec.agileMath.models.Player
import pt.isec.agileMath.models.messagePayloads.ClientMessagePayload
import pt.isec.agileMath.models.Result
import pt.isec.agileMath.models.messagePayloads.ServerMessagePayload
import pt.isec.agileMath.services.socketsService.ClientSocketsService
import pt.isec.agileMath.services.socketsService.ServerSocketsService
import pt.isec.agileMath.services.socketsService.SocketsService

class MultiplayerPlayerViewModel: GameViewModel() {
    lateinit var player: Player

    var isHost: Boolean = false

    val playersConnected = arrayListOf<Result>()

    private var wasAlreadyInitialized = false

    private var socketsService: SocketsService? = null

    fun initMultiplayer(context: Context, isHost: Boolean) {
        this.isHost = isHost
        this.player = EditProfileActivity.getProfilePlayer(context)

        wasAlreadyInitialized = true

        if (isHost) {
            socketsService = ServerSocketsService(this)
            setGameState(GameState.START_AS_HOST)
            return
        }

        socketsService = ClientSocketsService(this)
        setGameState(GameState.START_AS_CLIENT)
    }

    fun endGame() {
        socketsService?.close()
        setGameState(GameState.CONNECTION_ENDED)
    }

    fun startServer() {
        socketsService?.initServer()
    }

    fun connectToServer(hostname: String) {
        socketsService?.connect(hostname, ServerSocketsService.port)
    }

    fun replyToServer(messagePayload: ClientMessagePayload) {
        socketsService?.sendMessage(messagePayload)
    }

    fun onConnectionLost(playerConnection: MultiplayerConnection, clientUUID: String?) {
        playersConnected.filter { it.player.uuid != clientUUID }

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

        socketsService?.close()
    }
}