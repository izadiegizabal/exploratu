package xyz.izadi.exploratu.currencies

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.fragment_currencies_list_dialog.*
import xyz.izadi.exploratu.R
import kotlinx.android.synthetic.main.fragment_currencies_list_dialog_item.view.*
import xyz.izadi.exploratu.currencies.models.Currencies

// TODO: Customize parameter argument names
const val ARG_CURRENCIES = "item_count"

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_currencies_list_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        list.layoutManager = LinearLayoutManager(context)
        list.adapter = CurrenciesAdapter(arguments!!.getParcelable(ARG_CURRENCIES))
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
        fun onCurrencyClicked(position: Int)
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
        internal val cod : TextView = itemView.tv_currency_code
        internal val desc : TextView = itemView.tv_currency_desc

        init {
            itemView.setOnClickListener {
                mListener?.let {
                    it.onCurrencyClicked(adapterPosition)
                    dismiss()
                }
            }
        }
    }

    private inner class CurrenciesAdapter internal constructor(private val mCurrencies: Currencies) :
        RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context), parent)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val currency = mCurrencies.currencies[position]
            holder.cod.text = currency.code
            holder.desc.text = context?.getString(R.string.currency_desc, currency.name, currency.sign)

            val flagPath = "file:///android_asset/flags/${currency.code}.png"
            val transformation = RoundedCornersTransformation(32, 0)
            Picasso
                .get()
                .load(flagPath)
                .transform(transformation)
                .into(holder.flag)
        }

        override fun getItemCount(): Int {
            return mCurrencies.totalCurrencies
        }
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
