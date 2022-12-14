package pt.isec.agileMath.viewModels

import androidx.lifecycle.ViewModel
import pt.isec.agileMath.constants.Constants
import pt.isec.agileMath.databinding.ActivityGameBinding
import pt.isec.agileMath.models.Game


class SinglePlayerViewModel: ViewModel() {
    lateinit var activityBinding: ActivityGameBinding
    private val boardDimention = Constants.BOARD_LINES * Constants.BOARD_LINES

    var game: Game = Game()
        private set

    var vector = ArrayList<String>(boardDimention)
        get() {
            val newVector = ArrayList<String>(boardDimention)

            game.board.matrix.map{ line ->
                for (cell in line) {
                    newVector.add(cell)
                }
            }

            return newVector
        }


}