package com.example.myshoppal.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myshoppal.Firestore.FirestoreClass
import com.example.myshoppal.Model.CartItem
import com.example.myshoppal.Model.Product
import com.example.myshoppal.R
import com.example.myshoppal.utils.Constants
import com.example.myshoppal.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_add_product.*
import kotlinx.android.synthetic.main.activity_product_details.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_user_profile.*
import kotlinx.android.synthetic.main.item_cart_layout.view.*
import java.io.IOException

class ProductDetailsActivity : BaseActivity(), View.OnClickListener {

    private var mProductID: String = ""
    private var mProductOwnerID: String = ""
    private var mProduct: Product?= null
    private var mSelectedImageProductUri : Uri? = null      //link in the device(path)
    private var mProductImageURL : String = ""             //link in browser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)
        setupActionBar()

        if (intent.hasExtra(Constants.EXTRA_PRODUCT)){
            mProduct = intent.getParcelableExtra(Constants.EXTRA_PRODUCT)!!
            mProductID = mProduct!!.product_id
            mProductOwnerID = mProduct!!.user_id
        }

        if (mProductOwnerID == FirestoreClass().getCurrentUserID()) {
            btn_add_to_cart.visibility = View.GONE
            btn_go_to_cart.visibility = View.GONE
        }
        else {
            btn_add_to_cart.visibility = View.VISIBLE
        }

        getProductDetails()
        iv_edit_product_image.setOnClickListener(this)
        btn_save_edit.setOnClickListener(this)
        btn_add_to_cart.setOnClickListener(this)
        btn_go_to_cart.setOnClickListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_product_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when(id){
            R.id.action_edit_product -> {
                if (mProductOwnerID == FirestoreClass().getCurrentUserID()) {
                    sv_before_edit.visibility = View.GONE
                    sv_editing.visibility = View.VISIBLE
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_product_details_activity)

        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_product_details_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onClick(view: View?) {
        if (view != null){
            when(view.id){

                R.id.iv_edit_product_image -> {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        Constants.showImageChooser(this@ProductDetailsActivity)
                    }
                    else{
                        ActivityCompat.requestPermissions(this@ProductDetailsActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            Constants.READ_STORAGE_PERMISSION_CODE)
                    }
                }

                R.id.btn_save_edit -> {
                    if (validateProductDetails()){

                        sv_editing.visibility = View.GONE
                        sv_before_edit.visibility = View.VISIBLE

                        if(mSelectedImageProductUri == null){
                            updateUserProductDetails()
                        }
                        else{
                            uploadEditedProductImage()
                        }
                    }
                }

                R.id.btn_add_to_cart -> {
                    addToCart()
                }

                R.id.btn_go_to_cart -> {
                    startActivity(Intent(this@ProductDetailsActivity, CartListActivity::class.java))
                }
            }
        }
    }

    private fun getProductDetails(){
        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().getProductDetails(this, mProductID)
    }

    fun productDetailsSuccess(product: Product){

        mProductImageURL = product.image

        GlideLoader(this).loadProductPicture(product.image, iv_product_detail_image)
        tv_product_details_title.text = product.title
        tv_product_details_price.text = "${product.price}"
        tv_product_details_description.text = product.description
        tv_product_details_stock_quantity.text = product.stock_quantity

        GlideLoader(this).loadProductPicture(product.image, iv_edit_product_detail_image)
        et_edit_product_details_title.setText(product.title)
        et_edit_product_details_price.setText("${product.price}")
        et_edit_product_details_description.setText(product.description )
        et_edit_product_details_stock_quantity.setText(product.stock_quantity)

        if ( product.stock_quantity.toInt() == 0){
            hideProgressDialog()

            btn_add_to_cart.visibility = View.GONE
            tv_product_details_stock_quantity.text = resources.getString(R.string.lbl_out_of_stock)
            tv_product_details_stock_quantity.setTextColor(ContextCompat.getColor(this@ProductDetailsActivity,R.color.colorSnackBarError))
        }
        else {
            if (FirestoreClass().getCurrentUserID() == product.user_id){
                hideProgressDialog()
            }
            else{
                FirestoreClass().checkIfItemExistInCart(this@ProductDetailsActivity,mProductID)
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this@ProductDetailsActivity)
            }
            else{
                Toast.makeText(this@ProductDetailsActivity, resources.getString(R.string.read_storage_permission_denied), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK){
            if (requestCode == Constants.PICK_IMAGE_REQUEST_CODE){
                if (data != null){

                    iv_edit_product_image.setImageDrawable(ContextCompat.getDrawable(this@ProductDetailsActivity,R.drawable.ic_vector_edit))

                    try {
                        mSelectedImageProductUri = data.data!!
                        GlideLoader(this).loadProductPicture(mSelectedImageProductUri!!, iv_edit_product_detail_image)
                    }
                    catch (e : IOException){
                        e.printStackTrace()
                        Toast.makeText(this@ProductDetailsActivity, resources.getString(R.string.image_selection_failed), Toast.LENGTH_SHORT).show()
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
            TextUtils.isEmpty(et_edit_product_details_title.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_mobile_number), true)
                false
            }

            TextUtils.isEmpty(et_edit_product_details_price.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_mobile_number), true)
                false
            }

            TextUtils.isEmpty(et_edit_product_details_description.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_mobile_number), true)
                false
            }

            TextUtils.isEmpty(et_edit_product_details_stock_quantity.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_mobile_number), true)
                false
            }

            else -> {
                true
            }
        }
    }

    private fun uploadEditedProductImage(){
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().uploadImageToCloudStorage(this@ProductDetailsActivity,mSelectedImageProductUri,Constants.PRODUCT_IMAGE)
    }

    fun editedImageUploadSuccess(imageURL : String){

        mProductImageURL = imageURL

        updateUserProductDetails()
    }

    private fun updateUserProductDetails(){
        val productHashMap = HashMap< String, Any>()

        val title = et_edit_product_details_title.text.toString().trim { it <= ' ' }
        if (title != mProduct!!.title){
            productHashMap[Constants.TITLE] = title
        }

        val price = et_edit_product_details_price.text.toString().trim { it <= ' ' }
        if (price != mProduct!!.price){
            productHashMap[Constants.PRICE] = price
        }

        val description = et_edit_product_details_description.text.toString().trim { it <= ' ' }
        if (description != mProduct!!.description){
            productHashMap[Constants.DESCRIPTION] = description
        }

        val stock_quantity = et_edit_product_details_stock_quantity.text.toString().trim { it <= ' ' }
        if (stock_quantity != mProduct!!.stock_quantity){
            productHashMap[Constants.STOCK_QUANTITY] = stock_quantity
        }

        if (mProductImageURL.isNotEmpty()){
            productHashMap[Constants.IMAGE] = mProductImageURL
        }

        FirestoreClass().updateProductDetails(this@ProductDetailsActivity, mProductID, productHashMap)
    }

    fun productDetailsUpdateSuccess(){
        hideProgressDialog()

        getProductDetails()
    }

    private fun addToCart(){
        val cartItem = CartItem(
            FirestoreClass().getCurrentUserID(),
            mProductOwnerID,
            mProductID,
            mProduct!!.title,
            mProduct!!.price,
            mProduct!!.image,
            Constants.DEFAULT_CART_QUANTITY
        )

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addCartItems(this@ProductDetailsActivity,cartItem)
    }

    fun addToCartSuccess(){
        hideProgressDialog()

        Toast.makeText(this@ProductDetailsActivity, resources.getString(R.string.success_message_item_added_to_cart),Toast.LENGTH_SHORT).show()
        btn_add_to_cart.visibility = View.GONE
        btn_go_to_cart.visibility = View.VISIBLE
    }

    fun productExistInCart(){
        hideProgressDialog()
        btn_add_to_cart.visibility = View.GONE
        btn_go_to_cart.visibility = View.VISIBLE
    }
}