package pt.isec.agileMath.models

data class PlayerResult(
    var player: Player = Player(),
    var score: Long = 0,
    var totalTime: Long = 0,
)