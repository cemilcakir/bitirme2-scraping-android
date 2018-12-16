package com.ccakir.bitirme2_scraping_andoid.adapters

import android.content.Context
import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.ccakir.bitirme2_scraping_andoid.R
import com.ccakir.bitirme2_scraping_andoid.activities.DetailsActivity
import com.ccakir.bitirme2_scraping_andoid.activities.MainActivity
import com.ccakir.bitirme2_scraping_andoid.models.HistoryModel
import kotlin.concurrent.thread

class HistoryAdapter(private var history: ArrayList<HistoryModel>,private val context: Context): RecyclerView.Adapter<HistoryAdapter.ViewHolder>(){

    class ViewHolder(val  cardView: CardView) : RecyclerView.ViewHolder(cardView)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): HistoryAdapter.ViewHolder {
        val cardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.cell_history, parent, false) as CardView

        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val log = history[position]
        val txtHistory = holder.cardView.findViewById<TextView>(R.id.txtHistory)

        if(!log.query.isNullOrEmpty())
            txtHistory.text = if(log.query.length > 20) log.query.substring(0, 20) + "..." else log.query
        else if(!log.product.isNullOrEmpty())
            txtHistory.text = if(log.product.length > 20) log.product.substring(0, 20) + "..." else log.product

        holder.cardView.setOnClickListener {
            if(!log.product.isNullOrEmpty() && !log.link.isNullOrEmpty()) {
                val i = Intent(context, DetailsActivity::class.java)
                DetailsActivity.productDetailLink = log.link
                context.startActivity(i)
            }
            else if(!log.query.isNullOrEmpty()) {
                thread {
                    (context as MainActivity).getProducts(log.query)
                }
            }
        }
    }

    override fun getItemCount() = history.size

    fun addAll(logs: ArrayList<HistoryModel>) {
        history.addAll(logs)
        notifyDataSetChanged()
    }

    fun removeAll() {
        history = ArrayList()
        notifyDataSetChanged()
    }
}
