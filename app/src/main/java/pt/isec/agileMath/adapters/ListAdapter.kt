package pt.isec.agileMath.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pt.isec.agileMath.databinding.ContentListViewBinding
import pt.isec.agileMath.models.Result

class ListAdapter(var dataset: MutableList<Result>) :
    RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    class ViewHolder(val binding: ContentListViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(get: Result) {
            binding.score.text = get.score.toString()
            binding.time.text = get.totalTime.toString()
            binding.name.text = get.player.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ContentListViewBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataset[position])
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

}