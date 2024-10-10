package com.tapsss;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager2.widget.ViewPager2;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import org.osmdroid.config.Configuration;
import java.io.File;
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); setContentView(R.layout.activity_main); //force dark theme even on light themed devices
        setContentView(R.layout.activity_main);

        ViewPager2 viewPager = findViewById(R.id.view_pager);

        com.tapsss.ui.ViewPagerAdapter adapter = new com.tapsss.ui.ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        viewPager.setOffscreenPageLimit(4); // Adjust the number based on your requirement




        File osmdroidBasePath = new File(getCacheDir().getAbsolutePath(), "osmdroid");
        File osmdroidTileCache = new File(osmdroidBasePath, "tiles");
        Configuration.getInstance().setOsmdroidBasePath(osmdroidBasePath);
        Configuration.getInstance().setOsmdroidTileCache(osmdroidTileCache);


    }
}