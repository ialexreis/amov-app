package pt.isec.agileMath.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.TextView
import pt.isec.agileMath.R
import pt.isec.agileMath.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding

    public val _matrix = arrayOf(
        "2", "+", "6", "*", "7",
        "+", "6", "*", "7", "/",
        "2", "+", "6", "*", "9",
        "+", "6", "*", "7", "/",
        "0", "+", "6", "*", "10"
    )

    companion object {
        fun getIntent(ctx: Context): Intent {
            return Intent(ctx, GameActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val grid: GridView? = binding.gamematrix
        grid!!.adapter = object : BaseAdapter() {
            override fun getCount(): Int = _matrix.size

            override fun getItem(index: Int): String = _matrix[index]

            override fun getItemId(index: Int): Long {
                return _matrix[index].length.toLong()
            }

            @SuppressLint("ResourceAsColor")
            override fun getView(
                index: Int,
                convertView: View?,
                parent: ViewGroup?
            ): View {
                val textView = convertView ?: TextView(this@GameActivity).apply {
                    text = _matrix[index]
                    gravity = 1
                    setTextColor(R.color.md_theme_light_primary)
                    textSize = 40F
                }

                return textView
            }
        }
    }
}