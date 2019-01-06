package com.ccakir.bitirme2_scraping_andoid.activities

import android.support.v7.app.ActionBar
import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.Button
import android.support.v7.widget.SwitchCompat
import android.widget.EditText
import android.widget.TextView
import com.ccakir.bitirme2_scraping_andoid.R
import com.ccakir.bitirme2_scraping_andoid.adapters.HistoryAdapter
import com.ccakir.bitirme2_scraping_andoid.adapters.SearchResultAdapter
import com.ccakir.bitirme2_scraping_andoid.models.HistoryModel
import com.ccakir.bitirme2_scraping_andoid.models.SearchResultModel
import com.koushikdutta.ion.Ion
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.net.URLEncoder
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    companion object {
        var wordsForSearch: String? = null
    }

    private lateinit var recyclerQueries: RecyclerView
    private lateinit var adapterQueries: HistoryAdapter
    private lateinit var recyclerProducts: RecyclerView
    private lateinit var adapterProducts: HistoryAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var recyclerResult: RecyclerView
    private lateinit var adapterResult: SearchResultAdapter
    private lateinit var filteredResult: ArrayList<SearchResultModel>
    private lateinit var originalProducts: ArrayList<SearchResultModel>
    private lateinit var dialog: Dialog
    private lateinit var filterDialog: Dialog
    private lateinit var filters: ArrayList<String>
    private var min = 0
    private var max = 0
    private var isQueriesShowing = true
    private var isProductsShowing = true
    private var txtTitle: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        actionBar?.setDisplayShowCustomEnabled(true)
        actionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM;
        actionBar?.setCustomView(layoutInflater.inflate(R.layout.custom_title, null),
            ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
        )
        txtTitle = actionBar?.customView?.findViewById(R.id.txtTitle)
        txtTitle?.text = resources.getString(R.string.name)

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        filters = arrayListOf()
        filteredResult = arrayListOf()

        adapterQueries = HistoryAdapter(ArrayList(), this)
        adapterProducts = HistoryAdapter(ArrayList(), this)
        adapterResult = SearchResultAdapter(ArrayList(), this)

        viewManager = LinearLayoutManager(this)

        recyclerQueries = nav_view.findViewById(R.id.recyclerQueries)
        recyclerQueries.setHasFixedSize(true)
        recyclerQueries.layoutManager = viewManager
        recyclerQueries.adapter = adapterQueries

        viewManager = LinearLayoutManager(this)
        recyclerProducts = nav_view.findViewById(R.id.recyclerProducts)
        recyclerProducts.setHasFixedSize(true)
        recyclerProducts.layoutManager = viewManager
        recyclerProducts.adapter = adapterProducts

        viewManager = LinearLayoutManager(this)
        recyclerResult = findViewById(R.id.recyclerResult)
        recyclerResult.setHasFixedSize(true)
        recyclerResult.layoutManager = viewManager
        recyclerResult.adapter = adapterResult

        dialog = Dialog(this)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(false)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_loading, null)
        dialog.setContentView(dialogView)

        layoutQueries.setOnClickListener {
            if(isQueriesShowing) {
                recyclerQueries.visibility = View.GONE
                imgQueriesExpandCollapse.background = resources.getDrawable(R.drawable.ic_show_more)
            }
            else{
                recyclerQueries.visibility = View.VISIBLE
                imgQueriesExpandCollapse.background = resources.getDrawable(R.drawable.ic_show_less)
            }

            isQueriesShowing = !isQueriesShowing
        }

        layoutProducts.setOnClickListener {
            if(isProductsShowing){
                recyclerProducts.visibility = View.GONE
                imgProductsExpandCollapse.background = resources.getDrawable(R.drawable.ic_show_more)
            }
            else {
                recyclerProducts.visibility = View.VISIBLE
                imgProductsExpandCollapse.background = resources.getDrawable(R.drawable.ic_show_less)
            }

            isProductsShowing = !isProductsShowing
        }

        thread {
            getHistory()
        }

        thread {
            getProducts(null)
        }

        imgFilter.setOnClickListener {
            filterDialog = Dialog(this)
            val filterDialogView = LayoutInflater.from(this).inflate(R.layout.custom_search_view, null)

            val btnFilter = filterDialogView.findViewById<Button>(R.id.btnFilter)
            val btnClearFilter = filterDialogView.findViewById<Button>(R.id.btnClearFilter)
            val swtHepsiburada = filterDialogView.findViewById<SwitchCompat>(R.id.swtHepsiburada)
            val swtN11 = filterDialogView.findViewById<SwitchCompat>(R.id.swtN11)
            val swtTeknosa = filterDialogView.findViewById<SwitchCompat>(R.id.swtTeknosa)
            val swtKitapyurdu = filterDialogView.findViewById<SwitchCompat>(R.id.swtKitapyurdu)
            val swtDown = filterDialogView.findViewById<SwitchCompat>(R.id.swtDown)
            val swtUp = filterDialogView.findViewById<SwitchCompat>(R.id.swtUp)
            val txtMin = filterDialogView.findViewById<EditText>(R.id.txtMin)
            val txtMax = filterDialogView.findViewById<EditText>(R.id.txtMax)

            if(filters.contains("hepsiburada"))
                swtHepsiburada.isChecked = true
            if(filters.contains("n11"))
                swtN11.isChecked = true
            if(filters.contains("teknosa"))
                swtTeknosa.isChecked = true
            if(filters.contains("kitapyurdu"))
                swtKitapyurdu.isChecked = true
            if(filters.contains("up"))
                swtUp.isChecked = true
            if(filters.contains("down"))
                swtDown.isChecked = true
            txtMin.setText(min.toString())
            txtMax.setText(max.toString())

            swtDown.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked && swtUp.isChecked)
                    swtUp.isChecked = false
                if(isChecked)
                    filters.add("down")
                else
                    filters.remove("down")
            }

            swtUp.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked && swtDown.isChecked)
                    swtDown.isChecked = false
                if(isChecked)
                    filters.add("up")
                else
                    filters.remove("up")
            }

            swtHepsiburada.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked)
                    filters.add("hepsiburada")
                else
                    filters.remove("hepsiburada")
            }

            swtN11.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked)
                    filters.add("n11")
                else
                    filters.remove("n11")
            }

            swtTeknosa.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked)
                    filters.add("teknosa")
                else
                    filters.remove("teknosa")
            }

            swtKitapyurdu.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked)
                    filters.add("kitapyurdu")
                else
                    filters.remove("kitapyurdu")
            }

           btnFilter.setOnClickListener {
               max = if(txtMin.text.toString().toInt() > txtMax.text.toString().toInt()) txtMin.text.toString().toInt() else txtMax.text.toString().toInt()
               min = if(txtMax.text.toString().toInt() > txtMin.text.toString().toInt()) txtMin.text.toString().toInt() else txtMax.text.toString().toInt()
               if(filters.size > 0) {
                   filteredResult = arrayListOf()
                   originalProducts.forEach { product ->
                       if(filters.contains("hepsiburada") && product.siteName == "hepsiburada")
                           filteredResult.add(product)
                       if(filters.contains("n11") && product.siteName == "n11")
                           filteredResult.add(product)
                       if(filters.contains("teknosa") && product.siteName == "teknosa")
                           filteredResult.add(product)
                       if(filters.contains("kitapyurdu") && product.siteName == "kitapyurdu")
                           filteredResult.add(product)
                   }

                   filteredResult = filterByPrice(filteredResult)

                   if(filters.contains("up"))
                       if(filteredResult.size > 0)
                           filteredResult = ArrayList(filteredResult.sortedWith(compareBy { product ->
                               product.productPrice.trim().replace(",","").replace(".", "").toDouble()
                           }))
                       else
                           filteredResult = ArrayList(adapterResult.getAll().sortedWith(compareBy { product ->
                               product.productPrice.trim().replace(",","").replace(".", "").toDouble()
                           }))
                   if(filters.contains("down"))
                       if(filteredResult.size > 0)
                           filteredResult = ArrayList(filteredResult.sortedWith(compareBy { product ->
                               product.productPrice.trim().replace(",","").replace(".", "").toDouble()
                           }).reversed())
                       else
                           filteredResult = ArrayList(adapterResult.getAll().sortedWith(compareBy { product ->
                               product.productPrice.trim().replace(",","").replace(".", "").toDouble()
                           }).reversed())

                   adapterResult.removeAll()
                   adapterResult.addAll(filteredResult)
               }
               else if(min != 0 || max != 0) {
                   adapterResult.removeAll()
                   adapterResult.addAll(filterByPrice(originalProducts))
               }
               else {
                   filteredResult = arrayListOf()
                   filters = arrayListOf()
                   min = 0
                   max = 0
                   adapterResult.removeAll()
                   adapterResult.addAll(originalProducts)
               }

               filterDialog.dismiss()
            }

            btnClearFilter.setOnClickListener {
                filteredResult = arrayListOf()
                filters = arrayListOf()
                min = 0
                max = 0
                adapterResult.removeAll()
                adapterResult.addAll(originalProducts)
                filterDialog.dismiss()
            }

            filterDialog.setContentView(filterDialogView)
            filterDialog.show()
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        val searchItem = menu!!.findItem(R.id.search)
        val searchView = searchItem.actionView as SearchView
        searchView.maxWidth = Integer.MAX_VALUE

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                getProducts(query)
                searchView.onActionViewCollapsed()
                return false
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.search -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun getHistory() {
        val url = "http://35.240.12.190:3000/api/history"

        try {
            val response = Ion.with(this)
                .load(url)
                .asJsonObject()
                .withResponse()
                .get()

            if(response.headers.code() == 200){
                val moshi = Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()
                val type = Types.newParameterizedType(List::class.java, HistoryModel::class.java)
                val adapter = moshi.adapter<ArrayList<HistoryModel>>(type)
                val queries = adapter.fromJson(response.result["searchQueries"].asJsonArray.toString()) as ArrayList<HistoryModel>
                val products = adapter.fromJson(response.result["searchedProducts"].asJsonArray.toString()) as ArrayList<HistoryModel>

                val queriesTrimmed: ArrayList<HistoryModel> = ArrayList()
                val productsTrimmed: ArrayList<HistoryModel> = ArrayList()

                queries.forEach { query ->
                    if(!queriesTrimmed.contains(query) && queriesTrimmed.size <= 5)
                        queriesTrimmed.add(query)
                }

                products.forEach { product ->
                    if(!productsTrimmed.contains(product) && productsTrimmed.size <= 5)
                        productsTrimmed.add(product)
                }

                runOnUiThread {
                    adapterQueries.removeAll()
                    adapterQueries.addAll(queriesTrimmed)
                    adapterProducts.removeAll()
                    adapterProducts.addAll(productsTrimmed)
                }
            }
            else{
                Snackbar.make(activityMain, "Sorry.", Snackbar.LENGTH_SHORT).show()
            }
        }
        catch (error: Exception) {
            println(error)
            Snackbar.make(activityMain, "Sorry.", Snackbar.LENGTH_SHORT).show()
        }
    }

    fun getProducts(searchQuery: String?) {
        runOnUiThread {
            dialog.show()

            if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                drawer_layout.closeDrawer(GravityCompat.START)
            }
        }
        var url = ""

        url = if(searchQuery.isNullOrEmpty() && wordsForSearch.isNullOrEmpty())
            "http://35.240.12.190:3000/api/history/search"
        else if(wordsForSearch.isNullOrEmpty())
            "http://35.240.12.190:3000/api/search?q=${URLEncoder.encode(searchQuery)}"
        else
            "http://35.240.12.190:3000/api/search?q=${URLEncoder.encode(wordsForSearch)}"

        try {
            val response = Ion.with(this)
                .load(url)
                .asJsonObject()
                .withResponse()
                .get()

            runOnUiThread {
                dialog.dismiss()
            }

            if(response.headers.code() == 200){
                val moshi = Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()
                val type = Types.newParameterizedType(List::class.java, SearchResultModel::class.java)
                val adapter = moshi.adapter<ArrayList<SearchResultModel>>(type)

                var products:ArrayList<SearchResultModel> = ArrayList()

                if(searchQuery.isNullOrEmpty() && wordsForSearch.isNullOrEmpty()) {
                    products = adapter.fromJson(response.result["data"].asJsonArray.toString()) as ArrayList<SearchResultModel>
                }
                else {
                    runOnUiThread {
                        txtTitle?.text = if(!searchQuery.isNullOrEmpty()) searchQuery else wordsForSearch
                    }
                    products = adapter.fromJson(response.result["response"].asJsonArray.toString()) as ArrayList<SearchResultModel>
                    getHistory()
                }
                runOnUiThread {
                    adapterResult.removeAll()
                    adapterResult.addAll(products)
                    originalProducts = products
                }
            }
            else{
                Snackbar.make(activityMain, "Sorry.", Snackbar.LENGTH_SHORT).show()
            }
        }
        catch (error: Exception) {
            println(error)
            Snackbar.make(activityMain, "Sorry.", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun filterByPrice(products: ArrayList<SearchResultModel>): ArrayList<SearchResultModel> {
        val filteredResult: ArrayList<SearchResultModel> = arrayListOf()

        products.forEach { product ->
            val price = product.productPrice.trim().split(",")[0].replace(".","").toInt()
            if(price >= min) {
                if(max > 0) {
                    if(price <= max) {
                        filteredResult.add(product)
                    }
                }
                else {
                    filteredResult.add(product)
                }
            }
        }
        return filteredResult
    }
}
