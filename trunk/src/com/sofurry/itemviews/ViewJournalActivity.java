package com.sofurry.itemviews;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.json.JSONObject;

import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebView;
import android.widget.Toast;

import com.sofurry.SubmissionViewActivity;
import com.sofurry.requests.AjaxRequest;
import com.sofurry.util.FileStorage;

/**
 * @author SoFurry
 *
 * Activity that shows a single story
 */
public class ViewJournalActivity extends SubmissionViewActivity  {
	
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
		return req;
	}

	
	@Override
	public void sonData(int id, JSONObject obj) throws Exception {
		pbh.hideProgressDialog();
		content = obj.getString("content");
		webview.loadData(content, "text/html", "utf-8");
	}

	@Override
	public void createExtraMenuOptions(Menu menu) {
	}

	@Override
	public void save() {
		try {
			String targetPath = FileStorage.getUserStoragePath("Story", name + ".txt");

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
