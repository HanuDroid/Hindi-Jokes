package com.ayansh.hindijokes.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class PostDetailActivity extends AppCompatActivity implements
		PostDetailFragment.Callbacks {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		MobileAds.initialize(this, "ca-app-pub-4571712644338430~1102881505");

		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(myToolbar);

		// Get a support ActionBar corresponding to this toolbar
		ActionBar ab = getSupportActionBar();

		// Enable the Up button
		ab.setDisplayHomeAsUpEnabled(true);

		// Hide some views
		View postList = findViewById(R.id.post_list);
		if(postList != null){
			postList.setVisibility(View.GONE);
		}

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

		Intent intent = getIntent();
		int postIndex = intent.getIntExtra("PostIndex", 0);

		// Create the Fragment.
		FragmentManager fm = this.getSupportFragmentManager();

		// Create Post List Fragment
		Fragment fragment = new PostDetailFragment();
		Bundle arguments = new Bundle();
		arguments.putInt("PostIndex", postIndex);
		fragment.setArguments(arguments);

		fm.beginTransaction().replace(R.id.post_detail, fragment)
				.commitAllowingStateLoss();

	}

	@Override
	public void loadPostsByCategory(String taxonomy, String name) {
		// Nothing to do
	}

	@Override
	public boolean isDualPane() {
		return false;
	}
}