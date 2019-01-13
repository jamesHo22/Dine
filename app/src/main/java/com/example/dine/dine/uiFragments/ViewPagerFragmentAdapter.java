package com.example.dine.dine.uiFragments;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.example.dine.dine.R;

public class ViewPagerFragmentAdapter extends FragmentPagerAdapter {
    private Context context;
    public ViewPagerFragmentAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;

    }

    private Fragment mCurrentFragment;

    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (getCurrentFragment() != object) {
            mCurrentFragment = ((Fragment) object);
        }
        super.setPrimaryItem(container, position, object);
    }


    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new RecommendationFragment();
            case 1:
                return new MenuFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.tab_recommendations);
            case 1:
                return context.getString(R.string.tab_menu);
            default:
                return null;
        }
    }
}
