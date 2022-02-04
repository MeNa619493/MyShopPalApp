package com.example.myshoppal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myshoppal.Firestore.FirestoreClass
import com.example.myshoppal.Model.SoldProduct
import com.example.myshoppal.R
import com.example.myshoppal.ui.adapters.MySoldProductsListAdapter
import kotlinx.android.synthetic.main.fragment_sold_products.*


class SoldProductsFragment : BaseFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sold_products, container, false)
    }

    override fun onResume() {
        super.onResume()
        getSoldProductList()
    }

    private fun getSoldProductList(){
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getSoldProductsList(this@SoldProductsFragment)
    }

    fun populateSoldProductsListInUI(soldProductsList: ArrayList<SoldProduct>){
        hideProgressDialog()

        if (soldProductsList.size > 0){
            rv_sold_product_items.visibility = View.VISIBLE
            tv_no_sold_products_found.visibility = View.GONE

            rv_sold_product_items.layoutManager = LinearLayoutManager(activity)
            rv_sold_product_items.setHasFixedSize(true)
            val soldProductsAdapter = MySoldProductsListAdapter(requireActivity(),soldProductsList)
            rv_sold_product_items.adapter = soldProductsAdapter

        }
        else{
            rv_sold_product_items.visibility = View.GONE
            tv_no_sold_products_found.visibility = View.VISIBLE
        }
    }

}