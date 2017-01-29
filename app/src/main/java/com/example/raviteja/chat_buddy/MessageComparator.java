package com.example.raviteja.chat_buddy;

import java.util.Comparator;

/**
 * Created by kishorekolluru on 11/22/16.
 */

public class MessageComparator implements Comparator<Message>{

    @Override
    public int compare(Message lhs, Message rhs) {
        if(lhs.getTime()< rhs.getTime())
            return -1;
        else if(lhs.getTime()> rhs.getTime())
            return 1;
        else
            return 0;
    }
}
