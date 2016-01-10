package com.ayansh.hindijokes.android;

import android.content.Context;
import android.os.Bundle;

import org.varunverma.hanu.Application.HanuGCMListenerService;

import java.util.logging.Logger;

public class AppGcmListenerService extends HanuGCMListenerService {

	@Override
	public void onMessageReceived(String from, Bundle data) {

		String message = data.getString("message");

		if(message.contentEquals("info_msg")){
			// Show message.
			//showInfoMessage(data);
		}
		else{

			super.processMessage(from, data);
		}



	}

}