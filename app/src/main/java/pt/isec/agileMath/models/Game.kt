package pt.isec.agileMath.models

import pt.isec.agileMath.constants.Constants
import pt.isec.agileMath.constants.GameState

class Game {
    var isRunning: Boolean = false
    var level: Int = 1
    var timer: Int = 90

    private var successExpressionsToNextLevel: Int = 10
    private var successExpressionsCounter: Int = 0
    private var minBaseValue: Int = 1
    private var maxBaseValue: Long = 10L
    private var successTime: Int = 3
    private val successOperationMaxValue: Int = 2
    private val successOperationSecondMaxValue: Int = 1

    var operatorsToUse = arrayOf("+", "-", "*", "/")

    private var operators = operatorsToUse.copyOf()
        get() {
            if (level > 4) {
                return operatorsToUse.copyOfRange(0, operators.size)
            }
            return operatorsToUse.copyOfRange(0, level)
        }

    var board: Board

    constructor() {
        operators
        board = Board(maxBaseValue, operators)
    }

    constructor(level: Int, timer: Int, board: Board): this() {
        this.level = level
        this.timer = timer
        this.board = board
    }

    fun executeMove(positionFromTouch: Constants.BOARD_POSITION, playerResult: PlayerResult, skipBoardRefresh: Boolean? = false): GameState {
        var gameState = when(positionFromTouch) {
            board.maxValueBoardPosition -> onCorrectOperation(successOperationMaxValue, playerResult)
            board.secondMaxValueBoardPosition -> onCorrectOperation(successOperationSecondMaxValue, playerResult)
            else -> GameState.FAILED_EXPRESSION
        }

        if (gameState == GameState.FAILED_EXPRESSION) {
            return gameState
        }

        if (successExpressionsCounter == successExpressionsToNextLevel) {
            nextLevel()
            return GameState.LEVEL_COMPLETED
        }

        if (skipBoardRefresh == true) {
            return gameState
        }

        buildBoard()
        return gameState
    }

    fun clockTick() {
        synchronized(timer) {
            timer--
        }
    }

    fun getNewBoard(): Board {
        return Board(maxBaseValue, operators)
    }

    private fun nextLevel() {
        level += 1
        maxBaseValue *= level
        successExpressionsToNextLevel *= level

        successExpressionsCounter = 0

        buildBoard()
    }

    private fun onCorrectOperation(pointsToIncrement: Int, playerResult: PlayerResult): GameState {
        playerResult.score += pointsToIncrement
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
