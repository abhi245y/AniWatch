package com.abhi245y.aniwatch.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.abhi245y.aniwatch.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Fragment active;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        changeFragment(new RecentReleasedFragment(), RecentReleasedFragment.class
                .getSimpleName());
        bottomNavigationView.setSelectedItemId(R.id.home_lay);
        ActionBar actionBar = getSupportActionBar();

        assert actionBar != null;
        actionBar.setTitle("Recent Releases");
        actionBar.setDisplayShowHomeEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.custom_action_bar);
        actionBar.setDisplayUseLogoEnabled(true);
        View view = getSupportActionBar().getCustomView();
        TextView fragName = view.findViewById(R.id.fragment_name);
        fragName.setText("Recent Releases");

        bottomNavigationView.setOnItemSelectedListener(item -> {

            if(item.getItemId() == R.id.home_lay){
                changeFragment(new RecentReleasedFragment(), RecentReleasedFragment.class
                        .getSimpleName());
                fragName.setText("Recent Releases");
                return true;

            }else if(item.getItemId() == R.id.search_lay){
                changeFragment(new SearchFragment(), SearchFragment.class
                        .getSimpleName());
                fragName.setText("Search");
                return true;

            }else if(item.getItemId() == R.id.watch_history_lay){
                changeFragment(new HistoryFragment(), HistoryFragment.class
                        .getSimpleName());
                fragName.setText("Watch History");
                return true;

            }else if(item.getItemId() == R.id.my_list_lay){
                changeFragment(new MyListFragment(), MyListFragment.class
                        .getSimpleName());
                fragName.setText("My List");
                return true;
            }

            return false;
        });




    }

    public void changeFragment(Fragment fragment, String tagFragmentName) {

        FragmentManager mFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        Fragment currentFragment = mFragmentManager.getPrimaryNavigationFragment();
        if (currentFragment != null) {
            fragmentTransaction.hide(currentFragment);
        }

        Fragment fragmentTemp = mFragmentManager.findFragmentByTag(tagFragmentName);
        if (fragmentTemp == null) {
            fragmentTemp = fragment;
            fragmentTransaction.add(R.id.container, fragmentTemp, tagFragmentName);
        } else {
            fragmentTransaction.show(fragmentTemp);
        }

        fragmentTransaction.setPrimaryNavigationFragment(fragmentTemp);
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.commitNowAllowingStateLoss();
    }
}