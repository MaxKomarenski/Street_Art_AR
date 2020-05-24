package com.nta.streetartar.main_flow.step_3_map

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController

import com.nta.streetartar.R
import com.nta.streetartar.popups.CustomDialog
import kotlinx.android.synthetic.main.map_fragment.*

class MapFragment : Fragment(R.layout.map_fragment) {

    companion object {
        fun newInstance() =
            MapFragment()
    }

    private val viewModel: MapViewModel by viewModels()
    private lateinit var dialog : CustomDialog

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initDialog()
        setupListeners()
    }

    private fun initDialog(){
        dialog = CustomDialog(context!!)
        dialog.setTexts(R.string.map, R.string.map_text)
        dialog.show()
    }

    private fun setupListeners(){
        next_step_button.setOnClickListener {
            view?.findNavController()?.navigate(MapFragmentDirections.actionMapFragmentToArExampleFragment())
        }
    }

}
