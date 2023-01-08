package pt.isec.agileMath.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.isec.agileMath.R
import pt.isec.agileMath.databinding.FragmentScoresListItemBinding
import pt.isec.agileMath.models.MultiplayerPlayer
import pt.isec.agileMath.models.PlayerResult


class ScoresRecyclerListView@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleRes: Int = 0
): RecyclerView(context, attrs, defStyleRes) {

    private lateinit var scoresResults: List<MultiplayerPlayer>

    constructor(context: Context, scoresResults: List<MultiplayerPlayer>) : this(context) {
        val layoutManagerScores = LinearLayoutManager(context)

        layoutManagerScores.orientation = LinearLayoutManager.HORIZONTAL

        this.scoresResults = scoresResults

        this.layoutManager = layoutManagerScores
        this.adapter = getListViewAdapter()
    }

    private fun getListViewAdapter(): Adapter<ViewHolder> {
        return object : Adapter<ViewHolder>() {
            fun getItem(position: Int): MultiplayerPlayer = scoresResults.elementAt(position)

            override fun getItemId(position: Int): Long = position.toLong()

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                var binding = FragmentScoresListItemBinding.inflate(LayoutInflater.from(context), parent, false)

                return ViewHolder(binding)
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val item = getItem(position)

                // TODO set image from base64
                // holder.ivProfileImage.setImageBitmap()
                holder.tvNickname.text = item.playerDetails.player.name
                holder.tvScore.text = "${item.playerDetails.score} pts"
                holder.tvPodiumPosition.text = "${position + 1}º"

                if (item.lostGame) {
                    holder.ivLevelState.setBackgroundResource(R.drawable.ic_baseline_wrong)
                }
                else if (item.isLevelFinished) {
                    holder.ivLevelState.setBackgroundResource(R.drawable.ic_baseline_correct)
                }
                else {
                    holder.ivLevelState.setBackgroundResource(android.R.color.transparent)
                }

            }

            override fun getItemCount(): Int = scoresResults.size
        }
    }

    private class ViewHolder: RecyclerView.ViewHolder {

        var ivProfileImage: ImageView
        var tvNickname: TextView
        var tvScore: TextView
        var tvPodiumPosition: TextView
        var ivLevelState: ImageView

        constructor(itemView: FragmentScoresListItemBinding): super(itemView.root) {
            this.ivProfileImage = itemView.ivPlayerImage
            this.tvNickname = itemView.tvNickname
            this.tvScore = itemView.tvScore
            this.tvPodiumPosition = itemView.tvPodiumPosition
            this.ivLevelState = itemView.ivLevelState
        }
    }
}