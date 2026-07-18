package com.tcssol.expensetracker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<LinearLayout>(R.id.rowManageCategories).setOnClickListener {
            val intent = Intent(requireContext(), EditAdapter::class.java).apply {
                putExtra("calling_activity_class", "com.tcssol.expensetracker.MainActivity")
                putExtra("selected_tab_pager", 3) // pos 3 is settings
            }
            startActivity(intent)
        }

        view.findViewById<LinearLayout>(R.id.rowSetBudget).setOnClickListener {
            val intent = Intent(requireContext(), SetBudgetActivity::class.java)
            startActivity(intent)
        }

        view.findViewById<LinearLayout>(R.id.rowP2PLedger).setOnClickListener {
            val fragment = PeerToPeerFragment()
            (activity as? MainActivity)?.showOverlayFragment(fragment)
        }

        view.findViewById<LinearLayout>(R.id.rowExportData).setOnClickListener {
            (activity as? MainActivity)?.showExportDialog()
        }

        view.findViewById<LinearLayout>(R.id.rowBackupData).setOnClickListener {
            (activity as? MainActivity)?.triggerBackupExport()
        }

        view.findViewById<LinearLayout>(R.id.rowRestoreData).setOnClickListener {
            (activity as? MainActivity)?.triggerBackupImport()
        }

        view.findViewById<LinearLayout>(R.id.rowAbout).setOnClickListener {
            val intent = Intent(requireContext(), AboutActivity::class.java)
            startActivity(intent)
        }
    }
}
