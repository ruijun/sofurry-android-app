package com.sofurry.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.AdapterView;

import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.adapters.PrivateMessageAdapter;
import com.sofurry.base.classes.AbstractBrowseActivity;
import com.sofurry.mobileapi.SFPMList;
import com.sofurry.model.NetworkList;
import com.sofurry.model.PrivateMessage;

public class SFBrowsePMActivity extends AbstractBrowseActivity<PrivateMessage> {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.listlayout);
		setTitle("Private Messages");
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.listpmmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case R.id.compose:
                intent = new Intent(this, SendPMActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
	protected AdapterView getDataView() {
		return (AdapterView) findViewById(R.id.list_view);
	}

	@Override
	protected Adapter createAdapter() {
		return new PrivateMessageAdapter(this, R.layout.listitemtwolineicon, fList);
	}

	@Override
	protected void onDataViewItemClick(int aItemIndex) {
        PrivateMessage pm = fList.get(aItemIndex, false);
        Log.i(AppConstants.TAG_STRING, "ListPM: Viewing PM ID: " + pm.getId());
        Intent intent = new Intent(this, ViewPMActivity.class);
        intent.putExtra("PMID", pm.getId());
        startActivity(intent);

        if ((AppConstants.PM_STATUS_NEW+"").equals(pm.getStatus())) {
        	pm.setStatus(""+AppConstants.PM_STATUS_READ);
        	refreshDataView();
        }
	}

	@Override
	protected NetworkList<PrivateMessage> createBrowseList() {
		return new SFPMList();
	}

}
