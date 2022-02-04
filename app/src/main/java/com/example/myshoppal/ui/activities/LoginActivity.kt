package com.example.myshoppal.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import com.example.myshoppal.Firestore.FirestoreClass
import com.example.myshoppal.Model.User
import com.example.myshoppal.R
import com.example.myshoppal.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

class LoginActivity : BaseActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        }
        else {
            window.setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN )
        }

        tv_forget_password.setOnClickListener(this)
        tv_register.setOnClickListener(this)
        btn_login.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (view != null){
            when(view.id){

                R.id.tv_forget_password ->{
                    val intent = Intent(this@LoginActivity, ForgetPasswordActivity::class.java)
                    startActivity(intent)
                }

                R.id.tv_register ->{
                    val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                    startActivity(intent)
                }

                R.id.btn_login ->{
                    logInRegisteredUser()
                }
            }
        }
    }

    private fun validateLoginDetails(): Boolean {
        return when {
            TextUtils.isEmpty(et_email_login.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }

            TextUtils.isEmpty(et_password_login.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }

            else -> {
                true
            }
        }
    }

    private fun logInRegisteredUser(){
        if(validateLoginDetails()){

            showProgressDialog(resources.getString(R.string.please_wait))

            val email : String = et_email_login.text.toString().trim { it <= ' ' }
            val password : String = et_password_login.text.toString().trim { it <= ' ' }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener{ task ->

                        if (task.isSuccessful){
                            FirestoreClass().getUserDetails(this@LoginActivity)
                        }
                        else {
                            hideProgressDialog()
                            showErrorSnackBar(task.exception!!.message.toString(),true)
                        }
                }
        }
    }

    fun userLoggedInSuccess(user: User){

        hideProgressDialog()

        if (user.profileCompleted == 0){
           val intent = Intent(this@LoginActivity , UserProfileActivity::class.java)
            intent.putExtra(Constants.EXTRA_USER_DETAILS, user)
            startActivity(intent)
        }
        else{
            startActivity(Intent(this@LoginActivity , DashboardActivity::class.java))
        }
        finish()
    }
}