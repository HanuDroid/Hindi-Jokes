package com.ayansh.hindijokes.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ayansh.CommandExecuter.CommandExecuter;
import com.ayansh.CommandExecuter.Invoker;
import com.ayansh.CommandExecuter.ProgressInfo;
import com.ayansh.CommandExecuter.ResultObject;
import com.ayansh.hanudroid.Application;
import com.ayansh.hanudroid.CreateNewPostCommand;
import com.google.android.gms.ads.InterstitialAd;


/**
 * Created by varun on 1/7/16.
 */
public class CreateNewPost extends AppCompatActivity implements View.OnClickListener, Invoker {

    private ProgressDialog pd;
    private EditText title, content;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.create_post);
        setTitle("Upload new Joke");

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        title = (EditText) findViewById(R.id.title);
        content = (EditText) findViewById(R.id.content);

        // Initialize Application
        Application app = Application.getApplicationInstance();
        app.setContext(getApplicationContext());

        // Get Intent
        Intent intent = getIntent();

        String action = intent.getAction();
        String type = intent.getType();

        // Filter - to be sure
        if(action != null && action.contentEquals(Intent.ACTION_SEND) && type!=null && type.contains("text")){
            title.setText(intent.getStringExtra(Intent.EXTRA_SUBJECT));
            content.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
        }

        Button submit = (Button) findViewById(R.id.submit);
        submit.setOnClickListener(this);

        Button cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        // On Click of a View
        switch(view.getId()){

            case R.id.submit:
                createNewPost();
                break;

            case R.id.cancel:
                showInterstitialAd();
                finish();
                break;

        }

    }

    private void createNewPost() {
        // Create New Post
        String title = this.title.getEditableText().toString();
        String content = this.content.getEditableText().toString();
        String name = ((EditText) findViewById(R.id.nick_name)).getEditableText().toString();

        if(title == null || title.contentEquals("") || content == null || content.contentEquals("")){
            Toast.makeText(this, "Title/Content cannot be empty", Toast.LENGTH_LONG).show();
            return;
        }

        CommandExecuter ce = new CommandExecuter();
        CreateNewPostCommand command = new CreateNewPostCommand(this,title,content,name);

        pd = ProgressDialog.show(this, "Uploading Joke", "Uploading new joke, please wait...");

        ce.execute(command);

        Bundle bundle = new Bundle();
        bundle.putString("action", "joke_upload");
        Application.getApplicationInstance().getFirebaseAnalytics().logEvent("joke_upload", bundle);

    }

    @Override
    public void NotifyCommandExecuted(ResultObject result) {
        // Command Execution is success.

        pd.dismiss();

        if(result.isCommandExecutionSuccess() && result.getResultCode() == 200){
            Toast.makeText(this, "Joke uploaded successfully...", Toast.LENGTH_LONG).show();
            showInterstitialAd();
            finish();
        }
        else{
            String message = "Following error while uploading joke: " + result.getErrorMessage() + " -- Please try again!";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void ProgressUpdate(ProgressInfo progress) {
        // Nothing to do

    }

    private void showInterstitialAd(){

        InterstitialAd iad = MyInterstitialAd.getInterstitialAd(this);
        if(iad.isLoaded()){
            iad.show();
        }
    }

}
