package pt.isec.agileMath.viewModels.gameViewModel

import pt.isec.agileMath.constants.Constants

class SinglePlayerViewModel: GameViewModel() {
    override fun executeMove(positionFromTouch: Constants.BOARD_POSITION) {
        game.executeMove(positionFromTouch)

    }
}