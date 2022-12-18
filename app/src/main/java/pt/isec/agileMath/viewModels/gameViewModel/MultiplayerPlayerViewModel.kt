package pt.isec.agileMath.viewModels.gameViewModel

import pt.isec.agileMath.constants.Constants
import pt.isec.agileMath.constants.GameState

class MultiplayerPlayerViewModel: GameViewModel() {
    override fun executeMove(positionFromTouch: Constants.BOARD_POSITION) {
        val gameState = game.executeMove(positionFromTouch)

        setGameState(gameState)
    }
}