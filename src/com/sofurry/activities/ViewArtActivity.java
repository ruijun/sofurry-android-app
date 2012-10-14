package com.sofurry.activities;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;

import android.net.Uri;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.preference.PreferenceManager;

import android.util.FloatMath;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnPreDrawListener;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ViewFlipper;

import com.sofurry.AppConstants;
import com.sofurry.FixedViewFlipper;
import com.sofurry.R;
import com.sofurry.base.classes.FavableActivity;
import com.sofurry.base.interfaces.IJobStatusCallback;
import com.sofurry.mobileapi.downloaders.AsyncImageLoader;
import com.sofurry.mobileapi.downloaders.ThumbnailDownloader;
import com.sofurry.model.NetworkList;
import com.sofurry.model.Submission;
import com.sofurry.storage.FileStorage;
import com.sofurry.storage.ImageStorage;
import com.sofurry.storage.NetworkListStorage;
import com.sofurry.util.Utils;

import java.io.File;
import java.util.ArrayList;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;

import java.lang.Math;



/**
 * Art submission viewer
 */
public class ViewArtActivity
        extends FavableActivity 
        implements OnTouchListener, OnPreDrawListener {
	
	static final Boolean useOriginalScale = true;
	private Toast mytoast = null;
	
	
	/**
	 * Viewer flipper page contents
	 */
	private class PageHolder implements AsyncImageLoader.IImageLoadResult{
		private Bitmap    imageBitmap = null;
		private Boolean   imageLoaded = false;
		private ImageView image = null; 
		private ImageView savedIndicator = null;
		private ImageView playIndicator = null;
		private TextView  infoText = null;
		private TextView  HQIndicator = null;
		private View 	  loadingIndicator = null;
		private View	  myview = null;	
		
		private Submission submission = null;
		private int page_submission_index = -1;
		private AsyncImageLoader imageLoader = null;
		private boolean mustLoadImage = false;
		
		private Context context = null;
		
		public PageHolder(Context c) {
			context = c;
		}

		public void attachView(View view) {
	        myview = view;
	        image    		= (ImageView) 	myview.findViewById(R.id.imagepreview);
	        infoText 		= (TextView) 	myview.findViewById(R.id.InfoText);
	        savedIndicator 	= (ImageView) 	myview.findViewById(R.id.savedIndicator);
	        loadingIndicator = (View) 		myview.findViewById(R.id.loadingIndicator);
	        playIndicator 	= (ImageView) 	myview.findViewById(R.id.playIndicator);
	        HQIndicator		= (TextView) 	myview.findViewById(R.id.hq_indicator);
		}
		
		public void adjustInfo() {
        	HQIndicator.setVisibility(View.INVISIBLE);
        	
			if (submission == null) {
				infoText.setText("Loading...");
            	savedIndicator.setVisibility(View.INVISIBLE);
				return;
			}
			
            // set description
            infoText.setText(MakeTitle());

            // check if image already saved
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.getBoolean(AppConstants.PREFERENCE_IMAGE_CHECK_SAVED, true)) {
            	try {
                    File f = new File(submission.getSaveName(context));
                    if ( f.exists() ) {
                    	savedIndicator.setVisibility(View.VISIBLE);
                    } else {
                    	savedIndicator.setVisibility(View.INVISIBLE);
                    }
                } catch (Exception e) {
                }
            }
		}
		
		/**
		 *  unload picture
		 */
		public void unloadPic() {
        	// stop loading image
        	if (imageLoader != null) {
        		imageLoader.doCancel();
        		imageLoader = null;
        	}
        	
        	image.setImageBitmap(null);
        	
			// clean bitmap
        	if (imageBitmap != null) {
        		imageBitmap.recycle();
        		imageBitmap = null;
        	}
			imageLoaded = false;
		}

		/**
		 * Prepare/load picture.
		 * Set loading indicator, load thumbnail preview and initiate load HQ image
		 * @param showImage - true = show image when loaded; false = only ensure that image is downloaded
		 */
		public void loadPic(boolean showImage) {
			if (imageLoaded) return;
			if (submission == null) { // current page of submission list is loading
	           	loadingIndicator.setVisibility(View.VISIBLE);
				playIndicator.setVisibility(View.INVISIBLE);
				HQIndicator.setVisibility(View.INVISIBLE);
				return;
			}
			
        	// load thumbnail (30ms!)
            imageBitmap = ImageStorage.loadSubmissionIcon(submission.getId());
        	image.setImageBitmap(imageBitmap);
	
        	// scale/center thumbnail if it is on currently visible page
        	if (pages.get(curpageId) == this) {
        		centerImage(true, true, false, true, null);
        	}

        	HQIndicator.setText("LQ");
        	HQIndicator.setTextColor(Color.parseColor("#2C2C2C"));
			HQIndicator.setVisibility(View.VISIBLE);
        	
        	
        	// use only cached HQ images (do not download) until click if preference set
//            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean allowDL = ! Utils.getPreferences(context).getBoolean(AppConstants.PREFERENCE_IMAGE_CLICK_TO_LOAD, true); 
            startImageLoader(showImage, allowDL );
            
			if ((showImage) && (! submission.isSubmissionFileExists()) && (! allowDL))
	            showToast("Tap to download");
            
		}

		
		
		//TODO all this loading stuff should be in special download manager or at least in submission
		//TODO to avoid multiple download instances for single out file. must rework later.
		public void startImageLoader(boolean showImage, boolean allowDownload) {
			if (imageLoaded) return; // already done
			if (submission == null) return; // nothing to load

            // start load thread
           	loadingIndicator.setVisibility(View.VISIBLE);
			playIndicator.setVisibility(View.INVISIBLE);

			// can happen when click on image many times or flip to preloading page
			if (imageLoader != null) // already loading something
				if (imageLoader.getSubmissionId() == submission.getId()) { // already loading THIS submission
					// mark load file to memory when ready if it was not already requested
					mustLoadImage = showImage && imageLoader.getOnlyDl();  // onlyDL - was requested not to load in mem. On callback from loader we can try to load to mem.
					return;
				} else {
					// loading something wrong. cancel it.
					imageLoader.doCancel();
					imageLoader = null;
				}
				
			mustLoadImage = false; // we will request load to mem in new AsyncImageLoader
			// start new load / download
			// do not download if file already exist (forceDL = false) as it can be used by another download thread
            imageLoader = AsyncImageLoader.doLoad(context, this, submission, false, ! showImage, useOriginalScale, ! allowDownload);
		}
		
		/**
		 *  assign current submission
		 *  do not load image/thumbnail
		 * @param aIndex
		 */
		public void setSubmission(int aIndex) {
			// do nothing if no change
			if ( (submission != null) && (aIndex == page_submission_index))
				return;

			Submission s = submissions_list.get(aIndex);
			
			if (s == submission)
				return;
			
			unloadPic();
        	
        	// === load new submission ===
        	submission = s;
        	page_submission_index = aIndex;
        	
        	// set titles and indicators
        	adjustInfo(); // damn slowwwwwwwww (40-150ms)
        	
        	imageLoaded = false;
		}

		private void doRefresh() {
			if (imageLoader != null) {
				return;
			}
			
           	loadingIndicator.setVisibility(View.VISIBLE);
			playIndicator.setVisibility(View.INVISIBLE);
			
			if (submission == null)
				setSubmission(page_submission_index);
			
            imageLoader = AsyncImageLoader.doLoad(context, this, submission, true, false, useOriginalScale, false);
		}
		
		public void updateScale(Matrix m, float s){
			float trans[] = {0, 0};
			m.mapPoints( trans );
//			float scale = (float)1.0;
			float scale = m.mapRadius((float)1.0);
			
			m.reset();
			m.postScale(scale * s, scale * s);
			m.postTranslate(trans[0], trans[1]);
		}
		
		public void onImageLoad(int id, Object obj) {
			// do not show images for wrong submissions (late image loaders results)
			if ((submission == null) || (id != submission.getId() )) {
				if ((obj != null)&&(obj instanceof Bitmap)) {
					((Bitmap) obj).recycle();
				}
				return;
			}
			
			// load finished
           	loadingIndicator.setVisibility(View.INVISIBLE);
			imageLoader = null;
			
			// show play indicator
			if (submission.isVideo()) {
				if (submission.isSubmissionFileExists())
					playIndicator.setVisibility(View.VISIBLE);
				HQIndicator.setVisibility(View.INVISIBLE);
			}

			// AsyncLoad finished but with error or no bitmap loading was requested
			if ((obj == null)||(! (obj instanceof Bitmap))) {
				if (obj instanceof Exception)
					showToast("Error: "+((Exception) obj).getMessage());
				else
				if (mustLoadImage) // bitmap load was requested while AsyncLoad was already started
					startImageLoader(true, false); // download should be allowed on first stage. retry bmp load. do not retry download.
					
				return;
			}

			// preserve image size and position
    		RectF r = null;
    		float ratio = 1;
    		float scale = 0;
    		// if thumbnail is loaded
    		if (imageBitmap != null) {
    			r = new RectF();
        		r.set(0,0, imageBitmap.getWidth(), imageBitmap.getHeight());
        		matrix.mapRect(r);
        		
        		ratio = (float) imageBitmap.getWidth() / imageBitmap.getHeight();
        		scale = imageBitmap.getHeight();
    		} 

    		// clean bitmap if other image already loaded (thumbnail)
			unloadPic();

        	imageBitmap = (Bitmap) obj;
        	image.setImageBitmap(imageBitmap);
        	
        	imageLoaded = true;

        	HQIndicator.setText("HQ");
        	HQIndicator.setTextColor(Color.parseColor("#006C00"));
			HQIndicator.setVisibility(View.VISIBLE);
        	
        	// if image is on currently visible page
        	if (pages.get(curpageId) == this) {
        		// if ratio changes or thumb was not loaded
        		if (( Math.abs( ratio * imageBitmap.getHeight() - imageBitmap.getWidth() ) > 5 ) || (scale <= 0)) 
        			centerImage(true, true, false, false, null); // scale/center
        		else {
        			// preserve size and position
        			scale = (float) scale / imageBitmap.getHeight();
        			updateScale(savedMatrix, scale);
        			updateScale(matrix, scale);
        			pages.get(curpageId).image.setImageMatrix(matrix);
        		}
        			
        	}
		}

	    public void finish() {
	    	if (imageLoader != null) {
	    		imageLoader.doCancel();
	    		imageLoader = null;
	    	}
	    	
	    	unloadPic();

	    	image = null;
	    	submission = null;
	    	savedIndicator = null;
	    	infoText = null;
	    	context = null;
	    	myview = null;
	    }

	    public void retreive(String num) {
            context = (Context)  retrieveObject("pholder_context_"+num);
            imageLoader = (AsyncImageLoader)  retrieveObject("pholder_loader_"+num);
            imageBitmap = (Bitmap)  retrieveObject("pholder_bitmap_"+num);
            submission = (Submission) retrieveObject("pholder_submission_"+num);

            if (imageBitmap != null) {
            	image.setImageBitmap(imageBitmap);
            }
            
            if (imageLoader != null) {
            	loadingIndicator.setVisibility(View.VISIBLE);
				playIndicator.setVisibility(View.INVISIBLE);
            } else if ((submission != null) && (submission.isVideo())) {
				playIndicator.setVisibility(View.VISIBLE);
            }
            
            // set titles and indicators
            if (submission != null) {
            	adjustInfo();
            }
	    }

	    public void store(String num) {
            storeObject("pholder_context_"+num, context);
            storeObject("pholder_loader_"+num, imageLoader);
            storeObject("pholder_bitmap_"+num, imageBitmap);
            storeObject("pholder_submission_"+num, submission);
	    }

	    private String MakeTitle() {
			if (submission == null)
				return "";
			else
				return submission.getAuthorName()+": "+submission.getName();
	    }

	    /**
	     * Saves the file to the images folder
	     */
	    public void save() {
	    	if (submission == null)
	    		return;
	    	
	        try {
	            // source file in cache
	            File f = new File(ImageStorage.getSubmissionImagePath(submission.getCacheName()));
	            if (! f.exists()) {
	                throw new Exception("File has not downloaded properly yet. File does not exist.");
	            }

	            // target file in user images library
	            String targetPath = submission.getSaveName(context);
	            
	            File tf = new File(targetPath);
	            FileStorage.ensureDirectory(tf.getParent());
	            
	            // save file to user image library
	            FileStorage.copyFile(f, tf);
	            showToast("File saved to:\n" + targetPath);
	            
	            // display saved indicator
	            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(AppConstants.PREFERENCE_IMAGE_CHECK_SAVED, true)) {
	               	savedIndicator.setVisibility(View.VISIBLE);
	            }
	        } catch (Exception e) {
	            onError(e);
	        }
	    }
	}
	
	// ---------------------------------------------------------
	private LayoutInflater mInflater = null;
	
	private ArrayList<PageHolder> pages = null;
	private int curpageId = 0;
    
    private ArrayList<Submission> submissions_list = null;
    private int submissions_index = 0;

    private boolean disableMoreArt = false;
    		
	// screen dragging support
    private PointF downPoint = new PointF();
	private long downTimer;
	private long prevClickTime = 0;
	private Boolean mHasDoubleClicked = false;
	private VelocityTracker VTracker = null;
	
	// scaling
	float downDist = 0;
	PointF midPoint = new PointF();
	
	// These matrices will be used to move and zoom image
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();

	// Gesture states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

    // click feel constants
    static final float maxClickLength = 15;
    static final int maxClickTime = 200;
    static final int maxClickVelocity = 20;
    static final long doubleClickDuration = 500;


	private Animation aLeftIn = null;
	private Animation aLeftOut = null;
	private Animation aRightIn = null;
	private Animation aRightOut = null;

    /**
     * Method description
     *
     *
     * @param menu
     */
    @Override
    public void createExtraMenuOptions(Menu menu) {
        menu.add(0, AppConstants.MENU_HD, 0, "HD View").setIcon(android.R.drawable.ic_menu_gallery);
        menu.add(0, AppConstants.MENU_REFRESH, 0, "Reload").setIcon(android.R.drawable.ic_menu_rotate);
        super.createExtraMenuOptions(menu);
    }

    /**
     * If submission file downloaded then open it in associated viewer
     */
    public void doHdView(Submission s) {
    	File f = s.getSubmissionFile(); // return null if file does not exist
    	if (f == null)
    		return;
    		
    	// Starts the associated image viewer, so the user can zoom and tilt
    	Intent intent = new Intent();

    	intent.setAction(android.content.Intent.ACTION_VIEW);
    	if (s.FileExt.equals(".swf")) {
        	intent.setDataAndType(Uri.fromFile(f), "video/*");
    	} else {
        	intent.setDataAndType(Uri.fromFile(f), "image/*");
    	}
    	startActivity(intent);
    }

    /**
     * Method description
     *
     *
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
    	PageHolder curpage = null;

    	super.onCreate(savedInstanceState);

        pages = new ArrayList<ViewArtActivity.PageHolder>();
        
        setContentView(R.layout.artdetails);
        FixedViewFlipper imageFlipper = (FixedViewFlipper) findViewById(R.id.viewFlipper1);

        // init pages for flipper
        mInflater = LayoutInflater.from(this);
        
        curpage = new PageHolder(this);
        curpage.attachView(mInflater.inflate(R.layout.artdetails_page_tmpl, null));
        imageFlipper.addView(curpage.myview);
        pages.add(curpage);

        curpage = new PageHolder(this);
        curpage.attachView(mInflater.inflate(R.layout.artdetails_page_tmpl, null));
        imageFlipper.addView(curpage.myview);
        pages.add(curpage);

        curpage = new PageHolder(this);
        curpage.attachView(mInflater.inflate(R.layout.artdetails_page_tmpl, null));
        imageFlipper.addView(curpage.myview);
        pages.add(curpage);

        curpage = pages.get(0);

        // load data
        if (savedInstanceState == null) {
        	// init new object
            Bundle extras = getIntent().getExtras();

            if (extras != null) {
            	submissions_list = (ArrayList<Submission>) extras.get("list");
            	if (submissions_list == null)
					submissions_list = NetworkListStorage.get(extras.getLong("listId"));

            	submissions_index = (int) extras.getInt("listIndex");

            	disableMoreArt = extras.getBoolean("NoMoreFromUserButton", false); 
            	
            	// only assign submissions. load will be performed by onResume
        		pages.get(0).setSubmission(submissions_index);
            	if (submissions_index < submissions_list.size()-1) {
            		pages.get(1).setSubmission(submissions_index+1);
            	}
            	if (submissions_index > 0) {
            		pages.get(2).setSubmission(submissions_index-1);
            	}
            }

            matrix.reset();
        } else {
        	// load saved object
            submissions_list = (ArrayList<Submission>)  retrieveObject("list");
            
            Long listid = null;
        	if (submissions_list == null)
        		if ( (listid = (Long) retrieveObject("listId")) != null )
        			submissions_list = NetworkListStorage.get(listid);

 //       	if (submissions_list == null)
 //       		throw new Exception("submission list was not restored");
        	
            submissions_index = (Integer) retrieveObject("listIndex");
            matrix = (Matrix) retrieveObject("matrix");
            
            pages.get(0).retreive("0");
            pages.get(1).retreive("1");
            pages.get(2).retreive("2");
            
            curpageId = (Integer) retrieveObject("pageId");
            imageFlipper.setDisplayedChild(curpageId);
            
            disableMoreArt = (Boolean) retrieveObject("disableMoreArt");
        }
        
        updateSideMenu();
        
        // init dragging
        imageFlipper.setOnTouchListener((OnTouchListener) this);
        imageFlipper.captureAllTouch = true;
        VTracker = VelocityTracker.obtain();
        
        // prepare animation
    	aLeftIn = AnimationUtils.loadAnimation(this, R.anim.push_left_in);
    	aLeftOut = AnimationUtils.loadAnimation(this, R.anim.push_left_out);
    	aRightIn = AnimationUtils.loadAnimation(this, R.anim.push_right_in);
    	aRightOut = AnimationUtils.loadAnimation(this, R.anim.push_right_out);
    }

    public void updateSideMenu() {
        SharedPreferences prefs        = Utils.getPreferences(this);

        // init side menu
/*        Button ArtistGalleryButton = (Button) findViewById(R.id.ArtistGalleryButton);
        Button SaveButton = (Button) findViewById(R.id.SaveBtn);
        Button BackButton = (Button) findViewById(R.id.BackBtn);

        ArtistGalleryButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View arg0) {
                morefromuser(GalleryArtActivity.class,AppConstants.ACTIVITY_GALLERYART);
            }
        });
        
        SaveButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View arg0) {
                save();
            }
        });
        
        BackButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View arg0) {
                finish();
            }
        });/**/

        
        // setting menu position
        String MenuPosition = prefs.getString(AppConstants.PREFERENCE_IMAGE_MENU_POSITION, "0");
    	LinearLayout menuLayout = (LinearLayout) findViewById(R.id.menuLayout);
    	RelativeLayout.LayoutParams params = null;
        if ( MenuPosition.equals("1") ) {
        	menuLayout.setOrientation(LinearLayout.HORIZONTAL);
        	params = new RelativeLayout.LayoutParams(  LayoutParams.FILL_PARENT, 
        			    								LayoutParams.WRAP_CONTENT);
        	params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        	menuLayout.setLayoutParams(params);

        } else if (MenuPosition.equals("2")) {
        	menuLayout.setOrientation(LinearLayout.VERTICAL);
        	params = new RelativeLayout.LayoutParams(  LayoutParams.WRAP_CONTENT, 
        											LayoutParams.FILL_PARENT);
        	params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        	menuLayout.setLayoutParams(params);

        } else if (MenuPosition.equals("3")) {
        	menuLayout.setOrientation(LinearLayout.VERTICAL);
        	params = new RelativeLayout.LayoutParams(  LayoutParams.WRAP_CONTENT, 
        											LayoutParams.FILL_PARENT);
        	params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        	menuLayout.setLayoutParams(params);

        } else {
        	menuLayout.setVisibility(View.INVISIBLE);
		}	

        // reading button visibility from settings
        int i = 0;
        while (i < menuLayout.getChildCount()) {
        	View btn = menuLayout.getChildAt(i);
        	if (	(btn instanceof Button) &&
        			(btn.getTag() instanceof String) ) {
    			if (! prefs.getBoolean((String)btn.getTag(), true)    )
    				btn.setVisibility(View.GONE);
    			else
        		if (prefs.getBoolean((String)btn.getTag(), false)    )
        			btn.setVisibility(View.VISIBLE);
        	}
        	i++;
        }
    	
    	if (disableMoreArt) {
    		Button ArtistGalleryButton = (Button) findViewById(R.id.ArtistGalleryButton);
    		ArtistGalleryButton.setVisibility(View.GONE);
    	}
    }
    
    public void menuButtonClick(View v) {
        int id = v.getId();

        switch (id) {
        	case R.id.ArtistGalleryButton:
                morefromuser(GalleryArtActivity.class,AppConstants.ACTIVITY_GALLERYART);
        		break;

        	case R.id.SaveBtn:
                save();
        		break;

        	case R.id.BackBtn:
                finish();
        		break;
        		
        	case R.id.WatchBtn:
        		watch();
        		break;
        		
        	case R.id.FavBtn:
        		setFavorite();
        		break;
        }
    	
    }
    
    /**
     * Load submission to page if it was not loaded
     * @param PageID
     */
    public void RefreshPage(int PageID) {
    	PageHolder p = pages.get(PageID);
    	if (p.submission != null)
    		return;
    	
    	p.setSubmission(p.page_submission_index);
        p.loadPic(PageID == curpageId);
    }
    
    /*
     *  (non-Javadoc)
     * @see com.sofurry.IManagedActivity#finish()
     */

    @Override
    public void finish() {
    	if (submissions_list instanceof NetworkList)
        	((NetworkList) submissions_list).setStatusListener(null);
    	
        if (pages != null) {
    		pages.get(0).finish();
    		pages.get(1).finish();
    		pages.get(2).finish();
    		pages.clear();
    		pages = null;
    	};
    	
    	submissions_list = null;
		findViewById(R.id.viewFlipper1).getViewTreeObserver().removeOnPreDrawListener(this);

		Intent intent = new Intent();
		intent.putExtra("JumpTo", submissions_index);
		setResult(RESULT_OK, intent);			
		
        super.finish();
        System.gc();
    }

    @Override
	protected void onResume() {
		super.onResume();

        if (submissions_list instanceof NetworkList)
        	((NetworkList) submissions_list).setStatusListener(new IJobStatusCallback() {
				
				@Override
				public void onSuccess(Object job) {
					RefreshPage(0);
					RefreshPage(1);
					RefreshPage(2);
//					LoadThumbnails(); // moved to SFSubmissionList
				}
				
				@Override
				public void onStart(Object job) {
					showToast("Loading page...");
				}
				
				@Override
				public void onProgress(Object job, int progress, int total, String msg) {
				}
				
				@Override
				public void onError(Object job, String msg) {
					showToast("Error load page: "+msg);
				}
			});

        findViewById(R.id.viewFlipper1).getViewTreeObserver().addOnPreDrawListener(this);
        
        updateSideMenu();
	}


	public boolean onPreDraw() {
		findViewById(R.id.viewFlipper1).getViewTreeObserver().removeOnPreDrawListener(this);
    	if (pages != null) {
    		pages.get(curpageId).loadPic(true);
    	}

		return true;
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (pages != null) { // when destroing activity finish() is called first and destroy pages array
			pages.get(0).unloadPic();
			pages.get(1).unloadPic();
			pages.get(2).unloadPic();
		}
	}


    /**
     * Method description
     *
     *
     * @param id
     * @param e
     */
    @Override
    public void onError(Exception e) {
        super.onError(e);
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
                doHdView(pages.get(curpageId).submission);

                return true;

            case AppConstants.MENU_REFRESH:
            	pages.get(curpageId).doRefresh();
            	
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
//        pbh.hideProgressDialog();

        super.onOther(id, obj);
    }

    /**
     * Method description
     *
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        pages.get(0).store("0");
        pages.get(1).store("1");
        pages.get(2).store("2");
    	
        if (submissions_list instanceof NetworkList)
        	storeObject("listId", (Long) ((NetworkList<Submission>) submissions_list).getListId());
        else
        	storeObject("list", submissions_list);
        
        storeObject("listIndex", submissions_index);
        storeObject("pageId", curpageId);
        storeObject("matrix", matrix);
        
        storeObject("disableMoreArt", disableMoreArt);
        
        super.onSaveInstanceState(outState);
    }


	@Override
	public void save() {
		pages.get(curpageId).save();
	}

	public boolean showNext() {
		if (submissions_index < submissions_list.size()-1) {

			submissions_index ++;
			assignSubmission(submissions_list.get(submissions_index));

			int oldpageId = curpageId;
			
			curpageId ++;
			if (curpageId > 2) {
				curpageId = 0;
			}

			// reset image transformations
			matrix.reset();
			pages.get(curpageId).image.setImageMatrix(matrix);

			// start load image
			pages.get(curpageId).loadPic(true);
			
			 // preload next image
			int nextpage = curpageId + 1;
			if (nextpage > 2) {
				nextpage = 0;
			}
			
			if (submissions_index < submissions_list.size()-1) {
				pages.get(nextpage).setSubmission(submissions_index+1);
				pages.get(nextpage).loadPic(false); // preload file, do not load bitmap
			}
			
			// Get a reference to the ViewFlipper
	        ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipper1);
	         // Set the animation
	         vf.setInAnimation(aRightIn);
	         vf.setOutAnimation(aLeftOut);
	          // Flip!
	         vf.showNext();
	         
			 pages.get(oldpageId).unloadPic();
			
			 return true;
		}
		return false;
	}
	
	public boolean showPrev() {
		if (submissions_index > 0) {
			submissions_index --;
			assignSubmission(submissions_list.get(submissions_index));

			int oldpageId = curpageId;

			curpageId --;
			if (curpageId < 0) {
				curpageId = 2;
			}

			// reset image transformations
			matrix.reset();
			pages.get(curpageId).image.setImageMatrix(matrix);

			// start load image
			pages.get(curpageId).loadPic(true);
			
			// preload prev image
			int prevpage = curpageId - 1;
			if (prevpage < 0) {
				prevpage = 2;
			}
			
			if (submissions_index > 0) {
				pages.get(prevpage).setSubmission(submissions_index-1);
				pages.get(prevpage).loadPic(false); // preload file, do not load bitmap
			}
			
            // Get a reference to the ViewFlipper
            ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipper1);
            // Set the animation
	         vf.setInAnimation(aLeftIn);
	         vf.setOutAnimation(aRightOut);
             // Flip!
             vf.showPrevious();

             pages.get(oldpageId).unloadPic();
             
             return true;
		}
		return false;
	}

	public void setViewPosition(int dX, int dY) {
    	View centerview = pages.get(curpageId).myview; 
    	centerview.setVisibility(View.INVISIBLE);
    	centerview.layout(dX, dY, centerview.getWidth() + dX, centerview.getHeight() + dY);
    	centerview.setVisibility(View.VISIBLE);
	}

	public void getViewPosition(PointF offset) {
    	View centerview = pages.get(curpageId).myview;
    	offset.x = centerview.getLeft();
    	offset.y = centerview.getTop();
	}

	private void midPoint(PointF point, MotionEvent event) {
		   float x = event.getX(0) + event.getX(1);
		   float y = event.getY(0) + event.getY(1);
		   point.set(x / 2, y / 2);
	}
	
	private float spacing(MotionEvent event) {
		   float x = event.getX(0) - event.getX(1);
		   float y = event.getY(0) - event.getY(1);
		   return FloatMath.sqrt(x * x + y * y);
	}

	/**
	 * Update image transform matrix
	 * @param forceCenter - move to view center
	 * @param doFit - fit in to view
	 * @param rescale - fit if image scaled smaller than view
	 * @param allowEnlarge
	 * aBounds - fit to this bounds. if null - fit to page
	 * @return
	 */
	public float centerImage(Boolean forceCenter, Boolean doFit, Boolean rescale, Boolean allowEnlarge, RectF aBounds) {
    	View pageView = findViewById(R.id.viewFlipper1);
    	ImageView img = pages.get(curpageId).image;
    	Bitmap bmp = pages.get(curpageId).imageBitmap;
    	if (bmp == null)
    		return 0;
    	RectF r = new RectF();
    	r.set(0,0,bmp.getWidth(),bmp.getHeight());
    	matrix.mapRect(r);

    	if (rescale && (r.width() < pageView.getWidth()) && (r.height() < pageView.getHeight())) 
    		doFit = true;

    	if (doFit) {
    		matrix.reset();
    		float scale = 1;
    		if (aBounds != null)
    			scale = Math.min( (float) aBounds.width() / bmp.getWidth(), (float) aBounds.height() / bmp.getHeight() );
    		else
    			scale = Math.min( (float) pageView.getWidth() / bmp.getWidth(), (float) pageView.getHeight() / bmp.getHeight() );
    		
    		if ((scale > 0 )&&((scale < 1) || (allowEnlarge))) // do not enlarge
    			matrix.postScale(scale, scale);
	    	r.set(0,0,bmp.getWidth(),bmp.getHeight());
	    	matrix.mapRect(r);
    	}
    	
    	float dx = 0;
    	float dy = 0;
    	if (forceCenter) {
        	dx = pageView.getWidth()/2 - r.centerX();
        	dy = pageView.getHeight()/2 - r.centerY();
    	} else if (aBounds != null) {
    		dx = aBounds.left;
    		dy = aBounds.top;
    	} else {
    		if (r.width() <= pageView.getWidth()) {
            	dx = pageView.getWidth()/2 - r.centerX();
    		} else if (r.left > 0) {
    			dx = -r.left;
    		} else if (r.right < pageView.getWidth()) {
    			dx = pageView.getWidth() - r.right;
    		}

    		if (r.height() <= pageView.getHeight()) {
            	dy = pageView.getHeight()/2 - r.centerY();
    		} else if (r.top > 0) {
    			dy = -r.top;
    		} else if (r.bottom < pageView.getHeight()) {
    			dy = pageView.getHeight() - r.bottom;
    		}
    	}
    		
    	matrix.postTranslate(dx, dy);
    	
    	img.setImageMatrix(matrix);
    	
    	if ((r.left < 0)&&(r.right > pageView.getWidth())) {
    		return 0;
    	} else if ( (r.left > 0)&&(r.right < pageView.getWidth()) ) {
    		return r.left + r.right - pageView.getWidth();
    	} else if (r.left > 0) {
    		return r.left;
    	} else
    		return r.right - pageView.getWidth();
	}
	
	// calculate distance to move page in flipper
	public void calculatePageDisplacement(PointF delta) {
		if ((matrix == null) || (delta == null))
			return;
		
    	View pageView = findViewById(R.id.viewFlipper1);
    	Bitmap bmp = pages.get(curpageId).imageBitmap;
    	if (bmp == null)
    		return;
    	RectF r = new RectF();
//    	r.set(img.getDrawable().getBounds());
    	r.set(0,0,bmp.getWidth(),bmp.getHeight());
    	
    	matrix.mapRect(r);
    	
    	delta.x = 0;
    	delta.y = 0;

       	if ( (r.width() <= pageView.getWidth()) ) {
    		delta.x = (r.left + r.right - pageView.getWidth())/2;
    	} else if (r.left > 0) {
    		delta.x = r.left;
    	} else if (r.right < pageView.getWidth())
    		delta.x = r.right - pageView.getWidth();

       	if ( (r.height() <= pageView.getHeight()) ) {
    		delta.y = (r.top + r.bottom - pageView.getHeight())/2;
    	} else if (r.top > 0) {
    		delta.y = r.top;
    	} else if (r.bottom < pageView.getHeight())
    		delta.y = r.bottom - pageView.getHeight();
	}
	

	public boolean onTouch(View arg0, MotionEvent arg1) {
        // Get the action that was done on this touch event
        switch (arg1.getAction() & MotionEvent.ACTION_MASK)
        {
        	
            case MotionEvent.ACTION_DOWN:
            {
                // store the X value when the user's finger was pressed down
            	downPoint.set(arg1.getX(), arg1.getY());
                downTimer = System.currentTimeMillis();
                VTracker.clear();
                VTracker.addMovement(arg1);
                
                savedMatrix.set(matrix);
                
                // deny single click if new gesture started
            	if (System.currentTimeMillis() - prevClickTime <= doubleClickDuration) {
            		mHasDoubleClicked = true;
            	}
            	
                mode = DRAG;
                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN:
            {
            	downDist = spacing(arg1);
            	if (downDist > 10f) {
            		// start scaling
            	    PointF ofs = new PointF();
            	    getViewPosition(ofs);
            	    matrix.postTranslate(ofs.x, ofs.y);
            	    pages.get(curpageId).image.setImageMatrix(matrix);
            	    setViewPosition(0, 0);

            	    mode = ZOOM;
            		savedMatrix.set(matrix);
            	    midPoint(midPoint, arg1);
            	}
            	break;
            }
            
            case MotionEvent.ACTION_POINTER_UP:
            {
            	// set matrix and point to drag mode
            	savedMatrix.set(matrix);
            	int pid = ( arg1.getAction() & MotionEvent.ACTION_POINTER_ID_MASK ) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
            	downPoint.set(arg1.getX(1 - pid), arg1.getY(1 - pid));
            	mode = DRAG;
            	
            	break;
            }
            
            case MotionEvent.ACTION_UP:
            {
            	// end of drag event
            	mode = NONE;
            	
                float currentX = arg1.getX();
                float dX = downPoint.x - currentX;
                long clickTime = System.currentTimeMillis();
                long clickDuration = clickTime - downTimer;
                PointF d = new PointF();
                getViewPosition(d);
                
                // flip feel constants
                int longflipLength = pages.get(curpageId).myview.getWidth() / 3;
                int MinFlipVelocity = 40; 

                // read drag speed
                VTracker.addMovement(arg1);
                VTracker.computeCurrentVelocity(100); // pixels per last 100ms
                float vx = VTracker.getXVelocity();
                VTracker.recycle();

                // CLICK
                if ( (clickDuration < maxClickTime) && (Math.abs(dX) < maxClickLength) && (Math.abs(vx) < maxClickVelocity) ) {
                	// click
                	
                	if (clickTime - prevClickTime <= doubleClickDuration) {
                		// double click
                		mHasDoubleClicked = true;
                		
                		// zoom to actual pixels, keep tapped point
                		float[] p = {arg1.getX(), arg1.getY()};
                		Matrix m = new Matrix();
                		matrix.invert(m);
                		m.mapPoints(p);
                		matrix.reset();
                		matrix.postTranslate(arg1.getX() - p[0], arg1.getY() - p[1]);
            		    pages.get(curpageId).image.setImageMatrix(matrix);
                		
                	} else {
                		// wait for second click
                		mHasDoubleClicked = false;
                        Handler myHandler = new Handler() {
                             public void handleMessage(Message m) {
                                  if (!mHasDoubleClicked) {
                                	  doImageClick();
                                  }
                             }
                        };
                        Message m = new Message();
                        myHandler.sendMessageDelayed(m, doubleClickDuration);
                	}/**/

                	prevClickTime = clickTime;
                    return true;
                } else
                // DRAG

                if ( (Math.abs(vx) >= MinFlipVelocity) ) {
                	// velocity flip
                    if (vx > 0)
                    	if (showPrev()) return true;

                    if (vx < 0)
                    	if (showNext()) return true;
                    
                } else if (Math.abs(d.x) > longflipLength) {
                	// position flip
                	if (dX > 0)
                    	if (showNext()) return true;
                	
                	if (dX < 0)
                    	if (showPrev()) return true;
                }
                
            	// NONE of finally for DRAG (if page was not flipped)
                // return to center animation
                if (pages.get(curpageId).imageLoaded)
                	centerImage(false, false, true, false, null);
                setViewPosition(0, 0);
                
                break;
            }
            case MotionEvent.ACTION_MOVE:
            {
                VTracker.addMovement(arg1);
                
            	switch (mode) {
            		case DRAG: {
            			// move image inside view
                    	matrix.set(savedMatrix);
                        matrix.postTranslate(arg1.getX() - downPoint.x, arg1.getY() - downPoint.y);

                        // move page
                        PointF d = new PointF();
                        calculatePageDisplacement(d);
                        matrix.postTranslate(-d.x, 0);
                        setViewPosition(Math.round(d.x), 0);
                        
            		    pages.get(curpageId).image.setImageMatrix(matrix);
                        break;
            		}
            		
            		case ZOOM: {
            			float newDist = spacing(arg1);
            		    if (newDist > 10f) {
            		       matrix.set(savedMatrix);
            		       float scale = newDist / downDist;
            		       matrix.postScale(scale, scale, midPoint.x, midPoint.y);
            		    }
            		    pages.get(curpageId).image.setImageMatrix(matrix);
            			break;
            		}
            	}
                
                break;
            }
        }

        // if you return false, these actions will not be recorded
        return true;
	}
	
	public void doImageClick() {
		SharedPreferences prefs = Utils.getPreferences(this);
/*		if ( ( ! prefs.getBoolean(AppConstants.PREFERENCE_IMAGE_CLICK_TO_LOAD, true)) || 
			 ( pages.get(curpageId).imageLoaded ) || // short check
			 ( pages.get(curpageId).submission.isSubmissionFileExists() ) // hard check 
			) {
			if (pages.get(curpageId).imageLoader != null)
	            showToast("Warning: download in progress");
	  		doHdView(pages.get(curpageId).submission);
		} else
			pages.get(curpageId).startImageLoader(true, true);/**/

		boolean fieloaded = ( pages.get(curpageId).imageLoaded ) || // short check
				 			( pages.get(curpageId).submission.isSubmissionFileExists() ); // hard check
		
		if (fieloaded) {
				if (pages.get(curpageId).imageLoader != null)
		            showToast("Warning: download in progress");
		  		doHdView(pages.get(curpageId).submission);
		} else
			pages.get(curpageId).startImageLoader(true, true); // will handle running downloads correctly
		/**/
	}
	
	private void showToast(String msg) {
		if (mytoast != null)
			mytoast.setText(msg);
		else
			mytoast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);

		mytoast.show();
	}
	
}
