package pt.isec.agileMath.viewModels.gameViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import pt.isec.agileMath.constants.Constants
import pt.isec.agileMath.constants.GameState
import pt.isec.agileMath.models.Game
import pt.isec.agileMath.models.PlayerResult

abstract class GameViewModel: ViewModel() {
    private val boardDimension = Constants.BOARD_LINES * Constants.BOARD_LINES
    private var timerCoroutine: Job? = null

    private val gameState = MutableLiveData<GameState>()
    var gameStateObserver: LiveData<GameState> = gameState

    var countdownToInitNextLevel = 5
    var isCountdownPaused = false

    var result: PlayerResult = PlayerResult()

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
            this.gameState.postValue(gameState)
        }
    }

    open fun startGame() {
        result = PlayerResult()

        timerCoroutine = viewModelScope.launch { gameClockRoutine() }
    }

    open fun startNewLevel() {
        countdownToInitNextLevel = 5
        timerCoroutine = viewModelScope.launch { gameClockRoutine() }
    }

    fun initCountdownToNextLevel() {
        timerCoroutine?.cancel()
        timerCoroutine = viewModelScope.launch { nextLevelCountdownRoutine() }
    }

    fun togglePause() {
        if (isCountdownPaused) {
            setGameState(GameState.NEW_LEVEL_COUNTDOWN_RESUMED)
            initCountdownToNextLevel()
        } else {
            setGameState(GameState.NEW_LEVEL_COUNTDOWN_PAUSED)
            timerCoroutine?.cancel()
        }

        isCountdownPaused = !isCountdownPaused
    }

    abstract fun executeMove(positionFromTouch: Constants.BOARD_POSITION)

    abstract suspend fun nextLevelCountdownRoutine()

    open suspend fun gameClockRoutine() {
        while (game.timer > 0) {
            delay(1000)

            setGameState(GameState.CLOCK_TICK)

            game.clockTick()
        }

        setGameState(GameState.GAME_OVER_TIME_OUT)
    }
}