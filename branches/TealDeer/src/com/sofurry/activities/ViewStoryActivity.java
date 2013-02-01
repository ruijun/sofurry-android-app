package com.sofurry.activities;


import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.json.JSONObject;

import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebView;
import android.widget.Toast;

import com.sofurry.base.classes.FavableActivity;
import com.sofurry.mobileapi.ApiFactory;
import com.sofurry.mobileapi.core.Request;
import com.sofurry.requests.AndroidRequestWrapper;
import com.sofurry.requests.DataCall;
import com.sofurry.storage.FileStorage;

/**
 * @author SoFurry
 *
 * Activity that shows a single story
 */
public class ViewStoryActivity extends FavableActivity {
    private String  content;
    private WebView webview;

    /**
     * Method description
     *
     *
     * @param menu
     */
    @Override
    public void createExtraMenuOptions(Menu menu) {
        super.createExtraMenuOptions(menu);
    }

    /**
     * Method description
     *
     *
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        boolean mustFetch = true;

        super.onCreate(savedInstanceState);

        webview = new WebView(this);

        setContentView(webview);

        if (savedInstanceState != null) {
            content = (String) retrieveObject("content");

            // Sanity check to ensure that we actually have some contents here
            if ((content != null) && (content.length() > 100)) {
                mustFetch = false;
                content   = content.replace("\u00a0", "");

                showContent();
            }
        }

        // Check if we need to fetch
        if (mustFetch) {
            pbh.showProgressDialog("Fetching story...");

			Request req = ApiFactory.createGetPageContent(pageID);
    		AndroidRequestWrapper arw = new AndroidRequestWrapper(requesthandler, req);
    		arw.exec(new DataCall() { public void call() { handlePageData((JSONObject)arg1);	} });
        }
    }

    /**
     * Handle the data returned by the getPageContent request
     * @param obj
     */
    public void handlePageData(JSONObject obj) {
    	try {
            pbh.hideProgressDialog();

            content = obj.getString("content");
            content = content.replace("\u00a0", "");

            showContent();
		} catch (Exception e) {
			onError(e);
		}
    }

    /**
     * Method description
     *
     *
     * @param outState
     */
    protected void onSaveInstanceState(Bundle outState) {
        if (!pbh.isShowing()) {
            storeObject("content", content);
        }

        super.onSaveInstanceState(outState);
    }

    /**
     * Method description
     *
     */
    @Override
    public void save() {
        try {
            String targetPath = FileStorage.getUserStoragePath("Stories", FileStorage.sanitizeFileName(name) + ".html");
            File   tf         = new File(targetPath);

            FileStorage.ensureDirectory(tf.getParent());

            FileWriter  outFile = new FileWriter(targetPath);
            PrintWriter out     = new PrintWriter(outFile);

            out.println(content);
            out.close();
            Toast.makeText(getApplicationContext(), "File saved to:\n" + targetPath, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            onError(e);
        }
    }

    /**
     * Displays the content
     */
    public void showContent() {
        webview.loadData(content, "text/html", "utf-8");
    }

//    /**
//     * Returns a story request
//     * @param pageID
//     * The pageID of the story to be fetched
//     * @return
//     */
//    protected AjaxRequest getFetchParameters(int pageID) {
//        AjaxRequest req = new AjaxRequest();
//
//        req.addParameter("f", "getpagecontent");
//        req.addParameter("pid", "" + pageID);
//        req.setRequestID(AppConstants.REQUEST_ID_FETCHSUBMISSIONDATA);
//
//        return req;
//    }
}
