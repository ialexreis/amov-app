package pt.isec.agileMath.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import pt.isec.agileMath.databinding.ActivityMainMenuBinding
import pt.isec.agileMath.models.Board

class MainMenuActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSinglePlayer.setOnClickListener { onSinglePlayer() }
        binding.btnMultiplayer.setOnClickListener { onMultiplayer() }
        binding.btnEditProfile.setOnClickListener { onEditProfile() }

        val board = Board(10, arrayOf("+", "-", "*", "/"))
        Log.e("INFO", board.toString())
    }

    fun onEditProfile() = startActivity(EditProfileActivity.getIntent(this))
    fun onSinglePlayer() {}
    fun onMultiplayer() {}
}