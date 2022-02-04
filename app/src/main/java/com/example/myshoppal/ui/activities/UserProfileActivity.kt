package com.example.myshoppal.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myshoppal.Firestore.FirestoreClass
import com.example.myshoppal.Model.User
import com.example.myshoppal.R
import com.example.myshoppal.utils.Constants
import com.example.myshoppal.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_user_profile.*
import kotlinx.android.synthetic.main.activity_user_profile.iv_user_photo
import java.io.IOException
import java.util.regex.Pattern

class UserProfileActivity : BaseActivity() , View.OnClickListener {

    private lateinit var mUserDetails : User
    private var mSelectedImageFileUri : Uri? = null    //link in the device(path)
    private var mUserProfileImageURL : String = ""     //link in browser

    private var mobileNumberInput: String? = null
    private val MOBILE_NUMBER_PATTERN =
        Pattern.compile("^" +
                "[0-9]{11}" +               //at least 11 digits
                "$")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        if (intent.hasExtra(Constants.EXTRA_USER_DETAILS)) {
            mUserDetails = intent.getParcelableExtra(Constants.EXTRA_USER_DETAILS)!!
        }

        et_first_name.setText(mUserDetails.firstName)
        et_last_name.setText(mUserDetails.lastName)

        et_email.isEnabled = false
        et_email.setText(mUserDetails.email)

        if (mUserDetails.profileCompleted == 0){
            tv_title_user_profile.text = resources.getString(R.string.title_complete_profile)
            et_first_name.isEnabled = false
            et_last_name.isEnabled = false
        }
        else {
            setupActionBar()

            tv_title_user_profile.text = resources.getString(R.string.title_edit_profile)
            GlideLoader(this@UserProfileActivity).loadUserPicture(mUserDetails.image, iv_user_photo)

            if (mUserDetails.mobile != 0L){
                et_mobile_number.setText(mUserDetails.mobile.toString())
            }

            if (mUserDetails.gender == Constants.MALE){
                rb_male.isChecked = true
            }
            else {
                rb_female.isChecked = true
            }
        }

        iv_user_photo.setOnClickListener(this@UserProfileActivity)
        btn_save.setOnClickListener(this@UserProfileActivity)
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_user_profile_activity)

        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_settings_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onClick(view: View?) {
        if (view != null){
            when(view.id){

                R.id.iv_user_photo -> {
                    if (ContextCompat.checkSelfPermission
                            (this@UserProfileActivity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        Constants.showImageChooser(this@UserProfileActivity)
                    }
                    else {
                        ActivityCompat.requestPermissions(this@UserProfileActivity,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), Constants.READ_STORAGE_PERMISSION_CODE)
                    }
                }

                R.id.btn_save -> {
                    if (validateUserProfileDetails()){
                        showProgressDialog(resources.getString(R.string.please_wait))

                        if (mSelectedImageFileUri != null){
                            FirestoreClass().uploadImageToCloudStorage(this@UserProfileActivity, mSelectedImageFileUri,Constants.USER_PROFILE_IMAGE)
                        }
                        else {
                            updateUserProfileDetails()
                        }
                    }
                }
            }
        }
    }

    private fun updateUserProfileDetails(){
        val userHashMap = HashMap< String, Any>()

        val firstName = et_first_name.text.toString().trim { it <= ' ' }
        if (firstName != mUserDetails.firstName){
            userHashMap[Constants.FIRST_NAME] = firstName
        }

        val lastName = et_last_name.text.toString().trim { it <= ' ' }
        if (lastName != mUserDetails.lastName){
            userHashMap[Constants.LAST_NAME] = lastName
        }


        val gender = if (rb_male.isChecked){
            Constants.MALE
        } else {
            Constants.FEMALE
        }

        if (gender.isNotEmpty() && gender != mUserDetails.gender){
            userHashMap[Constants.GENDER] = gender
        }

        if (mUserProfileImageURL.isNotEmpty()){
            userHashMap[Constants.IMAGE] = mUserProfileImageURL
        }

        val mobileNumber = et_mobile_number.text.toString().trim { it <= ' ' }
        if (mobileNumber.isNotEmpty() && mobileNumber != mUserDetails.mobile.toString()){
            userHashMap[Constants.MOBILE] = mobileNumber.toLong()
        }

        userHashMap[Constants.COMPLETE_PROFILE] = 1

        FirestoreClass().updateUserProfileData(this@UserProfileActivity, userHashMap)
    }

    fun userProfileUpdateSuccess(){
        hideProgressDialog()
        Toast.makeText(this@UserProfileActivity,resources.getString(R.string.msg_profile_update_success),Toast.LENGTH_SHORT).show()
        startActivity(Intent(this@UserProfileActivity, DashboardActivity::class.java))
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this@UserProfileActivity)
            }
            else {
                Toast.makeText(this@UserProfileActivity, resources.getString(R.string.read_storage_permission_denied), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK){
            if (requestCode == Constants.PICK_IMAGE_REQUEST_CODE){
                if (data != null){
                    try {
                        mSelectedImageFileUri = data.data!!
                        GlideLoader(this@UserProfileActivity).loadUserPicture(mSelectedImageFileUri!!, iv_user_photo)
                    }
                    catch (e : IOException){
                        e.printStackTrace()
                        Toast.makeText(this@UserProfileActivity, resources.getString(R.string.image_selection_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        else if (resultCode == Activity.RESULT_CANCELED){
            Log.e("Request canceled", "Image selection canceled" )
        }
    }

    private fun validateUserProfileDetails(): Boolean {

        mobileNumberInput = et_mobile_number.text.toString().trim { it <= ' ' }

        return when {

            TextUtils.isEmpty(et_mobile_number.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_mobile_number), true)
                false
            }

            (!MOBILE_NUMBER_PATTERN.matcher(mobileNumberInput!!).matches()) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_valid_mobile_number), true)
                false;
            }

            else -> {

                true

            }
        }
    }

    fun imageUploadSuccess(imageURL : String){

        mUserProfileImageURL = imageURL
        updateUserProfileDetails()
    }
}