package com.ayansh.hindijokes.android;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * Created by I041474 on 07-Jan-16.
 */
public class PostPagerAdapter extends FragmentStatePagerAdapter {

    private int size;

    public PostPagerAdapter(FragmentManager fm, int size) {
        super(fm);
        this.size = size;
    }


    @Override
    public int getCount() {
        return size;
    }

    void setNewSize(int size){
        this.size = size;
    }

    @Override
    public Fragment getItem(int PostIndex) {

        // Post Id is actually position

        Fragment fragment = new PostDetailFragment();
        Bundle arguments = new Bundle();
        arguments.putInt("PostIndex", PostIndex);
        fragment.setArguments(arguments);

        return fragment;
    }

}