package pt.isec.agileMath.models

data class MultiplayerPlayer(
    var playerDetails: PlayerResult,
    var activeBoardIndex: Int = 0,
    var isLevelFinished: Boolean = false,
    var lostGame: Boolean = false
)
