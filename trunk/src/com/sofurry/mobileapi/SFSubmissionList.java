/**
 * 
 */
package com.sofurry.mobileapi;

import org.json.JSONObject;

import com.sofurry.AppConstants;
import com.sofurry.base.interfaces.IJobStatusCallback;
import com.sofurry.mobileapi.ApiFactory.ContentType;
import com.sofurry.mobileapi.ApiFactory.ParseBrowseResult;
import com.sofurry.mobileapi.ApiFactory.ViewSource;
import com.sofurry.mobileapi.core.Request;
import com.sofurry.model.NetworkList;
import com.sofurry.model.Submission;

/**
 * Implementation of SoFurry Submission List
 * Handle 'Browse' website API call
 * 
 * @author Night_Gryphon
 *
 */
public class SFSubmissionList extends NetworkList<Submission> {

	private int currentPage = -1;
	private int totalPages = -1;
	
	private ViewSource fSource = ViewSource.all;
	private String fExtra = null;
	private ContentType fContentType = ContentType.all;
	
	/**
	 * 
	 */
	public SFSubmissionList(ViewSource source, String extra, ContentType contentType) {
		super();
		fSource = source;
		fExtra = extra;
		fContentType = contentType;
	}

	@Override
	protected void doLoadNextPage(IJobStatusCallback StatusCallback) throws Exception {
			if (isFinalPage()) return; // don't fetch after last page
			
			currentPage++;
			
			Request req = ApiFactory.createBrowse(fSource, fExtra, fContentType, AppConstants.ENTRIESPERPAGE_GALLERY, currentPage);
			JSONObject res = req.execute();
			
			ParseBrowseResult loaded = ApiFactory.ParseBrowse(res, this);
			totalPages = loaded.NumPages;
	}

	@Override
	public Boolean isFinalPage() {
		return (totalPages > 0) && (currentPage >= totalPages-1);
	}

	@Override
	public int size() { // TODO return sizeLoaded if there was error in last page request
		if (isFinalPage() && (! isLoading()))
			return sizeLoaded();
		
		if (totalPages > 0) 
			return totalPages * AppConstants.ENTRIESPERPAGE_GALLERY;
		
		return sizeLoaded() + AppConstants.ENTRIESPERPAGE_GALLERY;
	}

	@Override
	protected void doCancel() {
		// can't do anything here as Request is not cancellable
	}


}
