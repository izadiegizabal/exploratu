package xyz.izadi.exploratu.currencies

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.fragment_currency.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.izadi.exploratu.R
import xyz.izadi.exploratu.currencies.data.RatesDatabase
import xyz.izadi.exploratu.currencies.data.api.ApiFactory
import xyz.izadi.exploratu.currencies.data.models.Currencies
import xyz.izadi.exploratu.currencies.data.models.Rates
import xyz.izadi.exploratu.currencies.others.Utils
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CurrencyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CurrencyFragment : Fragment(), CurrenciesListDialogFragment.Listener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val mLog = this.javaClass.simpleName
    private var ratesDB: RatesDatabase? = null
    private var currencies: Currencies? = null
    private var currencyRates: Rates? = null
    private var activeCurrencyIndex = -1
    private var selectingCurrencyIndex = -1
    private var activeCurrencyAmount = ""
    private var isDefaultValue = true
    private val activeCurCodes = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_currency, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ratesDB = context?.let { RatesDatabase.getInstance(it) }

        currencies = context?.let { Utils.getCurrencies(it) }
        setPreferredCurrencies()
        updateRates()

        setUpAmountListeners()
        setUpPadListeners()
        setUpCurrencySelectorListeners()
        setUpNetworkChangeListener()
        setUpToolTips()

        tv_currency_1_quantity.performClick()
    }

    private fun setUpToolTips() {
        TooltipCompat.setTooltipText(ll_currency_1, getString(R.string.tt_currency_1))
        TooltipCompat.setTooltipText(ll_currency_2, getString(R.string.tt_currency_2))
        TooltipCompat.setTooltipText(ll_currency_3, getString(R.string.tt_currency_3))
        TooltipCompat.setTooltipText(tv_currency_1_quantity, getString(R.string.tt_currency_active))
        TooltipCompat.setTooltipText(tv_currency_2_quantity, getString(R.string.tt_currency_active))
        TooltipCompat.setTooltipText(tv_currency_3_quantity, getString(R.string.tt_currency_active))
    }

    private fun setPreferredCurrencies() {
        // read preferences to load
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        activeCurCodes.add(sharedPref?.getString("currency_code_0", "EUR") ?: return)
        activeCurCodes.add(sharedPref.getString("currency_code_1", "USD") ?: return)
        activeCurCodes.add(sharedPref.getString("currency_code_2", "JPY") ?: return)

        // load them
        loadCurrencyTo(activeCurCodes[0], 0)
        loadCurrencyTo(activeCurCodes[1], 1)
        loadCurrencyTo(activeCurCodes[2], 2)
    }

    private fun loadCurrencyTo(code: String, listPos: Int) {
        // change global variable
        activeCurCodes[listPos] = code

        // update tv
        val curr = currencies?.getCurrency(code)
        val flagPath = "file:///android_asset/flags/${curr?.code}.png"
        val transformation = RoundedCornersTransformation(32, 0)
        when (listPos) {
            0 -> {
                Picasso
                    .get()
                    .load(flagPath)
                    .placeholder(R.drawable.ic_dollar_placeholder)
                    .transform(transformation)
                    .into(iv_currency_1_flag)
                tv_currency_1_code.text = curr?.code
                tv_currency_1_desc.text =
                    context?.getString(R.string.currency_desc, curr?.name, curr?.sign)
            }
            1 -> {
                Picasso
                    .get()
                    .load(flagPath)
                    .placeholder(R.drawable.ic_dollar_placeholder)
                    .transform(transformation)
                    .into(iv_currency_2_flag)
                tv_currency_2_code.text = curr?.code
                tv_currency_2_desc.text =
                    context?.getString(R.string.currency_desc, curr?.name, curr?.sign)
            }
            2 -> {
                Picasso
                    .get()
                    .load(flagPath)
                    .placeholder(R.drawable.ic_dollar_placeholder)
                    .transform(transformation)
                    .into(iv_currency_3_flag)
                tv_currency_3_code.text = curr?.code
                tv_currency_3_desc.text =
                    context?.getString(R.string.currency_desc, curr?.name, curr?.sign)
            }
        }

        calculateConversions()

        // update preferences
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref?.edit()) {
            this?.putString("currency_code_$listPos", code)
            this?.apply()
        }
    }

    private fun setUpCurrencySelectorListeners() {
        ll_currency_1.setOnClickListener {
            selectingCurrencyIndex = 0
            CurrenciesListDialogFragment.newInstance(currencies)
                .show(childFragmentManager, "dialog")
        }
        ll_currency_2.setOnClickListener {
            selectingCurrencyIndex = 1
            CurrenciesListDialogFragment.newInstance(currencies)
                .show(childFragmentManager, "dialog")
        }
        ll_currency_3.setOnClickListener {
            selectingCurrencyIndex = 2
            CurrenciesListDialogFragment.newInstance(currencies)
                .show(childFragmentManager, "dialog")
        }
    }

    override fun onCurrencyClicked(code: String) {
        loadCurrencyTo(code, selectingCurrencyIndex)
        calculateConversions()
    }

    private fun setUpNetworkChangeListener() {
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    updateRates()
                }

                override fun onLost(network: Network?) {
                    //take action when network connection is lost
                }
            })
    }

    private fun setUpAmountListeners() {
        tv_currency_1_quantity.setOnClickListener {
            if (activeCurrencyIndex != 0) {
                tv_currency_1_quantity.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_secondary
                    )
                )
                tv_currency_2_quantity.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_on_background
                    )
                )
                tv_currency_3_quantity.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
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
                        requireContext(),
                        R.color.color_on_background
                    )
                )
                tv_currency_2_quantity.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_secondary
                    )
                )
                tv_currency_3_quantity.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
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
                        requireContext(),
                        R.color.color_on_background
                    )
                )
                tv_currency_2_quantity.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_on_background
                    )
                )
                tv_currency_3_quantity.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_secondary
                    )
                )

                resetActiveCurrencyValues(2, 1)
            }
        }

        tv_currency_1_quantity.performClick()
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

        if (resetNumber == 0) {
            tv_currency_1_quantity.text = "0"
            tv_currency_2_quantity.text = "0"
            tv_currency_3_quantity.text = "0"
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
            activeCurrencyAmount = Utils.reformatIfNeeded(amount)
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

    private fun calculateConversions() {
        // Get conversions rates an calculate rates
        if (activeCurrencyIndex != -1) {
            makeConversions()
        }
    }

    private fun makeConversions() {
        if (currencyRates != null) {
            val rates = currencyRates
            val from = activeCurCodes[activeCurrencyIndex]
            val quantity = getAmountFloat()
            when (activeCurrencyIndex) {
                0 -> {
                    tv_currency_2_quantity.text =
                        rates?.convert(quantity, from, activeCurCodes[1])
                    tv_currency_3_quantity.text =
                        rates?.convert(quantity, from, activeCurCodes[2])
                }
                1 -> {
                    tv_currency_1_quantity.text =
                        rates?.convert(quantity, from, activeCurCodes[0])
                    tv_currency_3_quantity.text =
                        rates?.convert(quantity, from, activeCurCodes[2])
                }
                2 -> {
                    tv_currency_1_quantity.text =
                        rates?.convert(quantity, from, activeCurCodes[0])
                    tv_currency_2_quantity.text =
                        rates?.convert(quantity, from, activeCurCodes[1])
                }
            }

            if (rates != null) {
                val formattedDate = getFormattedDate(rates.date)
                tv_exchange_provider.text =
                    getString(R.string.exchanges_provided_by_at, formattedDate)
            }
        }
    }

    private fun getFormattedDate(timestamp: Date): String {
        val df = android.text.format.DateFormat.getDateFormat(context)
        return df.format(timestamp)
    }

    private fun getAmountFloat(): Float {
        return activeCurrencyAmount.replace(",", "").toFloat()
    }

    private fun updateRates() {
        GlobalScope.launch {
            // If there are no conversion rates or if they are older than today
            if (currencyRates == null || !DateUtils.isToday(currencyRates?.date?.time!!)) {
                // get the latest from db
                val latestRatesFromDB = ratesDB?.ratesDao()?.getLatestRates()
                // if there isn't any on db or if they are older than today
                if (latestRatesFromDB == null || !DateUtils.isToday(latestRatesFromDB.date.time)) {
                    // check for internet
                    if (context != null && Utils.isInternetAvailable(requireContext())) {
                        // Try to fetch from the API
                        val response = ApiFactory.exchangeRatesAPI.getLatestRates()
                        withContext(Dispatchers.Main) {
                            try {
                                if (response.isSuccessful) {
                                    currencyRates = response.body()
                                    currencyRates?.rates?.resetEur()
                                    ratesDB?.ratesDao()?.insertRates(response.body()!!)
                                    activity?.runOnUiThread {
                                        makeConversions()
                                    }
                                } else {
                                    Log.d(
                                        mLog,
                                        "Error while getting new data: ${response.code()}"
                                    )
                                    // DB fallback in case of error, no connection...
                                    if (latestRatesFromDB == null) {
                                        saveRatesFallbackInDB()
                                    } else {
                                        currencyRates = latestRatesFromDB
                                    }
                                    activity?.runOnUiThread {
                                        makeConversions()
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        // DB fallback in case of no connection...
                        if (latestRatesFromDB == null) {
                            saveRatesFallbackInDB()
                        } else {
                            currencyRates = latestRatesFromDB
                        }
                        activity?.runOnUiThread {
                            makeConversions()
                        }
                    }
                } else {
                    // if they are from today we just use them
                    currencyRates = latestRatesFromDB
                    activity?.runOnUiThread {
                        makeConversions()
                    }
                }
            }
        }
    }

    private suspend fun insertRateInDB(rates: Rates) {
        rates.rates.resetEur()
        ratesDB?.ratesDao()?.insertRates(rates)
    }

    private suspend fun saveRatesFallbackInDB() {
        currencyRates = currencies?.getRates() //fallback from JSON
        insertRateInDB(currencyRates!!)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CurrencyFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CurrencyFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}