package com.sofurry.mobileapi;

import org.json.JSONObject;

import com.sofurry.AppConstants;
import com.sofurry.base.interfaces.IJobStatusCallback;
import com.sofurry.mobileapi.core.Request;
import com.sofurry.model.NetworkList;
import com.sofurry.model.PrivateMessage;

public class SFPMList extends NetworkList<PrivateMessage> {
	private int currentPage = -1;
	private Boolean lastpage = false;
	
	@Override
	protected void doLoadNextPage(IJobStatusCallback StatusCallback) throws Exception {
			if (isFinalPage()) return; // don't fetch after last page
			
			currentPage++;
			
			Request req = ApiFactory.createListPMs(currentPage, AppConstants.ENTRIESPERPAGE_LIST);
			JSONObject res = req.execute();
			
			int loaded = ApiFactory.ParsePMList(res, this);
			lastpage = (loaded < AppConstants.ENTRIESPERPAGE_LIST);
	}

	@Override
	public Boolean isFinalPage() {
		return lastpage;
	}

	@Override
	protected void doCancel() {
		// can't do anything here as Request is not cancellable
	}

}
