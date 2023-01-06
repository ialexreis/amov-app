package pt.isec.agileMath.models

import pt.isec.agileMath.activities.MainMenuActivity

data class Player(var name: String? = "", var pictureUrl: String? = "") {
    var uuid = MainMenuActivity.APP_EXECUTION_UUID
}


