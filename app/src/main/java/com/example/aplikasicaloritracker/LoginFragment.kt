package com.example.aplikasicaloritracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.nutricare_uas.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentLoginBinding? = null
    private val binding get()= _binding!!

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root

        firebaseAuth = FirebaseAuth.getInstance()

        val authActivity = requireActivity() as? AuthActivity

        val sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rememberMe = sharedPreferences.getBoolean(PREF_REMEMBER_ME, false)

        with(binding) {
            checkBox.isChecked = rememberMe

            if (rememberMe) {
                textinputEmail.setText(sharedPreferences.getString(PREF_EMAIL, ""))
                textinputPass.setText(sharedPreferences.getString(PREF_PASSWORD, ""))
            }

            btnLogin.setOnClickListener {
                val email = textinputEmail.text.toString().trim()
                val pass = textinputPass.text.toString()

                if (textinputEmail.text!!.isEmpty()) {
                    textinputEmail.error = "E-mail required"
                } else if (textinputPass.text!!.isEmpty()) {
                    textinputPass.error = "Password required"
                } else {
                    firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            saveLoginCredentials(email, pass, checkBox.isChecked)
                            checkUserRole(email)
                            textinputEmail.text?.clear()
                            textinputPass.text?.clear()
                        } else {
                            val exception = task.exception
                            val errorMessage = exception?.message ?: "Authentication failed"
                            authActivity?.makeToast(errorMessage)
                            Log.e("LoginError", "Full exception: ${exception?.toString()}")
                        }
                    }
                }
            }

            txtToRegister.setOnClickListener {
                authActivity?.switchFragment(1)
            }
        }

        return view
    }


    private fun saveLoginCredentials(email: String, password: String, rememberMe: Boolean) {
        val sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString(PREF_EMAIL, if (rememberMe) email else "")
        editor.putString(PREF_PASSWORD, if (rememberMe) password else "")
        editor.putBoolean(PREF_REMEMBER_ME, rememberMe)

        editor.apply()

        Log.d("SharedPreferences", "Email: ${sharedPreferences.getString(PREF_EMAIL, "")}, Password: ${sharedPreferences.getString(PREF_PASSWORD, "")}, RememberMe: $rememberMe")
    }


    private fun checkUserRole(email: String) {
        val authActivity = requireActivity() as? AuthActivity
        val usersCollection = FirebaseFirestore.getInstance().collection("users")
        usersCollection.whereEqualTo("email", email).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val userDocument = querySnapshot.documents[0]
                    val isAdmin = userDocument.getBoolean("admin") ?: false

                    if (isAdmin) {
                        val intent = Intent(this@LoginFragment.requireActivity(), AdminActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this@LoginFragment.requireActivity(), MainActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
            .addOnFailureListener { e ->
                authActivity?.makeToast("Error retrieving user data: $e")
            }
    }

}