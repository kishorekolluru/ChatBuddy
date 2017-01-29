package com.example.raviteja.chat_buddy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import static com.example.raviteja.chat_buddy.R.id.info_image_incon;

public class ContactInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_info);

        Bundle data = getIntent().getExtras();
        if (data.containsKey(ContactListFragment.INTENT_RECIP_INFO)) {
            User user = (User) data.get(ContactListFragment.INTENT_RECIP_INFO);
            setData(user);
        }
    }

    TextView fname, lname, gender, email;
    private void setData(User user) {
        fname = (TextView) findViewById(R.id.info_fname_edit);
        lname = (TextView) findViewById(R.id.info_lname_edit);
        gender = (TextView) findViewById(R.id.info_gender_edit);
        email = (TextView) findViewById(R.id.info_email_edit);

        fname.setText(user.getFirstname());
        lname.setText(user.getLastname());
        gender.setText(user.getGender());
        email.setText(user.getEmailId());
        ImageView iv = (ImageView) findViewById(R.id.info_image_incon);
        if(user.getProfileImage()!=null && !user.getProfileImage().equals(""))
            Picasso.with(this).load(user.getProfileImage()).into(iv);
        Button doin = (Button) findViewById(R.id.info_done_button);
        doin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
