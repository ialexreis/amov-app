package pt.isec.agileMath.models

data class Result(
    val player: Player = Player(),
    var score: Int = 0,
    var totalTime: Int = 0
)
