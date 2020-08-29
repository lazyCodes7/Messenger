package com.example.messenger

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.messenger.NewMessagesActivity.Companion.USER_KEY
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_row.view.*
import kotlinx.android.synthetic.main.activity_newmessage.*
import kotlinx.android.synthetic.main.latest_messages_rows.view.*

class LatestMessagesActivity : AppCompatActivity(){
    companion object{
        var currentUser:User?=null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newmessage)
        recyclerview_latest_messages.adapter=adapter
        recyclerview_latest_messages.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))
        adapter.setOnItemClickListener { item, view ->
            val row = item as LatestMessagesRows
            val intent = Intent(this,ChatLogActivity::class.java)
            intent.putExtra(USER_KEY,row.chatPartnerUser)
            startActivity(intent)
        }
        listenForLatestMessages()
        fetchCurrentUser()
        isUserLoggedIn()
    }
    class LatestMessagesRows(val chatmessage: ChatLogActivity.ChatMessages) : Item<ViewHolder>(){
        var chatPartnerUser:User?=null
        override fun getLayout(): Int {
            return R.layout.latest_messages_rows
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.latest_messages_textview.text=chatmessage.text
            var chatPatnerToId: String
            if(chatmessage.fromId==FirebaseAuth.getInstance().uid){
                chatPatnerToId=chatmessage.toId
            }
            else{
                chatPatnerToId=chatmessage.fromId
            }
            val ref = FirebaseDatabase.getInstance().getReference("users/$chatPatnerToId")
            ref.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    chatPartnerUser = p0.getValue(User::class.java)
                    val uri = chatPartnerUser?.profileImageUrl
                    viewHolder.itemView.username_latest_messages_textview.text= chatPartnerUser?.username
                    Picasso.get().load(uri).into(viewHolder.itemView.latestmessages_image_view)


                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }

    }
    val adapter = GroupAdapter<ViewHolder>()

    private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }
    private fun isUserLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if(uid==null){
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
    }
    val latestMessagesMap = HashMap<String,ChatLogActivity.ChatMessages>()
    private fun refreshRecyclerViewMessages(){
        adapter.clear()
        latestMessagesMap.values.forEach{
            adapter.add(LatestMessagesRows(it))
        }
    }
    private fun listenForLatestMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatmessage = p0.getValue(ChatLogActivity.ChatMessages::class.java)?:return
                latestMessagesMap[p0.key!!] = chatmessage
                refreshRecyclerViewMessages()

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatmessage = p0.getValue(ChatLogActivity.ChatMessages::class.java)?:return
                latestMessagesMap[p0.key!!] = chatmessage
                refreshRecyclerViewMessages()
            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId){
            R.id.sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
            }
            R.id.new_message -> {
                val intent = Intent(this,NewMessagesActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_bar,menu)
        return super.onCreateOptionsMenu(menu)
    }

}


