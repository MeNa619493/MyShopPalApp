package com.example.myshoppal.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myshoppal.Firestore.FirestoreClass
import com.example.myshoppal.Model.Product
import com.example.myshoppal.Model.User
import com.example.myshoppal.R
import com.example.myshoppal.ui.fragments.ProductsFragment
import com.example.myshoppal.utils.Constants
import com.example.myshoppal.utils.GlideLoader
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_add_product.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_user_profile.*
import kotlinx.android.synthetic.main.activity_user_profile.et_email
import kotlinx.android.synthetic.main.activity_user_profile.et_first_name
import kotlinx.android.synthetic.main.activity_user_profile.et_last_name
import java.io.IOException

class AddProductActivity : BaseActivity(), View.OnClickListener{

    var mSelectedImageFileUri : Uri? = null    //link in the device(path)
    private var mUserProductImageURL : String = ""     //link in browser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)
        setupActionBar()

        iv_add_update_product.setOnClickListener(this@AddProductActivity)
        btn_submit.setOnClickListener(this@AddProductActivity)
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_add_product_activity)

        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_add_product_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onClick(view: View?) {
        if (view != null){
            when(view.id){

                R.id.iv_add_update_product -> {
                    if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        Constants.showImageChooser(this@AddProductActivity)
                    }
                    else{
                        ActivityCompat.requestPermissions(this@AddProductActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            Constants.READ_STORAGE_PERMISSION_CODE)
                    }
                }

                R.id.btn_submit -> {
                    if (validateProductDetails()){
                        uploadProductImage()
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this@AddProductActivity)
            }
            else{
                Toast.makeText(this@AddProductActivity, resources.getString(R.string.read_storage_permission_denied), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK){
            if (requestCode == Constants.PICK_IMAGE_REQUEST_CODE){
                if (data != null){

                    iv_add_update_product.setImageDrawable(ContextCompat.getDrawable(this@AddProductActivity,R.drawable.ic_vector_edit))

                    try {
                        mSelectedImageFileUri = data.data!!
                        GlideLoader(this@AddProductActivity).loadUserPicture(mSelectedImageFileUri!!,iv_product_image )
                    }
                    catch (e : IOException){
                        e.printStackTrace()
                        Toast.makeText(this@AddProductActivity, resources.getString(R.string.image_selection_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        else if (resultCode == Activity.RESULT_CANCELED){
            Log.e("Request canceled", "Image selection canceled" )
        }
    }

    private fun validateProductDetails(): Boolean {
        return when {
            TextUtils.isEmpty(et_product_title.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_title), true)
                false
            }

            TextUtils.isEmpty(et_product_price.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_price), true)
                false
            }

            TextUtils.isEmpty(et_product_description.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_description), true)
                false
            }

            TextUtils.isEmpty(et_product_quantity.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_quantity), true)
                false
            }

            mSelectedImageFileUri == null -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_select_product_image), true)
                false
            }

            else -> {

                true
            }
        }
    }

    private fun uploadProductImage(){
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().uploadImageToCloudStorage(this@AddProductActivity,mSelectedImageFileUri,Constants.PRODUCT_IMAGE)
    }

    fun imageUploadSuccess(imageURL : String){

        mUserProductImageURL = imageURL

        uploadProductDetails()
    }

    private fun uploadProductDetails(){

        val userName = this.getSharedPreferences(Constants.MYSHOPPAL_PREFERENCES, Context.MODE_PRIVATE)
            .getString(Constants.LOGGED_IN_USERNAME,"")!!

        val product = Product(
            FirestoreClass().getCurrentUserID(),
            userName,
            et_product_title.text.toString().trim { it <= ' ' },
            et_product_price.text.toString().trim { it <= ' ' },
            et_product_description.text.toString().trim { it <= ' ' },
            et_product_quantity.text.toString().trim { it <= ' ' },
            mUserProductImageURL
        )

        FirestoreClass().uploadProductDetails(this@AddProductActivity,product)
    }

    fun productUploadSuccess(){

        hideProgressDialog()
        Toast.makeText(this,resources.getString(R.string.product_uploaded_success_message),Toast.LENGTH_SHORT).show()
        finish()
    }
}