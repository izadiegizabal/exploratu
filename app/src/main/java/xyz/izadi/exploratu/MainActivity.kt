package xyz.izadi.exploratu

import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import xyz.izadi.exploratu.currencies.CurrencyFragment
import xyz.izadi.exploratu.currencies.OcrCaptureActivity
import xyz.izadi.exploratu.timezones.TimezonesFragment


class MainActivity : AppCompatActivity(), BottomNavigationDrawerFragment.Listener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(bottomAppBar)

        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.f_main_mode, CurrencyFragment())
        ft.commit()
        fab.show()
        fab.setOnClickListener {
            val intent = Intent(this, OcrCaptureActivity::class.java)
            startActivity(intent)
        }
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

    override fun onModeSelected(mode: Int) {
        when (mode) {
            R.id.currency -> {
                val ft = supportFragmentManager.beginTransaction()
                ft.replace(R.id.f_main_mode, CurrencyFragment())
                ft.commit()

                fab.show()
            }
            R.id.timezone -> {
                val ft = supportFragmentManager.beginTransaction()
                ft.replace(R.id.f_main_mode, TimezonesFragment())
                ft.commit()

                fab.hide()
            }
        }
    }

}
