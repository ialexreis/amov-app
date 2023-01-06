package pt.isec.agileMath.models.messagePayloads

import android.util.Log
import com.google.gson.Gson
import org.json.JSONObject
import pt.isec.agileMath.activities.MainMenuActivity
import pt.isec.agileMath.constants.GameState
import pt.isec.agileMath.models.Game
import pt.isec.agileMath.models.Result

class ClientMessagePayload: JsonParserInterface {
    var playerResult: Result
        private set
    var gameState: GameState
        private set

    constructor(playerResult: Result, gameState: GameState) {
        this.playerResult = playerResult
        this.gameState = gameState
    }

    override inline fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromString(string: String): ClientMessagePayload {
            val playerResultObject = JSONObject(string).getJSONObject("playerResult")
            val gameStateValue = JSONObject(string).getString("gameState")
            
            val playerResult = Gson().fromJson(playerResultObject.toString(), Result::class.java)

            Log.e("fromString", "${MainMenuActivity.APP_EXECUTION_UUID} -> ${playerResult.player.uuid} ")

            return ClientMessagePayload(playerResult, GameState.valueOf(gameStateValue))
        }
    }
}
