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
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

public class EditProfileActivity extends AppCompatActivity {

    DatabaseReference firebase_ref = FirebaseDatabase.getInstance().getReference();
    User mUser;
    private static int RESULT_LOAD_IMAGE = 1;
    String currentUserEmail;
    String currentPass;
//    User mUser;
    private Uri storedImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mUser = (User) getIntent().getExtras().get("mUser");
        Log.i("info", "Email sent is: " + currentUserEmail);


        final EditText editText_firstname_profile = (EditText) findViewById(R.id.firstName_EditScreen);
        editText_firstname_profile.setText(mUser.getFirstname());

        final EditText editText_lastname_profile = (EditText) findViewById(R.id.lastname_EditScreen);
        editText_lastname_profile.setText(mUser.getLastname());

        final EditText editText_email_profile = (EditText) findViewById(R.id.username_EditScreen);
        editText_email_profile.setText(mUser.getEmailId());
        editText_email_profile.setEnabled(false);

        final EditText editText_password_profile = (EditText) findViewById(R.id.password_EditScreen);
        editText_password_profile.setText(mUser.getPassword());

        final EditText editText_confpassword_profile = (EditText) findViewById(R.id.confirmpassword_EditScreen);
        editText_confpassword_profile.setText(mUser.getPassword());

        Button button_update_profile = (Button) findViewById(R.id.updateButton_EditScreen);
        button_update_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newFirstName = editText_firstname_profile.getText().toString();
                String newLasttName = editText_lastname_profile.getText().toString();

                String newPass = editText_password_profile.getText().toString();
                String confPass = editText_confpassword_profile.getText().toString();

                if(newFirstName.equals("") || newPass.equals("") || confPass.equals("")){
                    Toast.makeText(EditProfileActivity.this,"Please enter the complete details.",Toast.LENGTH_LONG).show();
                }
                else if(!(newPass.equals(confPass)))
                {
                    Toast.makeText(EditProfileActivity.this, "Passwords do not match!!", Toast.LENGTH_SHORT).show();
                }
                else{
                    DatabaseReference ref = firebase_ref.child(Constants.T_REF_USERS).child(mUser.getUserId());
                    if(selectedImage!=null)
                        storeImage(selectedImage);
                    ref.child("firstname").setValue(newFirstName);
                    ref.child("lastname").setValue(newLasttName);
                    ref.child("password").setValue(newPass);
                    if(storedImageUrl!=null)
                        ref.child("profileImage").setValue(storedImageUrl.toString());
                    finish();
                }
            }
        });

        Button button_cancel_profile = (Button) findViewById(R.id.cancelButton_EditScreen);
        button_cancel_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
    private void setLayout() {
        // TextView textView_firstname_profile = (TextView) findViewById(R.id.textView_fullname_editprofile);
        //textView_fullname_profile.setText(mUser.getFullName());



    }


    Uri selectedImage = null;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
            if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data!=null){
                selectedImage = data.getData();
                setProfImageViewPic(selectedImage);
            }
            else{
                Toast.makeText(this, "No image selected",Toast.LENGTH_LONG).show();
            }
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this,"Something went wrong.", Toast.LENGTH_LONG).show();
        }
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
                        storedImageUrl = taskSnapshot.getDownloadUrl();

                        Log.d("demo", "The download URL is " + storedImageUrl.toString());
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
        ImageView imageView_profile = (ImageView) findViewById(R.id.profileimage_EditActivity);
        imageView_profile.setImageURI(selectedImage);
    }

}
