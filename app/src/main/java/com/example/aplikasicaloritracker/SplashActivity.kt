package com.example.aplikasicaloritracker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT:Long = 2000

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        firebaseAuth = FirebaseAuth.getInstance()

        Handler().postDelayed({
            val sharedPreferences = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val rememberMe = sharedPreferences.getBoolean(PREF_REMEMBER_ME, false)

            if (rememberMe) {
                val email = sharedPreferences.getString(PREF_EMAIL, "")
                if (firebaseAuth.currentUser != null) {
                    if (email != null) {
                        checkUserRole(email)
                    }
                }
            } else {
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
            }
        }, SPLASH_TIME_OUT)
    }

    private fun checkUserRole(email: String) {
        val usersCollection = FirebaseFirestore.getInstance().collection("users")
        usersCollection.whereEqualTo("email", email).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val userDocument = querySnapshot.documents[0]
                    val isAdmin = userDocument.getBoolean("admin") ?: false

                    if (isAdmin) {
                        val intent = Intent(this, AdminActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
            .addOnFailureListener { e ->
                makeToast("Error retrieving user data: $e")
            }
    }

    private fun makeToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

}