package com.ayansh.hindijokes.android;

import org.varunverma.hanu.Application.Application;
import org.varunverma.hanu.Application.HanuFragmentInterface;
import org.varunverma.hanu.Application.Post;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class Main extends AppCompatActivity implements PostListFragment.Callbacks,
        PostDetailFragment.Callbacks{

    private boolean dualPane;
    private Application app;
    private HanuFragmentInterface fragmentUI;
    private int postId;
    private PostPagerAdapter pagerAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        if(savedInstanceState != null){
            postId = savedInstanceState.getInt("PostId");
        }
        else{
            postId = 0;
        }

        if (findViewById(R.id.post_list) != null) {
            dualPane = true;
        }
        else{
            dualPane = false;
            FrameLayout postDetail = (FrameLayout) findViewById(R.id.post_detail);
            if(postDetail != null){
                postDetail.setVisibility(View.GONE);
            }
        }

        // TODO Tracking.
        // https://developers.google.com/cloud-messaging/android/client?configured=true
        // https://developers.google.com/analytics/devguides/collection/android/v4/start?configured=true
        // https://developers.google.com/admob/android/app?configured=true

        // Get Application Instance.
        app = Application.getApplicationInstance();

        // Start the Main Activity
        startMainScreen();
    }

    private void startMainScreen() {

        // Show Ad.

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("9F11CAC92EB404500CAA3F8B0BBA5277").build();

        AdView adView = (AdView) findViewById(R.id.adView);

        // Start loading the ad in the background.
        adView.loadAd(adRequest);

        // Load Posts.
        Application.getApplicationInstance().getAllPosts();

        // Create the Fragment.
        FragmentManager fm = this.getSupportFragmentManager();
        Fragment fragment;

        if (dualPane) {
            // Create Post List Fragment
            fragment = new PostListFragment();
            Bundle arguments = new Bundle();
            arguments.putInt("PostId", postId);
            //arguments.putBoolean("DualPane", dualPane);
            //arguments.putBoolean("ShowFirstItem", true);
            fragment.setArguments(arguments);
            fm.beginTransaction().replace(R.id.post_list, fragment).commitAllowingStateLoss();

            fragmentUI = (HanuFragmentInterface) fragment;

        } else {
            // Create view Pager
            viewPager = (ViewPager) findViewById(R.id.post_pager);

            pagerAdapter = new PostPagerAdapter(getSupportFragmentManager(),app.getPostList().size());
            viewPager.setAdapter(pagerAdapter);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id;

        switch (item.getItemId()){

            case R.id.Help:
                Intent help = new Intent(Main.this, DisplayFile.class);
                help.putExtra("File", "help.html");
                help.putExtra("Title", "Help: ");
                Main.this.startActivity(help);
                break;

            case R.id.About:
                Intent info = new Intent(Main.this, DisplayFile.class);
                info.putExtra("File", "about.html");
                info.putExtra("Title", "About: ");
                Main.this.startActivity(info);
                break;

            case R.id.Rate:
                if(dualPane){
                    id = fragmentUI.getSelectedItem();
                }
                else{
                    id = viewPager.getCurrentItem();
                }
                Intent rate = new Intent(Main.this, PostRating.class);
                rate.putExtra("PostId", id);
                Main.this.startActivity(rate);
                break;

            case R.id.Share:
                try{
                    if(dualPane){
                        id = fragmentUI.getSelectedItem();
                    }
                    else{
                        id = viewPager.getCurrentItem();
                    }
                    Post post = app.getPostList().get(id);
                    Intent send = new Intent(android.content.Intent.ACTION_SEND);
                    send.setType("text/plain");
                    send.putExtra(android.content.Intent.EXTRA_SUBJECT, post.getTitle());
                    send.putExtra(android.content.Intent.EXTRA_TEXT, post.getContent(true));
                    startActivity(Intent.createChooser(send, "Share with..."));
                }catch(Exception e){
                    Log.e(Application.TAG, e.getMessage(), e);
                    finish();
                }
                break;

            case R.id.Upload:
                Intent upload = new Intent(Main.this, CreateNewPost.class);
                Main.this.startActivity(upload);
                break;

        }

        return true;
    }

    @Override
    public void onItemSelected(int id) {

        if (dualPane) {
            Bundle arguments = new Bundle();
            arguments.putInt("PostId", id);
            PostDetailFragment fragment = new PostDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.post_detail, fragment)
                    .commit();

        }
        else{
            Intent postDetail = new Intent(Main.this, PostDetailActivity.class);
            postDetail.putExtra("PostId", id);
            Main.this.startActivity(postDetail);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(fragmentUI != null){
            outState.putInt("PostId", fragmentUI.getSelectedItem());
        }
    }

    @Override
    protected void onDestroy(){
        app.close();
        super.onDestroy();
    }

    @Override
    public void loadPostsByCategory(String taxonomy, String name) {

        if(taxonomy.contentEquals("category")){
            app.getPostsByCategory(name);
        }
        else if(taxonomy.contentEquals("post_tag")){
            app.getPostsByTag(name);
        }
        else if(taxonomy.contentEquals("author")){
            app.getPostsByAuthor(name);
        }

        this.runOnUiThread(new Runnable() {
            public void run(){
                fragmentUI.reloadUI();
            }
        });
    }

    @Override
    public boolean isDualPane() {
        return dualPane;
    }
}
