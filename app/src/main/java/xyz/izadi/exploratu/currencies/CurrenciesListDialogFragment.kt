package xyz.izadi.exploratu.currencies

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import xyz.izadi.exploratu.currencies.data.models.Currencies
import xyz.izadi.exploratu.currencies.others.getParcelableCompat
import xyz.izadi.exploratu.currencies.ui.rv.CurrenciesAdapter
import xyz.izadi.exploratu.currencies.ui.rv.ordered
import xyz.izadi.exploratu.databinding.FragmentCurrenciesListDialogBinding


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
class CurrenciesListDialogFragment : BottomSheetDialogFragment(), CurrenciesAdapter.Listener {

    private var _binding: FragmentCurrenciesListDialogBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding
        get() = _binding

    private lateinit var mAdapter: CurrenciesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentCurrenciesListDialogBinding.inflate(inflater, container, false).apply {
        _binding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        mAdapter = CurrenciesAdapter(
            context = context ?: return,
            currencies = requireArguments().getParcelableCompat(ARG_CURRENCIES) ?: return,
            listener = this
        )

        binding?.apply {
            rvCurrencyList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = mAdapter
                setHasFixedSize(true)
                setItemViewCacheSize(20)
            }
            setUpQueryListener()
            setUpScrollListener()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun FragmentCurrenciesListDialogBinding.setUpScrollListener() =
        rvCurrencyList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

    private fun FragmentCurrenciesListDialogBinding.setUpQueryListener() =
        svCurrencies.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val parentWithBSBehavior = clBsCurrencySelector.parent as FrameLayout
                val mBottomSheetBehavior = BottomSheetBehavior.from(parentWithBSBehavior)

                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                mAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val parentWithBSBehavior = clBsCurrencySelector.parent as FrameLayout
                val mBottomSheetBehavior = BottomSheetBehavior.from(parentWithBSBehavior)
                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                mAdapter.filter.filter(newText)
                return false
            }
        })

    companion object {
        fun newInstance(currencies: Currencies?): CurrenciesListDialogFragment =
            CurrenciesListDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_CURRENCIES, currencies?.ordered())
                }
            }
    }

    override fun onCurrencyClicked(code: String) {
        ((parentFragment ?: context) as? CurrenciesAdapter.Listener)?.onCurrencyClicked(code)
        dismiss()
    }
}
