package com.ayansh.hindijokes.android;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ayansh.hanudroid.Application;
import com.ayansh.hanudroid.HanuFragmentInterface;
import com.ayansh.hanudroid.Post;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Random;


public class PostDetailFragment extends Fragment implements HanuFragmentInterface, View.OnClickListener{

	private Post post;
	private Callbacks activity = sDummyCallbacks;
	private Application app;
	private int postIndex;
	private ImageButton postFav;
	
	public interface Callbacks {
		public void loadPostsByCategory(String taxonomy, String name);
		public boolean isDualPane();
	}
	
	private static Callbacks sDummyCallbacks = new Callbacks() {

		@Override
		public void loadPostsByCategory(String taxonomy, String name) {			
		}

		@Override
		public boolean isDualPane() {
			return false;
		}
		
    };
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		app = Application.getApplicationInstance();
		
		if(app.getPostList().isEmpty()){
			return;
		}
		
		if(getArguments() != null){
			if (getArguments().containsKey("PostIndex")) {
				postIndex = getArguments().getInt("PostIndex");
	        	if(postIndex >= app.getPostList().size()){
					postIndex = app.getPostList().size() - 1;
	        	}
	            post = app.getPostList().get(postIndex);
	        }
		}
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.post_detail, container, false);

		if(post == null){
			return rootView;
		}

		Random r = new Random();
		int randomNo = r.nextInt(7);

		RelativeLayout rLL = rootView.findViewById(R.id.ll);
		setBackgroundColor(rLL);

		TextView tv_post_title = rootView.findViewById(R.id.post_title);
		tv_post_title.setText(post.getTitle());

		TextView tv_post_content = rootView.findViewById(R.id.post_content);
		ImageView imageView = rootView.findViewById(R.id.image_view);

		boolean isMeme = post.hasCategory("Meme");
		if(isMeme){


			try{
				File image_folder = new File(app.getFilesDirectory(),String.valueOf(post.getId()));
				File[] file_list = image_folder.listFiles();
				File image_file = file_list[0];
				Uri image_uri = Uri.fromFile(image_file);

				tv_post_content.setVisibility(View.GONE);
				imageView.setVisibility(View.VISIBLE);
				imageView.setImageURI(image_uri);
			}
			catch(Exception e){

				imageView.setVisibility(View.GONE);
				tv_post_content.setVisibility(View.VISIBLE);
				tv_post_content.setText("\n\nCould not load image. Please inform developer about this\n\n");

			}
		}
		else{
			imageView.setVisibility(View.GONE);
			tv_post_content.setVisibility(View.VISIBLE);
			tv_post_content.setMovementMethod(new ScrollingMovementMethod());
			tv_post_content.setText(post.getContent(true) + "\n\n");
		}

		TextView tv_post_meta = rootView.findViewById(R.id.post_meta);
		tv_post_meta.setText(getMetaText());

		ImageButton waShare = rootView.findViewById(R.id.WAShare);
		waShare.setOnClickListener(this);

		ImageButton Share = rootView.findViewById(R.id.Share);
		Share.setOnClickListener(this);

		ImageButton postRate = rootView.findViewById(R.id.Rate);
		postRate.setOnClickListener(this);

		postFav = rootView.findViewById(R.id.Favourite);
		setFavoutieIcon();
		postFav.setOnClickListener(this);

		return rootView;
	}

    @Override
    public void onDetach() {
        super.onDetach();
        activity = sDummyCallbacks;
    }
    
	@Override
	public void reloadUI() {
		// Reloading the UI
		post = app.getPostList().get(0);	
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			if(post == null){
				post = Application.getApplicationInstance().getPostList().get(0);
			}
			if(post != null){
				post.incrementViewCount(1);
			}
		}
	}

	@Override
	public int getSelectedItem() {
		return postIndex;
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()){
			case R.id.WAShare:
				shareContent("WhatsApp");
				break;

			case R.id.Share:
				shareContent("Normal");
				break;

			case R.id.Rate:
				Intent rate = new Intent(getActivity(), PostRating.class);
				rate.putExtra("PostIndex", postIndex);
				startActivity(rate);
				break;

			case R.id.Favourite:
				post.toggleFavourite();
				setFavoutieIcon();
				break;
		}
	}

	private void shareContent(String sharingApp){

		try{

			boolean isMeme = post.hasCategory("Meme");
			if(isMeme){
				File image_folder = new File(app.getFilesDirectory(),String.valueOf(post.getId()));
				File[] file_list = image_folder.listFiles();
				File image_file = file_list[0];

				Uri uri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName(), image_file);
				Intent intent = ShareCompat.IntentBuilder.from(getActivity())
						.setStream(uri) // uri from FileProvider
						.setType("text/html")
						.getIntent()
						.setAction(Intent.ACTION_SEND) //Change if needed
						.setDataAndType(uri, "image/*")
						.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

				if(sharingApp.contentEquals("WhatsApp")){
					intent.setPackage("com.whatsapp");
				}

				startActivity(Intent.createChooser(intent, "Share with..."));
			}
			else{
				String post_content = post.getContent(true);
				post_content += "\n\n \uD83D\uDC49 ayansh.com/hj \uD83D\uDC48";
				Intent send = new Intent(Intent.ACTION_SEND);
				send.setType("text/plain");
				send.putExtra(Intent.EXTRA_SUBJECT, post.getTitle());
				send.putExtra(Intent.EXTRA_TEXT, post_content);

				if(sharingApp.contentEquals("WhatsApp")){
					send.setPackage("com.whatsapp");
				}

				startActivity(Intent.createChooser(send, "Share with..."));
			}

			Bundle bundle = new Bundle();
			bundle.putString(FirebaseAnalytics.Param.ITEM_ID, post.getTitle());
			bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "post_share");
			app.getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.SHARE, bundle);

		}catch(Exception e){
			Log.e(Application.TAG, e.getMessage(), e);
		}

	}

	private CharSequence getMetaText() {

		SimpleDateFormat df = new SimpleDateFormat();

		String metaText = "Published On: " + df.format(post.getPublishDate());

		// Ratings
		if (post.getMetaData().size() > 0
				&& !post.getMetaData().get("ratings_users").contentEquals("0")) {

			metaText += "\n" + "Rating: "
					+ String.format("%.2g", Float.valueOf(post.getMetaData().get("ratings_average")))
					+ "/5 (by " + post.getMetaData().get("ratings_users") + " users)";
		}

		return metaText;
	}

	private void setBackgroundColor(RelativeLayout rLL) {

		Random r = new Random();
		int randomNo = r.nextInt(7);

		switch (randomNo){
			case 0:
				rLL.setBackgroundResource(R.color.colorPurple);
				break;

			case 1:
				rLL.setBackgroundResource(R.color.colorIndigo);
				break;

			case 2:
				rLL.setBackgroundResource(R.color.colorPrimary);
				break;

			case 3:
				rLL.setBackgroundResource(R.color.colorLime);
				break;

			case 4:
				rLL.setBackgroundResource(R.color.colorAccent);
				break;

			case 5:
				rLL.setBackgroundResource(R.color.colorBrown);
				break;

			case 6:
				rLL.setBackgroundResource(R.color.colorGray);
				break;

			default:
				rLL.setBackgroundResource(R.color.colorPurple);
				break;
		}
	}

	private void setFavoutieIcon(){

		if(post.isFavourite()){
			postFav.setImageResource(R.drawable.ic_favorite);
		}
		else{
			postFav.setImageResource(R.drawable.ic_favorite_empty);
		}

	}
}