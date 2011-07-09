package com.sofurry.activities;

//~--- imports ----------------------------------------------------------------

import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Bitmap;

import android.net.Uri;

import android.os.Bundle;

import android.preference.PreferenceManager;

import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.base.classes.FavableActivity;
import com.sofurry.requests.ContentDownloader;
import com.sofurry.requests.HttpRequest;
import com.sofurry.storage.FileStorage;
import com.sofurry.storage.ImageStorage;

import java.io.File;


//~--- classes ----------------------------------------------------------------

/**
 * Class description
 *
 */
public class ViewArtActivity
        extends FavableActivity
        implements Runnable {
    private Bitmap    imageBitmap = null;
    private Button    hdButton    = null;
    private ImageView image;
    private String    authorName;
    private String    content = null;
    private String    date;
    private String    filename = null;
    private String    level;
    private String    tags;
    private String    thumbnailUrl;
    private TextView  imageartisttext;
    private Thread    imageFetcher = null;
    private int       pid          = -1;


    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param menu
     */
    @Override
    public void createExtraMenuOptions(Menu menu) {
        menu.add(0, AppConstants.MENU_HD, 0, "HD View").setIcon(android.R.drawable.ic_menu_gallery);
        super.createExtraMenuOptions(menu);
    }

    /**
     * Views the button using the acitivty that is associated with images
     */
    public void doHdView() {
        File f = new File(FileStorage.getPath(ImageStorage.getSubmissionImagePath(filename)));

        if (!f.exists()) {
            return;    // Until that file exists, there is nothing we can do really.
        }

        // Starts the associated image viewer, so the user can zoom and tilt
        Intent intent = new Intent();

        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(f), "image/*");
        startActivity(intent);
    }

    /*
     *  (non-Javadoc)
     * @see com.sofurry.IManagedActivity#finish()
     */

    /**
     * Method description
     *
     */
    @Override
    public void finish() {
        super.finish();
    }

    /**
     * Method description
     *
     *
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//      Authentication.loadAuthenticationInformation(this);
        setContentView(R.layout.artdetails);

        image           = (ImageView) findViewById(R.id.imagepreview);
        imageartisttext = (TextView) findViewById(R.id.imageartisttext);
        hdButton        = (Button) findViewById(R.id.HDViewButton);

        hdButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View arg0) {
                doHdView();
            }
        });
        hdButton.setEnabled(false);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();

            if (extras != null) {
                name         = extras.getString("name");
                tags         = extras.getString("tags");
                date         = extras.getString("date");
                level        = extras.getString("level");
                pid          = extras.getInt("pageID");
                authorName   = extras.getString("authorName");
                thumbnailUrl = extras.getString("thumbnail");

                Bitmap thumb = ImageStorage.loadSubmissionIcon(pageID);

                if (thumb != null) {
                    image.setImageBitmap(thumb);
                }

                filename = name + HttpRequest.extractExtension(thumbnailUrl);
                filename = FileStorage.sanitize(filename);
                content  = name + "\n" + authorName;
            }

            imageFetcher = new Thread(this);

            imageFetcher.start();
        } else {
            content      = (String) retrieveObject("content");
            filename     = (String) retrieveObject("filename");
            imageBitmap  = (Bitmap) retrieveObject("image");
            imageFetcher = (Thread) retrieveObject("thread");
            pid          = (Integer) retrieveObject("pid");
            name         = (String) retrieveObject("name");
            tags         = (String) retrieveObject("tags");
            date         = (String) retrieveObject("date");
            level        = (String) retrieveObject("level");
            authorName   = (String) retrieveObject("authorName");
        }

        if (imageFetcher != null) {
            pbh.showProgressDialog("Fetching image...");
        }

        if (imageBitmap != null) {
            showImage();
        }

        imageartisttext.setText(content);
    }

    /**
     * Method description
     *
     *
     * @param id
     * @param e
     */
    @Override
    public void onError(int id, Exception e) {
        if (id == AppConstants.REQUEST_ID_DOWNLOADIMAGE) {
            imageFetcher = null;
        }

        super.onError(id, e);
    }

    /**
     * Method description
     *
     *
     * @param item
     *
     * @return
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case AppConstants.MENU_HD:
                doHdView();

                return true;

            default:
                return super.onOptionsItemSelected(item);
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
    public void onOther(int id, Object obj) throws Exception {
        pbh.hideProgressDialog();

        // If the returntype is bitmap, we know what to do with it
        if (id == AppConstants.REQUEST_ID_DOWNLOADIMAGE) {
            imageBitmap = (Bitmap) obj;

            showImage();

            imageFetcher = null;
        } else {
            super.onOther(id, obj);
        }
    }

    /**
     * Method description
     *
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        storeObject("content", content);
        storeObject("filename", filename);
        storeObject("image", imageBitmap);
        storeObject("thread", imageFetcher);
        storeObject("pid", pid);
        storeObject("name", name);
        storeObject("tags", tags);
        storeObject("date", date);
        storeObject("level", level);
        storeObject("authorName", authorName);
        super.onSaveInstanceState(outState);
    }

    /*
     *  (non-Javadoc)
     * @see java.lang.Runnable#run()
     *
     * Fetches an image, either from the icon storage
     *
     */

    /**
     * Method description
     *
     */
    public void run() {
        try {
            String url = thumbnailUrl.replace("/thumbnails/", "/preview/");

//          String url = thumbnailUrl.replace("/art/thumbnails/", "/content/" + pid + ".jpg/");
            Log.i(AppConstants.TAG_STRING, "ImageDownloader: Downloading image for id " + pageID + " from " + url);

            Bitmap b = ImageStorage.loadSubmissionImage(filename);

            if (b == null) {
                // 1. extract extension
                String ext = HttpRequest.extractExtension(url);

                // 2. download file
                ContentDownloader.downloadFile(url, ImageStorage.getSubmissionImagePath(filename), null);

                // 3. read file
                b = ImageStorage.loadSubmissionImage(filename);

                // b = ContentDownloader.downloadBitmap(url);
                // ImageStorage.saveSubmissionImage(pageID, b);
                if (b == null) {
                    throw new Exception("Downloaded Image failed to load.");
                }
            }

            // Send bitmap to our hungry thread
            requesthandler.postMessage(AppConstants.REQUEST_ID_DOWNLOADIMAGE, b);
        } catch (Exception e) {
            requesthandler.postMessage(AppConstants.REQUEST_ID_DOWNLOADIMAGE, e);
        }
    }

    /**
     * Saves the file to the images folder
     */
    public void save() {
        try {
            if (filename == null) {
                throw new Exception("File has not downloaded properly yet. Filename is null.");
            }

            File f = new File(FileStorage.getPath(ImageStorage.getSubmissionImagePath(filename)));

            if (!f.exists()) {
                throw new Exception("File has not downloaded properly yet. File does not exist.");
            }

            // Image filename template (Issue 38) by NGryph
            // load template from preferences
            SharedPreferences prefs        = PreferenceManager.getDefaultSharedPreferences(this);
            String            fileNameTmpl = prefs.getString(AppConstants.PREFERENCE_IMAGE_FILE_NAME_TMPL,
                                                             "%AUTHOR% - %NAME%");
            boolean           useOnlyAdult = prefs.getBoolean(AppConstants.PREFERENCE_IMAGE_TMPL_USE_ONLY_ADULT, false);

            // filename must have correct extension. not sure is it true or not
            String targetPath = fileNameTmpl + '.' + filename.substring(filename.lastIndexOf('.') + 1);

            /*
             *  sanitizeFileName removes '/' character so separately sanitize every unsecure data field that comes from
             * the web. We're also making sure that dots are filtered out of these as well, simply because we want to
             * avoid accidental hacks due to weird names.
             */
            targetPath = targetPath.replaceAll("%AUTHOR%", sanitizeFileName(authorName, true));
            targetPath = targetPath.replaceAll("%NAME%", sanitizeFileName(name, true));
            targetPath = targetPath.replaceAll("%DATE%", sanitizeFileName(date, true));

            // Determine the level
            if (level.equals("0")) {
                targetPath = targetPath.replaceAll("%LEVEL%", "clean");
            } else if ((level.equals("1")) || (useOnlyAdult)) {
                targetPath = targetPath.replaceAll("%LEVEL%", "adult");
            } else {
                targetPath = targetPath.replaceAll("%LEVEL%", "extreme");
            }

            // some kind of hack. getUserStoragePath performs sanitize on filename and broke '/' chars from template
            // let's treat that getUserStoragePath provide root dir for image lib when filename is empty
            targetPath = FileStorage.getUserStoragePath("Images", "") + targetPath;

            File td = new File(targetPath.substring(0, targetPath.lastIndexOf('/')));

            td.mkdirs();

            File tf = new File(targetPath);

            FileStorage.ensureDirectory(tf.getParent());
            FileStorage.copyFile(f, tf);
            Toast.makeText(getApplicationContext(), "File saved to:\n" + targetPath, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            onError(-1, e);
        }
    }

    /**
     * Shows the image
     */
    public void showImage() {
        image.setImageBitmap(imageBitmap);
        hdButton.setEnabled(true);
    }
}
