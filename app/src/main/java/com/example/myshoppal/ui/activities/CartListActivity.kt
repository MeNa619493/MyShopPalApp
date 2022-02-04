package com.example.myshoppal.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myshoppal.Firestore.FirestoreClass
import com.example.myshoppal.Model.CartItem
import com.example.myshoppal.Model.Product
import com.example.myshoppal.R
import com.example.myshoppal.ui.adapters.MyCartListAdapter
import com.example.myshoppal.utils.Constants
import kotlinx.android.synthetic.main.activity_add_product.*
import kotlinx.android.synthetic.main.activity_cart_list.*
import kotlinx.android.synthetic.main.item_cart_layout.*

class CartListActivity : BaseActivity() {

    private lateinit var mProductList: ArrayList<Product>
    private lateinit var mCartListItems: ArrayList<CartItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart_list)
        setupActionBar()

        btn_checkout.setOnClickListener {
            val intent = Intent(this@CartListActivity, AddressListActivity::class.java)
            intent.putExtra(Constants.EXTRA_SELECT_ADDRESS , true)
            startActivity(intent)
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_cart_list_activity)

        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_cart_list_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        getProductsList()
    }

    private fun getCartItemsList(){
        //showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getCartList(this@CartListActivity)
    }

    fun successCartItemList(cartList: ArrayList<CartItem>){
        hideProgressDialog()

        mCartListItems = cartList

        for (product in mProductList){
            for (cartItem in mCartListItems){
                if (product.product_id == cartItem.product_id){
                    cartItem.stock_quantity = product.stock_quantity

                    if (product.stock_quantity.toInt() == 0){
                        cartItem.cart_quantity = product.stock_quantity
                    }
                }
            }
        }

        if (mCartListItems.size > 0) {
            rv_cart_items_list.visibility = View.VISIBLE
            ll_checkout.visibility = View.VISIBLE
            tv_no_cart_item_found.visibility = View.GONE

            rv_cart_items_list.layoutManager = LinearLayoutManager(this@CartListActivity)
            rv_cart_items_list.setHasFixedSize(true)

            val cartListAdapter = MyCartListAdapter(this@CartListActivity, mCartListItems, true)
            rv_cart_items_list.adapter = cartListAdapter

            var subTotal: Double = 0.0

            for (item in mCartListItems){
                val availableQuantity = item.stock_quantity.toInt()

                if (availableQuantity > 0) {
                    val quantity = item.cart_quantity.toDouble()
                    val price = item.price.toDouble()
                    subTotal += (quantity * price)
                }
            }

            tv_sub_total.text = "$$subTotal"
            tv_shipping_charge.text = "$10.0"

            if (subTotal > 0){
                ll_checkout.visibility = View.VISIBLE

                val total = subTotal + 10
                tv_total_amount.text = "$$total"
            }
            else{
                ll_checkout.visibility = View.GONE
            }

        }
        else{
            rv_cart_items_list.visibility = View.GONE
            ll_checkout.visibility = View.GONE
            tv_no_cart_item_found.visibility = View.VISIBLE
        }
    }

    private fun getProductsList(){
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAllProductList(this@CartListActivity)
    }

    fun successProductsListFromFirestore(productsList: ArrayList<Product>){
        hideProgressDialog()
        mProductList = productsList

        getCartItemsList()
    }

    fun itemRemovedSuccess(){
        hideProgressDialog()
        Toast.makeText(this,resources.getString(R.string.msg_item_removed_successfully),Toast.LENGTH_SHORT).show()

        getCartItemsList()
    }

    fun itemUpdateSuccess(){
        hideProgressDialog()

        getCartItemsList()
    }

}