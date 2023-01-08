package pt.isec.agileMath.viewModels.gameViewModel

import android.content.Context
import android.util.Log
import kotlinx.coroutines.delay
import pt.isec.agileMath.activities.EditProfileActivity
import pt.isec.agileMath.constants.Constants
import pt.isec.agileMath.constants.GameState
import pt.isec.agileMath.models.*
import pt.isec.agileMath.models.PlayerResult
import pt.isec.agileMath.models.messagePayloads.ClientMessagePayload
import pt.isec.agileMath.models.messagePayloads.ServerMessagePayload
import pt.isec.agileMath.services.socketsService.ClientSocketsService
import pt.isec.agileMath.services.socketsService.ServerSocketsService
import pt.isec.agileMath.services.socketsService.SocketsService
import kotlin.math.log

class MultiplayerPlayerViewModel: GameViewModel() {
    lateinit var player: MultiplayerPlayer

    var isHost: Boolean = false

    val generatedBoardsData = arrayListOf<Board>()

    // Key -> Player UUID
    var playersMap = mutableMapOf<String, MultiplayerPlayer>()
    val playersGameMap = mutableMapOf<String, Game>()
    val playersConnectionMap = mutableMapOf<String, MultiplayerConnection>()

    private var wasAlreadyInitialized = false

    private var socketsService: SocketsService? = null

    fun initMultiplayer(context: Context, isHost: Boolean) {
        this.isHost = isHost
        this.player = MultiplayerPlayer(
            PlayerResult(
                EditProfileActivity.getProfilePlayer(context)
            ),
        )
        wasAlreadyInitialized = true

        addNewPlayer(this.player, game,null)

        if (isHost) {
            socketsService = ServerSocketsService(this)
            game.board = getBoard(this.player.activeBoardIndex)

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

    fun replyToClients(gameState: GameState) {
        for (playerUUID in playersMap.keys) {
            val connection = playersConnectionMap[playerUUID]
            val player = playersMap[playerUUID]

            if (connection != null && player != null) {
                Log.e("REPLY TO CLIENTS", gameState.toString())
                socketsService?.sendMessage(generateServerPayloadResponse(player, gameState), connection)
            }
        }
    }

    fun onConnectionLost(playerConnection: MultiplayerConnection, clientUUID: String?) {
        removeConnectedPlayer(clientUUID)

        onConnectionLost(playerConnection)
    }

    fun onConnectionLost(playerConnection: MultiplayerConnection) {
        Log.e("onConnectionLost", "CONNECTION LOSS WITH CLIENT")
        setGameState(GameState.CLIENT_DISCONNECTED)
    }

    fun onMultiplayerGameStateChange(state: GameState) {
        when(state) {
            GameState.CONNECTION_TO_SERVER_ESTABLISHED -> {
                val clientMessagePayload = ClientMessagePayload(
                    PlayerResult(player.playerDetails.player),
                    GameState.CONNECT_CLIENT
                )
                replyToServer(clientMessagePayload)
            }
            GameState.GAME_STARTED -> {
                replyToClients(GameState.GAME_STARTED)
            }
            GameState.CLOCK_TICK -> {
                replyToClients(GameState.CLOCK_TICK)
            }
            else -> {}
        }

        Log.i("onMultiplayerGameStateChange", state.toString())
    }

    // ServerSocketService - Clients messages reader routine
    fun onClientMessageReceived(socketConnection: MultiplayerConnection, messagePayload: ClientMessagePayload) {

        var clientUUID = messagePayload.playerResult.player.uuid
        var player = playersMap[clientUUID]

        when(messagePayload.gameState) {
            GameState.CONNECT_CLIENT -> {
                val multiplayerPlayer = MultiplayerPlayer(messagePayload.playerResult)

                addNewPlayer(multiplayerPlayer, Game(), socketConnection)

                socketsService?.sendMessage(generateServerPayloadResponse(multiplayerPlayer, GameState.CLIENT_CONNECTED), socketConnection)
                setGameState(GameState.REFRESH_PLAYERS_LIST)
            }
            GameState.VALIDATE_EXPRESSION -> validateExpression(socketConnection, messagePayload)
            else -> {}
        }

        Log.e("onClientMessageReceived", messagePayload.gameState.toString())
    }

    // ClientSocketService - Clients messages reader routine
    fun onServerMessageReceived(socketConnection: MultiplayerConnection, messagePayload: ServerMessagePayload) {
        when(messagePayload.gameState) {
            GameState.CLIENT_CONNECTED -> {
                game = messagePayload.clientGame
                playersMap = messagePayload.players

                setGameState(GameState.REFRESH_PLAYERS_LIST)
            }
            GameState.CLOCK_TICK,
            GameState.CORRECT_EXPRESSION,
            GameState.FAILED_EXPRESSION -> game = messagePayload.clientGame
            else -> {}
        }


        Log.e("onServerMessageReceived", messagePayload.gameState.toString())
        setGameState(messagePayload.gameState)
    }


    override fun executeMove(positionFromTouch: Constants.BOARD_POSITION) {
        if (isHost) {
            setGameState(validateMyExpression(positionFromTouch)?: GameState.NONE)
            return
        }

        replyToServer(
            ClientMessagePayload(
                player.playerDetails,
                GameState.VALIDATE_EXPRESSION,
                positionFromTouch
            )
        )
    }

    override suspend fun nextLevelCountdownRoutine() {

    }

    override fun onCleared() {
        super.onCleared()

        socketsService?.close()
    }

    override suspend fun gameClockRoutine() {
        if (!isHost) {
            return
        }

        while (game.timer > 0) {
            delay(1000)

            for (playerGame in playersGameMap.values) {
                playerGame.clockTick()
            }

            setGameState(GameState.CLOCK_TICK)
        }

        setGameState(GameState.GAME_OVER_TIME_OUT)
    }

    private fun getBoard(index: Int): Board {
        if (index >= generatedBoardsData.size) {
            generatedBoardsData.add(game.getNewBoard())
        }

        return generatedBoardsData[index]
    }

    private fun addNewPlayer(multiplayerPlayer: MultiplayerPlayer, game: Game, socketConnection: MultiplayerConnection?) {
        val playerUUID = multiplayerPlayer.playerDetails.player.uuid

        synchronized(playersMap) {
            playersMap[playerUUID] = multiplayerPlayer
        }

        synchronized(playersGameMap) {
            playersGameMap[playerUUID] = game
        }

        if (socketConnection == null) {
            return
        }

        synchronized(playersConnectionMap) {
            playersConnectionMap[playerUUID] = socketConnection
        }
    }

    private fun removeConnectedPlayer(playerUUID: String?) {
        synchronized(playersMap) {
            playersMap.remove(playerUUID)
        }
        synchronized(playersGameMap) {
            playersGameMap.remove(playerUUID)
        }
        synchronized(playersConnectionMap) {
            playersConnectionMap.remove(playerUUID)
        }
    }

    private fun generateServerPayloadResponse(player: MultiplayerPlayer, gameState: GameState): ServerMessagePayload {
        var clientGame = playersGameMap[player.playerDetails.player.uuid]

        if (clientGame == null) {
            clientGame = game
        }

        return ServerMessagePayload(clientGame, playersMap, gameState)
    }

    private fun validateMyExpression(boardPosition: Constants.BOARD_POSITION): GameState? {
        val playerGame = playersGameMap[player.playerDetails.player.uuid] ?: return null
        val player = playersMap[player.playerDetails.player.uuid] ?: return null

        val gameState = playerGame.executeMove(boardPosition, player.playerDetails, true)

        when(gameState) {
            GameState.CORRECT_EXPRESSION -> {
                player.activeBoardIndex++
                playerGame.board = getBoard(player.activeBoardIndex)
            }
            // TODO
            // GameState.LEVEL_COMPLETED -> {}
            else -> {}
        }

        return gameState
    }

    private fun validateExpression(messagePayload: ClientMessagePayload): GameState? {
        val playerGame = playersGameMap[messagePayload.playerResult.player.uuid] ?: return null
        val player = playersMap[messagePayload.playerResult.player.uuid] ?: return null

        val gameState = playerGame.executeMove(messagePayload.boardPosition, player.playerDetails, true)

        when(gameState) {
            GameState.CORRECT_EXPRESSION -> {
                player.activeBoardIndex++
                playerGame.board = getBoard(player.activeBoardIndex)
            }
            // TODO
            // GameState.LEVEL_COMPLETED -> {}
            else -> {}
        }

        return gameState
    }

    private fun validateExpression(socketConnection: MultiplayerConnection, messagePayload: ClientMessagePayload) {
        var gameState = validateExpression(messagePayload) ?: return

        socketsService?.sendMessage(generateServerPayloadResponse(player, gameState), socketConnection)
    }

}