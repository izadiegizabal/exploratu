package xyz.izadi.exploratu

import BottomNavigationDrawerFragment
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.activity_main.*
import xyz.izadi.exploratu.currencies.camera.OcrCaptureActivity
import xyz.izadi.exploratu.currencies.models.Currencies
import xyz.izadi.exploratu.others.Utils
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private val LOG_TAG = this.javaClass.simpleName
    private var currencies: Currencies? = null
    private var activeCurrencyIndex = -1
    private var activeCurrencyAmount = ""
    private var isDefaultValue = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(bottomAppBar)

        fab.setOnClickListener {
            val intent = Intent(this, OcrCaptureActivity::class.java)
            startActivity(intent)
        }
        setUpAmountListeners()
        setUpPadListeners()

        loadCurrencies()
        setPreferredCurrencies()
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

            val jsonString =
                applicationContext.assets.open("currencyInfo.json").bufferedReader().use {
                    it.readText()
                }

            currencies = gson.fromJson(jsonString, Currencies::class.java)

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun setPreferredCurrencies() {
        // TODO "Read from sharedpreferences which ones to load"
        loadCurrencyTo("USD", 0)
        loadCurrencyTo("CNY", 1)
        loadCurrencyTo("JPY", 2)
    }

    private fun loadCurrencyTo(code: String, listPos: Int) {
        val curr = currencies?.getCurrency(code)
        val flagPath = "file:///android_asset/flags/${curr?.code}.png"
        val transformation = RoundedCornersTransformation(32, 0)
        when (listPos) {
            0 -> {
                Picasso
                    .get()
                    .load(flagPath)
                    .transform(transformation)
                    .into(iv_currency_1_flag)
                tv_currency_1_code.text = curr?.code
                tv_currency_1_desc.text =
                    applicationContext.getString(R.string.currency_desc, curr?.name, curr?.sign)
            }
            1 -> {
                Picasso
                    .get()
                    .load(flagPath)
                    .transform(transformation)
                    .into(iv_currency_2_flag)
                tv_currency_2_code.text = curr?.code
                tv_currency_2_desc.text =
                    applicationContext.getString(R.string.currency_desc, curr?.name, curr?.sign)
            }
            2 -> {
                Picasso
                    .get()
                    .load(flagPath)
                    .transform(transformation)
                    .into(iv_currency_3_flag)
                tv_currency_3_code.text = curr?.code
                tv_currency_3_desc.text =
                    applicationContext.getString(R.string.currency_desc, curr?.name, curr?.sign)
            }
        }
    }

    private fun setUpAmountListeners() {
        tv_currency_1_quantity.setOnClickListener {
            if (activeCurrencyIndex != 0) {
                tv_currency_1_quantity.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.color_secondary
                    )
                )
                tv_currency_2_quantity.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.color_on_background
                    )
                )
                tv_currency_3_quantity.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.color_on_background
                    )
                )

                resetActiveCurrencyValues(0, 1)
            }
        }

        tv_currency_2_quantity.setOnClickListener {
            if (activeCurrencyIndex != 1) {
                tv_currency_1_quantity.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.color_on_background
                    )
                )
                tv_currency_2_quantity.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.color_secondary
                    )
                )
                tv_currency_3_quantity.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.color_on_background
                    )
                )

                resetActiveCurrencyValues(1, 1)
            }
        }

        tv_currency_3_quantity.setOnClickListener {
            if (activeCurrencyIndex != 2) {
                tv_currency_1_quantity.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.color_on_background
                    )
                )
                tv_currency_2_quantity.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.color_on_background
                    )
                )
                tv_currency_3_quantity.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.color_secondary
                    )
                )

                resetActiveCurrencyValues(2, 1)
            }
        }
    }

    private fun resetActiveCurrencyValues(activeIndex: Int, resetNumber: Int) {
        activeCurrencyIndex = activeIndex
        activeCurrencyAmount = "" + resetNumber
        isDefaultValue = true
        when (activeIndex) {
            0 -> {
                tv_currency_1_quantity.text = activeCurrencyAmount
            }
            1 -> {
                tv_currency_2_quantity.text = activeCurrencyAmount
            }
            2 -> {
                tv_currency_3_quantity.text = activeCurrencyAmount
            }
        }

        calculateConversions()
    }

    private fun setUpPadListeners() {
        bt_pad_0.setOnClickListener {
            addAmount("0")
        }

        bt_pad_1.setOnClickListener {
            addAmount("1")
        }

        bt_pad_2.setOnClickListener {
            addAmount("2")
        }

        bt_pad_3.setOnClickListener {
            addAmount("3")
        }
        bt_pad_4.setOnClickListener {
            addAmount("4")
        }

        bt_pad_5.setOnClickListener {
            addAmount("5")
        }
        bt_pad_6.setOnClickListener {
            addAmount("6")
        }
        bt_pad_7.setOnClickListener {
            addAmount("7")
        }
        bt_pad_8.setOnClickListener {
            addAmount("8")
        }
        bt_pad_9.setOnClickListener {
            addAmount("9")
        }
        bt_pad_comma.setOnClickListener {
            isDefaultValue = false
            addAmount(".")
        }
        bt_pad_0.setOnClickListener {
            addAmount("0")
        }
        bt_pad_00.setOnClickListener {
            addAmount("00")
        }
        bt_pad_backspace.setOnClickListener {
            changeActiveAmountTo(activeCurrencyAmount.dropLast(1))
        }
        bt_pad_ac.setOnClickListener {
            resetActiveCurrencyValues(activeCurrencyIndex, 0)
        }
    }

    private fun addAmount(addDigit: String) {
        var amount = activeCurrencyAmount

        if (isDefaultValue) {
            amount = addDigit
            isDefaultValue = false
        } else {
            amount += addDigit
        }

        changeActiveAmountTo(amount)
    }

    private fun changeActiveAmountTo(amount: String) {
        if (activeCurrencyIndex != -1) {
            // limit length to 15
            if (amount.length > 15) {
                return
            }
            activeCurrencyAmount = amount
            reformatIfNeeded()
            if (amount.isBlank()) {
                resetActiveCurrencyValues(activeCurrencyIndex, 0)
            }
            when (activeCurrencyIndex) {
                0 -> {
                    tv_currency_1_quantity.text = activeCurrencyAmount
                }
                1 -> {
                    tv_currency_2_quantity.text = activeCurrencyAmount
                }
                2 -> {
                    tv_currency_3_quantity.text = activeCurrencyAmount
                }
            }

            calculateConversions()
        }
    }

    private fun reformatIfNeeded() {
        val amountParts = activeCurrencyAmount.split(".")

        // Remove unwanted second comma 1,000.52.
        if (amountParts.size > 2) {
            activeCurrencyAmount = activeCurrencyAmount.dropLast(1)
        }

        // Add 1,000 thousand comma
        if (amountParts[0].length >= 4) {
            val originalString = amountParts[0].replace(",", "")
            val numberWithCommas = Utils.insertPeriodically(originalString, ",", 3)
            activeCurrencyAmount = numberWithCommas
            if (amountParts.size > 1) {
                activeCurrencyAmount += "." + amountParts[1]
            }
        }

        // Remove unwanted leading zeros
        if (amountParts[0].length == 2) {
            if (amountParts[0][0] == '0') {
                activeCurrencyAmount = activeCurrencyAmount.substring(1)
            }
        }
        if (amountParts[0].length == 3) {
            if (amountParts[0][0] == '0') {
                activeCurrencyAmount = activeCurrencyAmount.substring(1)
                if (amountParts[0][1] == '0') {
                    activeCurrencyAmount = activeCurrencyAmount.substring(1)
                }
            }
        }

        // Limit the decimals to 4
        if (amountParts.size > 1 && amountParts[1].length >= 4) {
            activeCurrencyAmount = amountParts[0] + "." + amountParts[1].substring(0, 4)
        }
    }

    private fun calculateConversions() {
        // Get conversions rates an calculate exchanges
    }
}
