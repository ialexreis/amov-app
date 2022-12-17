package pt.isec.agileMath.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import pt.isec.agileMath.constants.GameState
import pt.isec.agileMath.databinding.ActivityGameBinding
import pt.isec.agileMath.databinding.FragmentScoreBinding
import pt.isec.agileMath.viewModels.gameViewModel.SinglePlayerViewModel
import pt.isec.agileMath.views.BoardGridView

class GameActivity : AppCompatActivity() {
    companion object {
        fun getIntent(ctx: Context): Intent {
            return Intent(ctx, GameActivity::class.java)
        }
    }

    private lateinit var binding: ActivityGameBinding
    private lateinit var fragmentScoreBinding: FragmentScoreBinding
    private val singlePlayerViewModel: SinglePlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        fragmentScoreBinding = FragmentScoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        singlePlayerViewModel.activityBinding = binding
        singlePlayerViewModel.fragmentScoreBinding = fragmentScoreBinding

        binding.frScore?.addView(fragmentScoreBinding.root)
        binding.frGameMatrix?.addView(BoardGridView(this, singlePlayerViewModel))

        singlePlayerViewModel.gameStateObserver.observe(this) {
            onGameStateChange(it)
        }

        singlePlayerViewModel.startGame()
    }


    private fun onGameStateChange(state: GameState) {
        Log.d("onGameStateChange", state.toString())

        when(state) {
            GameState.START -> {}
            GameState.START -> {}
            GameState.START -> {}
            GameState.START -> {}
            else -> {}
        }

        fragmentScoreBinding?.score?.text = singlePlayerViewModel.game.totalPoints.toString()
        fragmentScoreBinding?.timeleft?.text = singlePlayerViewModel.game.timer.toString()
    }
}