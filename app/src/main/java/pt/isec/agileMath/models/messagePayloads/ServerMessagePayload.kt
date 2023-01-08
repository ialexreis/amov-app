package pt.isec.agileMath.models.messagePayloads

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import pt.isec.agileMath.constants.GameState
import pt.isec.agileMath.models.Game
import pt.isec.agileMath.models.MultiplayerPlayer
import pt.isec.agileMath.models.PlayerResult

class ServerMessagePayload: JsonParserInterface {
    var clientGame: Game
        private set
    var players: MutableMap<String, MultiplayerPlayer>
        private set
    var gameState: GameState
        private set

    constructor(clientGame: Game, players: MutableMap<String, MultiplayerPlayer>, gameState: GameState) {
        this.clientGame = clientGame
        this.players = players
        this.gameState = gameState
    }

    override inline fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromString(string: String): ServerMessagePayload {
            synchronized(this) {
                var gameObject = JSONObject(string).getJSONObject("clientGame")
                var playersArray = JSONObject(string).getJSONObject("players")
                var gameStateValue = JSONObject(string).getString("gameState")


                val game = Gson().fromJson(gameObject.toString(), Game::class.java)

                val playersListType = object : TypeToken<MutableMap<String, MultiplayerPlayer>>() {}.type
                val players = Gson().fromJson<MutableMap<String, MultiplayerPlayer>>(playersArray.toString(), playersListType)

                return ServerMessagePayload(game, players, GameState.valueOf(gameStateValue))
            }
        }
    }
}
