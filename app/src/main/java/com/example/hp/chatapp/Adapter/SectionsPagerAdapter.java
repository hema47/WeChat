package com.example.hp.chatapp.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.hp.chatapp.Fragment.ChatsFragment;
import com.example.hp.chatapp.Fragment.FriendsFragment;
import com.example.hp.chatapp.Fragment.RequestFragment;

/**
 * Created by HP on 05-04-2018.
 */

public class SectionsPagerAdapter extends FragmentPagerAdapter{
    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
            RequestFragment  requestFragment = new RequestFragment();
            return requestFragment;

            case 1:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3; //since three tabs
    }
// To set title of tabs
    public CharSequence getPageTitle(int position){
        switch (position){
            case 0:
                return "REQUESTS";
            case 1:
                return "CHATS";
            case 2:
                return "FRIENDS";
            default:
                return null;
        }
    }
}
