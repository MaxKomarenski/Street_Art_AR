package com.nta.streetartar.popups

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import com.nta.streetartar.R
import kotlinx.android.synthetic.main.custom_popup_layout.*

class CustomDialog(context: Context) : Dialog(context){



    init {
        setContentView(R.layout.custom_popup_layout)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.attributes.windowAnimations = R.style.DialogAnimation


        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        window?.setLayout(width, height)

        got_it_button.setOnClickListener {
            this.dismiss()
        }

    }

    fun setTexts(titleId : Int, textId : Int ){
        title.text = context.resources.getString(titleId)
        text.text = context.resources.getString(textId)
    }


}