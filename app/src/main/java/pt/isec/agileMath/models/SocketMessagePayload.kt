package pt.isec.agileMath.models

import com.google.gson.Gson
import pt.isec.agileMath.constants.GameState

class SocketMessagePayload {
    var game: Game
        private set
    var playerResult: Result
        private set
    var gameState: GameState
        private set

    constructor(game: Game, playerResult: Result, gameState: GameState) {
        this.game = game
        this.playerResult = playerResult
        this.gameState = gameState
    }

    companion object {
        fun fromByteArray(byteArrayJSONMessage: ByteArray): SocketMessagePayload
        {
            val jsonObject = Gson().toJson(byteArrayJSONMessage)
            return Gson().fromJson<SocketMessagePayload>(jsonObject, SocketMessagePayload.javaClass)
        }

        fun toJSONByteArray(messagePayload: SocketMessagePayload): ByteArray
        {
            return Gson().toJson(messagePayload).toByteArray()
        }
    }
}
