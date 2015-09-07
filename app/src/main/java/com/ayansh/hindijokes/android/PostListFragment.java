package com.ayansh.hindijokes.android;


import java.util.ArrayList;
import java.util.List;

import org.varunverma.hanu.Application.Application;
import org.varunverma.hanu.Application.HanuFragmentInterface;
import org.varunverma.hanu.Application.Post;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class PostListFragment extends ListFragment implements HanuFragmentInterface{

	private ListView listView;
	private Callbacks activity = sDummyCallbacks;
	private myAdapter adapter;
	private List<Post> postList;
	private int selection;
	private boolean dualPane;

    public interface Callbacks {

        public void onItemSelected(int id);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(int id) {
        }
    };
    
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
    	
    	View view = inflater.inflate(R.layout.post_list, container, false);
    	listView = (ListView) view.findViewById(android.R.id.list);
    	listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    	return view;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        
        Application app = Application.getApplicationInstance();
        
        if(getArguments() != null){
			if (getArguments().containsKey("PostId")) {
				selection = getArguments().getInt("PostId");
	        	if(selection > app.getPostList().size()){
	        		selection = app.getPostList().size();
	        	}
	        }
			dualPane = getArguments().getBoolean("DualPane");
		}
    }
    
    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
    	
    	super.onActivityCreated(savedInstanceState);
    	
    	if(postList == null){
    		postList = new ArrayList<Post>();
    	}
    	
    	Context c = getActivity().getApplicationContext();
    	postList.addAll(Application.getApplicationInstance().getPostList());
    	adapter = new myAdapter(c,R.layout.post_list_row,R.id.post_title,postList);
    	listView.setAdapter(adapter);
    	listView.setSelection(selection);
    	
    	if(getArguments() != null){
    		if (getArguments().containsKey("ShowFirstItem")) {
    			boolean showFirstItem = getArguments().getBoolean("ShowFirstItem");
    			if(showFirstItem){
    				listView.performItemClick(adapter.getView(selection, null, null), selection, 0);
    			}
	        }
    	}
    }
	
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        this.activity = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = sDummyCallbacks;
    }
    
    @Override
    public void onListItemClick(ListView lv, View view, int position, long id) {
        super.onListItemClick(lv, view, position, id);
        activity.onItemSelected(position);
        selection = position;
    }

	@Override
	public void reloadUI() {
		if(postList!=null){
			postList.clear();
			postList.addAll(Application.getApplicationInstance().getPostList());
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public int getSelectedItem() {
		return selection;
	}

	class myAdapter extends ArrayAdapter<Post> {

		private List<Post> postList;

		public myAdapter(Context context, int resource, int resId,
				List<Post> objects) {
			super(context, resource, resId, objects);
			postList = objects;
		}

		@Override
		public View getView(int position, View convertView,
				final ViewGroup parent) {

			View rowView;

			if (convertView == null) {
				rowView = LayoutInflater.from(getContext()).inflate(
						R.layout.post_list_row, parent, false);
			} else {
				rowView = convertView;
			}
			
			if(postList.isEmpty() || position > postList.size()){
				return rowView;
			}
			
			TextView title = (TextView) rowView.findViewById(R.id.post_title);
			title.setText(postList.get(position).getTitle());
			if (dualPane) {
				title.setTextSize(30);
			}

			return rowView;
		}
	}

}