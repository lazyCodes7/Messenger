package com.example.messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        register_button.setOnClickListener {
            createRegistration()
        }
        already_have_an_account_textView.setOnClickListener {
            Log.d("MainActivity","Try to show login activity")
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }
    }
    private fun createRegistration(){
        val email = editTextTextEmailAddress.text.toString()
        val password = editTextTextPassword.text.toString()
        if(email.isEmpty()||password.isEmpty()){
            Toast.makeText(this,"Pls enter email/password",Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("MainActivity","email is "+email)
        Log.d("MainActivity","password is: "+password)
        // FireBase authentication to create a user with email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener{
            if(!it.isSuccessful) return@addOnCompleteListener
            //else
            Log.d("MainActivity","user created with uid: ${it.result?.user?.uid}")
        }
            .addOnFailureListener {
                Log.d("Main","Failed to create user: ${it.message}")
                Toast.makeText(this,"Pls enter email/password",Toast.LENGTH_SHORT).show()

            }
    }
}