@file:Suppress("DEPRECATION")

package com.example.messenger

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.getBitmap
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

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
        select_photobutton.setOnClickListener {
            Log.d("Main","Try to get image")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            startActivityForResult(intent,0)
        }
    }
        var selectphotoUri : Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==0 && resultCode== Activity.RESULT_OK && data!=null){
            Log.d("Main","Image successfully selected")
            selectphotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectphotoUri)
            select_imageview.setImageBitmap(bitmap)
            select_photobutton.alpha=0f

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
            uploadImagesToFireBase()
        }
            .addOnFailureListener {
                Log.d("Main","Failed to create user: ${it.message}")
                Toast.makeText(this,"Pls enter email/password",Toast.LENGTH_SHORT).show()

            }
    }
    private fun uploadImagesToFireBase(){
        if(selectphotoUri==null)return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectphotoUri!!).addOnSuccessListener {
            Log.d("Main","successfuly uploaded image: ${it.metadata?.path}")
            ref.downloadUrl.addOnSuccessListener {
                Log.d("Main","file location: $it")
                saveUserToDatabase(it.toString())
            }
        }
    }
    private fun saveUserToDatabase(profileImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid.toString(),username_text.text.toString(),profileImageUrl)
        ref.setValue(user).addOnSuccessListener {
            Log.d("Main","uploaded user to database")
        }
    }
}
class User(val uid:String,val username:String,val profileImageUrl: String){

}