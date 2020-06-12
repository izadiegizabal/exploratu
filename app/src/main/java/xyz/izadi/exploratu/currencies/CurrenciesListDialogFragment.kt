package xyz.izadi.exploratu.currencies

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.fragment_currencies_list_dialog.*
import kotlinx.android.synthetic.main.fragment_currencies_list_dialog_item.view.*
import xyz.izadi.exploratu.R
import xyz.izadi.exploratu.currencies.data.models.Currencies
import xyz.izadi.exploratu.currencies.data.models.Currency
import java.util.*
import kotlin.collections.ArrayList


const val ARG_CURRENCIES = "currencies_object"

/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    CurrenciesListDialogFragment.newInstance(30).show(supportFragmentManager, "dialog")
 * </pre>
 *
 * You activity (or fragment) needs to implement [CurrenciesListDialogFragment.Listener].
 */
class CurrenciesListDialogFragment : BottomSheetDialogFragment() {

    private var mListener: Listener? = null
    private var mAdapter: CurrenciesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_currencies_list_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val layoutManager = LinearLayoutManager(context)
        rv_currency_list.layoutManager = layoutManager
        mAdapter = CurrenciesAdapter(requireArguments().getParcelable(ARG_CURRENCIES)!!)
        rv_currency_list.adapter = mAdapter

        setUpQueryListener()
        setUpScrollListener()
    }

    private fun setUpScrollListener() {
        rv_currency_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val imm =
                        context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    //Find the currently focused view, so we can grab the correct window token from it.
                    val view = view?.rootView?.windowToken
                    imm.hideSoftInputFromWindow(view, 0)
                }
            }
        })
    }

    private fun setUpQueryListener() {
        sv_currencies.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val parentWithBSBehavior = cl_bs_currency_selector.parent as FrameLayout
                val mBottomSheetBehavior = BottomSheetBehavior.from(parentWithBSBehavior)

                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                mAdapter?.filter?.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val parentWithBSBehavior = cl_bs_currency_selector.parent as FrameLayout
                val mBottomSheetBehavior = BottomSheetBehavior.from(parentWithBSBehavior)

                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                mAdapter?.filter?.filter(newText)
                return false
            }
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parent = parentFragment
        mListener = if (parent != null) {
            parent as Listener
        } else {
            context as Listener
        }
    }

    override fun onDetach() {
        mListener = null
        super.onDetach()
    }

    interface Listener {
        fun onCurrencyClicked(code: String)
    }

    private inner class ViewHolder internal constructor(
        inflater: LayoutInflater,
        parent: ViewGroup
    ) : RecyclerView.ViewHolder(
        inflater.inflate(
            R.layout.fragment_currencies_list_dialog_item,
            parent,
            false
        )
    ) {

        internal val flag: ImageView = itemView.iv_currency_flag
        internal val cod: TextView = itemView.tv_currency_code
        internal val desc: TextView = itemView.tv_currency_desc

        init {
            itemView.setOnClickListener {
                mListener?.let {
                    it.onCurrencyClicked(cod.text.toString())
                    dismiss()
                }
            }
        }
    }

    private inner class CurrenciesAdapter internal constructor(
        private val mCurrencies: Currencies,
        private var mCurrenciesFiltered: Currencies = mCurrencies
    ) : RecyclerView.Adapter<ViewHolder>(), Filterable {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context), parent)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (position < mCurrenciesFiltered.currencies.size) {
                val currency = mCurrenciesFiltered.currencies[position]
                holder.cod.text = currency.code
                holder.desc.text =
                    context?.getString(R.string.currency_desc, currency.name, currency.sign)

                val flagPath = "file:///android_asset/flags/${currency.code}.png"
                val transformation = RoundedCornersTransformation(32, 0)
                Picasso
                    .get()
                    .load(flagPath)
                    .placeholder(R.drawable.ic_dollar_placeholder)
                    .transform(transformation)
                    .into(holder.flag)
            }
        }

        override fun getItemCount(): Int {
            return mCurrenciesFiltered.totalCurrencies
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(charSequence: CharSequence): FilterResults {
                    val charString = charSequence.toString().toLowerCase(Locale.ROOT)

                    mCurrenciesFiltered = if (charString.isEmpty()) {
                        mCurrencies
                    } else {
                        val filteredList = ArrayList<Currency>()
                        for (currency in mCurrencies.currencies) {
                            if (currency.name.toLowerCase(Locale.ROOT).contains(charString)
                                || currency.code.toLowerCase(Locale.ROOT).contains(charString)
                                || currency.sign.toLowerCase(Locale.ROOT).contains(charString)
                                || countryNameMatch(currency.countries, charString)
                            ) {
                                filteredList.add(currency)
                            }
                        }
                        Currencies(2.0f, Date(), filteredList.size, filteredList.toTypedArray())
                    }

                    val filterResults = FilterResults()
                    filterResults.values = mCurrenciesFiltered

                    return filterResults
                }

                override fun publishResults(
                    charSequence: CharSequence,
                    filterResults: FilterResults
                ) {
                    mCurrenciesFiltered = filterResults.values as Currencies

                    // refresh the list with filtered data
                    notifyDataSetChanged()
                }
            }
        }
    }

    private fun countryNameMatch(countries: Array<String>, query: String): Boolean {
        for (country in countries) {
            if (country.toLowerCase(Locale.ROOT).contains(query)) {
                return true
            }
        }
        return false
    }

    companion object {
        fun newInstance(currencies: Currencies?): CurrenciesListDialogFragment =
            CurrenciesListDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_CURRENCIES, currencies)
                }
            }

    }
}
