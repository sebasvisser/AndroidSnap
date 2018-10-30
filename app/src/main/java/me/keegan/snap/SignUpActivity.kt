package me.keegan.snap

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText

import com.parse.ParseException
import com.parse.ParseUser
import com.parse.SignUpCallback

class SignUpActivity : Activity() {

    protected lateinit var mUsername: EditText
    protected lateinit var mPassword: EditText
    protected lateinit var mEmail: EditText
    protected lateinit var mSignUpButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
        setContentView(R.layout.activity_sign_up)

        mUsername = findViewById<View>(R.id.usernameField) as EditText
        mPassword = findViewById<View>(R.id.passwordField) as EditText
        mEmail = findViewById<View>(R.id.emailField) as EditText
        mSignUpButton = findViewById<View>(R.id.signupButton) as Button
        mSignUpButton.setOnClickListener {
            var username = mUsername.text.toString()
            var password = mPassword.text.toString()
            var email = mEmail.text.toString()

            username = username.trim { it <= ' ' }
            password = password.trim { it <= ' ' }
            email = email.trim { it <= ' ' }

            if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                val builder = AlertDialog.Builder(this@SignUpActivity)
                builder.setMessage(R.string.signup_error_message)
                        .setTitle(R.string.signup_error_title)
                        .setPositiveButton(android.R.string.ok, null)
                val dialog = builder.create()
                dialog.show()
            } else {
                // create the new user!
                setProgressBarIndeterminateVisibility(true)

                val newUser = ParseUser()
                newUser.username = username
                newUser.setPassword(password)
                newUser.email = email
                newUser.signUpInBackground { e ->
                    setProgressBarIndeterminateVisibility(false)

                    if (e == null) {
                        // Success!
                        val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    } else {
                        val builder = AlertDialog.Builder(this@SignUpActivity)
                        builder.setMessage(e.message)
                                .setTitle(R.string.signup_error_title)
                                .setPositiveButton(android.R.string.ok, null)
                        val dialog = builder.create()
                        dialog.show()
                    }
                }
            }
        }
    }
}
