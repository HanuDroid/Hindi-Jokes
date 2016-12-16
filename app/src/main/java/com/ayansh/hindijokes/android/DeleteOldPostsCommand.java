package com.ayansh.hindijokes.android;

import com.ayansh.CommandExecuter.Command;
import com.ayansh.CommandExecuter.Invoker;
import com.ayansh.CommandExecuter.ResultObject;
import com.ayansh.hanudroid.Application;
import com.ayansh.hanudroid.Post;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Varun Verma on 8 Dec 2016.
 */

public class DeleteOldPostsCommand extends Command {

    public DeleteOldPostsCommand(Invoker caller) {
        super(caller);
    }

    @Override
    protected void execute(ResultObject result) throws Exception {

        Application app = Application.getApplicationInstance();
        List<Post> toDelete = new ArrayList<Post>();
        Iterator<Post> i;
        List<Post> postList;
        long seven_days   =   7*24*60*60;
        long hundred_days = 100*24*60*60;

        String keep_old_jokes = app.getOptions().get("Keep_Old_Jokes");
        String keep_old_memes = app.getOptions().get("Keep_Old_Memes");

        postList = app.getAllPosts();

        if(postList.size() < 150){
            return;
        }

        i = postList.iterator();
        while(i.hasNext()){

            Post p = i.next();
            Date today = new Date();
            Date pubDate = p.getPublishDate();
            long diff = today.getTime()/1000 - pubDate.getTime()/1000;

            if(p.hasCategory("Meme")){

                if(keep_old_memes == null || keep_old_memes.contentEquals("")){
                    if(diff >= seven_days){
                        // Collect into a delete list
                        toDelete.add(p);
                    }
                }
            }
            else{

                if(keep_old_jokes == null || keep_old_jokes.contentEquals("")){
                    if(diff >= hundred_days){
                        // Collect into a delete list
                        toDelete.add(p);
                    }
                }
            }

        }

        // Now iterate on delete list and delete one by one.
        i = toDelete.iterator();
        while(i.hasNext()){
            app.deletePost(i.next().getId());
        }

        result.getData().putInt("PostsDeleted", toDelete.size());

    }
}
