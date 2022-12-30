package pt.isec.agileMath.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import pt.isec.agileMath.constants.ListType
import pt.isec.agileMath.databinding.ActivityOptionsBinding

class OptionsActivity : AppCompatActivity() {

    companion object {
        fun getIntent(ctx: Context): Intent {
            return Intent(ctx, OptionsActivity::class.java)
        }
    }

    private lateinit var binding: ActivityOptionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEditProfile.setOnClickListener { onEditProfile() }
        binding.btnHighestScores.setOnClickListener { onScores(ListType.HIGHSCORES.tag) }
        binding.btnMyScores.setOnClickListener { onScores(ListType.MYSCORES.tag) }
    }

    fun onEditProfile() = startActivity(EditProfileActivity.getIntent(this))
    fun onScores(type: String) {
        val intent = ScoreRegistryActivity.getIntent(this)
        intent.putExtra("type", type)
        startActivity(intent)
    }
}