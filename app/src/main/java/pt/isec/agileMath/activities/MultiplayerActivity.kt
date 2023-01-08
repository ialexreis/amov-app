package pt.isec.agileMath.activities

import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import pt.isec.agileMath.R
import pt.isec.agileMath.constants.GameState
import pt.isec.agileMath.databinding.ActivityMultiplayerBinding
import pt.isec.agileMath.databinding.FragmentNewLevelTransitionBinding
import pt.isec.agileMath.databinding.FragmentScoreBinding
import pt.isec.agileMath.services.Popups
import pt.isec.agileMath.viewModels.gameViewModel.MultiplayerPlayerViewModel
import pt.isec.agileMath.viewModels.gameViewModel.SinglePlayerViewModel
import pt.isec.agileMath.views.BoardGridView
import pt.isec.agileMath.views.ScoresRecyclerListView

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

        if (viewModel.isGameStarted) {
            refreshBoard()
        }

        refreshPlayersList()

        binding.frScore?.addView(fragmentScoreBinding.root)

        fragmentNewLevelTransition.fabPauseToggle.setOnClickListener{ viewModel.togglePause() }
        viewModel.gameStateObserver.observe(this) {
            onUiGameStateChange(it)
        }
        viewModel.gameStateObserver.observe(this) {
            viewModel.onMultiplayerGameStateChange(it)
        }

        viewModel.initMultiplayer(this, intent.getBooleanExtra(TO_START_AS_HOST_KEY, false))
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
                refreshPlayersList()

                Popups.serverPopup(this, { onStartGameAsServer() }, { onCancelServerGame() })
            }
            GameState.START_AS_CLIENT -> {
                refreshPlayersList()

                Popups.clientPopup(this, { viewModel.connectToServer(it) }, {
                    finish()
                })
            }
            GameState.REFRESH_PLAYERS_LIST -> refreshPlayersList()
            GameState.CORRECT_EXPRESSION -> {
                refreshBoard()
                refreshPlayersList()
                binding.imgOperationResult?.setBackgroundResource(R.drawable.ic_baseline_correct)
            }
            GameState.FAILED_EXPRESSION -> {
                binding.imgOperationResult?.setBackgroundResource(R.drawable.ic_baseline_wrong)
            }
            GameState.GAME_STARTED -> {
                refreshBoard()
                viewModel.startGame()
                Popups.close()
            }
            GameState.LEVEL_COMPLETED -> setLayoutOnNewLevelTransition(state)
            GameState.NEW_LEVEL_COUNTDOWN_STARTED -> setLayoutOnNewLevelTransition(state)
            GameState.NEW_LEVEL_STARTED -> {
                setLayoutOnNewLevelTransition(state)
                refreshBoard()
                refreshPlayersList()
            }
            GameState.GAME_OVER_TIME_OUT -> setLayoutOnNewLevelTransition(state)
            GameState.GAME_OVER -> setLayoutOnNewLevelTransition(state)
            GameState.CLIENT_DISCONNECTED, GameState.SOCKET_ERROR -> {
                refreshPlayersList()
            }
            GameState.CONNECTION_TO_SERVER_ESTABLISHED ->
                Popups.waitingPopupSpinner(this, R.string.popup_waiting_game_to_start) { finish() }
            GameState.CONNECTION_TO_SERVER_ERROR -> {
                Toast.makeText(this, R.string.error_address, Toast.LENGTH_LONG).show()
                finish()
                return
            }

            else -> {}
        }

        fragmentScoreBinding?.score?.text = viewModel.player.playerDetails.score.toString()
        fragmentScoreBinding?.tvLevel?.text = viewModel.game.level.toString()
        fragmentScoreBinding?.timeleft?.text = viewModel.game.timer.toString()
    }

    private fun onStartGameAsServer() {
        if (viewModel.playersMap.size >= 2) {
            viewModel.setGameState(GameState.GAME_STARTED)
            return
        }

        Toast.makeText(this, R.string.popup_min_players_required, Toast.LENGTH_LONG).show()
    }

    private fun onCancelServerGame() {
        viewModel.endGame()
        finish()
    }

    private fun refreshPlayersList() {
        val sortedPlayersList = viewModel.playersMap.values.sortedByDescending { it.playerDetails.score }

        binding.frPlayersList?.removeAllViewsInLayout()
        binding.frPlayersList?.addView(
            ScoresRecyclerListView(this, sortedPlayersList)
        )
    }

    private fun refreshBoard() {
        boardGridView = BoardGridView(this, viewModel)
        binding.frGameMatrix?.removeAllViewsInLayout()
        binding.frGameMatrix?.addView(boardGridView)
    }

    private fun setLayoutOnNewLevelTransition(gameState: GameState) {
        refreshPlayersList()

        binding.imgOperationResult?.setBackgroundResource(0)
        binding.frGameMatrix?.removeAllViewsInLayout()

        when(gameState) {
            GameState.LEVEL_COMPLETED -> {
                val textView = TextView(this)
                textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                textView.setText(R.string.fragment_level_finish)

                binding.frGameMatrix?.addView(textView)
            }
            GameState.NEW_LEVEL_COUNTDOWN_STARTED -> {
                binding.frGameMatrix?.addView(fragmentNewLevelTransition.root)

                fragmentNewLevelTransition?.tvLevel?.text = viewModel.game.level.toString()
                fragmentNewLevelTransition?.tvCountdown?.text = "5 sec"
                fragmentNewLevelTransition?.fabPauseToggle?.visibility = FloatingActionButton.INVISIBLE
            }
            GameState.NEW_LEVEL_STARTED -> {
                refreshBoard()
                refreshPlayersList()
            }
            GameState.GAME_OVER -> {
                Toast.makeText(this, R.string.fragment_multiplayer_game_over, Toast.LENGTH_LONG).show()
                finish()
            }
            GameState.GAME_OVER_TIME_OUT -> {
                val textView = TextView(this)
                textView.setText(R.string.fragment_game_over)
                textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

                binding.frGameMatrix?.addView(textView)
            }
            else -> {}
        }
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