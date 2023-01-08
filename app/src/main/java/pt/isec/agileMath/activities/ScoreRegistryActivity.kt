package pt.isec.agileMath.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.QuerySnapshot
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import pt.isec.agileMath.adapters.ListAdapter
import pt.isec.agileMath.constants.Constants
import pt.isec.agileMath.constants.ListType
import pt.isec.agileMath.constants.Tables
import pt.isec.agileMath.databinding.ActivityScoreRegistryBinding
import pt.isec.agileMath.models.Player
import pt.isec.agileMath.models.PlayerResult
import pt.isec.agileMath.services.FirebaseService
import pt.isec.agileMath.services.PreferenceServices
import pt.isec.agileMath.services.PreferenceServices.nickname

class ScoreRegistryActivity : AppCompatActivity() {

    companion object {
        fun getIntent(ctx: Context): Intent {
            return Intent(ctx, ScoreRegistryActivity::class.java)
        }
    }

    private lateinit var adapter: ListAdapter
    private lateinit var binding: ActivityScoreRegistryBinding
    private lateinit var sharedPreferences: SharedPreferences

    var data: MutableList<PlayerResult> = listOf<PlayerResult>().toMutableList()
    var gson: Gson = Gson()
    var type: String = ListType.HIGHSCORES.tag

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScoreRegistryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        type = intent.getStringExtra("type").toString()
        sharedPreferences = PreferenceServices.customPreference(applicationContext, Constants.PREFERENCE_NAME)

        runBlocking {
            fetchResultList(type)
        }
    }

    override fun onStart() {
        super.onStart()
        binding.rvRecycler.layoutManager = LinearLayoutManager(this)
        adapter = ListAdapter(data)
        binding.rvRecycler.adapter = adapter
    }

    private suspend fun fetchResultList(type: String) {
        var response: QuerySnapshot? = FirebaseService.list(Tables.SCORES.parent).await()

        var result = response?.documents
        result?.forEach { item ->
            val playerhash : HashMap<String, String> = item.data?.get("player") as HashMap<String, String>
            var result: PlayerResult = PlayerResult(
                Player( name = playerhash.get("name"), pictureUrl = playerhash["pictureUrl"]),
                score = item.data?.get("score") as Long,
                totalTime = item.data?.get("totalTime") as Long
            )

            data.add(result)
            Log.i("AGILES", data.toString())
        }

        if(type == ListType.HIGHSCORES.tag) {
            data.sortedByDescending {
                it.score
            }
        } else {
            data = data.filter {
                it.player.name ==  sharedPreferences.nickname
            }.toMutableList()
        }
    }
}