/**
 * 
 */
package com.ayansh.hindijokes.android;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;

import com.ayansh.hanudroid.Application;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author varun
 * 
 */
public class Eula extends Activity implements OnClickListener{

	private Application application;
	private String EULA_Text;
	private WebView EULA;
	private Button accept, decline;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.eula);
		setTitle("End-User License Agreement");

		EULA = (WebView) findViewById(R.id.EULA);

		accept = (Button) findViewById(R.id.Accept);
		accept.setOnClickListener(this);

		decline = (Button) findViewById(R.id.Reject);
		decline.setOnClickListener(this);

		application = Application.getApplicationInstance();
		application.setContext(getApplicationContext());
		
		// Read EULA text from the file.
		readEULA(getResources());

		// Now display this EULA.
		EULA.setBackgroundColor(Color.TRANSPARENT);
		EULA.setBackgroundResource(R.drawable.background);
		EULA.loadDataWithBaseURL("fake://not/needed", EULA_Text, "text/html", "UTF-8", "");

	}

	private void readEULA(Resources res) {

		try {
			
			InputStream is = res.getAssets().open("eula.html");
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			
			EULA_Text = "";
			String line = "";
			while ((line = reader.readLine()) != null) {
				EULA_Text = EULA_Text + line;
			}
		} catch (IOException e) {
			// OOps... So let's pretend that user did not accept the EULA.
			EULADeclined();
		}
	}

	public void onClick(View v) {
		// Handle Button click
		switch (v.getId()) {

		case R.id.Accept:
			EULAAccepted();
			break;

		case R.id.Reject:
			EULADeclined();
			break;

		}
	}

	private void EULAAccepted() {

		application.setEULAResult(true);
		setResult(RESULT_OK);
		this.finish();
	}

	private void EULADeclined() {

		application.setEULAResult(false);
		setResult(RESULT_OK);
		this.finish();
	}

}