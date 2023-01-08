package pt.isec.agileMath.viewModels.gameViewModel

import android.content.Context
import android.util.Log
import kotlinx.coroutines.delay
import pt.isec.agileMath.activities.EditProfileActivity
import pt.isec.agileMath.activities.MainMenuActivity
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

    var isHost = false
    var isGameStarted = false

    var firstFinishGame = true

    // Key -> Player UUID
    var playersMap = mutableMapOf<String, MultiplayerPlayer>()
    private val playersGameMap = mutableMapOf<String, Game>()
    private val playersConnectionMap = mutableMapOf<String, MultiplayerConnection>()
    private val generatedBoardsData = arrayListOf<Board>()

    private var wasAlreadyInitialized = false

    private var socketsService: SocketsService? = null

    fun initMultiplayer(context: Context, isHost: Boolean) {
        if (wasAlreadyInitialized) {
            return
        }

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
            val connection = playersConnectionMap[playerUUID] ?: continue

            if (playersMap[playerUUID]?.lostGame == true
                && gameState != GameState.REFRESH_PLAYERS_LIST
                && gameState != GameState.GAME_OVER)
            { continue }

            socketsService?.sendMessage(generateServerPayloadResponse(playerUUID, gameState), connection)

        }
    }

    fun onConnectionLost(playerConnection: MultiplayerConnection, clientUUID: String?) {
        removeConnectedPlayer(clientUUID)

        onConnectionLost(playerConnection)
    }

    fun onConnectionLost(playerConnection: MultiplayerConnection) {
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
            GameState.CLOCK_TICK -> replyToClients(GameState.CLOCK_TICK)
            GameState.NEW_LEVEL_COUNTDOWN_STARTED -> initCountdownToNextLevel()
            else -> {}
        }

        Log.i("onMultiplayerGameStateChange", state.toString())
    }

    // ServerSocketService - Clients messages reader routine
    fun onClientMessageReceived(socketConnection: MultiplayerConnection, messagePayload: ClientMessagePayload) {

        var playerUUID = messagePayload.playerResult.player.uuid

        when(messagePayload.gameState) {
            GameState.CONNECT_CLIENT -> {
                val multiplayerPlayer = MultiplayerPlayer(messagePayload.playerResult)
                val playerGame = Game(getBoard(multiplayerPlayer.activeBoardIndex))

                addNewPlayer(multiplayerPlayer, playerGame, socketConnection)

                socketsService?.sendMessage(generateServerPayloadResponse(playerUUID, GameState.CLIENT_CONNECTED), socketConnection)
                setGameState(GameState.REFRESH_PLAYERS_LIST)
            }
            GameState.VALIDATE_EXPRESSION -> {
                var gameState = validateExpression(socketConnection, messagePayload)

                if (gameState == GameState.LEVEL_COMPLETED && isEveryLevelFinished()) {
                    setGameState(GameState.NEW_LEVEL_COUNTDOWN_STARTED)
                    replyToClients(GameState.NEW_LEVEL_COUNTDOWN_STARTED)
                }

                replyToClients(GameState.REFRESH_PLAYERS_LIST)
                setGameState(GameState.REFRESH_PLAYERS_LIST)
            }
            else -> {}
        }
    }

    // ClientSocketService - Clients messages reader routine
    fun onServerMessageReceived(socketConnection: MultiplayerConnection, messagePayload: ServerMessagePayload) {
        when(messagePayload.gameState) {
            GameState.CLIENT_CONNECTED -> {
                game = messagePayload.clientGame
                playersMap = messagePayload.players

                setGameState(GameState.REFRESH_PLAYERS_LIST)
            }
            GameState.REFRESH_PLAYERS_LIST -> playersMap = messagePayload.players
            GameState.CLOCK_TICK -> game = messagePayload.clientGame
            GameState.FAILED_EXPRESSION -> game = messagePayload.clientGame
            GameState.CORRECT_EXPRESSION -> updateAllData(messagePayload)
            GameState.LEVEL_COMPLETED -> updateAllData(messagePayload)

            else -> {}
        }
        setGameState(messagePayload.gameState)
    }

    override fun startGame() {
        this.isGameStarted = true

        super.startGame()
    }

    override fun executeMove(positionFromTouch: Constants.BOARD_POSITION) {
        if (isHost) {
            var gameState = validateMyExpression(positionFromTouch)?: GameState.NONE
            setGameState(gameState)

            if (gameState == GameState.LEVEL_COMPLETED && isEveryLevelFinished()) {
                setGameState(GameState.NEW_LEVEL_COUNTDOWN_STARTED)
                replyToClients(GameState.NEW_LEVEL_COUNTDOWN_STARTED)
            }

            replyToClients(GameState.REFRESH_PLAYERS_LIST)

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
        if (!isHost) {
            return
        }

        while (countdownToInitNextLevel > 0) {
            delay(1000)
            countdownToInitNextLevel--
            // setGameState(GameState.NEW_LEVEL_COUNTDOWN_TICK)
        }

        onNewLevelResetData()

        startNewLevel()

        replyToClients(GameState.NEW_LEVEL_STARTED)
        setGameState(GameState.NEW_LEVEL_STARTED)
    }

    override fun onCleared() {
        super.onCleared()

        socketsService?.close()
    }

    override suspend fun gameClockRoutine() {
        if (!isHost) {
            return
        }

        while (true) {
            delay(1000)

            val isSomeGameRunning = runClock()

            setGameState(GameState.CLOCK_TICK)

            if (!isSomeGameRunning) { return }
        }
    }

    private fun runClock(): Boolean {
        var everyPlayerLoosed = true
        var isSomeGameRunning = false

        for (playerUUID in playersMap.keys) {
            var player = playersMap[playerUUID]
            var playerGame = playersGameMap[playerUUID]
            var playerConnection = playersConnectionMap[playerUUID]

            if (player == null || playerGame == null || player.lostGame || player.isLevelFinished) {continue}

            if (playerGame.timer > 0) {
                playerGame.clockTick()

                isSomeGameRunning = true
                everyPlayerLoosed = false
                continue
            }

            player.lostGame = true
            player.isLevelFinished = true

            if (everyPlayerLoosed) {
                setGameState(GameState.GAME_OVER)
                replyToClients(GameState.GAME_OVER)
                continue
            }

            if (playerUUID == MainMenuActivity.APP_EXECUTION_UUID) {
                setGameState(GameState.GAME_OVER_TIME_OUT)
                replyToClients(GameState.REFRESH_PLAYERS_LIST)
                continue
            }

            if (playerConnection != null) {
                socketsService?.sendMessage(generateServerPayloadResponse(playerUUID, GameState.GAME_OVER_TIME_OUT), playerConnection)
            }

            replyToClients(GameState.REFRESH_PLAYERS_LIST)
        }

        return isSomeGameRunning
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

    private fun generateServerPayloadResponse(playerUUID: String, gameState: GameState): ServerMessagePayload {
        var clientGame = playersGameMap[playerUUID]

        if (clientGame == null) {
            clientGame = game
        }

        return ServerMessagePayload(clientGame, playersMap, gameState)
    }

    private fun validateMyExpression(boardPosition: Constants.BOARD_POSITION): GameState? {
        val playerUUID = player.playerDetails.player.uuid

        val playerGame = playersGameMap[playerUUID] ?: return null
        val player = playersMap[playerUUID] ?: return null

        val gameState = playerGame.executeMove(boardPosition, player.playerDetails, true)

        when(gameState) {
            GameState.CORRECT_EXPRESSION -> {
                player.activeBoardIndex++
                playerGame.board = getBoard(player.activeBoardIndex)
            }
            GameState.LEVEL_COMPLETED -> {
                player.activeBoardIndex = 0
                player.isLevelFinished = true

                if (firstFinishGame) {
                    firstFinishGame = false
                    player.playerDetails.score += 5
                }

            }
            else -> {}
        }

        return gameState
    }

    private fun validateExpression(messagePayload: ClientMessagePayload): GameState? {
        val playerUUID = messagePayload.playerResult.player.uuid

        val playerGame = playersGameMap[playerUUID] ?: return null
        val player = playersMap[playerUUID] ?: return null

        val gameState = playerGame.executeMove(messagePayload.boardPosition, player.playerDetails, true)

        when(gameState) {
            GameState.CORRECT_EXPRESSION -> {
                player.activeBoardIndex++
                playerGame.board = getBoard(player.activeBoardIndex)
            }
            GameState.LEVEL_COMPLETED -> {
                player.activeBoardIndex = 0
                player.isLevelFinished = true

                if (firstFinishGame) {
                    firstFinishGame = false
                    player.playerDetails.score += 5
                }

            }
            else -> {}
        }

        return gameState
    }

    private fun validateExpression(socketConnection: MultiplayerConnection, messagePayload: ClientMessagePayload): GameState {
        var gameState = validateExpression(messagePayload) ?: return GameState.NONE

        socketsService?.sendMessage(generateServerPayloadResponse(messagePayload.playerResult.player.uuid, gameState), socketConnection)

        return gameState
    }

    private fun updatePlayerData(playersMap: MutableMap<String, MultiplayerPlayer>) {
        val playerResult = playersMap[MainMenuActivity.APP_EXECUTION_UUID]
        if (playerResult != null) {
            player.playerDetails = playerResult.playerDetails
        }
    }

    private fun updateAllData(messagePayload: ServerMessagePayload) {
        game = messagePayload.clientGame
        playersMap = messagePayload.players
        updatePlayerData(playersMap)
    }

    private fun isEveryLevelFinished(): Boolean {
        for (players in playersMap.values) {
            if (!players.isLevelFinished) {
                return false
            }
        }

        return true
    }

    private fun onNewLevelResetData() {
        generatedBoardsData.clear()

        for (playerUUID in playersMap.keys) {
            var player = playersMap[playerUUID]
            var playerGame = playersGameMap[playerUUID]

            if (player?.lostGame == true) { continue }

            player?.activeBoardIndex = 0
            player?.isLevelFinished = false

            playerGame?.board = getBoard(0)
        }
    }

}