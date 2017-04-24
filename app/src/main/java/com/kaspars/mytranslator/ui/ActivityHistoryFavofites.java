package com.kaspars.mytranslator.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.kaspars.mytranslator.R;
import com.kaspars.mytranslator.data.HistoryItem;
import com.kaspars.mytranslator.ui.adapter.MyPagerAdapter;
import com.kaspars.mytranslator.ui.fragment.FragmentHistory;

public class ActivityHistoryFavofites extends AppCompatActivity implements FragmentHistory.OnHistoryItemClickedListener {
   MyPagerAdapter pagerAdapter;
   ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_favofites);
        Toolbar toolbar = (Toolbar)findViewById(R.id.history_toolbar);
        if (toolbar != null) setSupportActionBar(toolbar);
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        viewPager = (ViewPager)findViewById(R.id.viewPager);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        Bundle args1 = new Bundle();
        args1.putString("add", "favorites");
        Fragment myFavoritesFragment = new FragmentHistory();
        myFavoritesFragment.setArguments(args1);
        Bundle args0 = new Bundle();
        args0.putString("add", "history");
        Fragment myHistoryFragment = new FragmentHistory();
        myHistoryFragment.setArguments(args0);
        pagerAdapter.addFragment(myHistoryFragment, "History");
        pagerAdapter.addFragment(myFavoritesFragment, "Favorites");
        viewPager.setAdapter(pagerAdapter);
    }
    @Override
    public void onHistoryItemClicked(HistoryItem item) {
        Intent intent = new Intent();
        intent.putExtra(ActivityTranslator.ORIGINAL_TEXT_EXTRA, item.getOriginal());
        setResult(RESULT_OK, intent);
        finish();
    }
}
