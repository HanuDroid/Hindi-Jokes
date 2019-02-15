package com.ayansh.hindijokes.android;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.ayansh.hanudroid.Application;
import com.ayansh.hanudroid.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class HJAppWidgetProvider extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, Main.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
            views.setOnClickPendingIntent(R.id.widget_rl, pendingIntent);

            // Application Coding
            Application app = Application.getApplicationInstance();
            app.setContext(context);

            Post post = getRandomPost();
            views.setTextViewText(R.id.post_content,post.getContent(true));

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private Post getRandomPost() {

        Post post;
        List<Post> memePostList = new ArrayList<Post>();
        Application app = Application.getApplicationInstance();

        post = app.getRandomPostFromDB();

        while(post.hasCategory("Meme")){
            post.incrementViewCount(1);
            memePostList.add(post);
            post = app.getRandomPostFromDB();
        }

        ListIterator<Post> iterator = memePostList.listIterator();
        while (iterator.hasNext()){
            iterator.next().incrementViewCount(-1);
        }

        return post;
    }

}