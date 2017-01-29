package com.example.raviteja.chat_buddy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

import static android.R.attr.data;

public class SignupActivity extends AppCompatActivity {
    private static final String T_EMAIL_ID = "emailId";
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseAuth.AuthStateListener mAuthListener;
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    String firstname;
    String lastname;
    private static int RESULT_LOAD_IMAGE = 1;
    String username;
    String password;
    String confpassword;
    String gender;
    MainActivity mActivity = new MainActivity();
    ImageView imageView_profile_profile;


    EditText firstnameET;
    EditText lastnameET;
    EditText usernameET;
    EditText passwordET;
    EditText confpasswordET;
    RadioGroup genderRG;
    private Uri selectedImage = Uri.parse("");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Button signup = (Button) findViewById(R.id.updateButton_EditScreen);
        Button cancel = (Button) findViewById(R.id.cancelButton_signupScreen);
        imageView_profile_profile = (ImageView) findViewById(R.id.profileimage_EditActivity);
        // byte[] decodedString = Base64.decode(mUser.getImage(), Base64.DEFAULT);
        //Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        //imageView_profile_profile.setImageBitmap(decodedByte);
        imageView_profile_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("demo", "inside signup Onclick");
                firstnameET = (EditText) findViewById(R.id.firstName_EditScreen);
                lastnameET = (EditText) findViewById(R.id.lastname_EditScreen);
                usernameET = (EditText) findViewById(R.id.username_EditScreen);
                passwordET = (EditText) findViewById(R.id.password_EditScreen);
                confpasswordET = (EditText) findViewById(R.id.confirmpassword_EditScreen);
                genderRG = (RadioGroup) findViewById(R.id.GenderRG);

                firstname = firstnameET.getText().toString();
                lastname = lastnameET.getText().toString();
                username = usernameET.getText().toString();
                password = passwordET.getText().toString();
                confpassword = confpasswordET.getText().toString();
                gender = new String();
                genderRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        RadioButton radiobutton = (RadioButton) radioGroup.findViewById(i);
                        gender = (String) radiobutton.getText();
                    }
                });
                if (firstname.isEmpty() || lastname.isEmpty() || username.isEmpty() || password.isEmpty() || confpassword.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "All fields are Mandatory!!!", Toast.LENGTH_SHORT).show();
                } else if (!(password.equals(confpassword))) {
                    Toast.makeText(SignupActivity.this, "Passwords donot match!!", Toast.LENGTH_SHORT).show();
                } else {
                    checkAndCreateUser();
//                        createuser();
                }

            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    DatabaseReference mUsersRef = FirebaseDatabase.getInstance().getReference(Constants.T_REF_USERS);

    private void checkAndCreateUser() {
        mUsersRef.orderByChild(T_EMAIL_ID).equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            createuser();
                        } else {
                            toast("Email " + username + " already exists. Please use another");
                            usernameET.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("demo", "Something went wrong with the db request " + databaseError.getDetails() + databaseError.getMessage());
                        toast("Something went wrong with the database access");
                    }
                });
    }


    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

    public void createuser() {
        Log.d("demo", "inside create mUser" + username + " " + password);
        mAuth.createUserWithEmailAndPassword(username, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String[] name = username.split("@");
                    Log.d("demo", "Creation of mUser successful");
                    User user = createUserObj();
                    Log.d("mUser object", user.toString());
                    DatabaseReference ref = mDatabase.child(Constants.T_REF_USERS).push();
                    String userId = ref.getKey();
                    user.setUserId(userId);
                    ref.setValue(user);

                    Log.d("demo", "createUserWithEmail:onComplete:" + task.isSuccessful());
                    toast("SignedUp Successfully!!!!!");


//                    DatabaseReference myRef = mRootRef.child("message/"+userId);
//                    myRef.setValue("Hello, User234!");
                    mActivity.signIn(username, password, SignupActivity.this);
                    Intent intent = new Intent(SignupActivity.this, ChatActivity.class);
                    intent.putExtra("CurrentUserEmail", username);
                    startActivity(intent);
                }
                if (!task.isSuccessful()) {
                    toast("Authentication failed!!!!!");
                    task.getException().printStackTrace();
                }

            }
        });
    }

    private User createUserObj() {
        User user = new User();
        user.setFirstname(firstname);
        user.setLastname(lastname);
        int id = genderRG.getCheckedRadioButtonId();
        RadioButton btn = (RadioButton) genderRG.findViewById(id);
        String gender  = btn.getText().toString();
        user.setGender(gender);
        user.setPassword(password);
        user.setEmailId(username);
        user.setProfileImage(selectedImage.toString());
        return user;
    }

    StorageReference mountainRef = FirebaseStorage.getInstance().getReference("images");

    private void storeImage(final Uri imageUri) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        } catch (IOException e) {
            Log.e("demo", "Exception", e);
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        final byte[] imageBytes = baos.toByteArray();
        mountainRef.child("image_" + new Date().getTime() + "_" + new Random().nextLong())
                .putBytes(imageBytes, new StorageMetadata.Builder().setContentType("image/jpg").build())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        SignupActivity.this.setProfImageViewPic(imageUri);
                        selectedImage = taskSnapshot.getDownloadUrl();
                        Log.d("demo", "The download URL is " + selectedImage.toString());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("demo", "The upload failed :", e);
                    }
                });
    }

    private void setProfImageViewPic(Uri imageUri) {
        imageView_profile_profile.setImageURI(imageUri);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
                storeImage(data.getData());
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Something went wrong.", Toast.LENGTH_LONG).show();
        }
    }

    private void toast(String s) {
        Toast.makeText(SignupActivity.this, s, Toast.LENGTH_LONG).show();
    }
}
