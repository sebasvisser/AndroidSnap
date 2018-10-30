package me.keegan.snap

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

import com.parse.LogInCallback
import com.parse.ParseException
import com.parse.ParseUser

class LoginActivity : Activity() {

    protected lateinit var mUsername: EditText
    protected lateinit var mPassword: EditText
    protected lateinit var mLoginButton: Button

    protected lateinit var mSignUpTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
        setContentView(R.layout.activity_login)

        mSignUpTextView = findViewById<View>(R.id.signUpText) as TextView
        mSignUpTextView.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(intent)
        }

        mUsername = findViewById<View>(R.id.usernameField) as EditText
        mPassword = findViewById<View>(R.id.passwordField) as EditText
        mLoginButton = findViewById<View>(R.id.loginButton) as Button
        mLoginButton.setOnClickListener {
            var username = mUsername.text.toString()
            var password = mPassword.text.toString()

            username = username.trim { it <= ' ' }
            password = password.trim { it <= ' ' }

            if (username.isEmpty() || password.isEmpty()) {
                val builder = AlertDialog.Builder(this@LoginActivity)
                builder.setMessage(R.string.login_error_message)
                        .setTitle(R.string.login_error_title)
                        .setPositiveButton(android.R.string.ok, null)
                val dialog = builder.create()
                dialog.show()
            } else {
                // Login
                setProgressBarIndeterminateVisibility(true)

                ParseUser.logInInBackground(username, password) { user, e ->
                    setProgressBarIndeterminateVisibility(false)

                    if (e == null) {
                        // Success!
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    } else {
                        val builder = AlertDialog.Builder(this@LoginActivity)
                        builder.setMessage(e.message)
                                .setTitle(R.string.login_error_title)
                                .setPositiveButton(android.R.string.ok, null)
                        val dialog = builder.create()
                        dialog.show()
                    }
                }
            }
        }
    }
}
