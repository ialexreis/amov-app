package pt.isec.agileMath.models

data class Game(
    var isRunning: Boolean = false,
    var level: Int = 1,
    var successExpressionsToNextLevel: Int = 10,
    var successExpressionsCounter: Int = 0,
    var minBaseValue: Int = 1,
    var maxBaseValue: Long = 10L,
    var timer: Int = 90,
    var successTime: Int = 3
){
    var operators = arrayOf("+", "-", "*", "/")
        private set
        get() {
            if (level > operators.size) {
                return operators.copyOfRange(0, operators.size)
            }

            return operators.copyOfRange(0, level)
        }

    fun nextLevel() {
        level += 1
        maxBaseValue *= level
        successExpressionsToNextLevel *= level

        successExpressionsCounter = 0
    }
}
