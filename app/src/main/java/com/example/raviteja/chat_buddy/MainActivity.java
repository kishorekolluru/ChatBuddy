package com.example.raviteja.chat_buddy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseUser mUser;
    DatabaseReference firebase_ref;
    CallbackManager callbackManager;
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mUserRef = mDatabase.child(Constants.T_REF_USERS);
    GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 101;

    Button signup, login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_main);
        pdialog = new ProgressDialog(this);
        pdialog.setIndeterminate(true);
        pdialog.setMessage("Logging in...");

        setTitle("Chat Buddy");
        setupNormalLogin();
        setupGoogleButton();
        setupFacebookLoginButton();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mUser = FirebaseAuth.getInstance().getCurrentUser();
                mUser = mAuth.getCurrentUser();
                if(Util.loginType!=null)
                    storeTypeofAuthPrefrences(Util.loginType);
                if (mUser != null) {
                    pdialog.dismiss();
                    if (Util.loginType.equals(Constants.LOGIN_FB) || Util.loginType.equals(Constants.LOGIN_GOOGLE)) {
                        loginWithProvider();
                        return;
                    }
                    // User is signed in
                    Log.d("demo", "onAuthStateChanged:signed_in:" + mUser.getUid() + mUser.getDisplayName());
                    Toast.makeText(MainActivity.this, "Logged In!!!", Toast.LENGTH_SHORT).show();
                    switchActivity(MainActivity.this);
                } else {
                    // User is signed out
                    pdialog.dismiss();
//                    toast("Not able to login");
                    Log.d("demo", "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
        else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("demo", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("demo", "signInWithGoogleCredential:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Log.w("demo", "signInWithCredential", task.getException());
                            Toast.makeText(MainActivity.this, "Google Authentication failed!",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }
    private void loginWithProvider() {
        String email = mUser.getEmail();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(Constants.T_REF_USERS);
        ref.orderByChild("emailId").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {//todo put proper check for existing cases
                            DatabaseReference userRef = mUserRef.push();
                            Log.d("demo datasnapshot", "is null adding values");
                            User user = new User();
                            user.setUserId(userRef.getKey());
                            String[] names= mUser.getDisplayName().split(" ");
                            user.setFirstname(names[0]);
                            user.setLastname(names[1]);
                            user.setPassword("");
                            user.setProfileImage(mUser.getPhotoUrl().toString());
                            user.setGender("Male");
                            user.setEmailId(mUser.getEmail());
                            userRef.setValue(user);
                            switchActivity(MainActivity.this);
                        } else {
                            toast("Welcome Back!");
                            switchActivity(MainActivity.this);
                            //email.setText("");
                            //dismissDialog();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("demo", "Something went wrong with the db request " + databaseError.getDetails() + databaseError.getMessage());
                        toast("Something went wrong with the database access");
                        // dismissDialog();
                    }
                });
    }

    private void setupNormalLogin() {
        signup = (Button) findViewById(R.id.signupButton_loginScreen);
        login = (Button) findViewById(R.id.loginButton_loginScreen);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.loginType = Constants.LOGIN_NORMAL;
                EditText username = (EditText) findViewById(R.id.username_loginScreen);
                EditText password = (EditText) findViewById(R.id.password_loginScreen);
                String user = username.getText().toString();
                String pwd = password.getText().toString();
                if(user==null || "".equals(user)){
                    toast("Enter valid login id");
                }else if(pwd==null ||pwd.equals("")){

                    toast("Enter valid password");
                }else{

                    signIn(user,pwd, MainActivity.this);
                }
                //switchActivity(MainActivity.this,mUser);

            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupFacebookLoginButton() {
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.connectWithFbButton);
        loginButton.setReadPermissions(Arrays.asList("email"));//todo gender

        loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        Log.d("demo", "facebook:onSuccess:" + loginResult + "token=   " + loginResult.getAccessToken().toString());
                        Util.loginType = Constants.LOGIN_FB;
                        handleFacebookAccessToken(loginResult.getAccessToken());

                    }

                    @Override
                    public void onCancel() {
                        // App code
                        Log.d("demo", "facebook:onCancel");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        Log.d("demo", "facebook:onError", exception);
                    }
                });
    }


    private void setupGoogleButton() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
// options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, (GoogleApiClient.OnConnectionFailedListener) this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
// Set the dimensions of the sign-in button.
        SignInButton signInGoogleButton = (SignInButton) findViewById(R.id.sign_in_googlebutton);
        signInGoogleButton.setSize(SignInButton.SIZE_STANDARD);
        signInGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    ProgressDialog pdialog;
    public void signIn(String email, String password, final Activity activity) {
pdialog.show();
        //showDialog();
        mAuth.signInWithEmailAndPassword(email, password).
                addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("demo", "signInWithEmail:onComplete:" + task.isSuccessful());
                        try {
                            if (task.isSuccessful()) {
                                Log.w("demo", "signInWithEmail success");
                            } else {
                                pdialog.dismiss();
                                toast(task.getException().getMessage());
                            }
                        } catch (Exception e) {
                            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("demo", "Exception in login :" + e.getMessage());
                        }

                    }
                });
    }

    private void switchActivity(Activity activity) {
        Log.d("demo", "In SwitchActivity");
        Intent intent = new Intent(activity, ChatActivity.class);
        intent.putExtra("CurrentUserEmail", mAuth.getCurrentUser().getEmail());
        startActivity(intent);
        finish();
    }

    public static String loginType = "";


    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("demo", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("demo", "signInWithCredential:onComplete:" + task.isSuccessful());

//                        switchActivity(MainActivity.this, mUser);
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w("demo", "signInWithCredential", task.getException());
                            Toast.makeText(MainActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();
                            LoginManager.getInstance().logOut();
                        }
                    }
                });
    }

    ;

    private void toast(String s) {
        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
