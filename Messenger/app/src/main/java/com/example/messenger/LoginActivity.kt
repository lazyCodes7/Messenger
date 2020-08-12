package com.example.messenger

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity(): AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        login_button.setOnClickListener {
            val email = editTextTextEmailAddress2.text.toString()
            val password = editTextTextPassword2.text.toString()
            if(email.isEmpty()||password.isEmpty()){
                Toast.makeText(this,"Pls enter email/password",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener{
                if(!it.isSuccessful)return@addOnCompleteListener
                Log.d("Main","user logged in with ${it.result?.user?.uid}")
            }
                .addOnFailureListener{
                    Toast.makeText(this,"User not found",Toast.LENGTH_SHORT).show()
                }
        }
        back_to_register.setOnClickListener {
            finish()
        }

    }
}