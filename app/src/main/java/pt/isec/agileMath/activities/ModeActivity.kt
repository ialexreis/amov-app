package pt.isec.agileMath.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import pt.isec.agileMath.databinding.ActivityModeBinding

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

        binding.btnStartMultiplayer.setOnClickListener {
            startActivity(MultiplayerActivity.getIntent(this, true))
        }
        binding.btnStartClient.setOnClickListener {
            startActivity(MultiplayerActivity.getIntent(this))
        }
    }
}