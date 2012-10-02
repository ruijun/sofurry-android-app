/**
 * 
 */
package com.sofurry.mobileapi;

import org.json.JSONObject;

import android.os.Parcelable.Creator;

import com.sofurry.AppConstants;
import com.sofurry.base.interfaces.IJobStatusCallback;
import com.sofurry.mobileapi.ApiFactory.ContentType;
import com.sofurry.mobileapi.ApiFactory.ParseBrowseResult;
import com.sofurry.mobileapi.ApiFactory.ViewSource;
import com.sofurry.mobileapi.core.Request;
import com.sofurry.model.SubmissionList;

/**
 * Implementation of SoFurry Submission List
 * Handle 'Browse' website API call
 * 
 * @author Night_Gryphon
 *
 */
public class SFSubmissionList extends SubmissionList {

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
	protected void doLoadNextPage(IJobStatusCallback StatusCallback) {
		try {
			if (IsFinalPage()) return; // don't fetch after last page
			
			if (StatusCallback != null) {
				StatusCallback.onStart(this);
			}

			currentPage++;
			
			Request req = ApiFactory.createBrowse(fSource, fExtra, fContentType, AppConstants.ENTRIESPERPAGE_GALLERY, currentPage);
			JSONObject res = req.execute();
			
			ParseBrowseResult loaded = ApiFactory.ParseBrowse(res, this);
			totalPages = loaded.NumPages;
			
			if (StatusCallback != null) {
				StatusCallback.onFinish(this);
			}
		} catch (Exception e) {
			if (StatusCallback != null) {
				StatusCallback.onError(this, e.getMessage());
			}
		}
	}

	@Override
	protected Boolean IsFinalPage() {
		return (totalPages > 0) && (currentPage >= totalPages-1);
	}

	@Override
	protected void doCancel() {
		// can't do anything here as Request is not cancellable
	}


}
