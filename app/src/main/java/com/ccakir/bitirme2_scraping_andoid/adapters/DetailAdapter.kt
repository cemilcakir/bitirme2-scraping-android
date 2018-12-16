package com.ccakir.bitirme2_scraping_andoid.adapters

import android.content.Context
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.ccakir.bitirme2_scraping_andoid.R
import com.ccakir.bitirme2_scraping_andoid.models.DetailModel

class DetailAdapter(private var details: ArrayList<DetailModel>,private val context: Context): RecyclerView.Adapter<DetailAdapter.ViewHolder>(){

    class ViewHolder(val  cardView: CardView) : RecyclerView.ViewHolder(cardView)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): DetailAdapter.ViewHolder {
        val cardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.cell_detail, parent, false) as CardView

        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val detail = details[position]

        val txtTitle = holder.cardView.findViewById<TextView>(R.id.txtTitle)
        val txtValue = holder.cardView.findViewById<TextView>(R.id.txtValue)

        txtTitle.text = detail.title
        txtValue.text = detail.value
    }

    override fun getItemCount() = details.size

    fun addAll(detail: ArrayList<DetailModel>) {
        details.addAll(detail)
        notifyDataSetChanged()
    }

    fun removeAll() {
        details = ArrayList()
        notifyDataSetChanged()
    }
}
