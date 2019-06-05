package com.aziz.flashchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainChatActivity extends AppCompatActivity {

    // TODO: Add member variables here:
    private String mDisplayName;
    private ListView mChatListView;
    private EditText mInputText;
    private ImageButton mSendButton;
    private FirebaseUser muser;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;
    private ChatListAdapter mAdapter;
    private CircleImageView user_img;
    private TextView uset_name;
    Button Logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat);
        user_img=findViewById(R.id.user_img);
        uset_name=findViewById(R.id.user_name);
        Logout=findViewById(R.id.user_logout);

        // TODO: Set up the display name and get the Firebase reference
        mAuth=FirebaseAuth.getInstance();
        muser=mAuth.getCurrentUser();
        setupDisplayName();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();


        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogOutUser();
            }
        });
        // Link the Views in the layout to the Java code
        mInputText = (EditText) findViewById(R.id.messageInput);
        mSendButton = (ImageButton) findViewById(R.id.sendButton);
        mChatListView = (ListView) findViewById(R.id.chat_list_view);

        PrepareUserData();


        // TODO: Send the message when the "enter" button is pressed
//        mInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//
//                sendMessage();
//                return true;
//            }
//        });

        // TODO: Add an OnClickListener to the sendButton to send a message
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

    }

    void PrepareUserData()
    {
        uset_name.setText(muser.getDisplayName());

        Glide.with(this)
                .load(muser.getPhotoUrl())
                .into(user_img);
    }

    void LogOutUser()
    {
        if(muser!=null)
        {
            mAuth.signOut();
            startActivity(new Intent(this,LoginActivity.class));
            finish();
        }



    }

    // TODO: Retrieve the display name from the Shared Preferences
    private void setupDisplayName(){

//        SharedPreferences prefs = getSharedPreferences(RegisterActivity.CHAT_PREFS, MODE_PRIVATE);

        mDisplayName = muser.getDisplayName();

//        if (mDisplayName == null) mDisplayName = "Anonymous";
//        Toast.makeText(this, mDisplayName, Toast.LENGTH_LONG).show();
    }


    private void sendMessage() {

        Log.d("FlashChat", "I sent something");
        // TODO: Grab the text the user typed in and push the message to Firebase
        String input = mInputText.getText().toString();
        if (!input.equals("")) {
            InstantMessage chat = new InstantMessage(input, mDisplayName);
            mDatabaseReference.child("messages").push().setValue(chat).addOnCompleteListener(this,new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful())
                    {
                        Toast.makeText(MainChatActivity.this, "Added", Toast.LENGTH_SHORT).show();
                    }

                }
            }).addOnFailureListener(this,new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            mInputText.setText("");
        }

    }

    // TODO: Override the onStart() lifecycle method. Setup the adapter here.
    @Override
    public void onStart() {
        super.onStart();
        mAdapter = new ChatListAdapter(this, mDatabaseReference, mDisplayName);
        mChatListView.setAdapter(mAdapter);
    }


    @Override
    public void onStop() {
        super.onStop();

        // TODO: Remove the Firebase event listener on the adapter.
        mAdapter.cleanup();

    }

}
