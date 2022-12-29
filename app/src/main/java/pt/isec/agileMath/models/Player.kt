package pt.isec.agileMath.models

data class Player(
    var name: String = "",
    var pictureUrl: String = "",
) {
    init {
        // TODO get data from sharedpreferences

        name = "";
        pictureUrl = "";
    }
}
