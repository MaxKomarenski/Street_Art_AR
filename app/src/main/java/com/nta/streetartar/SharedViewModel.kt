package com.nta.streetartar

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent

class SharedViewModel : ViewModel() {
    val startAuthLiveEvent = LiveEvent<Boolean>()
    val successfulAuthLiveEvent = LiveEvent<Boolean>()

    val addObjectLiveEvent = LiveEvent<Boolean>()

    val uidMutableLiveData = MutableLiveData<String>()
}