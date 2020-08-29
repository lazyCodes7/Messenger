package com.example.messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_chat_row.view.*
import kotlinx.android.synthetic.main.chat_from_row.view.*

class ChatLogActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        val user = intent.getParcelableExtra<User>(NewMessagesActivity.USER_KEY)
        supportActionBar?.title = "${user.username}"
        listenForMessages()
        recyclerview_chatlog.adapter=adapter

        send_chatlog.setOnClickListener{
            sendTextMessages()
            Log.d("ChatLog","Attempt to send message")
        }
    }
    class ChatMessages(val id: String,val text:String,val fromId: String,val toId:String,val timestamp: Long){
        constructor(): this("","","","",-1)
    }
    private fun listenForMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessagesActivity.USER_KEY)
        val toId = user.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
               val chatmessage = p0.getValue(ChatMessages::class.java)
                if (chatmessage != null) {
                    if (chatmessage.fromId == FirebaseAuth.getInstance().uid) {
                        Log.d("ChatLog",chatmessage.fromId.toString())
                        val user = LatestMessagesActivity.currentUser
                        adapter.add(ChatFromItem(chatmessage.text,user!!))

                    } else {
                        val toUser = intent.getParcelableExtra<User>(NewMessagesActivity.USER_KEY)
                        adapter.add(ChatToItem(chatmessage.text,toUser))
                    }
                }
                recyclerview_chatlog.scrollToPosition(adapter.itemCount-1)

            }

            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
    }
    private fun sendTextMessages(){
        val text = entermsg_chatlog.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessagesActivity.USER_KEY)
        val toId = user.uid
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatmessage = ChatMessages(reference.key!!,text,fromId!!,toId,System.currentTimeMillis())
        reference.setValue(chatmessage).addOnSuccessListener {
            Log.d("ChatLog","Saved text message successfully ${reference.key}")
            entermsg_chatlog.text.clear()
            recyclerview_chatlog.scrollToPosition(adapter.itemCount-1)
        }
        toReference.setValue(chatmessage)
        val latestMessagesRef = FirebaseDatabase.getInstance().getReference("latest-messages/$fromId/$toId")
        latestMessagesRef.setValue(chatmessage)
        val toMessagesRef = FirebaseDatabase.getInstance().getReference("latest-messages/$toId/$fromId")
        toMessagesRef.setValue(chatmessage)

    }

}
class ChatToItem(val text:String,val user:User): Item<ViewHolder>(){
    val uri = user.profileImageUrl
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.chat_from_row.text=text
        Picasso.get().load(uri).into(viewHolder.itemView.chat_to_imageView)

    }

    override fun getLayout(): Int {
        return R.layout.activity_chat_row
    }
}
class ChatFromItem(val text: String,val user: User):Item<ViewHolder>(){
    val uri = user.profileImageUrl
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.chat_row.text=text
        Picasso.get().load(uri).into(viewHolder.itemView.image_chat_from_row)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}