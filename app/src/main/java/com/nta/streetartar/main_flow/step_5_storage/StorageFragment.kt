package com.nta.streetartar.main_flow.step_5_storage

import android.net.Uri
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.google.android.gms.tasks.Tasks
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.mlsdev.rximagepicker.RxImagePicker
import com.mlsdev.rximagepicker.Sources

import com.nta.streetartar.R
import com.nta.streetartar.SharedViewModel
import com.nta.streetartar.popups.CustomDialog
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.storage_fragment.*

class StorageFragment : Fragment(R.layout.storage_fragment) {

    companion object {
        fun newInstance() = StorageFragment()
    }

    private lateinit var storageRef: StorageReference
    private var saved: Boolean = false

    private val viewModel: StorageViewModel by viewModels()

    val storage = FirebaseStorage.getInstance()

    private val sharedViewModel : SharedViewModel by activityViewModels()

    private val disposable  = CompositeDisposable()

    private lateinit var dialog : CustomDialog

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initDialog()
        setupListeners()

    }

    private fun setupListeners(){
        next_step_button.setOnClickListener {
            view?.findNavController()?.navigate(StorageFragmentDirections.actionStorageFragmentToThankYouFragment())
        }

        upload_object_button.setOnClickListener {
            disposable.add(
                RxImagePicker.with(fragmentManager!!).requestImage(Sources.GALLERY)
                    .subscribe({
                        uploadDataAndGetUrl(sharedViewModel.uidMutableLiveData.value!! , it)
                    },{
                        it.printStackTrace()
                    }))
        }
    }

    private fun initDialog(){
        dialog = CustomDialog(context!!)
        dialog.setTexts(R.string.storage, R.string.storage_text)
        dialog.show()
    }

    private fun uploadDataAndGetUrl(userId: String, uri: Uri) {

        progress_bar.visibility = View.VISIBLE
        val fileRef = storage.reference.child("images/$userId").child(uri.lastPathSegment!!)
        try {
            val task = fileRef.putFile(uri).continueWithTask {uploadTask ->
                progress_bar.visibility = View.GONE
                if (!uploadTask.isSuccessful){
                    throw uploadTask.exception!!
                }else {
                    Toast.makeText(context, "file is uploaded", Toast.LENGTH_LONG).show()
                }

                return@continueWithTask fileRef.downloadUrl
            }

            val downloadUrl = Tasks.await(task)


        } catch (e : Exception){

        }
    }

}
