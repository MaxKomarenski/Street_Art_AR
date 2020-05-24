package com.nta.streetartar.main_flow.step_1_welcome

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController

import com.nta.streetartar.R
import kotlinx.android.synthetic.main.welcome_fragment.*

class WelcomeFragment : Fragment(R.layout.welcome_fragment) {

    companion object {
        fun newInstance() =
            WelcomeFragment()
    }

    private val viewModel: WelcomeViewModel by viewModels()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupListeners()
    }

    private fun setupListeners(){
        start_button.setOnClickListener {
            view?.findNavController()?.navigate(WelcomeFragmentDirections.actionWelcomeFragmentToLoginFragment())
        }
    }

}
