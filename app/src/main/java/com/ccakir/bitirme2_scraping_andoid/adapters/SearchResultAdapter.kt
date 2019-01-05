package com.ccakir.bitirme2_scraping_andoid.adapters

import android.content.Context
import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.ccakir.bitirme2_scraping_andoid.R
import com.ccakir.bitirme2_scraping_andoid.activities.DetailsActivity
import com.ccakir.bitirme2_scraping_andoid.models.SearchResultModel
import com.koushikdutta.ion.Ion
import kotlinx.android.synthetic.main.cell_search_result.view.*

class SearchResultAdapter(private var result: ArrayList<SearchResultModel>, private val context: Context): RecyclerView.Adapter<SearchResultAdapter.ViewHolder>(){

    class ViewHolder(val  cardView: CardView) : RecyclerView.ViewHolder(cardView)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): SearchResultAdapter.ViewHolder {
        val cardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.cell_search_result, parent, false) as CardView

        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = result.get(position)

        val imgProduct = holder.cardView.findViewById<ImageView>(R.id.imgProduct)
        Ion.with(imgProduct)
            .load(product.productImage)

        val txtProductName = holder.cardView.txtProductName
        txtProductName.text = product.productName

        val txtPrice = holder.cardView.txtPrice

        var price = product.productPrice.split("K")[0].trim()
        if(!price.contains("TL"))
            price += " TL"

        txtPrice.text = price

        val txtSite = holder.cardView.txtSite
        txtSite.text = product.siteName

        holder.cardView.setOnClickListener {
            val i = Intent(context, DetailsActivity::class.java)
            DetailsActivity.productDetailLink = product.productLink
            context.startActivity(i)
        }
    }

    override fun getItemCount() = result.size

    fun addAll(products: ArrayList<SearchResultModel>) {
        result.addAll(products)
        notifyDataSetChanged()
    }

    fun removeAll() {
        result = ArrayList()
        notifyDataSetChanged()
    }

    fun getAll() = result
}