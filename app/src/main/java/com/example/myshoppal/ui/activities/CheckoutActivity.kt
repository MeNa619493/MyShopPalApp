package com.example.myshoppal.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myshoppal.Firestore.FirestoreClass
import com.example.myshoppal.Model.Address
import com.example.myshoppal.Model.CartItem
import com.example.myshoppal.Model.Order
import com.example.myshoppal.Model.Product
import com.example.myshoppal.R
import com.example.myshoppal.ui.adapters.MyCartListAdapter
import com.example.myshoppal.utils.Constants
import kotlinx.android.synthetic.main.activity_add_edit_address.*
import kotlinx.android.synthetic.main.activity_checkout.*

class CheckoutActivity : BaseActivity() {

    private var mAddressDetails: Address? = null
    private lateinit var mProductsList: ArrayList<Product>
    private lateinit var mCartItemsList: ArrayList<CartItem>
    private var subTotal: Double = 0.0
    private var mTotalAmount: Double = 0.0
    private lateinit var mOrderDetails: Order


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        setupActionBar()

        if (intent.hasExtra(Constants.EXTRA_SELECTED_ADDRESS)){
            mAddressDetails = intent.getParcelableExtra(Constants.EXTRA_SELECTED_ADDRESS)
        }

        if (mAddressDetails != null){
            tv_checkout_address_type.text = mAddressDetails?.type
            tv_checkout_full_name.text = mAddressDetails?.fullName
            tv_checkout_address.text = "${mAddressDetails!!.address}, ${mAddressDetails!!.zipCode}"
            tv_checkout_additional_note.text = mAddressDetails?.additionalNote
            tv_checkout_mobile_number.text = mAddressDetails?.phoneNumber

            if (mAddressDetails?.otherDetails!!.isNotEmpty()){
                tv_checkout_other_details.text = mAddressDetails?.otherDetails
            }
        }

        getProductList()

        btn_place_order.setOnClickListener {
            placeAnOrder()
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_checkout_activity)

        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_checkout_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun getProductList(){
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAllProductList(this@CheckoutActivity)
    }

    fun successProductsListFromFirestore(productsList: ArrayList<Product>){
        mProductsList = productsList
        getCartItemsList()
    }

    private fun getCartItemsList(){
        FirestoreClass().getCartList(this@CheckoutActivity)
    }

    fun successCartItemList(cartList: ArrayList<CartItem>){
        hideProgressDialog()

        for (product in mProductsList){
            for (cartItem in mCartItemsList){
                if (product.product_id == cartItem.product_id){
                    cartItem.stock_quantity = product.stock_quantity
                }
            }
        }

        mCartItemsList = cartList

        rv_cart_list_items.layoutManager = LinearLayoutManager(this@CheckoutActivity)
        rv_cart_list_items.setHasFixedSize(true)
        val cartListAdapter = MyCartListAdapter(this@CheckoutActivity, mCartItemsList, false)
        rv_cart_list_items.adapter = cartListAdapter

        for (item in mCartItemsList){
            val availableQuantity = item.stock_quantity.toInt()

            if (availableQuantity > 0){
                val price = item.price.toDouble()
                val quantity = item.cart_quantity.toInt()
                subTotal += ( price * quantity )
            }
        }

        tv_checkout_sub_total.text = "$${subTotal}"
        tv_checkout_shipping_charge.text = "$10.0"

        if (subTotal > 0){
            ll_checkout_place_order.visibility = View.VISIBLE

            mTotalAmount = subTotal + 10.0
            tv_checkout_total_amount.text = "$${mTotalAmount}"
        }
        else{
            ll_checkout_place_order.visibility = View.GONE
        }
    }

    private fun placeAnOrder(){
        showProgressDialog(resources.getString(R.string.please_wait))

        if (mAddressDetails != null) {
                mOrderDetails = Order(
                FirestoreClass().getCurrentUserID(),
                mCartItemsList,
                mAddressDetails!!,
                "My order ${System.currentTimeMillis()}",
                mCartItemsList[0].image,
                subTotal.toString(),
                "$10.0",
                mTotalAmount.toString(),
                System.currentTimeMillis()
            )

            FirestoreClass().placeOrder(this@CheckoutActivity, mOrderDetails)
        }
    }

    fun placedOrderSuccess(){
        FirestoreClass().updateAllDetails(this@CheckoutActivity, mCartItemsList, mOrderDetails)
    }

    fun allDetailsUpdatedSuccessfully(){
        hideProgressDialog()

        Toast.makeText(this@CheckoutActivity, "Your order was placed successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(this@CheckoutActivity, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}