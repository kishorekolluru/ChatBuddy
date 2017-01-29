package com.example.raviteja.chat_buddy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MessageActivity extends AppCompatActivity {
    private static final int SELECT_PICTURE_REQUEST = 100;
    public static User recipientUser;
    public static User senderUser;
    ListView mListView;
    MessageListAdapter mAdapter;
    List<Message> items = new ArrayList<>();

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mMessageRef = mRootRef.child(Constants.T_REF_MSGS);
    private Uri uri_global;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Bundle data = getIntent().getExtras();
        if (data.containsKey(ContactListFragment.INTENT_RECIP_INFO)) {
            hook();
            recipientUser = (User) data.get(ContactListFragment.INTENT_RECIP_INFO);
            senderUser = (User) data.get(ContactListFragment.INTENT_SENDER_INFO);
            if(recipientUser.getFirstname()!=null)
                setTitle(recipientUser.getFirstname());
            else
                setTitle("Chat Buddy");

            mAdapter = new MessageListAdapter(this, R.layout.message_list_item, items);
            mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            mListView.setAdapter(mAdapter);
            mAdapter.sort(new MessageComparator());
            mAdapter.setNotifyOnChange(true);
        }
    }

    ImageView sendImage, selectGallery;
    EditText msgContentEt;

    private void hook() {
        mListView = (ListView) findViewById(R.id.message_list_view);
        sendImage = (ImageView) findViewById(R.id.message_send_icon);
        selectGallery = (ImageView) findViewById(R.id.image_select);
        msgContentEt = (EditText) findViewById(R.id.message_message_text);
        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference ref = mMessageRef.push();
                String msgId = ref.getKey();
                Message msg = makeMessageTextObject(msgId);
                ref.setValue(msg);
                msgContentEt.setText("");
            }
        });
        selectGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(v);
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("demo", "On Child DELETED");
                Message msg=items.get(position);
                if(senderUser.getUserId().equals(msg.getSender()))
                {
                    String str = msg.getMsgId();
                    mMessageRef.child(str).setValue(null);
                }
                return true;
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("demo","on ITEM CLICK fired");
                Message user=mAdapter.getItem(position);
                if(!user.isMessageRead() && user.getReceiver().equals(senderUser.getUserId())) {
                    user.setMessageRead(true);
                    String str = user.getMsgId();
                    mMessageRef.child(str).setValue(user);
                }
            }
        });

    }

    ChildEventListener mChildListener;

    @Override
    protected void onStop() {
        super.onStop();
        mMessageRef.removeEventListener(mChildListener);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("demo","Child ADDED");
                Message tempMsgs = getRelevantMsg(dataSnapshot);
                addUniqueToAdapter(tempMsgs);
                mAdapter.sort(new MessageComparator());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("demo","Child CHANGED");
                Message tempMsgs = getRelevantMsg(dataSnapshot);
//                for(int i =0; i< mAdapter.getCount();i++) {
//                    Message msg = mAdapter.getItem(i);
                for(int i =0; i< items.size();i++) {
                    Message msg = items.get(i);
                    if (msg.getMsgId().equals(tempMsgs.getMsgId())) {
                        mAdapter.remove(msg);
                        mAdapter.add(tempMsgs);
                        break;
                    }
                }
                mAdapter.notifyDataSetChanged();
                mAdapter.sort(new MessageComparator());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d("demo","Child DELETED");
                Message tempMsgs = getRelevantMsg(dataSnapshot);
                for(int i =0; i< mAdapter.getCount();i++){
                    Message msg = mAdapter.getItem(i);

                    if(msg.getMsgId().equals(tempMsgs.getMsgId())){
                        mAdapter.remove(msg);
                    }
                }
                mAdapter.notifyDataSetChanged();
                mAdapter.sort(new MessageComparator());

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("demo", "!!!!!!!!Something went wrong with MESSAGE listening...!!!!!!!");
            }
        };
        mMessageRef.addChildEventListener(mChildListener);
    }

    private void addUniqueToAdapter(Message tempMsgs) {
        List<Message> msgfs = new ArrayList<>();

        if(tempMsgs!=null){
            if (mAdapter.getCount()<1) {
                mAdapter.addAll(tempMsgs);
            } else {
                for (int i = 0; i < mAdapter.getCount(); i++) {
                    Message msg = mAdapter.getItem(i);
                    if (msg!=null && tempMsgs.getMsgId().equals(msg.getMsgId())) {
                        break;
                    }
                    if (i == mAdapter.getCount() - 1) {
                        msgfs.add(tempMsgs);
                    }
                }
                mAdapter.addAll(msgfs);
            }
            mAdapter.sort(new MessageComparator());
        }

    }

    public void chooseImage(View v) {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, SELECT_PICTURE_REQUEST);
        Log.d("sel", "Selected1");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("data", data.getDataString());
        Log.d("reg", String.valueOf(requestCode));
        Log.d("sel", String.valueOf(resultCode));
        if (requestCode == SELECT_PICTURE_REQUEST) {
            if (resultCode == RESULT_OK) {
                uri_global = data.getData();
                storeImage(uri_global);
                Log.d("uri", uri_global.toString());

            }

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
                        Uri selectedImageUri = taskSnapshot.getDownloadUrl();
                        DatabaseReference ref = mMessageRef.push();
                        String msgId = ref.getKey();
                        Message msg = makeMessagePicObject(msgId, selectedImageUri.toString());
                        ref.setValue(msg);
                        Log.d("demo", "The download URL is " + selectedImageUri.toString());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("demo", "The upload failed :", e);
                    }
                });
    }

    private Message makeMessagePicObject(String msgId, String imgUrl) {
        Message msg = new Message();
        msg.setMessageText("");
        msg.setMsgId(msgId);
        msg.setTime(System.currentTimeMillis());
        msg.setImageUrl(imgUrl);
        msg.setReceiver(recipientUser.getUserId());
        msg.setSender(senderUser.getUserId());
        msg.setMessageRead(false);
        return msg;
    }

    private Message getRelevantMsg(DataSnapshot dataSnapshot) {
        List<Message> tempMsgs = new ArrayList<>();
        HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
        Message msg = makeMessageObjectFromMap(map);
        if ((msg.getSender().equals(senderUser.getUserId())
                && msg.getReceiver().equals(recipientUser.getUserId())) ||
                (msg.getSender().equals(recipientUser.getUserId())
                        && msg.getReceiver().equals(senderUser.getUserId()))) {
            return msg;
        }

        return null;
    }

    private Message makeMessageTextObject(String msgId) {
        Message msg = new Message();
        msg.setMessageText(msgContentEt.getText().toString().trim());
        msg.setMsgId(msgId);
        msg.setTime(System.currentTimeMillis());
        msg.setImageUrl("");
        msg.setReceiver(recipientUser.getUserId());
        msg.setSender(senderUser.getUserId());
        msg.setMessageRead(false);
        return msg;
    }

    private Message makeMessageObjectFromMap(HashMap<String, Object> map) {
        Message msg = new Message();
        msg.setTime((Long) map.get("time"));
        msg.setMessageRead((Boolean) map.get("messageRead"));
        msg.setSender((String) map.get("sender"));
        msg.setReceiver((String) map.get("receiver"));
        msg.setMessageText((String) map.get("messageText"));
        msg.setMsgId((String) map.get("msgId"));
        msg.setImageUrl((String) map.get("imageUrl"));
        return msg;
    }
}
