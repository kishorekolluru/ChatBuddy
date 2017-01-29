package com.example.raviteja.chat_buddy;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by kishorekolluru on 11/22/16.
 */

public class ContactListAdapter extends ArrayAdapter<User> {


    private List<User> items;
    private ChatActivity activity;
    public ContactListAdapter(Context context, int resource, List<User> objects, ChatActivity activity) {
        super(context, resource, objects);
        this.items = objects;
        this.activity = activity;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            convertView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.contact_list_item, parent, false);
        }
        User user = getItem(position);
        TextView tv = (TextView) convertView.findViewById(R.id.contact_list_textview);
        ImageView iv = (ImageView) convertView.findViewById(R.id.contact_list_image);

        tv.setText(user.getFirstname());
        if(user.getProfileImage()!=null && !user.getProfileImage().equals(""))
            Picasso.with(activity).load(user.getProfileImage()).into(iv);
        else
            iv.setImageResource(R.drawable.images);
        return convertView;
    }

}
