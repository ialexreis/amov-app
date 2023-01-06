package pt.isec.agileMath.models.messagePayloads

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import pt.isec.agileMath.constants.GameState
import pt.isec.agileMath.models.Game
import pt.isec.agileMath.models.Result

class ServerMessagePayload: JsonParserInterface {
    var game: Game
        private set
    var players: List<Result>
        private set
    var gameState: GameState
        private set

    constructor(game: Game, players: List<Result>, gameState: GameState) {
        this.game = game
        this.players = players
        this.gameState = gameState
    }

    override inline fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromString(string: String): ServerMessagePayload {
            val gameObject = JSONObject(string).getJSONObject("game")
            val playersArray = JSONObject(string).getJSONArray("players")
            val gameStateValue = JSONObject(string).getString("gameState")

            val game = Gson().fromJson(gameObject.toString(), Game::class.java)

            val playersListType = object : TypeToken<List<Result>>() {}.type
            val players = Gson().fromJson<List<Result>>(playersArray.toString(), playersListType)

            return ServerMessagePayload(game, players, GameState.valueOf(gameStateValue))
        }
    }
}
