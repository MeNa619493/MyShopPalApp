package com.example.myshoppal.ui.activities

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.example.myshoppal.Firestore.FirestoreClass
import com.example.myshoppal.Model.User
import com.example.myshoppal.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.et_email
import kotlinx.android.synthetic.main.activity_register.et_first_name
import kotlinx.android.synthetic.main.activity_register.et_last_name
import kotlinx.android.synthetic.main.activity_user_profile.*
import java.util.regex.Pattern

class RegisterActivity : BaseActivity() {

    private var emailInput: String? = null
    private val EMAIL_ADDRESS_PATTERN
            = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "(gmail|yahoo)" +
                "(" +
                "\\." +
                "com" +
                ")+")

    private var passwordInput: String? = null
    private val PASSWORD_PATTERN =
        Pattern.compile("^" +
                "(?=.*[0-9])" +         //at least 1 digit
                //"(?=.*[a-z])" +         //at least 1 lower case letter
                //"(?=.*[A-Z])" +         //at least 1 upper case letter
                //"(?=.*[a-zA-Z])" +      //any letter
                //"(?=.*[@#$%^&+=])" +    //at least 1 special character
                //"(?=\\S+$)" +           //no white spaces
                ".{4,}" +               //at least 4 characters
                "$")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        }
        else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        setupActionBar()

        tv_login.setOnClickListener {
            onBackPressed()
        }

        btn_register.setOnClickListener {
            registerUser()
        }

    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_register_activity)

        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24)
        }

        toolbar_register_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun validateRegisterDetails(): Boolean {

        emailInput = et_email.text.toString().trim { it <= ' ' }
        passwordInput = et_password.text.toString().trim { it <= ' ' }


        return when {

            TextUtils.isEmpty(et_first_name.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_first_name), true)
                false
            }

            TextUtils.isEmpty(et_last_name.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_last_name), true)
                false
            }

            TextUtils.isEmpty(et_email.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }

            TextUtils.isEmpty(et_password.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }


            TextUtils.isEmpty(et_confirm_password.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_confirm_password), true)
                false
            }

            et_password.text.toString().trim { it <= ' ' } != et_confirm_password.text.toString().trim { it <= ' ' } -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_password_and_confirm_password_mismatch), true)
                false
            }

            !cb_terms_and_condition.isChecked -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_agree_terms_and_condition), true)
                false
            }

            (!EMAIL_ADDRESS_PATTERN.matcher(emailInput!!).matches()) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_valid_email), true)
                false
            }

            (!PASSWORD_PATTERN.matcher(passwordInput!!).matches()) -> {
                Toast.makeText(this, getString(R.string.err_msg_enter_valid_password), Toast.LENGTH_LONG).show()
                false
            }


            else -> {

                true

            }
        }
    }

    private fun registerUser(){
        if(validateRegisterDetails()){

            showProgressDialog(resources.getString(R.string.please_wait))

            val email : String = et_email.text.toString().trim { it <= ' ' }
            val password : String = et_password.text.toString().trim { it <= ' ' }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                  OnCompleteListener<AuthResult>{ task ->

                      if (task.isSuccessful){

                          val firebaseUser : FirebaseUser = task.result!!.user!!

                          val user = User( firebaseUser.uid,
                                           et_first_name.text.toString().trim { it <= ' ' },
                                           et_last_name.text.toString().trim { it <= ' ' },
                                           et_email.text.toString().trim { it <= ' ' } )

                          FirestoreClass().registerUser(this@RegisterActivity, user)

                          FirebaseAuth.getInstance().signOut()

                          finish()
                      }
                      else{
                          hideProgressDialog()

                          showErrorSnackBar(task.exception!!.message.toString(),true)
                      }
                  }
                )
        }
    }

    fun userRegistrationSuccess(){

        Toast.makeText(this,resources.getString(R.string.register_success), Toast.LENGTH_SHORT).show()

    }

    override fun onDestroy() {
        super.onDestroy()
        hideProgressDialog()
    }

}