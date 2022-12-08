package pt.isec.agileMath.models

import pt.isec.agileMath.constants.Constants
import kotlin.random.Random

data class Board(
    private val valuesRange: Long,
    private val operators: Array<String>,
    ) {

    val operationsResult = mapOf(
        mapOf(
            0 to "value", Constants.BOARD_POSITION.NONE to "position"
        ) to "max",
        mapOf(
            0 to "value", Constants.BOARD_POSITION.NONE to "position"
        ) to "secondMax"
    )

    var matrix: MutableList<MutableList<String>> = MutableList(Constants.BOARD_LINES){
        MutableList(Constants.BOARD_LINES){""}
    }
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

            getOperationsMax(lineIndex, columns);
        }
    }

    private fun getOperationsMax(lineIndex: Int, columns: MutableList<String>) {

    }
}
