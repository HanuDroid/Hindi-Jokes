package com.ayansh.hindijokes.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

import com.ayansh.CommandExecuter.ResultObject;
import com.ayansh.hanudroid.HanuGCMListenerService;

import java.util.ArrayList;
import java.util.Iterator;

public class AppGcmListenerService extends HanuGCMListenerService {

	@Override
	public void onMessageReceived(String from, Bundle data) {

		String message = data.getString("message");

		if(message.contentEquals("InfoMessage")){
			// Show message.
			showInfoMessage(data);
		}
		else {

			ResultObject result = processMessage(from, data);
			if (result.getData().getBoolean("ShowNotification")) {
				createNotification(result);
			}
		}

	}

	private void showInfoMessage(Bundle data) {
		// Show Info Message

		String subject = data.getString("subject");
		String content = data.getString("content");
		String mid = data.getString("message_id");
		String message = data.getString("message");

		if(mid == null || mid.contentEquals("")){
			mid = "0";
		}
		int id = Integer.valueOf(mid);

		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// Create Intent and Set Extras
		Intent notificationIntent = new Intent(this, DisplayFile.class);

		notificationIntent.putExtra("Title", "Info:");
		notificationIntent.putExtra("Subject", subject);
		notificationIntent.putExtra("Content", content);
		notificationIntent.addCategory(subject);

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		Notification notification = new NotificationCompat.Builder(this)
				.setContentTitle(subject)
				.setContentText(content)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setContentIntent(pendingIntent).build();

		notification.icon = R.mipmap.ic_launcher;
		notification.tickerText = subject;
		notification.when = System.currentTimeMillis();

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;

		nm.notify(id, notification);
	}

	private void createNotification(ResultObject result) {
		// Create Notification

		ArrayList<String> postTitleList = result.getData().getStringArrayList("PostTitle");

		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		String title;
		String text = postTitleList.get(0);

		int postsDownloaded = result.getData().getInt("PostsDownloaded");
		if (postsDownloaded == 0) {
			title = "New Jokes downloaded.";
		} else {
			title = postsDownloaded + " new joke(s) have been downloaded";
		}

		Intent notificationIntent = new Intent(this, Main.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
		inboxStyle.setBigContentTitle(title + ": " + text);

		// Add joke titles
		Iterator<String> i = postTitleList.listIterator();
		while (i.hasNext()) {
			inboxStyle.addLine(i.next());
		}

		Notification notification = new NotificationCompat.Builder(this)
				.setContentTitle(title)
				.setContentText(text)
				.setContentInfo(String.valueOf(postsDownloaded))
				.setSmallIcon(R.mipmap.ic_launcher)
				.setStyle(inboxStyle)
				.setContentIntent(pendingIntent).build();

		notification.icon = R.mipmap.ic_launcher;
		notification.tickerText = title;
		notification.when = System.currentTimeMillis();

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;

		nm.notify(1, notification);

	}
}