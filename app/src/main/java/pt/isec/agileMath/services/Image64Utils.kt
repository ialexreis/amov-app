package pt.isec.agileMath.services

object Image64Utils {

    fun byteArrToString(arr: ByteArray): String {
        return arr.toString()
    }

    fun encodeToFirebase(base64: String): MutableList<Byte> {
        var aux = base64.encodeToByteArray().toMutableList()
//        aux = aux.replace("/", "____")
//        aux = aux.replace("+", "_-_")
//        aux = aux.replace("=", "----")

        return aux
    }

    fun decodeToJson(encoded64: ByteArray): String {
        var decoded64 = String(encoded64)
//        decoded64 = decoded64.replace("____", "/")
//        decoded64 = decoded64.replace("_-_", "+")
//        decoded64 = decoded64.replace("----", "=")

        return decoded64
    }

}