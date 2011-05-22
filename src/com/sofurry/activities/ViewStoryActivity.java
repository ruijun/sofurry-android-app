package com.sofurry.activities;

//~--- imports ----------------------------------------------------------------

import android.os.Bundle;

import android.view.Menu;

import android.webkit.WebView;

import android.widget.Toast;

import com.sofurry.AppConstants;
import com.sofurry.base.classes.FavableActivity;
import com.sofurry.requests.AjaxRequest;
import com.sofurry.storage.FileStorage;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;


//~--- classes ----------------------------------------------------------------

/**
 * @author SoFurry
 *
 * Activity that shows a single story
 */
public class ViewStoryActivity
        extends FavableActivity {
    private String  content;
    private WebView webview;


    //~--- methods ------------------------------------------------------------

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
            AjaxRequest req = getFetchParameters(pageID);

            pbh.showProgressDialog("Fetching story...");
            req.execute(requesthandler);
        }
    }

    /**
     * Method description
     *
     *
     * @param id
     * @param obj
     *
     * @throws Exception
     */
    @Override
    public void onData(int id, JSONObject obj) throws Exception {
        pbh.hideProgressDialog();

        if (id == AppConstants.REQUEST_ID_FETCHSUBMISSIONDATA) {
            content = obj.getString("content");
            content = content.replace("\u00a0", "");

            showContent();
        } else {
            super.onData(id, obj);    // Handle inherited events
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
            String targetPath = FileStorage.getUserStoragePath("Stories", sanitizeFileName(name) + ".html");
            File   tf         = new File(targetPath);

            FileStorage.ensureDirectory(tf.getParent());

            FileWriter  outFile = new FileWriter(targetPath);
            PrintWriter out     = new PrintWriter(outFile);

            out.println(content);
            out.close();
            Toast.makeText(getApplicationContext(), "File saved to:\n" + targetPath, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            onError(-1, e);
        }
    }

    /**
     * Displays the content
     */
    public void showContent() {
        webview.loadData(content, "text/html", "utf-8");
    }

    //~--- get methods --------------------------------------------------------

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
}
