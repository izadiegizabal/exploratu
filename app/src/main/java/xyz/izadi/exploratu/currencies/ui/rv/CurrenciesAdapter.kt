package xyz.izadi.exploratu.currencies.ui.rv

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import xyz.izadi.exploratu.R
import xyz.izadi.exploratu.currencies.data.models.Currencies
import xyz.izadi.exploratu.currencies.data.models.Currency
import xyz.izadi.exploratu.currencies.others.Utils.updateCurrencyViews
import xyz.izadi.exploratu.databinding.FragmentCurrenciesListDialogItemBinding

class CurrenciesAdapter(
    private val context: Context,
    private val currencies: Currencies,
    private val listener: Listener
) : ListAdapter<Currency, CurrenciesAdapter.CurrencyViewHolder>
    (CurrencyDiffCallback), Filterable {

    private var prevPosition = 0

    init {
        submitList(currencies.currencies)
    }

    interface Listener {
        fun onCurrencyClicked(code: String)
    }

    inner class CurrencyViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        binding: FragmentCurrenciesListDialogItemBinding =
            FragmentCurrenciesListDialogItemBinding.inflate(inflater, parent, false)
    ) : RecyclerView.ViewHolder(binding.root) {

        private val flag = binding.ivCurrencyFlag
        private val cod = binding.tvCurrencyCode
        private val desc = binding.tvCurrencyDesc
        private val container = binding.clRvItem

        private val itemAnimation: Animation = AnimationUtils.loadAnimation(
            context,
            R.anim.currency_rv_item
        )

        init {
            itemView.setOnClickListener {
                listener.onCurrencyClicked(cod.text.toString())
            }
        }

        fun bind(currency: Currency) {
            context.updateCurrencyViews(
                currency = currency,
                flagIv = flag,
                codeTv = cod,
                descTv = desc
            )
            if (prevPosition < bindingAdapterPosition) {
                container.startAnimation(itemAnimation)
            }
            prevPosition = bindingAdapterPosition
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
        return CurrencyViewHolder(LayoutInflater.from(parent.context), parent)
    }

    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString().lowercase()

                val filteredCurrencies: List<Currency> = if (charString.isEmpty()) {
                    currencies.currencies
                } else {
                    mutableListOf<Currency>().apply {
                        for (currency in currencies.currencies) {
                            if (currency.name.lowercase().contains(charString)
                                || currency.code.lowercase().contains(charString)
                                || currency.sign.lowercase().contains(charString)
                                || countryNameMatch(currency.countries, charString)
                            ) {
                                add(currency)
                            }
                        }
                    }
                }.ordered()

                return FilterResults().apply {
                    values = currencies.copy(currencies = filteredCurrencies)
                }
            }

            override fun publishResults(
                charSequence: CharSequence,
                filterResults: FilterResults
            ) {
                val updatedCurrencies = filterResults.values as Currencies
                submitList(updatedCurrencies.currencies)
            }
        }
    }
}

object CurrencyDiffCallback : DiffUtil.ItemCallback<Currency>() {
    override fun areItemsTheSame(oldItem: Currency, newItem: Currency): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Currency, newItem: Currency): Boolean {
        return oldItem.code == newItem.code
    }

}

private fun countryNameMatch(countries: List<String>, query: String): Boolean {
    for (country in countries) {
        if (country.lowercase().contains(query)) {
            return true
        }
    }
    return false
}

fun Currencies.ordered(): Currencies = copy(
    currencies = currencies.ordered()
)

private fun List<Currency>.ordered() = sortedBy { it.code }
