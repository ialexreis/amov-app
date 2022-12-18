package pt.isec.agileMath.multiplayer

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.ConnectivityManager
import android.provider.Settings.Global.getString
import android.text.InputFilter
import android.text.Spanned
import android.util.Patterns
import android.util.Patterns.IP_ADDRESS
import android.view.Gravity
import android.view.View
import android.widget.*
import android.widget.Toast.*
import pt.isec.agileMath.R

class Popups {

    companion object {

        private fun Context.getConnectivityManager() =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        private fun getIpAddress(context: Context) = with(context.getConnectivityManager()) {
            getLinkProperties(activeNetwork)!!.linkAddresses[1].address.hostAddress!!
        }

        fun serverPopup(ctx: Context) {
            val ip = getIpAddress(ctx)
            val ll = LinearLayout(ctx).apply {
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                this.setPadding(50, 50, 50, 50)
                layoutParams = params
                setBackgroundColor(Color.rgb(240, 224, 208))
                orientation = LinearLayout.HORIZONTAL
                addView(ProgressBar(context).apply {
                    isIndeterminate = true
                    val paramsPB = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    paramsPB.gravity = Gravity.CENTER_VERTICAL
                    layoutParams = paramsPB
                    indeterminateTintList = ColorStateList.valueOf(Color.rgb(96, 96, 32))
                })
                addView(TextView(context).apply {
                    val paramsTV = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams = paramsTV
                    text = String.format("%s", ip)
                    textSize = 20f
                    setTextColor(Color.rgb(96, 96, 32))
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                })
            }
            val dlg = AlertDialog.Builder(ctx)
                .setTitle(R.string.start_as_server)
                .setView(ll)
                .create()

            dlg?.show()
        }

        fun clientPopup(ctx: Context) {
            val edtBox = EditText(ctx).apply {
                maxLines = 1
                filters = arrayOf(object : InputFilter {
                    override fun filter(
                        source: CharSequence?,
                        start: Int,
                        end: Int,
                        dest: Spanned?,
                        dstart: Int,
                        dend: Int
                    ): CharSequence? {
                        source?.run {
                            var ret = ""
                            forEach {
                                if (it.isDigit() || it.equals('.'))
                                    ret += it
                            }
                            return ret
                        }
                        return null
                    }

                })
            }
            val dlg = AlertDialog.Builder(ctx)
                .setTitle(R.string.start_server_as_client)
                .setMessage(R.string.ask_ip)
                .setPositiveButton(R.string.button_connect) { _: DialogInterface, _: Int ->
                    val strIP = edtBox.text.toString()
                    if (strIP.isEmpty() || !IP_ADDRESS.matcher(strIP).matches()) {
                        makeText(ctx, R.string.error_address, LENGTH_LONG).show()
                    } else {
                    }
                }
                .setNeutralButton(R.string.btn_emulator) { _: DialogInterface, _: Int ->
                    // Configure port redirect on the Server Emulator:
                    // telnet localhost <5554|5556|5558|...>
                    // auth <key>
                    // redir add tcp:9998:9999
                }
                .setNegativeButton(R.string.button_cancel) { _: DialogInterface, _: Int ->
                }
                .setCancelable(false)
                .setView(edtBox)
                .create()

            dlg.show()
        }
    }
}
