package pt.isec.agileMath.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import pt.isec.agileMath.R
import pt.isec.agileMath.constants.GameState
import pt.isec.agileMath.constants.Tables
import pt.isec.agileMath.databinding.ActivityGameBinding
import pt.isec.agileMath.databinding.FragmentNewLevelTransitionBinding
import pt.isec.agileMath.databinding.FragmentScoreBinding
import pt.isec.agileMath.models.Player
import pt.isec.agileMath.models.Result
import pt.isec.agileMath.services.FirebaseService
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
    private lateinit var fragmentNewLevelTransition: FragmentNewLevelTransitionBinding

    private lateinit var boardGridView: BoardGridView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        fragmentScoreBinding = FragmentScoreBinding.inflate(layoutInflater)
        fragmentNewLevelTransition = FragmentNewLevelTransitionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        singlePlayerViewModel.activityBinding = binding
        singlePlayerViewModel.fragmentScoreBinding = fragmentScoreBinding

        boardGridView = BoardGridView(this, singlePlayerViewModel)

        binding.frScore?.addView(fragmentScoreBinding.root)
        binding.frGameMatrix?.addView(boardGridView)

        fragmentNewLevelTransition.fabPauseToggle.setOnClickListener{ singlePlayerViewModel.togglePause() }

        singlePlayerViewModel.gameStateObserver.observe(this) {
            onGameStateChange(it)
        }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setMessage(R.string.exit_confirmation_message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(R.string.yes) { _ ,_ ->
                // TODO the score in firebase
                runBlocking {
                    FirebaseService.save(Tables.SCORES.parent, singlePlayerViewModel.result)
                }
                finish()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun onGameStateChange(state: GameState) {
        when(state) {
            GameState.CORRECT_EXPRESSION -> {
                boardGridView?.refreshBoard()
                binding.imgOperationResult?.setBackgroundResource(R.drawable.ic_baseline_correct)
            }
            GameState.FAILED_EXPRESSION -> {
                binding.imgOperationResult?.setBackgroundResource(R.drawable.ic_baseline_wrong)
            }
            GameState.LEVEL_COMPLETED -> {
                singlePlayerViewModel.initCountdownToNextLevel()
                setLayoutOnNewLevelTransition(state)
            }
            GameState.NEW_LEVEL_COUNTDOWN_TICK -> {
                fragmentNewLevelTransition?.tvLevel?.text = singlePlayerViewModel.game.level.toString()
                fragmentNewLevelTransition?.tvCountdown?.text = singlePlayerViewModel.countdownToInitNextLevel.toString()
            }
            GameState.NEW_LEVEL_COUNTDOWN_PAUSED -> {
                fragmentNewLevelTransition.fabPauseToggle.setImageResource(android.R.drawable.ic_media_play)
            }
            GameState.NEW_LEVEL_COUNTDOWN_RESUMED -> {
                fragmentNewLevelTransition.fabPauseToggle.setImageResource(android.R.drawable.ic_media_pause)
            }
            GameState.NEW_LEVEL_STARTED -> {
                singlePlayerViewModel.startNewLevel()
                setLayoutOnNewLevelTransition(state)
                boardGridView?.refreshBoard()
            }
            GameState.GAME_OVER_TIME_OUT -> {
                // TODO handle game over and save the score in firebase
                runBlocking {
                    FirebaseService.save(Tables.SCORES.parent, singlePlayerViewModel.result)
                    singlePlayerViewModel.setGameState(GameState.GAME_OVER)
                }
            }
            else -> {}
        }

        fragmentScoreBinding?.score?.text = singlePlayerViewModel.result.score.toString()
        fragmentScoreBinding?.tvLevel?.text = singlePlayerViewModel.game.level.toString()
        fragmentScoreBinding?.timeleft?.text = singlePlayerViewModel.game.timer.toString()
    }

    private fun setLayoutOnNewLevelTransition(gameState: GameState) {
        binding.imgOperationResult?.setBackgroundResource(0)
        binding.frGameMatrix?.removeAllViewsInLayout()

        when(gameState) {
            GameState.LEVEL_COMPLETED -> {
                binding.frGameMatrix?.addView(fragmentNewLevelTransition.root)

                fragmentNewLevelTransition?.tvLevel?.text = singlePlayerViewModel.game.level.toString()
                fragmentNewLevelTransition?.tvCountdown?.text = singlePlayerViewModel.countdownToInitNextLevel.toString()
            }
            GameState.NEW_LEVEL_STARTED -> binding.frGameMatrix?.addView(boardGridView)
            else -> {}
        }
    }
}