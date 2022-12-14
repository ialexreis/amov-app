package pt.isec.agileMath.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.OnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.activity.viewModels
import pt.isec.agileMath.R
import pt.isec.agileMath.databinding.ActivityGameBinding
import pt.isec.agileMath.viewModels.SinglePlayerViewModel
import pt.isec.agileMath.views.BoardGridView

class GameActivity : AppCompatActivity() {
    companion object {
        fun getIntent(ctx: Context): Intent {
            return Intent(ctx, GameActivity::class.java)
        }
    }

    private lateinit var binding: ActivityGameBinding
    private val singlePlayerViewModel: SinglePlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)

        singlePlayerViewModel.activityBinding = binding

        setContentView(binding.root)

        binding.frGameMatrix?.addView(BoardGridView(this, singlePlayerViewModel.vector))
    }
}