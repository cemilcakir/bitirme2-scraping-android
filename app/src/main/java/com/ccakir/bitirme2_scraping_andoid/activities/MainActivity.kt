package com.ccakir.bitirme2_scraping_andoid.activities

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
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
import java.lang.Exception
import java.net.URLEncoder
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerHistory: RecyclerView
    private lateinit var adapterHistory: HistoryAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var recyclerResult: RecyclerView
    private lateinit var adapterResult: SearchResultAdapter
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        adapterHistory = HistoryAdapter(ArrayList(), this)
        adapterResult = SearchResultAdapter(ArrayList(), this)

        viewManager = LinearLayoutManager(this)
        recyclerHistory = nav_view.findViewById(R.id.recyclerHistory)
        recyclerHistory.setHasFixedSize(true)
        recyclerHistory.layoutManager = viewManager
        recyclerHistory.adapter = adapterHistory

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

        thread {
            getHistory()
        }

        thread {
            getProducts(null)
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

                queries.addAll(products)

                runOnUiThread {
                    adapterHistory.removeAll()
                    adapterHistory.addAll(queries)
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
        }
        var url = ""

        url = if(searchQuery.isNullOrEmpty())
            "http://35.240.12.190:3000/api/history/search"
        else
            "http://35.240.12.190:3000/api/search?q=${URLEncoder.encode(searchQuery)}"

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

                if(searchQuery.isNullOrEmpty())
                    products = adapter.fromJson(response.result["data"].asJsonArray.toString()) as ArrayList<SearchResultModel>
                else {
                    products = adapter.fromJson(response.result["response"].asJsonArray.toString()) as ArrayList<SearchResultModel>
                    getHistory()
                }
                runOnUiThread {
                    adapterResult.removeAll()
                    adapterResult.addAll(products)
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
}
