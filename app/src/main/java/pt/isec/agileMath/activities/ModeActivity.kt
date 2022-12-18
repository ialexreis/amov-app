package pt.isec.agileMath.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import pt.isec.agileMath.databinding.ActivityModeBinding
import pt.isec.agileMath.multiplayer.Popups

class ModeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModeBinding

    companion object {
        fun getIntent(ctx: Context): Intent {
            return Intent(ctx, ModeActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModeBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.btnStartMultiplayer.setOnClickListener { Popups.serverPopup(this) }
        binding.btnStartClient.setOnClickListener { Popups.clientPopup(this) }
    }
}