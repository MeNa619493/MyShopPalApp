package com.example.myshoppal.Firestore

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myshoppal.Model.*
import com.example.myshoppal.R
import com.example.myshoppal.ui.activities.*
import com.example.myshoppal.ui.fragments.DashboardFragment
import com.example.myshoppal.ui.fragments.OrdersFragment
import com.example.myshoppal.ui.fragments.ProductsFragment
import com.example.myshoppal.ui.fragments.SoldProductsFragment
import com.example.myshoppal.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FirestoreClass {

    val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity : RegisterActivity, userInfo : User){

        mFireStore.collection(Constants.USERS).document(userInfo.id).set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegistrationSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while registering the user.", e )
            }
    }

    fun getCurrentUserID(): String{
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""

        if (currentUser != null){
            currentUserID = currentUser.uid
        }

        return currentUserID
    }

    fun getUserDetails( activity: Activity){

        mFireStore.collection(Constants.USERS).document(getCurrentUserID()).get().
        addOnSuccessListener { document ->
            Log.i(activity.javaClass.simpleName, document.toString())
            val user = document.toObject(User::class.java)!!

            val sharedPreferences = activity.getSharedPreferences(Constants.MYSHOPPAL_PREFERENCES, Context.MODE_PRIVATE)
            val editor : SharedPreferences.Editor = sharedPreferences.edit()
            editor.putString(Constants.LOGGED_IN_USERNAME, "${user.firstName} ${user.lastName}")
            editor.apply()

            when(activity){
                is LoginActivity ->{
                    activity.userLoggedInSuccess(user)
                }

                is SettingsActivity ->{
                    activity.userDetailsSuccess(user)
                }
            }
        }.addOnFailureListener { e ->

            when(activity){
                is LoginActivity ->{
                    activity.hideProgressDialog()
                }

                is SettingsActivity ->{
                    activity.hideProgressDialog()
                }
            }

            Log.e(activity.javaClass.simpleName, "Error while getting user details.", e)
        }
    }

    fun updateUserProfileData(activity: Activity, userHashMap: HashMap< String, Any>){

        mFireStore.collection(Constants.USERS).document(getCurrentUserID()).update(userHashMap)
            .addOnSuccessListener {

                when(activity){
                    is UserProfileActivity -> {
                        activity.userProfileUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener { e->

                when(activity){
                    is UserProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName, "Error while updating the user detail.", e )
            }
    }

    fun uploadImageToCloudStorage(activity: Activity, imageFileUri: Uri?, imageType: String){

        val sRef : StorageReference = FirebaseStorage.getInstance().reference.child(
            imageType + System.currentTimeMillis() + "." + Constants.getFileExtension(activity, imageFileUri) )

        sRef.putFile(imageFileUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.e("Firebase Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { url ->
                        Log.e("Downloadable Image URL", url.toString())

                        when (activity) {
                            is UserProfileActivity -> {
                                activity.imageUploadSuccess(url.toString())
                            }

                            is AddProductActivity -> {
                                activity.imageUploadSuccess(url.toString())
                            }

                            is ProductDetailsActivity -> {
                                activity.editedImageUploadSuccess(url.toString())
                            }
                        }
                    }
            }
            .addOnFailureListener { exception ->

                when (activity) {
                    is UserProfileActivity -> {
                        activity.hideProgressDialog()
                    }

                    is AddProductActivity -> {
                        activity.hideProgressDialog()
                    }

                    is ProductDetailsActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(activity.javaClass.simpleName, exception.message, exception)
            }
    }

    fun uploadProductDetails(activity : AddProductActivity, productInfo : Product){

        mFireStore.collection(Constants.PRODUCTS).document().set(productInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.productUploadSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while uploading the product details.", e )
            }
    }

    fun getProductsList(fragment: Fragment) {

        mFireStore.collection(Constants.PRODUCTS).whereEqualTo(Constants.USER_ID,getCurrentUserID()).get()
            .addOnSuccessListener { document ->
                Log.e("Products List", document.documents.toString() )
                val productList : ArrayList<Product> = ArrayList()

                for (i in document.documents){
                    val product = i.toObject(Product::class.java)
                    product!!.product_id = i.id

                    productList.add(product)
                }

                when(fragment){
                    is ProductsFragment -> {
                        fragment.successProductsList(productList)
                    }
                }
            }.addOnFailureListener {
                when (fragment) {
                    is ProductsFragment -> {
                        fragment.hideProgressDialog()
                    }
                }
            }
    }

    fun deleteProduct(fragment: ProductsFragment, productID: String){

        mFireStore.collection(Constants.PRODUCTS).document(productID).delete()
            .addOnSuccessListener {
                fragment.productDeleteSuccess()
            }.addOnFailureListener { e ->
                fragment.hideProgressDialog()
                Log.e(fragment.javaClass.simpleName, "Error while deleting the product.", e)
            }
    }

    fun getDashboardList(fragment: DashboardFragment) {

        mFireStore.collection(Constants.PRODUCTS).get()
            .addOnSuccessListener { document ->
                Log.e("Products List", document.documents.toString() )
                val productList : ArrayList<Product> = ArrayList()

                for (i in document.documents){
                    val product = i.toObject(Product::class.java)
                    product!!.product_id = i.id

                    productList.add(product)
                }

                    fragment.successDashboardItemList(productList)

            }.addOnFailureListener { e ->

                fragment.hideProgressDialog()
                Log.e(fragment.javaClass.simpleName, "Error while getting dashboard items list.", e)
            }
    }

    fun getProductDetails(activity: ProductDetailsActivity, productId: String){

        mFireStore.collection(Constants.PRODUCTS).document(productId).get()
            .addOnSuccessListener {document ->
                Log.e(activity.javaClass.simpleName, document.toString())
                val product = document.toObject(Product::class.java)
                if (product != null) {
                    activity.productDetailsSuccess(product)
                }
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while getting the product details.", e )
            }
    }

    fun updateProductDetails(activity: ProductDetailsActivity,productId: String ,productHashMap: HashMap< String, Any>){

        mFireStore.collection(Constants.PRODUCTS).document(productId).update(productHashMap)
            .addOnSuccessListener {

                activity.productDetailsUpdateSuccess()

            }
            .addOnFailureListener { e ->

                        activity.hideProgressDialog()

                Log.e(activity.javaClass.simpleName, "Error while updating the product detail.", e )
            }
    }

    fun addCartItems(activity: ProductDetailsActivity, cartItem: CartItem){

        mFireStore.collection(Constants.CART_ITEMS).document().set(cartItem, SetOptions.merge())
            .addOnSuccessListener {
                activity.addToCartSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating the document for cart item.", e )
            }
    }

    fun checkIfItemExistInCart(activity: ProductDetailsActivity,productId: String) {

        mFireStore.collection(Constants.CART_ITEMS).whereEqualTo(Constants.USER_ID,getCurrentUserID()).whereEqualTo(Constants.PRODUCT_ID,productId)
            .get().addOnSuccessListener { document ->

                if (document.documents.size > 0){
                    activity.productExistInCart()
                }
                else{
                    activity.hideProgressDialog()
                }

            }.addOnFailureListener { e ->
               activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while checking existing cart list.", e )
            }
    }

    fun getCartList(activity: Activity) {

        mFireStore.collection(Constants.CART_ITEMS).whereEqualTo(Constants.USER_ID,getCurrentUserID()).get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString() )
                val cartList : ArrayList<CartItem> = ArrayList()

                for (i in document.documents){
                    val cartItem = i.toObject(CartItem::class.java)
                    cartItem!!.id = i.id

                    cartList.add(cartItem)
                }

                when(activity){

                    is CartListActivity -> {
                        activity.successCartItemList(cartList)
                    }

                    is CheckoutActivity -> {
                        activity.successCartItemList(cartList)
                    }
                }

            }.addOnFailureListener { e ->

                when(activity){

                    is CartListActivity -> {
                        activity.hideProgressDialog()
                    }

                    is CheckoutActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(activity.javaClass.simpleName, "Error while getting the cart list items.", e )
            }
    }

    fun getAllProductList(activity: Activity) {

        mFireStore.collection(Constants.PRODUCTS).get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString() )
                val productsList : ArrayList<Product> = ArrayList()

                for (i in document.documents){
                    val product = i.toObject(Product::class.java)
                    product!!.product_id = i.id

                    productsList.add(product)
                }

                when(activity){

                    is CartListActivity -> {
                        activity.successProductsListFromFirestore(productsList)
                    }

                    is CheckoutActivity -> {
                        activity.successProductsListFromFirestore(productsList)
                    }
                }

            }.addOnFailureListener { e ->

                when(activity){

                    is CartListActivity -> {
                        activity.hideProgressDialog()
                    }

                    is CheckoutActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e("Get product list", "Error while getting all products list.", e )
            }
    }

    fun removeItemFromCart(context: Context, cartId: String){

        mFireStore.collection(Constants.CART_ITEMS).document(cartId).delete()
            .addOnSuccessListener {

                when(context){
                    is CartListActivity -> {
                        context.itemRemovedSuccess()
                    }
                }

            }.addOnFailureListener { e ->

                when(context){
                    is CartListActivity -> {
                        context.hideProgressDialog()
                    }
                }

                Log.e(context.javaClass.simpleName, "Error while deleting the cart Item.", e)
            }
    }

    fun updateMyCart(context: Context, cart_Id: String, itemHashMap: HashMap<String, Any>){

        mFireStore.collection(Constants.CART_ITEMS).document(cart_Id).update(itemHashMap)
            .addOnSuccessListener {
                when(context){
                    is CartListActivity -> {
                        context.itemUpdateSuccess()
                    }
                }
            }.addOnFailureListener { e ->
                when(context){
                    is CartListActivity -> {
                        context.hideProgressDialog()
                    }
                }

                Log.e(context.javaClass.simpleName, "Error while updating the cart Item.", e)
            }
    }

    fun addAddress(activity : AddEditAddressActivity, address: Address){

        mFireStore.collection(Constants.ADDRESSES).document().set(address, SetOptions.merge())
            .addOnSuccessListener {
                activity.addUpdateAddressSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while uploading the address details.", e )
            }
    }

    fun getAddressesList(activity: AddressListActivity) {

        mFireStore.collection(Constants.ADDRESSES).whereEqualTo(Constants.USER_ID,getCurrentUserID()).get()
            .addOnSuccessListener { document ->

                Log.e(activity.javaClass.simpleName, document.documents.toString() )

                val addressesList : ArrayList<Address> = ArrayList()
                for (i in document.documents){
                    val addressItem = i.toObject(Address::class.java)
                    addressItem!!.id = i.id

                    addressesList.add(addressItem)
                }

                activity.successAddressListFromFirestore(addressesList)

            }.addOnFailureListener { e ->

                activity.hideProgressDialog()

                Log.e(activity.javaClass.simpleName, "Error while getting the address list items.", e )
            }
    }

    fun updateAddress(activity : AddEditAddressActivity, address: Address, addressId: String){

        mFireStore.collection(Constants.ADDRESSES).document(addressId).set(address, SetOptions.merge())
            .addOnSuccessListener {
                activity.addUpdateAddressSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while updating the address details.", e )
            }
    }

    fun deleteAddress(activity: AddressListActivity, addressId: String){
        mFireStore.collection(Constants.ADDRESSES).document(addressId).delete()
            .addOnSuccessListener {
                activity.deleteAddressSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while deleting the address.", e )
            }
    }

    fun updateAllDetails(activity: CheckoutActivity, cartList: ArrayList<CartItem>, order: Order){
        val writeBatch = mFireStore.batch()

        for (cartItem in cartList){

            //val productHashMap = HashMap<String, Any>()
            //productHashMap[Constants.STOCK_QUANTITY] = (cartItem.stock_quantity.toInt() - cartItem.cart_quantity.toInt()).toString()

                val soldProduct = SoldProduct(
                    cartItem.product_owner_id,
                    cartItem.title,
                    cartItem.price,
                    cartItem.cart_quantity,
                    cartItem.image,
                    order.title,
                    order.order_datetime,
                    order.sub_total_amount,
                    order.shipping_charge,
                    order.total_amount,
                    order.address
                )

            val documentReference = mFireStore.collection(Constants.SOLD_PRODUCTS).document(cartItem.product_id)
            writeBatch.set(documentReference, soldProduct)
        }

        for (cartItem in cartList){
            val documentReference = mFireStore.collection(Constants.CART_ITEMS).document(cartItem.id)
            writeBatch.delete(documentReference)
        }

        writeBatch.commit()
            .addOnSuccessListener {
                activity.allDetailsUpdatedSuccessfully()
            }
            .addOnFailureListener{ e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while updating all the details.", e )
            }
    }

    fun placeOrder(activity : CheckoutActivity, order: Order){

        mFireStore.collection(Constants.ORDERS).document().set(order, SetOptions.merge())
            .addOnSuccessListener {
                activity.placedOrderSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while uploading the address details.", e )
            }
    }

    fun getMyOrdersList(fragment: OrdersFragment) {

        mFireStore.collection(Constants.ORDERS).whereEqualTo(Constants.USER_ID,getCurrentUserID()).get()
            .addOnSuccessListener { document ->

                Log.e(fragment.javaClass.simpleName, document.documents.toString() )

                val ordersList : ArrayList<Order> = ArrayList()
                for (i in document.documents){
                    val orderItem = i.toObject(Order::class.java)
                    orderItem!!.id = i.id

                    ordersList.add(orderItem)
                }

                fragment.populateOrdersListInUI(ordersList)

            }.addOnFailureListener { e ->

                fragment.hideProgressDialog()

                Log.e(fragment.javaClass.simpleName, "Error while getting the orders list.", e )
            }
    }

    fun getSoldProductsList(fragment: SoldProductsFragment){
        mFireStore.collection(Constants.SOLD_PRODUCTS).whereEqualTo(Constants.USER_ID, getCurrentUserID()).get()
            .addOnSuccessListener { document ->
                Log.e(fragment.javaClass.simpleName, document.documents.toString() )

                val soldProductsList : ArrayList<SoldProduct> = ArrayList()
                for (i in document.documents){
                    val soldProduct = i.toObject(SoldProduct::class.java)
                    soldProduct!!.id = i.id

                    soldProductsList.add(soldProduct)
                }

                fragment.populateSoldProductsListInUI(soldProductsList)


            }.addOnFailureListener { e ->
                fragment.hideProgressDialog()

                Log.e(fragment.javaClass.simpleName, "Error while getting the sold products list.", e )
            }
    }
}
