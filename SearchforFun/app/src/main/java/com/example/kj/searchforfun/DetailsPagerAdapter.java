package com.example.kj.searchforfun;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;


public class DetailsPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public DetailsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                InfoFragment infoFragment = InfoFragment.newInstance(DetailsActivity.getDetailsObject());
                return infoFragment;
            case 1:
                PhotosFragment photosFragment = PhotosFragment.newInstance(DetailsActivity.getDetailsObject());
                return photosFragment;
            case 2:
                MapFragment mapFragment = MapFragment.newInstance(DetailsActivity.getDetailsObject());
                return mapFragment;
            case 3:
                CommentsFragment commentsFragment = CommentsFragment.newInstance(DetailsActivity.getDetailsObject());
                return commentsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position){
        return mFragmentTitleList.get(position);
    }

    public void addFrag(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }
}
