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
    private val singlePlayerViewModel: SinglePlayerViewModel by viewModels()

    private lateinit var binding: ActivityGameBinding
    private lateinit var fragmentScoreBinding: FragmentScoreBinding
    private lateinit var boardGridView: BoardGridView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        fragmentScoreBinding = FragmentScoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        boardGridView = BoardGridView(this, singlePlayerViewModel)

        singlePlayerViewModel.activityBinding = binding
        singlePlayerViewModel.fragmentScoreBinding = fragmentScoreBinding

        binding.frScore?.addView(fragmentScoreBinding.root)
        binding.frGameMatrix?.addView(boardGridView)

        singlePlayerViewModel.gameStateObserver.observe(this) {
            onGameStateChange(it)
        }

        singlePlayerViewModel.startGame()
    }


    private fun onGameStateChange(state: GameState) {
        when(state) {
            GameState.START -> {}
            GameState.CORRECT_EXPRESSION, GameState.LEVEL_COMPLETED -> boardGridView.buildBoard()
            else -> {}
        }

        fragmentScoreBinding?.score?.text = singlePlayerViewModel.game.totalPoints.toString()
        fragmentScoreBinding?.timeleft?.text = singlePlayerViewModel.game.timer.toString()
    }
}