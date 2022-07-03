package xyz.izadi.exploratu

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import xyz.izadi.exploratu.currencies.CurrencyFragment
import xyz.izadi.exploratu.currencies.OcrCaptureActivity
import xyz.izadi.exploratu.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityMainBinding.inflate(layoutInflater).apply {
            setSupportActionBar(bottomAppBar)

            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.f_main_mode, CurrencyFragment())
            ft.commit()

            fab.setOnClickListener {
                val intent = Intent(this@MainActivity, OcrCaptureActivity::class.java)
                startActivity(intent)
            }
        }.also {
            setContentView(it.root)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bottom_app_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val bottomNavDrawerFragment = BottomNavigationDrawerFragment()
                bottomNavDrawerFragment.show(supportFragmentManager, bottomNavDrawerFragment.tag)
            }
        }
        return true
    }

}
