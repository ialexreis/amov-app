package pt.isec.agileMath.services

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

object PreferenceServices {

    val USER_NICKNAME = "NICKNAME"
    val USER_AVATAR = "PROFILE_URL"
    var USER_DOCUMENT = "DOCUMENT"

    fun defaultPreference(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    fun customPreference(context: Context, name: String): SharedPreferences =
        context.getSharedPreferences(name, Context.MODE_PRIVATE)

    inline fun SharedPreferences.editMe(operation: (SharedPreferences.Editor) -> Unit) {
        val editMe = edit()
        operation(editMe)
        editMe.apply()
    }

    var SharedPreferences.nickname
        get() = getString(USER_NICKNAME, "")
        set(value) {
            editMe {
                it.putString(USER_NICKNAME, value)
            }
        }

    var SharedPreferences.profile_url
        get() = getString(USER_AVATAR, "")
        set(value) {
            editMe {
                it.putString(USER_AVATAR, value)
            }
        }

    var SharedPreferences.id
        get() = getString(USER_DOCUMENT, "")
        set(value) {
            editMe {
                it.putString(USER_DOCUMENT, value)
            }
        }

    var SharedPreferences.clearValues
        get() = { }
        set(value) {
            editMe {
                it.clear()
            }
        }
}