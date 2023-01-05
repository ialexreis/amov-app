package pt.isec.agileMath.models

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import org.json.JSONObject
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
        fun fromString(string: String): SocketMessagePayload
        {
            val gameObject = JSONObject(string).getJSONObject("game")
            val playerResultObject = JSONObject(string).getJSONObject("playerResult")
            val gameStateValue = JSONObject(string).getString("gameState")

            val game = Gson().fromJson(gameObject.toString(), Game::class.java)
            val playerResult = Gson().fromJson(playerResultObject.toString(), Result::class.java)

            return SocketMessagePayload(game, playerResult, GameState.valueOf(gameStateValue))
        }

        fun toJson(messagePayload: SocketMessagePayload): String
        {
            return Gson().toJson(messagePayload)
        }
    }
}
