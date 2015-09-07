package com.ayansh.hindijokes.android;

import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ProgressInfo;
import org.varunverma.CommandExecuter.ResultObject;
import org.varunverma.hanu.Application.Application;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

public class SplashScreen extends Activity implements Invoker {
	
	private Application app;
	private TextView statusView;
	private boolean appStarted = false;
	private boolean firstUse = false;
	private boolean showNewFeatures = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);
		
		statusView = (TextView) findViewById(R.id.status);
		
        // Get Application Instance.
        app = Application.getApplicationInstance();
        
        // Set the context of the application
        app.setContext(getApplicationContext());

		// Accept my Terms
        app.setEULAResult(true);
		if (!app.isEULAAccepted()) {
			
			Intent eula = new Intent(SplashScreen.this, DisplayFile.class);
        	eula.putExtra("File", "eula.html");
			eula.putExtra("Title", "End User License Aggrement: ");
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
        app.registerAppForGCM();
        
		// Initialize app...
		if (app.isThisFirstUse()) {
			// This is the first run !

			firstUse = true;

			statusView.setText("Initializing app for first use.\nPlease wait, this may take a while");
			app.initializeAppForFirstUse(this);

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
			}
		}

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
			// Start the app
			startApp();			
		}
		
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

}