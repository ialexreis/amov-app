package pt.isec.agileMath.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alex.amov_app.databinding.ActivityMainMenuBinding

class MainMenuActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSinglePlayer.setOnClickListener { onSinglePlayer() }
        binding.btnMultiplayer.setOnClickListener { onMultiplayer() }
        binding.btnEditProfile.setOnClickListener { onEditProfile() }
    }

    fun onEditProfile() = startActivity(EditProfileActivity.getIntent(this))
    fun onSinglePlayer() {}
    fun onMultiplayer() {}
}