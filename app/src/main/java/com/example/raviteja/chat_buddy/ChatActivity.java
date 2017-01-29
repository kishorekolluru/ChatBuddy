package com.example.raviteja.chat_buddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;


public class ChatActivity extends AppCompatActivity implements ChatFragment.OnChatFragmentInteractionListener,
        ContactListFragment.OnContactFragInteractionListener, TabLayout.OnTabSelectedListener {
    ViewPager mViewPager;
    TabLayout mTabLayout;
    ChatTabAdapter mTabAdapter;
    Button signOut;
    GoogleApiClient mGoogleApiClient;
    //firebase
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mMessageRef = mRootRef.child(Constants.T_REF_MSGS);
    public User mUser;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
//        mUser = mAuth.getCurrentUser();
        Util.loginType = getTypeOfAuthSharedPreferences();
        DatabaseReference ref = mRootRef.child(Constants.T_REF_USERS);
setTitle("Chat Buddy");
        ref.orderByChild("emailId").equalTo(mAuth.getCurrentUser().getEmail()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snap : dataSnapshot.getChildren()) {
                                HashMap<String, Object> map = (HashMap<String, Object>) snap.getValue();
                                mUser = new User();
                                mUser.setEmailId((String) map.get("emailId"));
                                mUser.setFirstname((String) map.get("firstname"));
                                mUser.setLastname((String) map.get("lastname"));
                                mUser.setUserId((String) map.get("userId"));
                                mUser.setProfileImage((String) map.get("profileImage"));
                                mUser.setGender((String) map.get("gender"));
                            }
                            Log.d("rdemo", "The data snap shot is...");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
        android.widget.Toolbar bar = new android.widget.Toolbar(this);
        bar.setTitle("Chat Buddy");
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mTabAdapter = new ChatTabAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTabAdapter);
//add new tabs
        mTabLayout.addTab(mTabLayout.newTab().setText("Chats"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Contacts"));
        mTabLayout.addOnTabSelectedListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_conversation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_profile_menu:
                if (Util.loginType.equals(Constants.LOGIN_FB) || Util.loginType.equals(Constants.LOGIN_GOOGLE)) {
                    toast("You cannot edit your profile if logged in through " + Util.loginType);
                    return true;
                } else {
                    Intent intent = new Intent(ChatActivity.this, EditProfileActivity.class);
                intent.putExtra("mUser", mUser);
                startActivity(intent);
                }
                break;
            case R.id.logout_menu:
                //do logout stuff
                String loginType = getTypeOfAuthSharedPreferences();
                if (loginType.equals(Constants.LOGIN_FB)) {
                    FirebaseAuth.getInstance().signOut();
                    LoginManager.getInstance().logOut();
                    loginIntent();
                } else if (loginType.equals(Constants.LOGIN_GOOGLE)) {
                    mAuth.signOut();
                    googlesignOut();
                } else {
                    mAuth.signOut();
                    loginIntent();
                }
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    private void googlesignOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                Toast.makeText(ChatActivity.this, "Signed out!", Toast.LENGTH_LONG).show();
                ChatActivity.this.loginIntent();
//                ChatActivity.this.finish();
            }
        });
    }

    private void loginIntent() {
        Intent intent1 = new Intent(ChatActivity.this, MainActivity.class);
        startActivity(intent1);
    }
    //action istenere


    //interface implementations
    @Override
    public void onChatFragInteractionListener(String str) {

    }

    @Override
    public void onContactFragmentInteraction(Uri uri) {

    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }


    //across methods
    public void createNewMessage(String userId) {
        Log.d("demo",
                "########New message requested... with " + userId);
        DatabaseReference ref = mMessageRef.push();
        String msgId = ref.getKey();
        Message msg = new Message();
        msg.setMessageRead(false);
        msg.setSender("system");
        msg.setTime(System.currentTimeMillis());
        msg.setMessageText("");
        msg.setMsgId(msgId);
        ref.setValue(msg);
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        finish();
    }

    public void storeTypeofAuthPrefrences(String converted) {
        Log.d("SHared", converted);
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_WORD, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.SHARED_PREF_AUTH_WORD, converted);
        editor.commit();

    }

    public String getSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_WORD, MODE_PRIVATE);
        String edited = sharedPreferences.getString("Users", "No Users Available");
        return edited;
    }

    public String getTypeOfAuthSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_WORD, MODE_PRIVATE);
        String edited = sharedPreferences.getString(Constants.SHARED_PREF_AUTH_WORD, "nil");
        return edited;
    }

    private void toast(String s) {
        Toast.makeText(ChatActivity.this, s, Toast.LENGTH_SHORT).show();
    }


}
