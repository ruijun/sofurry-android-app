package com.sofurry.itemviews;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.json.JSONObject;

import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebView;
import android.widget.Toast;

import com.sofurry.AppConstants;
import com.sofurry.FavableActivity;
import com.sofurry.requests.AjaxRequest;
import com.sofurry.util.FileStorage;

/**
 * @author SoFurry
 *
 * Activity that shows a single story
 */
public class ViewStoryActivity extends FavableActivity  {
	
	private WebView webview;
	private String content;

	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);	
	    webview = new WebView(this);
	    setContentView(webview);
	        
		AjaxRequest req = getFetchParameters(pageID);
		pbh.showProgressDialog("Fetching story...");
		req.execute(requesthandler);
	}

	/**
	 * Returns a story request
	 * @param pageID
	 * The pageID of the story to be fetched
	 * @return
	 */
	protected AjaxRequest getFetchParameters(int pageID) {
		AjaxRequest req = new AjaxRequest();
		req.addParameter("f", "getpagecontent");
		req.addParameter("pid", "" + pageID);
		req.setRequestID(AppConstants.REQUEST_ID_FETCHSUBMISSIONDATA);
		return req;
	}

	
	@Override
	public void sonData(int id, JSONObject obj) throws Exception {
		pbh.hideProgressDialog();
		if (id == AppConstants.REQUEST_ID_FETCHSUBMISSIONDATA) {
			content = obj.getString("content");
			webview.loadData(content, "text/html", "utf-8");
		} else
			super.sonData(id, obj); // Handle inherited events
	}

	@Override
	public void createExtraMenuOptions(Menu menu) {
		super.createExtraMenuOptions(menu);
	}

	@Override
	public void save() {
		try {
			String targetPath = FileStorage.getUserStoragePath("Stories", name + ".html");

			File tf = new File(targetPath);
			FileStorage.ensureDirectory(tf.getParent());
			
			FileWriter outFile = new FileWriter(targetPath);
			PrintWriter out = new PrintWriter(outFile);
			out.println(content);
			out.close();
			
			Toast.makeText(getApplicationContext(), "File saved to:\n" + targetPath, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			sonError(-1, e);
		}
	}

}
