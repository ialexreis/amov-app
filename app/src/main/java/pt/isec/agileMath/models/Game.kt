package pt.isec.agileMath.models

import pt.isec.agileMath.constants.Constants
import pt.isec.agileMath.constants.GameState

data class Game(
    var isRunning: Boolean = false,

    var level: Int = 1,

    var successExpressionsToNextLevel: Int = 10,
    var successExpressionsCounter: Int = 0,
    var minBaseValue: Int = 1,
    var maxBaseValue: Long = 10L,
    var timer: Int = 90,
    var successTime: Int = 3,
    var totalPoints: Int = 0,
    val successOperationMaxValue: Int = 2,
    val successOperationSecondMaxValue: Int = 1,
){
    var operatorsToUse = arrayOf("+", "-", "*", "/")

    var operators = operatorsToUse.copyOf()
        get() {
            if (level > 4) {
                return operatorsToUse.copyOfRange(0, operators.size)
            }
            return operatorsToUse.copyOfRange(0, level)
        }

    var board: Board
        private set

    init {
        operators
        board = Board(maxBaseValue, operators)
    }

    fun executeMove(positionFromTouch: Constants.BOARD_POSITION): GameState {
        var gameState = when(positionFromTouch) {
            board.maxValueBoardPosition -> onCorrectOperation(successOperationMaxValue)
            board.secondMaxValueBoardPosition -> onCorrectOperation(successOperationSecondMaxValue)
            else -> GameState.FAILED_EXPRESSION
        }

        if (gameState == GameState.FAILED_EXPRESSION) {
            return gameState
        }

        if (successExpressionsCounter == successExpressionsToNextLevel) {
            nextLevel()
            return GameState.LEVEL_COMPLETED
        }

        buildBoard()
        return gameState
    }

    fun clockTick() {
        synchronized(timer) {
            timer--
        }
    }

    private fun nextLevel() {
        level += 1
        maxBaseValue *= level
        successExpressionsToNextLevel *= level
        timer -= 10 * level

        successExpressionsCounter = 0

        buildBoard()
    }

    private fun onCorrectOperation(pointsToIncrement: Int): GameState {
        totalPoints += pointsToIncrement
        successExpressionsCounter++

        synchronized(timer) {
            timer += successTime
        }

        return GameState.CORRECT_EXPRESSION
    }


    private fun buildBoard() {
        board = Board(maxBaseValue, operators)
    }
}
