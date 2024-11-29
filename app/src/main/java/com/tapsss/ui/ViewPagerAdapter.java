package com.tapsss.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new Fragment_black();
            case 1:
                return new SettingsFragment();
            case 2:
                return new HomeFragment();


            case 3 :
                return  new RecordsFragment();
        }
        return  new Fragment_black();
    }

    @Override
    public int getItemCount() {
        return 4; // Number of fragments
    }
}