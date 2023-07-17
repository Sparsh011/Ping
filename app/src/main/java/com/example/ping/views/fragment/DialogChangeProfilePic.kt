package com.example.ping.views.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.ping.R
import com.example.ping.views.activity.EditProfileActivity


class DialogChangeProfilePic(private val editProfileActivity: EditProfileActivity) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_fragment_for_profile_pic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tvGallery = view.findViewById<TextView>(R.id.tv_select_from_gallery)
        val tvCamera = view.findViewById<TextView>(R.id.tv_select_from_camera)

        tvGallery.setOnClickListener{
            editProfileActivity.openGallery()
        }
        tvCamera.setOnClickListener{
            editProfileActivity.openCamera()
        }
    }
}