package pt.isec.agileMath.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import pt.isec.agileMath.databinding.ActivityModeBinding
import pt.isec.agileMath.multiplayer.Connection
import pt.isec.agileMath.multiplayer.Popups
import pt.isec.agileMath.viewModels.gameViewModel.MultiplayerPlayerViewModel
import pt.isec.agileMath.viewModels.gameViewModel.SinglePlayerViewModel

class ModeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModeBinding
    private val multiplayerPlayerViewModel: MultiplayerPlayerViewModel by viewModels()

    companion object {
        fun getIntent(ctx: Context): Intent {
            return Intent(ctx, ModeActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModeBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.btnStartMultiplayer.setOnClickListener {
            Connection(this.multiplayerPlayerViewModel).startServer()
            Popups.serverPopup(this)
        }
        binding.btnStartClient.setOnClickListener { Popups.clientPopup(this) }
    }
}