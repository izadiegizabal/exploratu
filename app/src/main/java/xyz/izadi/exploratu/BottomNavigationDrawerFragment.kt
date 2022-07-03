package xyz.izadi.exploratu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import xyz.izadi.exploratu.databinding.FragmentBottomNavigationDrawerBinding

class BottomNavigationDrawerFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentBottomNavigationDrawerBinding.inflate(inflater, container, false).apply {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // Bottom Navigation Drawer menu item clicks
            when (menuItem.itemId) {
                // R.id.nav1 -> context!!.toast(getString(R.string.nav1_clicked))
            }
            // Add code here to update the UI based on the item selected
            // For example, swap UI fragments here
            true
        }
    }.root
}
