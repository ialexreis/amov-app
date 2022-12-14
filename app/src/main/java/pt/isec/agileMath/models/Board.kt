package pt.isec.agileMath.models

import pt.isec.agileMath.constants.Constants
import kotlin.random.Random

data class Board(
    private val valuesRange: Long,
    private val operators: Array<String>,
    ) {

    val matrix: MutableList<MutableList<String>> = MutableList(Constants.BOARD_LINES){
        MutableList(Constants.BOARD_LINES){""}
    }

    var operationMaxValue = 0.0f
        private set
    var maxValueBoardPosition = Constants.BOARD_POSITION.NONE
        private set

    var operationSecondMaxValue = 0.0f
        private set
    var secondMaxValueBoardPosition = Constants.BOARD_POSITION.NONE
        private set


    init {
        fillCells()
    }

    private fun fillCells() {
        matrix.mapIndexed { lineIndex, columns ->
            for (columnIndex in columns.indices) {
                // Linha par e célula par
                if (lineIndex % 2 == 0 && columnIndex % 2 == 0) {
                    columns[columnIndex] = Random.nextLong(1, valuesRange).toString()
                    continue
                }

                // Linha impar e célula impar
                if (lineIndex % 2 == 1 && columnIndex % 2 == 1) {
                    columns[columnIndex] = ""
                    continue
                }

                // Célula par
                columns[columnIndex] = operators[Random.nextInt(0, operators.size)]
            }

            getOperationsMaxResultFromLines(lineIndex)
        }

        getOperationsMaxResultFromColumns()
    }

    // TODO Está com BUGGGG! N determina corretamente o valor das colunas
    private fun getOperationsMaxResultFromColumns() {
        for (lineIndex in matrix.indices) {
            if (lineIndex % 2 != 0) { return }

            val operationResult = calculateOperationFromColumn(lineIndex)

            if (operationResult >= operationMaxValue) {
                operationMaxValue = operationResult
                maxValueBoardPosition = getBoardPositionFromIndex(lineIndex, true)
            }else if (operationResult >= operationSecondMaxValue) {
                operationSecondMaxValue = operationResult
                secondMaxValueBoardPosition = getBoardPositionFromIndex(lineIndex, true)
            }
        }
    }

    private fun getOperationsMaxResultFromLines(lineIndex: Int) {
        if (lineIndex % 2 != 0) { return }

        val operationResult = calculateOperationFromLine(lineIndex)

        if (operationResult >= operationMaxValue) {
            operationMaxValue = operationResult
            maxValueBoardPosition = getBoardPositionFromIndex(lineIndex)
        }else if (operationResult >= operationSecondMaxValue) {
            operationSecondMaxValue = operationResult
            secondMaxValueBoardPosition = getBoardPositionFromIndex(lineIndex)
        }
    }

    private fun calculateOperationFromLine(lineIndex: Int): Float {
        var result = 0.0f
        var line = matrix[lineIndex]
        var operatorIndexExecuted = 0

        if (line[1] == "/" || line[1] == "*") {
            result = calculate(line[0], line[2], line[1])
            operatorIndexExecuted = 1
        }
        else if (line[3] == "/" || line[3] == "*") {
            result = calculate(line[2], line[4], line[3])
            operatorIndexExecuted = 3
        }

        if (operatorIndexExecuted == 1) {
            return calculate(result.toString(), line[4], line[3])
        } else if (operatorIndexExecuted == 3) {
            return calculate(line[0], result.toString(), line[1])
        }

        result = calculate(line[0], line[2], line[1])

        return calculate(result.toString(), line[4], line[3])
    }

    private fun calculateOperationFromColumn(columnIndex: Int): Float {
        var result = 0.0f
        var operatorIndexExecuted = 0

        if (matrix[1][columnIndex] == "/" || matrix[1][columnIndex] == "*") {
            result = calculate(matrix[0][columnIndex], matrix[2][columnIndex], matrix[1][columnIndex])
            operatorIndexExecuted = 1
        }
        else if (matrix[3][columnIndex] == "/" || matrix[3][columnIndex] == "*") {
            result = calculate(matrix[2][columnIndex], matrix[4][columnIndex], matrix[3][columnIndex])
            operatorIndexExecuted = 3
        }

        if (operatorIndexExecuted == 1) {
            return calculate(result.toString(), matrix[4][columnIndex], matrix[3][columnIndex])
        } else if (operatorIndexExecuted == 3) {
            return calculate(result.toString(), matrix[0][columnIndex], matrix[1][columnIndex])
        }

        result = calculate(matrix[0][columnIndex], matrix[2][columnIndex], matrix[1][columnIndex])

        return calculate(result.toString(), matrix[4][columnIndex], matrix[3][columnIndex])
    }

    private fun calculate(firstStringValue: String, secondStringValue: String, operator: String): Float {
        var firstValue = firstStringValue.toFloat()
        var secondValue = secondStringValue.toFloat()

        when(operator){
            "*" -> { return firstValue * secondValue }
            "/" -> { return firstValue / secondValue }
            "+" -> { return firstValue + secondValue }
            "-" -> { return firstValue - secondValue }
        }

        return 0.0f
    }

    private fun getBoardPositionFromIndex(index: Int, isColumn: Boolean = false): Constants.BOARD_POSITION  {
        when(index) {
            0 -> return if (isColumn) Constants.BOARD_POSITION.COLUMN_LEFT else Constants.BOARD_POSITION.LINE_TOP
            2 -> return if (isColumn) Constants.BOARD_POSITION.COLUMN_CENTER else Constants.BOARD_POSITION.LINE_MIDDLE
            4 -> return if (isColumn) Constants.BOARD_POSITION.COLUMN_RIGHT else Constants.BOARD_POSITION.LINE_BOTTOM
        }

        return Constants.BOARD_POSITION.NONE
    }
}
