package pt.isec.agileMath.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import pt.isec.agileMath.databinding.ActivityCreditsBinding

class CreditsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreditsBinding

    companion object {
        fun getIntent(ctx: Context): Intent {
            return Intent(ctx, CreditsActivity::class.java)
        }

        var aluno1 = object {
            val nome = "Alexandre Reis"
            val nMecanografico = "2018019414"
            val email = "a21280926@isec.pt"

            override fun toString() = "$nome - $nMecanografico \n $email"
        }
        var aluno2 = object {
            val nome = "Diogo Barbosa"
            val nMecanografico = "2018012425"
            val email = "a21280926@isec.pt"

            override fun toString() = "$nome - $nMecanografico \n $email"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreditsBinding.inflate(layoutInflater)

        setContentView(binding.root)
        binding.aluno1.text = aluno1.toString()
        binding.aluno2.text = aluno2.toString()
    }
}