package com.ayansh.hindijokes.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.ayansh.CommandExecuter.CommandExecuter;
import com.ayansh.CommandExecuter.Invoker;
import com.ayansh.CommandExecuter.ProgressInfo;
import com.ayansh.CommandExecuter.ResultObject;
import com.ayansh.hanudroid.Application;
import com.ayansh.hanudroid.SaveRegIdCommand;
import com.google.firebase.analytics.FirebaseAnalytics;

public class SplashScreen extends Activity implements Invoker {
	
	private Application app;
	private TextView statusView;
	private boolean appStarted = false;
	private boolean firstUse = false;
	private boolean showNewFeatures = false;
	private FirebaseAnalytics mFirebaseAnalytics;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);
		
		statusView = (TextView) findViewById(R.id.status);
		
        // Get Application Instance.
        app = Application.getApplicationInstance();
        
        // Set the context of the application
        app.setContext(getApplicationContext());

		// Obtain the FirebaseAnalytics instance.
		mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

		// Accept my Terms
        //app.setEULAResult(true);
		if (!app.isEULAAccepted()) {
			
			Intent eula = new Intent(SplashScreen.this, Eula.class);
			SplashScreen.this.startActivityForResult(eula, Application.EULA);
			
		} else {
			// Start the Main Activity
			startMainActivity();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    	
    		return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}
	
	private void startMainActivity() {
		
		// Register application.
		String regStatus = (String) app.readParameterValue("RegistrationStatus");
		String regId = (String) app.readParameterValue("RegistrationId");

		if(regId != null && !regId.contentEquals("")) {

			if(regStatus == null || regStatus.contentEquals("")) {

				CommandExecuter ce = new CommandExecuter();

				SaveRegIdCommand command = new SaveRegIdCommand(new Invoker() {
					public void NotifyCommandExecuted(ResultObject result) {
					}

					public void ProgressUpdate(ProgressInfo pi) {
					}
				}, regId);

				ce.execute(command);

			}
		}

		// Initialize app...
		if (app.isThisFirstUse()) {
			// This is the first run !

			// Set Default Settings
			setDefaultSettings();

			firstUse = true;

			statusView.setText("Initializing app for first use.\nPlease wait, this may take a while");
			app.initializeAppForFirstUse(this);

			// Create Notification Channels
			createNotificationChannels();

		} else {

			// Regular use. Initialize App
			statusView.setText("Initializing application...");
			app.initialize(this);

			// Check if this is upgrade
			int oldFrameworkVersion = app.getOldFrameworkVersion();
			int newFrameworkVersion = app.getNewFrameworkVersion();

			int oldAppVersion = app.getOldAppVersion();
			int newAppVersion;
			
			try {
				
				newAppVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
				
			} catch (NameNotFoundException e) {
				newAppVersion = 0;
				Log.e(Application.TAG, e.getMessage(), e);
			}

			if (newAppVersion > oldAppVersion || newFrameworkVersion > oldFrameworkVersion) {
				showNewFeatures = true;
				app.updateVersion();

				// Create Notification Channels
				createNotificationChannels();
			}

			if(oldAppVersion <= 12){
				setDefaultSettings();
			}

			// Now start the app.
			startApp();
		}

	}

	private void setDefaultSettings() {

		app.addSyncCategory("Veg Joke");
		app.addSyncCategory("Meme");

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch (requestCode) {

		case Application.EULA:
			if (!app.isEULAAccepted()) {
				finish();
			} else {
				// Start Main Activity
				startMainActivity();
			}
			break;
			
		case 900:
			firstUse = false;
			startApp();
			break;
			
		case 901:
			showNewFeatures = false;
			startApp();
			break;
		}
	}

	@Override
	public void NotifyCommandExecuted(ResultObject result) {
		
		Log.i(Application.TAG, "Command Execution completed");
		
		if (!result.isCommandExecutionSuccess()) {

			if (result.getResultCode() == 420) {
				// Application is not registered.
				String message = "This application is not registered with Hanu-Droid.\n"
						+ "Please inform the developer about this error.";
				
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
				
				alertDialogBuilder
					.setTitle("Application not registered !")
					.setMessage(message).setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							SplashScreen.this.finish();
						}
					}).
					create().
					show();
			}
			else{
				// Start the app
				startApp();
			}
		}
		else{

			// Did we download new jokes?
			if(!firstUse){
				boolean show_notification = result.getData().getBoolean("ShowNotification");
				if(show_notification){
					showInfoNotification(result);
				}
			}

			// Start the app
			startApp();			
		}
		
	}

	private void showInfoNotification(ResultObject result) {

		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		String title = "";

		int postsDownloaded = result.getData().getInt("PostsDownloaded");
		if (postsDownloaded < 1) {
			return;
		} else {
			title = postsDownloaded + " new joke(s) have been downloaded";
		}

		Notification notification = new NotificationCompat.Builder(this,"NEW_CONTENT")
				.setContentTitle(title)
				.setContentText(title)
				.setSmallIcon(R.mipmap.ic_launcher)
				.build();

		notification.icon = R.mipmap.ic_launcher;
		notification.tickerText = title;
		notification.when = System.currentTimeMillis();

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;

		nm.notify(4, notification);

	}

	@Override
	public void ProgressUpdate(ProgressInfo progress) {
		
		String message = progress.getProgressMessage();
		if(message != null && !message.contentEquals("")){
			
			if (message.contentEquals("Show UI")) {
				startApp();
			}
			
		}
	}

	private void showHelp() {
		
		Intent help = new Intent(SplashScreen.this, DisplayFile.class);
		help.putExtra("File", "help.html");
		help.putExtra("Title", "Help: ");
		SplashScreen.this.startActivityForResult(help, 900);
		
	}

	private void showWhatsNew() {
		
		Intent newFeatures = new Intent(SplashScreen.this, DisplayFile.class);
		newFeatures.putExtra("File", "NewFeatures.html");
		newFeatures.putExtra("Title", "New Features: ");
		SplashScreen.this.startActivityForResult(newFeatures, 901);
	}

	private void startApp() {
		
		if(appStarted){
			return;
		}
		
		if (firstUse) {
			showHelp();
			return;
		}
		
		if (showNewFeatures) {
			showWhatsNew();
			return;
		}

		appStarted = true;

		// Start the Quiz List
		Log.i(Application.TAG, "Start the main screen");
		Intent start = new Intent(SplashScreen.this, Main.class);
		SplashScreen.this.startActivity(start);
		
		// Kill this activity.
		Log.i(Application.TAG, "Kill splash Activity");
		SplashScreen.this.finish();
	}

	private void createNotificationChannels(){

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// Create the NotificationChannel, but only on API 26+ because
		// the NotificationChannel class is new and not in the support library
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

			NotificationChannel channel;
			int importance = NotificationManager.IMPORTANCE_DEFAULT;

			channel = new NotificationChannel("NEW_CONTENT", "New or Updated Content", importance);
			channel.setDescription("Notifications when new or updated content is received");
			notificationManager.createNotificationChannel(channel);

			channel = new NotificationChannel("INFO_MESSAGE", "Information and Announcements", importance);
			channel.setDescription("Notification when important information or announcement is published from App Developer");
			notificationManager.createNotificationChannel(channel);

		}

	}
}