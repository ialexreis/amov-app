package pt.isec.agileMath.viewModels.gameViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import pt.isec.agileMath.constants.Constants
import pt.isec.agileMath.constants.GameState
import pt.isec.agileMath.databinding.ActivityGameBinding
import pt.isec.agileMath.databinding.FragmentScoreBinding
import pt.isec.agileMath.models.Game

abstract class GameViewModel: ViewModel() {
    lateinit var activityBinding: ActivityGameBinding
    lateinit var fragmentScoreBinding: FragmentScoreBinding

    private val boardDimension = Constants.BOARD_LINES * Constants.BOARD_LINES
    private lateinit var timerCoroutine: Job

    private val gameState = MutableLiveData<GameState>()
    var gameStateObserver: LiveData<GameState> = gameState

    var game: Game = Game()
        protected set

    var vector = ArrayList<String>(boardDimension)
        get() {
            val newVector = ArrayList<String>(boardDimension)

            game.board.matrix.map{ line ->
                for (cell in line) {
                    newVector.add(cell)
                }
            }

            return newVector
        }

    override fun onCleared() {
        super.onCleared()

        timerCoroutine?.cancel()
    }

    fun setGameState(gameState: GameState) {
        synchronized(gameStateObserver) {
            this.gameState.value = gameState
        }
    }

    fun startGame() {
        setGameState(GameState.START)
        timerCoroutine = viewModelScope.launch { gameClockRoutine() }
    }

    abstract fun executeMove(positionFromTouch: Constants.BOARD_POSITION)


    private suspend fun gameClockRoutine() {
        while (game.timer > 0) {
            setGameState(GameState.CLOCK_TICK)

            game.clockTick()

            delay(1000)
        }

        setGameState(GameState.GAME_OVER_TIME_OUT)
    }
}