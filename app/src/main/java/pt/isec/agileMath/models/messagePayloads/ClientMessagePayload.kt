package pt.isec.agileMath.models.messagePayloads

import android.util.Log
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import pt.isec.agileMath.activities.MainMenuActivity
import pt.isec.agileMath.constants.Constants
import pt.isec.agileMath.constants.GameState
import pt.isec.agileMath.models.PlayerResult

class ClientMessagePayload: JsonParserInterface {
    var playerResult: PlayerResult
        private set
    var gameState: GameState
        private set
    var boardPosition: Constants.BOARD_POSITION
        private set

    constructor(
        playerResult: PlayerResult,
        gameState: GameState,
        boardPosition: Constants.BOARD_POSITION = Constants.BOARD_POSITION.NONE
    ) {
        this.playerResult = playerResult
        this.gameState = gameState
        this.boardPosition = boardPosition
    }

    override inline fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromString(string: String): ClientMessagePayload {
            synchronized(this) {
                var playerResultObject = JSONObject(string).getJSONObject("playerResult")
                var gameStateValue = JSONObject(string).getString("gameState")

                var boardPositionValue = try {
                    JSONObject(string).getString("boardPosition")
                } catch (e: JSONException) {
                    Constants.BOARD_POSITION.NONE.toString()
                }

                val playerResult = Gson().fromJson(playerResultObject.toString(), PlayerResult::class.java)

                return ClientMessagePayload(playerResult, GameState.valueOf(gameStateValue), Constants.BOARD_POSITION.valueOf(boardPositionValue))
            }
        }
    }
}
