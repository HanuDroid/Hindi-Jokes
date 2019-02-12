package com.ayansh.hindijokes.android;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ayansh.hanudroid.Application;
import com.ayansh.hanudroid.HanuFragmentInterface;
import com.ayansh.hanudroid.Post;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;

public class Main extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private Application app;
    private int postIndex;
    private PostPagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, "ca-app-pub-4571712644338430~1102881505");

        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.AllPosts);
        navigationView.setNavigationItemSelectedListener(this);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        if(savedInstanceState != null){
            postIndex = savedInstanceState.getInt("PostIndex");
        }
        else{
            postIndex = 0;
        }

        // Get Application Instance.
        app = Application.getApplicationInstance();
        app.setContext(this);

        // Start the Main Activity
        startMainScreen();

    }

    private void startMainScreen() {

        // Show Ad.
        Bundle extras = new Bundle();
        extras.putString("max_ad_content_rating", "G");

        AdRequest adRequest = new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("9F11CAC92EB404500CAA3F8B0BBA5277").build();

        AdView adView = (AdView) findViewById(R.id.adView);

        // Start loading the ad in the background.
        adView.loadAd(adRequest);

        // Request InterstitialAd
        MyInterstitialAd.getInterstitialAd(this);
        MyInterstitialAd.requestNewInterstitial();

        // Load Posts.
        Application.getApplicationInstance().getAllPosts();

        // Create view Pager
        viewPager = (ViewPager) findViewById(R.id.post_pager);

        viewPager.setClipToPadding(false);
        viewPager.setPageMargin(-50);

        pagerAdapter = new PostPagerAdapter(getSupportFragmentManager(),app.getPostList().size());
        viewPager.setAdapter(pagerAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;

            case R.id.Upload:
                Intent upload = new Intent(Main.this, CreateNewPost.class);
                Main.this.startActivity(upload);
                break;

        }

        return true;
    }

    @Override
    protected void onDestroy(){
        app.close();
        super.onDestroy();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        //menuItem.setChecked(true);
        mDrawerLayout.closeDrawers();

        switch (menuItem.getItemId()){

            case R.id.AllPosts:
                // Load Posts.
                Application.getApplicationInstance().getAllPosts();
                updateUI();
                break;

            case R.id.MyFavs:
                // Load Posts.
                Application.getApplicationInstance().getFavouritePosts();
                updateUI();
                break;

            case R.id.MemePosts:
                // Load Posts.
                Application.getApplicationInstance().loadPostByCategory("Meme");
                updateUI();
                break;

            case R.id.Help:
                Intent help = new Intent(Main.this, DisplayFile.class);
                help.putExtra("File", "help.html");
                help.putExtra("Title", "Help: ");
                Main.this.startActivity(help);
                break;

            case R.id.ShowEula:
                Intent eula = new Intent(Main.this, DisplayFile.class);
                eula.putExtra("File", "eula.html");
                eula.putExtra("Title", "Terms and Conditions: ");
                Main.this.startActivity(eula);
                break;

            case R.id.Settings:
                Intent settings = new Intent(Main.this, SettingsActivity.class);
                Main.this.startActivity(settings);
                break;

            case R.id.About:
                Intent info = new Intent(Main.this, DisplayFile.class);
                info.putExtra("File", "about.html");
                info.putExtra("Title", "About: ");
                Main.this.startActivity(info);
                break;

            case R.id.MyApps:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Ayansh+TechnoSoft+Pvt.+Ltd"));
                startActivity(browserIntent);
                break;
        }

        return true;
    }

    private void updateUI(){

        pagerAdapter.setNewSize(app.getPostList().size());
        pagerAdapter.notifyDataSetChanged();
        viewPager.setAdapter(pagerAdapter);
        if(app.getPostList().size() < 1){
            // Show warning
            Toast toast = Toast.makeText(this,"Posts for selected criteria not found",Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
        }
    }
}
