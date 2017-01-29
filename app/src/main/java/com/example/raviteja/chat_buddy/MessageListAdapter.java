package com.example.raviteja.chat_buddy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;

import static android.os.Build.VERSION_CODES.M;
import static junit.runner.Version.id;

/**
 * Created by kishorekolluru on 11/22/16.
 */

public class MessageListAdapter extends ArrayAdapter<Message> {

    MessageActivity activity;
    public MessageListAdapter(Context context, int resource, List<Message> objects) {
        super(context, resource, objects);
        activity = (MessageActivity) context;
    }

    public static PrettyTime prettyTime = new PrettyTime();

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            convertView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.message_list_item, parent, false);
        }
        TextView mess = (TextView) convertView.findViewById(R.id.message_msg_text);
        TextView time = (TextView) convertView.findViewById(R.id.message_time_text);
        TextView user = (TextView) convertView.findViewById(R.id.message_user_name);
        ImageView iv = (ImageView) convertView.findViewById(R.id.message_msg_image);
        Message msg = getItem(position);
        adjustUi(iv, mess, msg);
        adjustAlignments(convertView, position);
        setColor(msg, convertView);
        mess.setText(msg.getMessageText());
        time.setText(prettyTime.format(new Date(msg.getTime())));
        User userr = new User();
        if(msg.getSender().equals(activity.recipientUser.getUserId())){
            userr = activity.recipientUser;
            user.setText(new StringBuilder().append(userr.getFirstname())
                    .append(" ").append(userr.getLastname()).toString());
        }
        else{
            user.setText("You");
        }

        return convertView;
    }

    private void setColor(Message msgDetail, View convertView) {
        LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.interior_lin_lay);
        if(!msgDetail.isMessageRead() && msgDetail.getReceiver().equals(MessageActivity.senderUser.getUserId())) {
            layout.setBackgroundColor(Color.parseColor("#FFD54F"));
        }
        else{
            layout.setBackgroundResource(R.color.com_facebook_button_like_background_color_selected);

        }
    }

    private void adjustAlignments(View convertView, int position) {
        boolean isSender = true;
        if(getItem(position).getSender().equals(MessageActivity.recipientUser.getUserId())){
            isSender= false;
        }
        LinearLayout.LayoutParams params;
//        Object obj = convertView.getLayoutParams();
        LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.interior_lin_lay);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        if(isSender){
            params.setMarginStart(150);
            setGravity(convertView, Gravity.END);
        }else{
            params.setMarginEnd(150);
            setGravity(convertView, Gravity.START);
        }
        params.setMargins(0, 15, 0,0);
            layout.setLayoutParams(params);


    }

    private void setGravity(View convertView, int end) {

        TextView mess = (TextView) convertView.findViewById(R.id.message_msg_text);
        mess.setLayoutParams(getNewLayoutParams(mess, end));
        TextView time = (TextView) convertView.findViewById(R.id.message_time_text);
        time.setLayoutParams(getNewLayoutParams(time, end));
        TextView user = (TextView) convertView.findViewById(R.id.message_user_name);
        user.setLayoutParams(getNewLayoutParams(time, end));
        ImageView iv = (ImageView) convertView.findViewById(R.id.message_msg_image);
        iv.setLayoutParams(getNewLayoutParams(iv, end));
    }

    private ViewGroup.LayoutParams getNewLayoutParams(View mess, int end) {
        LinearLayout.LayoutParams params;
        params = new LinearLayout.LayoutParams(mess.getLayoutParams());
        params.gravity=end;
        params.setMarginStart(7);
        params.setMarginEnd(7);
        return params;
    }

    private void adjustUi(ImageView iv, View msgARea, Message msg) {
        if(msg.getImageUrl()==null || "".equals(msg.getImageUrl())){
           iv.setVisibility(View.GONE);
            msgARea.setVisibility(View.VISIBLE);
        }else{
            iv.setVisibility(View.VISIBLE);
            Picasso.with(activity).load(msg.getImageUrl()).into(iv);
            msgARea.setVisibility(View.GONE);
        }
    }
}
