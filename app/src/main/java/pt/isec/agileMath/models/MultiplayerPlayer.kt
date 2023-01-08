package pt.isec.agileMath.models

data class MultiplayerPlayer(
    val playerDetails: PlayerResult,
    var activeBoardIndex: Int = 0)
