package com.example.myshoppal.ui.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myshoppal.Firestore.FirestoreClass
import com.example.myshoppal.Model.Address
import com.example.myshoppal.Model.CartItem
import com.example.myshoppal.Model.Product
import com.example.myshoppal.R
import com.example.myshoppal.ui.activities.AddEditAddressActivity
import com.example.myshoppal.ui.activities.CartListActivity
import com.example.myshoppal.ui.activities.CheckoutActivity
import com.example.myshoppal.ui.activities.ProductDetailsActivity
import com.example.myshoppal.utils.Constants
import com.example.myshoppal.utils.GlideLoader
import kotlinx.android.synthetic.main.item_address_layout.view.*
import kotlinx.android.synthetic.main.item_cart_layout.view.*
import kotlinx.android.synthetic.main.item_dashboard_layout.view.*
import kotlinx.android.synthetic.main.item_list_layout.view.*
import java.util.*

class MyAddressListAdapter (private val context: Context, private val addressList: ArrayList<Address>, private val selectAddress: Boolean)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_address_layout, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = addressList[position]

        if (holder is MyViewHolder){
            holder.itemView.tv_address_full_name.text = model.fullName
            holder.itemView.tv_address_type.text = model.type
            holder.itemView.tv_address_details.text = "${model.address}, ${model.zipCode}"
            holder.itemView.tv_address_mobile_number.text = model.phoneNumber

            if (selectAddress){
                holder.itemView.setOnClickListener {
                    Toast.makeText(context,"Selected Address: ${model.address}, ${model.zipCode}", Toast.LENGTH_SHORT).show()

                    val intent = Intent(context, CheckoutActivity::class.java)
                    intent.putExtra(Constants.EXTRA_SELECTED_ADDRESS, model)
                    context.startActivity(intent)
                }
            }
        }
    }

    fun notifyEditItem(activity: Activity, position: Int){
        val intent = Intent(context, AddEditAddressActivity::class.java)
        intent.putExtra(Constants.EXTRA_ADDRESS_DETAILS, addressList[position])
        activity.startActivityForResult(intent, Constants.ADD_ADDRESS_REQUEST_CODE)
        notifyItemChanged(position)
    }


    override fun getItemCount(): Int {
        return addressList.size
    }

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view)
}