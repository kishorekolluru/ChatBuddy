package com.example.raviteja.chat_buddy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.example.raviteja.chat_buddy.ContactListFragment.INTENT_RECIP_INFO;
import static com.example.raviteja.chat_buddy.ContactListFragment.INTENT_SENDER_INFO;


public class ChatFragment extends Fragment {

    private OnChatFragmentInteractionListener mListener;
    ListView mListView;
    public ChatFragment() {
        // Required empty public constructor
    }

List<User> items = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d("demo", "ONCREATE in CHAT FRAG#########");

        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        mListView = (ListView) v.findViewById(R.id.chat_list_view);
        mCOntactAdapter = new ContactListAdapter(getActivity(), R.layout.contact_list_item, items, (ChatActivity) getActivity());
        mListView.setAdapter(mCOntactAdapter);
        addActions();
        return v;
    }

    private void addActions() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent inte = new Intent(getActivity(), MessageActivity.class);
                inte.putExtra(INTENT_RECIP_INFO, (User) parent.getItemAtPosition(position));
                ChatActivity activity= (ChatActivity)getActivity();
                inte.putExtra(INTENT_SENDER_INFO, activity.mUser);
                startActivity(inte);
            }
        });
    }
    public String getTypeOfAuthSharedPreferences() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.SHARED_PREF_WORD, MODE_PRIVATE);
        String edited = sharedPreferences.getString(Constants.SHARED_PREF_AUTH_WORD, "nil");
        return edited;
    }
    ValueEventListener mValLisener;
    ContactListAdapter mCOntactAdapter;
    DatabaseReference mMessagesRef, mContactsRef = FirebaseDatabase.getInstance().getReference().child(Constants.T_REF_USERS);
    @Override
    public void onStart() {
        super.onStart();
        mValLisener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<User> userList = new ArrayList<>();
                Object val = dataSnapshot.getValue();
                Log.d("demo", "Chidlren count " + dataSnapshot.getChildrenCount());
                String key = dataSnapshot.getKey();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
//                        HashMap<String, Object> mainMap = (HashMap<String, Object>) snap.getValue();
                        User user = snap.getValue(User.class);
                        /*String fname = (String) mainMap.get("firstname");
                        String lname = (String) mainMap.get("lastname");
                        String email = (String) mainMap.get("emailId");
                        String gender = (String) mainMap.get("gender");
                        String userId = (String) mainMap.get("userId");
                        String profileImage = (String) mainMap.get("profileImage");*/
//                        User user = Util.createUser(fname, lname, email, gender, profileImage, userId);
                        ChatActivity activity= (ChatActivity)getActivity();
                        if(!Util.loginType.equals(Constants.LOGIN_GOOGLE) && !user.getUserId().equals(activity.mUser.getUserId()))
                            userList.add(user);
                    }
//                    mItemiList = (ArrayList<User>) CollectionUtils.union(mItemiList, userList);//todo correctly  add elements
                    mCOntactAdapter.clear();
                    mCOntactAdapter.addAll(userList);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mContactsRef.addValueEventListener(mValLisener);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChatFragmentInteractionListener) {
            mListener = (OnChatFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnChatFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnChatFragmentInteractionListener {
        // TODO: Update argument type and name
        void onChatFragInteractionListener(String str);
    }
}
