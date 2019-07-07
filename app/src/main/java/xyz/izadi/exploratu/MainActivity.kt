package xyz.izadi.exploratu

import BottomNavigationDrawerFragment
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import xyz.izadi.exploratu.currencies.camera.OcrCaptureActivity
import xyz.izadi.exploratu.currencies.models.Currencies
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors
import java.nio.file.Files.readAllBytes



class MainActivity : AppCompatActivity() {
    var currencies: Currencies? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(bottomAppBar)

        fab.setOnClickListener {
            val intent = Intent(this, OcrCaptureActivity::class.java)
            startActivity(intent)
        }

        loadCurrencies()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_bottom_app_bar, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                val bottomNavDrawerFragment = BottomNavigationDrawerFragment()
                bottomNavDrawerFragment.show(supportFragmentManager, bottomNavDrawerFragment.tag)
            }
        }
        return true
    }

    private fun loadCurrencies() {
        try {
            val gson = Gson()

            val jsonString = application.assets.open("currencyInfo.json").bufferedReader().use {
                it.readText()
            }
//
//            print(jsonString)



//            val reader = InputStreamReader(inputStream, "UTF-8")
//            currencies = gson.fromJson(jsonString, Currencies::class.java)
            print(currencies)
            print("mmmk")

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
