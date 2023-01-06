package pt.isec.agileMath.activities


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pt.isec.agileMath.databinding.ActivityMainMenuBinding
import java.util.UUID

class MainMenuActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSinglePlayer.setOnClickListener { onSinglePlayer() }
        binding.btnMultiplayer.setOnClickListener { onMultiplayer() }
        binding.btnOptions.setOnClickListener { onOptions() }

    }

    fun onOptions() = startActivity(OptionsActivity.getIntent(this))
    fun onSinglePlayer() = startActivity(GameActivity.getIntent(this))
    fun onMultiplayer() = startActivity(ModeActivity.getIntent(this))

    companion object {
        val APP_EXECUTION_UUID = UUID.randomUUID().toString()
    }
}