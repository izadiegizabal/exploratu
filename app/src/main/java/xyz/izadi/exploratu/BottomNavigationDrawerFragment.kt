package xyz.izadi.exploratu

import android.content.Intent
import android.net.Uri
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
            when (menuItem.itemId) {
                R.id.follow_us -> startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://twitter.com/izadiegizabal")
                    )
                )
                else -> dismiss()
            }
            true
        }
    }.root
}
