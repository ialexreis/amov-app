package pt.isec.agileMath.viewModels.gameViewModel

import android.content.Context
import kotlinx.coroutines.delay
import pt.isec.agileMath.activities.EditProfileActivity
import pt.isec.agileMath.constants.Constants
import pt.isec.agileMath.constants.GameState
import pt.isec.agileMath.models.Game
import pt.isec.agileMath.models.MultiplayerPlayer
import pt.isec.agileMath.models.PlayerResult
import pt.isec.agileMath.services.socketsService.ClientSocketsService
import pt.isec.agileMath.services.socketsService.ServerSocketsService

class SinglePlayerViewModel: GameViewModel() {

    private var wasAlreadyInitialized = false

    fun initSinglePlayer(context: Context) {
        if (wasAlreadyInitialized) {
            return
        }

        wasAlreadyInitialized = true
        startGame()
    }

    fun initSinglePlayer(context: Context, level: Int, currentTimer: Int) {
        if (wasAlreadyInitialized) {
            return
        }

        game = Game(level!!, currentTimer!!)
        wasAlreadyInitialized = true
        startGame()
    }

    override fun executeMove(positionFromTouch: Constants.BOARD_POSITION) {
        val gameState = game.executeMove(positionFromTouch, result)

        setGameState(gameState)
    }

    override suspend fun nextLevelCountdownRoutine() {
        while (countdownToInitNextLevel > 0) {
            delay(1000)
            countdownToInitNextLevel--

            setGameState(GameState.NEW_LEVEL_COUNTDOWN_TICK)
        }

        setGameState(GameState.NEW_LEVEL_STARTED)
    }

}