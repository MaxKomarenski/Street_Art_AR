package com.nta.streetartar.main_flow.step_2_login

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.nta.streetartar.SharedViewModel

import com.nta.streetartar.R
import com.nta.streetartar.popups.CustomDialog
import kotlinx.android.synthetic.main.login_fragment.*

class LoginFragment : Fragment(R.layout.login_fragment) {

    companion object {
        fun newInstance() =
            LoginFragment()
    }

    private val sharedViewModel : SharedViewModel by activityViewModels()
    private val viewModel: LoginViewModel by viewModels()

    private lateinit var dialog : CustomDialog

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initDialog()
        setupListeners()
        setupObservers()

    }

    private fun initDialog(){
        dialog = CustomDialog(context!!)
        dialog.setTexts(R.string.login, R.string.login_text)
        dialog.show()
    }

    private fun setupListeners(){
        login_button.setOnClickListener {
            sharedViewModel.startAuthLiveEvent.value = true
        }
    }

    private fun setupObservers(){
        sharedViewModel.successfulAuthLiveEvent.observe(viewLifecycleOwner, Observer {
            if (it){
                view?.findNavController()?.navigate(LoginFragmentDirections.actionLoginFragmentToMapFragment())
            }
        })
    }

}
