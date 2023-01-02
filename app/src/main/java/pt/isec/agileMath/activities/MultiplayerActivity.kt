package pt.isec.agileMath.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import pt.isec.agileMath.R
import pt.isec.agileMath.constants.GameState
import pt.isec.agileMath.databinding.ActivityMultiplayerBinding
import pt.isec.agileMath.databinding.FragmentNewLevelTransitionBinding
import pt.isec.agileMath.databinding.FragmentScoreBinding
import pt.isec.agileMath.models.Game
import pt.isec.agileMath.models.Result
import pt.isec.agileMath.models.SocketMessagePayload
import pt.isec.agileMath.services.multiplayerSockets.Popups
import pt.isec.agileMath.viewModels.gameViewModel.MultiplayerPlayerViewModel
import pt.isec.agileMath.views.BoardGridView
import pt.isec.agileMath.views.ScoresRecyclerListView
import java.io.IOException

class MultiplayerActivity : AppCompatActivity() {
    private val viewModel: MultiplayerPlayerViewModel by viewModels()

    private lateinit var binding: ActivityMultiplayerBinding
    private lateinit var fragmentScoreBinding: FragmentScoreBinding
    private lateinit var fragmentNewLevelTransition: FragmentNewLevelTransitionBinding

    private lateinit var boardGridView: BoardGridView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiplayerBinding.inflate(layoutInflater)
        fragmentScoreBinding = FragmentScoreBinding.inflate(layoutInflater)
        fragmentNewLevelTransition = FragmentNewLevelTransitionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        boardGridView = BoardGridView(this, viewModel)

        binding.frScore?.addView(fragmentScoreBinding.root)
        // binding.frGameMatrix?.addView(boardGridView)
        // binding.frPlayersList?.addView(ScoresRecyclerListView(this, arrayListOf()))

        fragmentNewLevelTransition.fabPauseToggle.setOnClickListener{ viewModel.togglePause() }
        viewModel.gameStateObserver.observe(this) {
            onUiGameStateChange(it)
        }
        viewModel.gameStateObserver.observe(this) {
            viewModel.onMultiplayerGameStateChange(it)
        }

        viewModel.initGame(intent.getBooleanExtra(TO_START_AS_HOST_KEY, false))
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setMessage(R.string.exit_confirmation_message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(R.string.yes) { _, _ ->
                // TODO send exit message to server
                // todo if host: close the game
                viewModel.endGame()
                finish()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun onUiGameStateChange(state: GameState) {
        when(state) {
            GameState.START_AS_HOST -> {
                viewModel.startServer()
                Popups.serverPopup(this, { onStartGameAsServer() }, { onCancelServerGame() })
            }
            GameState.START_AS_CLIENT ->
                Popups.clientPopup(this, {  viewModel.connectToServer(it) }, {
                    finish()
                })
            GameState.REFRESH_PLAYERS_LIST -> binding.frPlayersList?.addView(
                ScoresRecyclerListView(this, viewModel.playersConnected)
            )
            GameState.CORRECT_EXPRESSION -> {
            }
            GameState.FAILED_EXPRESSION -> {
            }
            GameState.LEVEL_COMPLETED -> {
            }
            GameState.NEW_LEVEL_COUNTDOWN_TICK -> {
            }
            GameState.NEW_LEVEL_COUNTDOWN_PAUSED -> {
            }
            GameState.NEW_LEVEL_COUNTDOWN_RESUMED -> {
            }
            GameState.NEW_LEVEL_STARTED -> {
            }
            GameState.GAME_OVER_TIME_OUT -> {
            }
            GameState.CONNECTION_ESTABLISHED ->
               return viewModel.replyToServer(SocketMessagePayload(Game(), Result(), GameState.CLIENT_CONNECTED))

            GameState.CONNECTION_TO_SERVER_ERROR -> {
                Toast.makeText(this, R.string.error_address, Toast.LENGTH_LONG).show()
                finish()
                return
            }

            else -> {}
        }

        fragmentScoreBinding?.score?.text = viewModel.result.score.toString()
        fragmentScoreBinding?.tvLevel?.text = viewModel.game.level.toString()
        fragmentScoreBinding?.timeleft?.text = viewModel.game.timer.toString()
    }

    private fun onStartGameAsServer() {

    }

    private fun onCancelServerGame() {
        viewModel.endGame()
        finish()
    }

    companion object {
        const val TO_START_AS_HOST_KEY = "TO_START_AS_HOST_KEY"

        fun getIntent(ctx: Context, toStartAsHost: Boolean = false): Intent {
            val intent = Intent(ctx, MultiplayerActivity::class.java)

            intent.putExtra(TO_START_AS_HOST_KEY, toStartAsHost)

            return intent
        }
    }
}