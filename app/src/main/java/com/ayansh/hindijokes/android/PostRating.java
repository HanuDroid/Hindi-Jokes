package com.ayansh.hindijokes.android;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.RatingBar;

import com.ayansh.hanudroid.Application;
import com.ayansh.hanudroid.Post;

/**
 * Created by varun on 1/7/16.
 */
public class PostRating extends Activity implements RatingBar.OnRatingBarChangeListener {

    private RatingBar ratingBar;
    Post post;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.rate);

        Intent intent = getIntent();
        int postId = intent.getIntExtra("PostId", 0);

        try{
            post = Application.getApplicationInstance().getPostList().get(postId);
        }catch (Exception e){
            Log.e(Application.TAG, e.getMessage(), e);
            finish();
        }

        if(post == null){
            finish();
        }

        setTitle("Rate this post:");

        ratingBar = (RatingBar) findViewById(R.id.ratingbar);
        ratingBar.setNumStars(5);
        ratingBar.setRating(post.getMyRating());
        ratingBar.setOnRatingBarChangeListener(this);

    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig)
    {
        // Ignore orientation change to keep activity from restarting
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onRatingChanged(RatingBar view, float rating, boolean fromUser) {
        // Rating was changed
        if(fromUser){

            post.addRating(rating);

        }
        // Close activity after the rating is finished
        finish();
    }

}
