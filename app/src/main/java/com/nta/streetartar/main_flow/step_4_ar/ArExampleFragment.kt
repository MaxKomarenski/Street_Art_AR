package com.nta.streetartar.main_flow.step_4_ar

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.nta.streetartar.SharedViewModel

import com.nta.streetartar.R
import com.nta.streetartar.popups.CustomDialog
import kotlinx.android.synthetic.main.ar_example_fragment.*

class ArExampleFragment : Fragment(R.layout.ar_example_fragment) {

    companion object {
        fun newInstance() = ArExampleFragment()
    }

    private val sharedViewModel : SharedViewModel by activityViewModels()
    private val viewModel: ArExampleViewModel by viewModels()

    private lateinit var dialog : CustomDialog

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initDialog()
        setupListeners()
    }

    private fun setupListeners(){
        add_object_button.setOnClickListener {
            sharedViewModel.addObjectLiveEvent.value = true
        }

        next_step_button.setOnClickListener {
            view?.findNavController()?.navigate(ArExampleFragmentDirections.actionArExampleFragmentToStorageFragment())
        }
    }

    private fun initDialog(){
        dialog = CustomDialog(context!!)
        dialog.setTexts(R.string.ar, R.string.ar_text)
        dialog.show()
    }

}
