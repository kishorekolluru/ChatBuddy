package com.example.raviteja.chat_buddy;

import static com.example.raviteja.chat_buddy.MainActivity.loginType;

/**
 * Created by kishorekolluru on 11/22/16.
 */

public class Util {
    public static String loginType = "";
    public static User createUser( String fname, String lname, String emailId, String gender, String profileImage, String userId) {
        User user = new User();
        user.setProfileImage(profileImage);
        user.setEmailId(emailId);
        user.setFirstname(fname);
        user.setLastname(lname);
        user.setGender(gender);
        user.setUserId(userId);
        return user;
    }
}
