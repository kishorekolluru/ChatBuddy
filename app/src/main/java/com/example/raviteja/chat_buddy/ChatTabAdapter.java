package com.example.raviteja.chat_buddy;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

/**
 * Created by kishorekolluru on 11/21/16.
 */

public class ChatTabAdapter  extends FragmentStatePagerAdapter {

    public ChatTabAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Log.d("demo", "Item no is " + i);
        switch (i) {
            case 0:
                return new ChatFragment();
            case 1:
                return new ContactListFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(position==1){
            return "Contacts";
        }else if(position ==0){
            return "Chats";
        }
        return null;
    }
}
