package com.sofurry;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

/**
 * @author Rangarig
 * 
 * A class that contains all the tools for an item that is favable, ratable and you know. 
 * 
 */
public abstract class SubmissionViewActivity extends ActivityWithRequests implements ICanCancel {
	
	protected int pageID;
	protected String name;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras() ;
	    if( extras != null ) {
	        pageID = extras.getInt("pageID");
	        name = extras.getString("name");
	    }
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 * 
	 * Creates the Context Menu for this Activity.
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0,AppConstants.MENU_SAVE   ,0,"Save").setIcon(android.R.drawable.ic_menu_save);

		createExtraMenuOptions(menu);
		
		return result;
	}

	/**
	 * Allows for the implementing view to add extra menu options to the menu
	 * @param menu
	 */
	public abstract void createExtraMenuOptions(Menu menu);
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case AppConstants.MENU_SAVE:
			save();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	
	@Override
	public void onError(int id, Exception e) {
		pbh.hideProgressDialog();
		super.onError(id, e);
	}
	
	public abstract void save();

	/* (non-Javadoc)
	 * @see com.sofurry.IManagedActivity#finish()
	 */
	@Override
	public void finish() {
		super.finish();
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
        	finish();
        }

        return super.onKeyDown(keyCode, event);
    }

    
    

}

