package xyz.izadi.exploratu.currencies

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import xyz.izadi.exploratu.R
import xyz.izadi.exploratu.currencies.data.models.Currencies
import xyz.izadi.exploratu.currencies.data.models.Rates
import xyz.izadi.exploratu.currencies.others.Utils
import xyz.izadi.exploratu.currencies.others.Utils.updateCurrencyViews
import xyz.izadi.exploratu.databinding.FragmentCurrencyBinding
import java.util.*

@AndroidEntryPoint
class CurrencyFragment : Fragment(), CurrenciesListDialogFragment.Listener {
    private var _binding: FragmentCurrencyBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding
        get() = _binding

    private val vm by viewModels<CurrenciesViewModel>()

    private var currencyRates: Rates? = null
    private var currencies: Currencies? = null
    private var activeCurrencyIndex = -1
    private var selectingCurrencyIndex = -1
    private var activeCurrencyAmount = ""
    private var isDefaultValue = true
    private val activeCurCodes = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentCurrencyBinding.inflate(inflater, container, false).apply {
        _binding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            setPreferredCurrencies()

            setUpAmountListeners()
            setUpPadListeners()
            setUpCurrencySelectorListeners()

            setUpToolTips()
            tvCurrency1Quantity.performClick()

            vm.rates.onEach {
                currencyRates = it
                makeConversions()
            }.launchIn(lifecycleScope)

            vm.currencies.onEach {
                currencies = it
                setPreferredCurrencies()
            }.launchIn(lifecycleScope)
        }

        setUpNetworkChangeListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        vm.syncRates()
    }

    private fun FragmentCurrencyBinding.setUpToolTips() {
        TooltipCompat.setTooltipText(llCurrency1, getString(R.string.tt_currency_1))
        TooltipCompat.setTooltipText(llCurrency2, getString(R.string.tt_currency_2))
        TooltipCompat.setTooltipText(llCurrency3, getString(R.string.tt_currency_3))
        TooltipCompat.setTooltipText(tvCurrency1Quantity, getString(R.string.tt_currency_active))
        TooltipCompat.setTooltipText(tvCurrency2Quantity, getString(R.string.tt_currency_active))
        TooltipCompat.setTooltipText(tvCurrency3Quantity, getString(R.string.tt_currency_active))
    }

    private fun FragmentCurrencyBinding.setPreferredCurrencies() {
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

    private fun FragmentCurrencyBinding.loadCurrencyTo(code: String, listPos: Int) {
        // change global variable
        activeCurCodes[listPos] = code

        // update tv
        val curr = currencies?.getCurrency(code) ?: return
        when (listPos) {
            0 -> context?.updateCurrencyViews(
                currency = curr,
                flagIv = ivCurrency1Flag,
                codeTv = tvCurrency1Code,
                descTv = tvCurrency1Desc
            )
            1 -> context?.updateCurrencyViews(
                currency = curr,
                flagIv = ivCurrency2Flag,
                codeTv = tvCurrency2Code,
                descTv = tvCurrency2Desc
            )
            2 -> context?.updateCurrencyViews(
                currency = curr,
                flagIv = ivCurrency3Flag,
                codeTv = tvCurrency3Code,
                descTv = tvCurrency3Desc
            )
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

    private fun FragmentCurrencyBinding.setUpCurrencySelectorListeners() {
        fun onClick(index: Int) {
            selectingCurrencyIndex = index
            CurrenciesListDialogFragment.newInstance(currencies)
                .show(childFragmentManager, "dialog")
        }

        llCurrency1.setOnClickListener {
            onClick(0)
        }
        llCurrency2.setOnClickListener {
            onClick(1)
        }
        llCurrency3.setOnClickListener {
            onClick(2)
        }
    }

    override fun onCurrencyClicked(code: String) {
        binding?.apply {
            loadCurrencyTo(code, selectingCurrencyIndex)
            calculateConversions()
        }
    }

    private fun setUpNetworkChangeListener() {
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    vm.syncRates()
                }
            })
    }

    private fun FragmentCurrencyBinding.setUpAmountListeners() {
        tvCurrency1Quantity.setOnClickListener {
            if (activeCurrencyIndex != 0) {
                tvCurrency1Quantity.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_secondary
                    )
                )
                tvCurrency2Quantity.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_on_background
                    )
                )
                tvCurrency3Quantity.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_on_background
                    )
                )

                resetActiveCurrencyValues(0, 1)
            }
        }

        tvCurrency2Quantity.setOnClickListener {
            if (activeCurrencyIndex != 1) {
                tvCurrency1Quantity.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_on_background
                    )
                )
                tvCurrency2Quantity.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_secondary
                    )
                )
                tvCurrency3Quantity.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_on_background
                    )
                )

                resetActiveCurrencyValues(1, 1)
            }
        }

        tvCurrency3Quantity.setOnClickListener {
            if (activeCurrencyIndex != 2) {
                tvCurrency1Quantity.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_on_background
                    )
                )
                tvCurrency2Quantity.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_on_background
                    )
                )
                tvCurrency3Quantity.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_secondary
                    )
                )

                resetActiveCurrencyValues(2, 1)
            }
        }

        tvCurrency1Quantity.performClick()
    }

    private fun FragmentCurrencyBinding.setUpPadListeners() {
        btPad0.setOnClickListener {
            addAmount("0")
        }

        btPad1.setOnClickListener {
            addAmount("1")
        }

        btPad2.setOnClickListener {
            addAmount("2")
        }

        btPad3.setOnClickListener {
            addAmount("3")
        }
        btPad4.setOnClickListener {
            addAmount("4")
        }

        btPad5.setOnClickListener {
            addAmount("5")
        }
        btPad6.setOnClickListener {
            addAmount("6")
        }
        btPad7.setOnClickListener {
            addAmount("7")
        }
        btPad8.setOnClickListener {
            addAmount("8")
        }
        btPad9.setOnClickListener {
            addAmount("9")
        }
        btPadComma.setOnClickListener {
            isDefaultValue = false
            addAmount(".")
        }
        btPad0.setOnClickListener {
            addAmount("0")
        }
        btPad00.setOnClickListener {
            addAmount("00")
        }
        btPadBackspace.setOnClickListener {
            changeActiveAmountTo(activeCurrencyAmount.dropLast(1))
        }
        btPadAc.setOnClickListener {
            resetActiveCurrencyValues(activeCurrencyIndex, 0)
        }
    }

    private fun FragmentCurrencyBinding.resetActiveCurrencyValues(
        activeIndex: Int,
        resetNumber: Int
    ) {
        activeCurrencyIndex = activeIndex
        activeCurrencyAmount = "" + resetNumber
        isDefaultValue = true
        when (activeIndex) {
            0 -> {
                tvCurrency1Quantity.text = activeCurrencyAmount
            }
            1 -> {
                tvCurrency2Quantity.text = activeCurrencyAmount
            }
            2 -> {
                tvCurrency3Quantity.text = activeCurrencyAmount
            }
        }

        calculateConversions()

        if (resetNumber == 0) {
            tvCurrency1Quantity.text = "0"
            tvCurrency2Quantity.text = "0"
            tvCurrency3Quantity.text = "0"
        }
    }

    private fun FragmentCurrencyBinding.addAmount(addDigit: String) {
        var amount = activeCurrencyAmount

        if (isDefaultValue) {
            amount = addDigit
            isDefaultValue = false
        } else {
            amount += addDigit
        }

        changeActiveAmountTo(amount)
    }

    private fun FragmentCurrencyBinding.changeActiveAmountTo(amount: String) {
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
                    tvCurrency1Quantity.text = activeCurrencyAmount
                }
                1 -> {
                    tvCurrency2Quantity.text = activeCurrencyAmount
                }
                2 -> {
                    tvCurrency3Quantity.text = activeCurrencyAmount
                }
            }

            calculateConversions()
        }
    }

    private fun FragmentCurrencyBinding.calculateConversions() {
        // Get conversions rates an calculate rates
        if (activeCurrencyIndex != -1) {
            makeConversions()
        }
    }

    private fun FragmentCurrencyBinding.makeConversions() {
        if (currencyRates != null) {
            val rates = currencyRates
            val from = activeCurCodes[activeCurrencyIndex]
            val quantity = getAmountFloat()
            when (activeCurrencyIndex) {
                0 -> {
                    tvCurrency2Quantity.text =
                        rates?.convert(quantity, from, activeCurCodes[1])
                    tvCurrency3Quantity.text =
                        rates?.convert(quantity, from, activeCurCodes[2])
                }
                1 -> {
                    tvCurrency1Quantity.text =
                        rates?.convert(quantity, from, activeCurCodes[0])
                    tvCurrency3Quantity.text =
                        rates?.convert(quantity, from, activeCurCodes[2])
                }
                2 -> {
                    tvCurrency1Quantity.text =
                        rates?.convert(quantity, from, activeCurCodes[0])
                    tvCurrency2Quantity.text =
                        rates?.convert(quantity, from, activeCurCodes[1])
                }
            }

            rates?.date?.let {
                tvExchangeProvider.text = getString(
                    R.string.exchanges_provided_by_at,
                    getFormattedDate(it)
                )
            } ?: run {
                tvExchangeProvider.text = ""
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
}
