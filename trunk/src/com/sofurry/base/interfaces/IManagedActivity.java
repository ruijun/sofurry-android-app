package com.sofurry.base.interfaces;

import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;

import com.sofurry.base.classes.ActivityManager;
import com.sofurry.requests.AjaxRequest;

public interface IManagedActivity<T> {

	public abstract void onCreate(Bundle savedInstanceState);
	
	public abstract void plugInAdapter();

	public abstract void updateView();

	public abstract boolean onCreateOptionsMenu(Menu menu);

	public abstract boolean onOptionsItemSelected(MenuItem item);

	public abstract void setSelectedIndex(int selectedIndex);

	public abstract AjaxRequest getFetchParameters(int page, int source);

	public abstract BaseAdapter getAdapter(Context context);

	public abstract void parseResponse(JSONObject obj) throws Exception;

	public abstract void finish();
	
	public abstract void resetViewSourceExtra(int newViewSource);
	
	public abstract ActivityManager<T> getActivityManager();
	
	public abstract long getUniqueKey();
	
	public abstract void setUniqueKey(long key);
	
	public abstract void onSaveInstanceState(Bundle outState);
	
}