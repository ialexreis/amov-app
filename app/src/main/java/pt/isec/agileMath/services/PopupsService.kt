package pt.isec.agileMath.services

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.ConnectivityManager
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import pt.isec.agileMath.R

class Popups {
    companion object {
        private var activePopup: AlertDialog? = null

        private fun Context.getConnectivityManager() =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        private fun getIpAddress(context: Context) = with(context.getConnectivityManager()) {
            getLinkProperties(activeNetwork)!!.linkAddresses[0].address.hostAddress!!
        }

        fun close() {
            activePopup?.cancel()
            activePopup = null
        }

        fun serverPopup(ctx: Context, onStartGame: (() -> Unit), onCancel: (() -> Unit)) {
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
            activePopup = AlertDialog.Builder(ctx)
                .setTitle(R.string.start_as_server)
                .setPositiveButton(R.string.button_start, null) //{ _, _ -> onStartGame()}
                .setNegativeButton(R.string.button_cancel) { _, _ ->
                    onCancel()
                    close()
                }
                .setView(ll)
                .setCancelable(false)
                .create()

            activePopup?.show()

            activePopup?.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener{ onStartGame() }
        }

        fun clientPopup(ctx: Context, onConnect: (hostname: String) -> Unit, onCancel: () -> Unit) {
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
            activePopup = AlertDialog.Builder(ctx)
                .setTitle(R.string.start_server_as_client)
                .setMessage(R.string.ask_ip)
                .setPositiveButton(R.string.button_connect, null)
                .setNegativeButton(R.string.button_cancel) { _, _ ->
                    onCancel()
                    close()
                }
                .setCancelable(false)
                .setView(edtBox)
                .create()

            activePopup?.show()

            activePopup?.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener{
                val strIP = edtBox.text.toString()

                if (strIP.isEmpty()) {
                    return@setOnClickListener
                }

                onConnect.invoke(strIP)
                close()
            }
        }

        fun waitingPopupSpinner(ctx: Context, titleStringResource: Int, onCancel: () -> Unit) {
            val ll = LinearLayout(ctx).apply {
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams = params
                setBackgroundColor(Color.rgb(240, 224, 208))
                orientation = LinearLayout.HORIZONTAL
                addView(ProgressBar(context).apply {
                    isIndeterminate = true
                    val paramsPB = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    paramsPB.gravity = Gravity.CENTER
                    paramsPB.layoutDirection = Gravity.CENTER
                    layoutParams = paramsPB
                    indeterminateTintList = ColorStateList.valueOf(Color.rgb(96, 96, 32))
                })
            }
            activePopup = AlertDialog.Builder(ctx)
                .setTitle(titleStringResource)
                .setView(ll)
                .setNegativeButton(R.string.button_cancel) { _, _ ->
                    onCancel()
                    close()
                }
                .setCancelable(false)
                .create()

            activePopup?.show()
        }
    }
}
