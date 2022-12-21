package pt.isec.agileMath.viewModels.gameViewModel

import android.util.Log
import kotlinx.coroutines.delay
import pt.isec.agileMath.constants.Constants
import pt.isec.agileMath.constants.GameState

class SinglePlayerViewModel: GameViewModel() {
    override fun executeMove(positionFromTouch: Constants.BOARD_POSITION) {
        val gameState = game.executeMove(positionFromTouch)

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