package com.example.raviteja.chat_buddy;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnContactFragInteractionListener} interface
 * to handle interaction events.
 */
public class ContactListFragment extends Fragment {

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mContactsRef = mRootRef.child(Constants.T_REF_USERS);
    private OnContactFragInteractionListener mActivityListener;
    ValueEventListener mValLisener;

    ListView mListView;
    ContactListAdapter mCOntactAdapter;
    ArrayList<User> mItemiList = new ArrayList<>();
    public ContactListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("demo", "ONCREATEVIEW of CONTACT FRAG");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contact_list, container, false);
        mListView = (ListView) view.findViewById(R.id.contact_list_view);

        mCOntactAdapter = new ContactListAdapter(getActivity(), R.layout.contact_list_item, mItemiList, (ChatActivity) getActivity());
        mCOntactAdapter.setNotifyOnChange(true);
        mListView.setAdapter(mCOntactAdapter);
        addActions();
        return view;
    }

    public static final String INTENT_RECIP_INFO = "contactInfo";
    public static final String INTENT_SENDER_INFO = "sendDerUser";
    private void addActions() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent inte = new Intent(getActivity(), ContactInfoActivity.class);
                inte.putExtra(INTENT_RECIP_INFO, (User) parent.getItemAtPosition(position));
                startActivity(inte);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("demo", "ONSTOP for CONTACT FRAG#####");
        mContactsRef.removeEventListener(mValLisener);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("demo", "ONSTART for CONTACT FRAG#####");
        mValLisener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<User> userList = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        HashMap<String, Object> mainMap = (HashMap<String, Object>) snap.getValue();
                        String fname = (String) mainMap.get("firstname");
                        String lname = (String) mainMap.get("lastname");
                        String email = (String) mainMap.get("emailId");
                        String gender = (String) mainMap.get("gender");
                        String userId = (String) mainMap.get("userId");
                        String profileImage = (String) mainMap.get("profileImage");
                        User user = Util.createUser(fname, lname, email, gender, profileImage, userId);
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
        if (context instanceof OnContactFragInteractionListener) {
            mActivityListener = (OnContactFragInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnChatFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivityListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnContactFragInteractionListener {
        // TODO: Update argument type and name
        void onContactFragmentInteraction(Uri uri);
    }
}
