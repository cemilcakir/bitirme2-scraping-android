package com.ccakir.bitirme2_scraping_andoid.activities

import android.app.Dialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.ccakir.bitirme2_scraping_andoid.R
import com.ccakir.bitirme2_scraping_andoid.adapters.DetailAdapter
import com.ccakir.bitirme2_scraping_andoid.models.DetailModel
import com.daimajia.slider.library.SliderLayout
import com.koushikdutta.ion.Ion
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.android.synthetic.main.activity_details.*
import java.lang.Exception
import kotlin.concurrent.thread
import com.daimajia.slider.library.Animations.DescriptionAnimation
import com.daimajia.slider.library.SliderTypes.BaseSliderView
import com.daimajia.slider.library.SliderTypes.TextSliderView



class DetailsActivity : AppCompatActivity() {

    companion object {
        lateinit var productDetailLink: String
    }

    private lateinit var recyclerDetails: RecyclerView
    private lateinit var adapterDetails: DetailAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var dialog:Dialog
    private lateinit var slider:SliderLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

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

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val txtTitle: TextView? = actionBar?.customView?.findViewById(R.id.txtTitle)
        txtTitle?.text = resources.getString(R.string.detail_activity)

        adapterDetails = DetailAdapter(ArrayList(), this)

        viewManager = LinearLayoutManager(this)
        recyclerDetails = findViewById(R.id.recyclerDetails)
        recyclerDetails.setHasFixedSize(true)
        recyclerDetails.layoutManager = viewManager
        recyclerDetails.adapter = adapterDetails

        dialog = Dialog(this)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(false)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_loading, null)
        dialog.setContentView(dialogView)

        slider = findViewById(R.id.slider)

        dialog.show()
        thread {
            getDetails()
            dialog.dismiss()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.detail, menu)

        val searchItem = menu!!.findItem(R.id.search)
        val searchView = searchItem.actionView as SearchView
        searchView.maxWidth = Integer.MAX_VALUE

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                val i = Intent(this@DetailsActivity, MainActivity::class.java)
                MainActivity.wordsForSearch = query
                startActivity(i)
                return false
            }
        })

        return true
    }

    fun getDetails() {
        val url = "http://35.240.12.190:3000/api/search/detail?url=$productDetailLink"

        try {
            val response = Ion.with(this)
                .load(url)
                .asJsonObject().withResponse()
                .get()

            if(response.headers.code() == 200){
                val moshi = Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()
                val type = Types.newParameterizedType(List::class.java, DetailModel::class.java)
                val adapter = moshi.adapter<ArrayList<DetailModel>>(type)
                val details = adapter.fromJson(response.result["details"].asJsonArray.toString()) as ArrayList<DetailModel>

                runOnUiThread {
                    adapterDetails.removeAll()
                    adapterDetails.addAll(details)
1
                    val imagesJson = response.result.get("images").asJsonArray
                    val images: HashMap<String, String> = hashMapOf()
                    val name = response.result.get("name").asString

                    txtDetailIndicator.text = name

                    var i = 0
                    imagesJson.forEach {
                        i++
                        images[name + " - " + i.toString()] = it.asString
                    }

                    for (image in images.keys) {
                        val textSliderView = TextSliderView(this)
                        // initialize a SliderLayout
                        textSliderView
                            .description(image)
                            .image(images[image])
                            .setScaleType(BaseSliderView.ScaleType.Fit)

                        //add your extra information
                        textSliderView.bundle(Bundle())
                        textSliderView.bundle
                            .putString("extra", name)

                        slider.addSlider(textSliderView)
                    }
                    slider.setPresetTransformer(SliderLayout.Transformer.Accordion)
                    slider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom)
                    slider.setCustomAnimation(DescriptionAnimation())
                    slider.setDuration(4000)
                }
            }
            else{
                Snackbar.make(activityDetails, "Sorry.", Snackbar.LENGTH_SHORT).show()
            }
        }
        catch (error: Exception) {
            println(error)
            Snackbar.make(activityDetails, "Sorry.", Snackbar.LENGTH_SHORT).show()
        }
    }
}
