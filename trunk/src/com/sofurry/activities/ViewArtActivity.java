package com.sofurry.activities;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;

import android.net.Uri;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.preference.PreferenceManager;

import android.util.FloatMath;
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
import com.sofurry.mobileapi.downloaders.AsyncImageLoader;
import com.sofurry.model.Submission;
import com.sofurry.storage.FileStorage;
import com.sofurry.storage.ImageStorage;

import java.io.File;
import java.util.ArrayList;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.lang.Math;



/**
 * Class description
 *
 */
public class ViewArtActivity
        extends FavableActivity 
        implements OnTouchListener, OnPreDrawListener {
	
	static final Boolean useOriginalScale = true;
	
	// current page controls (change when page flips)
	private class PageHolder implements AsyncImageLoader.IImageLoadResult{
		private Bitmap    imageBitmap = null;
		private Boolean   imageLoaded = false;
		private ImageView image = null; 
		private ImageView savedIndicator = null;
		private ImageView playIndicator = null;
		private TextView  InfoText = null;
		private View 	  loadingIndicator = null;
		private View	  myview = null;	
		
		private Submission submission = null;
		private AsyncImageLoader imageLoader = null;
		
		private Context context = null;
		
		public PageHolder(Context c) {
			context = c;
			/*			mutex1 = new Object();
			myThumbLoader = new thumbLoader();
			myThumbLoader.run();/**/
		}

/*		private thumbLoader myThumbLoader = null;
		
		private class thumbLoader extends Thread {
			public boolean cancelled = false;
			
			@Override
			public void run() {
				try {
					while (! cancelled) {
//						mutex1.wait();
						if (! cancelled) {
							imageBitmap = ImageStorage.loadSubmissionIcon(submission.getId());
							image.setImageBitmap(imageBitmap);
						}
					}
				} catch (Exception e) {
					Log.i(AppConstants.TAG_STRING, e.getMessage());
				}
			}
		}/**/

		public void adjustInfo() {
//			MemoryInfo mi = new MemoryInfo(); // DEBUG
//			Debug.getMemoryInfo(mi);

/*			MemoryInfo mi = new MemoryInfo();
			android.app.ActivityManager activityManager = (android.app.ActivityManager) getSystemService(ACTIVITY_SERVICE);
			activityManager.getMemoryInfo(mi);
			long availableMegs = mi.availMem / 1048576L; /**/
			
            // set description
            InfoText.setText(MakeTitle());
//          InfoText.setText(MakeTitle()+" Dalvik: "+mi.dalvikPss+" Native: "+mi.nativePss+" Other: "+mi.otherPss);

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
		
		// unload picture
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

		// load picture.
		public void loadPic(boolean showImage) {
			if (imageLoaded) return;
			if (submission == null) return;
			
        	// load thumbnail (30ms!)
            imageBitmap = ImageStorage.loadSubmissionIcon(submission.getId());
        	image.setImageBitmap(imageBitmap);
	
        	// scale/center thumbnail if it is on currently visible page
        	if (pages.get(curpageId) == this) {
        		centerImage(true, true, false);
        	}
        	
//			(new thumbLoader()).run();
//			myThumbLoader.notify();

/*			// can possible cause interference with image loading while setting/using imageBitmap
			Thread t = new Thread() {
				public void run() {
					imageBitmap = ImageStorage.loadSubmissionIcon(submission.getId());
					image.setImageBitmap(imageBitmap);
				}
			};
			t.start();/**/
        	
            // start load thread
           	loadingIndicator.setVisibility(View.VISIBLE);
			playIndicator.setVisibility(View.INVISIBLE);

            imageLoader = AsyncImageLoader.doLoad(context, this, submission, false, ! showImage, useOriginalScale);
		}

		// assign current submission
		// do not load image/thumbnail
		public void setSubmission(Submission s) {
			// do nothing if no change
			if (s == submission) {
				return;
			}
			
			// === clean loaded submission ===
        	// clean bitmap in case of reload
/*        	if (imageBitmap != null) {
        		imageBitmap.recycle();
        		imageBitmap = null;
        	}/**/
			
			unloadPic();
        	
        	// === load new submission ===
        	submission = s;
        	
        	// set titles and indicators
        	adjustInfo(); // damn slowwwwwwwww (40-150ms)
        	
        	/*        	// can cause unproper info loading in case of fast submission changes
        	Thread t = new Thread() {
				public void run() {
		        	adjustInfo();
				}
        	};
        	t.start();/**/

        	imageLoaded = false;
//        	loadPic(); // load only when needed

		}

		private void doRefresh() {
			if (imageLoader != null) {
				return;
			}
			
           	loadingIndicator.setVisibility(View.VISIBLE);
			playIndicator.setVisibility(View.INVISIBLE);
            imageLoader = AsyncImageLoader.doLoad(context, this, submission, true, false, useOriginalScale);
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
				playIndicator.setVisibility(View.VISIBLE);
			}

			// if no bitmap loaded or error
			if ((obj == null)||(! (obj instanceof Bitmap))) {
				return;
			}

			// clean bitmap if other image already loaded
			unloadPic();

        	imageBitmap = (Bitmap) obj;
        	image.setImageBitmap(imageBitmap);
        	
        	imageLoaded = true;
        	
        	// scale/center image if it is on currently visible page
        	if (pages.get(curpageId) == this) {
        		centerImage(true, true, false);
        	}
		}

	    public void finish() {
	    	if (imageLoader != null) {
	    		imageLoader.doCancel();
	    		imageLoader = null;
	    	}
	    	
	    	/*	    	myThumbLoader.cancelled = true;
	    	myThumbLoader.notify();
	    	myThumbLoader = null;/**/
	    	
	    	unloadPic();

	    	image = null;
	    	submission = null;
	    	savedIndicator = null;
	    	InfoText = null;
	    	context = null;
	    	myview = null;
	    }

	    public void retreive(String num) {
            context = (Context)  retrieveObject("pholder_context_"+num);
            imageLoader = (AsyncImageLoader)  retrieveObject("pholder_loader_"+num);
            imageBitmap = (Bitmap)  retrieveObject("pholder_binmap_"+num);
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
            storeObject("pholder_binmap_"+num, imageBitmap);
            storeObject("pholder_submission_"+num, submission);
	    }

	    private String MakeTitle() {
	    	return submission.getAuthorName()+": "+submission.getName();
	    }

	    /**
	     * Saves the file to the images folder
	     */
	    public void save() {
	        try {
	            // source file in cache
	            File f = new File(ImageStorage.getSubmissionImagePath(submission.getCacheName()));
	            if (! f.exists()) {
	                throw new Exception("File has not downloaded properly yet. File does not exist.");
	            }

	            // target file in user images library
	            String targetPath = submission.getSaveName(context);
	            
	            // create directories. alredy done by ensureDirectory
//	            File td = new File(targetPath.substring(0, targetPath.lastIndexOf('/')));
//	            td.mkdirs();
	            
	            File tf = new File(targetPath);
	            FileStorage.ensureDirectory(tf.getParent());
	            
	            // save file to user image library
	            FileStorage.copyFile(f, tf);
	            Toast.makeText(getApplicationContext(), "File saved to:\n" + targetPath, Toast.LENGTH_LONG).show();
	            
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
	private ArrayList<PageHolder> pages = null;
	private int curpageId = 0;
    
    private ArrayList<Submission> submissions_list = null;
    private int submissions_index = 0;
//    protected ActivityManager man = null; 
    		
	// screen dragging support
/*	private float downXValue; 
	private float downYValue;/**/
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

/*	// definitions to use with old API	
	static final int ACTION_MASK = 0x000000ff;
	static final int ACTION_POINTER_UP = 0x00000006;
	static final int ACTION_POINTER_DOWN = 0x00000005;/**/
	
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
        menu.add(0, AppConstants.MENU_REFRESH, 0, "Reload").setIcon(android.R.drawable.ic_menu_rotate);
        super.createExtraMenuOptions(menu);
    }

    /**
     * Views the button using the acitivty that is associated with images
     */
    public void doHdView(Submission s) {
    	File f = null;
    	
    	try {
    		SharedPreferences prefs        = PreferenceManager.getDefaultSharedPreferences(this);
    		if (prefs.getBoolean(AppConstants.PREFERENCE_IMAGE_USE_LIB, false)) {
    			f = new File(s.getSaveName(this));
    		}
    	} catch (Exception e) {
		} 
    	
    	if ( (f == null) || (!f.exists())) {
    		f = new File(ImageStorage.getSubmissionImagePath(s.getCacheName()));
    		if (!f.exists()) {
    			return;    // Until that file exists, there is nothing we can do really.
    		}
    	}
    		
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
        
//      Authentication.loadAuthenticationInformation(this);
        setContentView(R.layout.artdetails);
        FixedViewFlipper imageFlipper = (FixedViewFlipper) findViewById(R.id.viewFlipper1);

        // init pages for flipper
        curpage = new PageHolder(this);
        curpage.myview = (View) findViewById(R.id.page1);
        curpage.image           = (ImageView) findViewById(R.id.imagepreview1);
        curpage.InfoText = (TextView) findViewById(R.id.InfoText1);
        curpage.savedIndicator = (ImageView) findViewById(R.id.savedIndicator1);
        curpage.loadingIndicator = (View) findViewById(R.id.loadingIndicator1);
        curpage.playIndicator = (ImageView) findViewById(R.id.playIndicator1);

        pages.add(curpage);

        curpage = new PageHolder(this);
        curpage.myview = (View) findViewById(R.id.page2);
        curpage.image           = (ImageView) findViewById(R.id.imagepreview2);
        curpage.InfoText = (TextView) findViewById(R.id.InfoText2);
        curpage.savedIndicator = (ImageView) findViewById(R.id.savedIndicator2);
        curpage.loadingIndicator = (View) findViewById(R.id.loadingIndicator2);
        curpage.playIndicator = (ImageView) findViewById(R.id.playIndicator2);
        pages.add(curpage);

        curpage = new PageHolder(this);
        curpage.myview = (View) findViewById(R.id.page3);
        curpage.image           = (ImageView) findViewById(R.id.imagepreview3);
        curpage.InfoText = (TextView) findViewById(R.id.InfoText3);
        curpage.savedIndicator = (ImageView) findViewById(R.id.savedIndicator3);
        curpage.loadingIndicator = (View) findViewById(R.id.loadingIndicator3);
        curpage.playIndicator = (ImageView) findViewById(R.id.playIndicator3);
        pages.add(curpage);
        
        curpage = pages.get(0);

        // init side menu
        Button ArtistGalleryButton = (Button) findViewById(R.id.ArtistGalleryButton);
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
        });

        
        SharedPreferences prefs        = PreferenceManager.getDefaultSharedPreferences(this);

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

        // load data
        if (savedInstanceState == null) {
        	// init new object
            Bundle extras = getIntent().getExtras();

            if (extras != null) {
            	submissions_list = (ArrayList<Submission>) extras.get("list");
            	submissions_index = (int) extras.getInt("listId");
//            	man = (ActivityManager) extras.getSerializable("manager"); // can't pass man through intent

            	// only assign submissions. load will be performed by onResume
        		pages.get(0).setSubmission(submissions_list.get(submissions_index));
            	if (submissions_index < submissions_list.size()-1) {
            		pages.get(1).setSubmission(submissions_list.get(submissions_index+1));
            	}
            	if (submissions_index > 0) {
            		pages.get(2).setSubmission(submissions_list.get(submissions_index-1));
            	}
            }

            matrix.reset();
        } else {
        	// load saved object
            submissions_list = (ArrayList<Submission>)  retrieveObject("list");
            submissions_index = (Integer) retrieveObject("listId");
            matrix = (Matrix) retrieveObject("matrix");
            
            pages.get(0).retreive("0");
            pages.get(1).retreive("1");
            pages.get(2).retreive("2");
            
            curpageId = (Integer) retrieveObject("pageId");
            imageFlipper.setDisplayedChild(curpageId);
        }
        
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

    /*
     *  (non-Javadoc)
     * @see com.sofurry.IManagedActivity#finish()
     */

    @Override
    public void finish() {
    	if (pages != null) {
    		pages.get(0).finish();
    		pages.get(1).finish();
    		pages.get(2).finish();
    		pages.clear();
    		pages = null;
    	};
    	
    	submissions_list = null;

        super.finish();
        System.gc();
    }

    @Override
	protected void onResume() {
		super.onResume();

//		getWindow().getDecorView().requestLayout();
/*		pages.get(0).loadPic();
		pages.get(1).loadPic();
		pages.get(2).loadPic(); /**/
		
		findViewById(R.id.viewFlipper1).getViewTreeObserver().addOnPreDrawListener(this);
	}


	public boolean onPreDraw() {
		findViewById(R.id.viewFlipper1).getViewTreeObserver().removeOnPreDrawListener(this);
		pages.get(curpageId).loadPic(true);

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
    	
        storeObject("list", submissions_list);
        storeObject("listId", submissions_index);
        storeObject("pageId", curpageId);
        storeObject("matrix", matrix);
        
        super.onSaveInstanceState(outState);
    }


	@Override
	public void save() {
		pages.get(curpageId).save();
	}

	public boolean showNext() {
		if (submissions_index < submissions_list.size()-1) {
//			long t = System.currentTimeMillis(); // DEBUG
			
			submissions_index ++;
			assignSubmission(submissions_list.get(submissions_index));

			int oldpageId = curpageId;
			
			curpageId ++;
			if (curpageId > 2) {
				curpageId = 0;
			}
//			long tt1 = System.currentTimeMillis() - t; // DEBUG

			// reset image transformations
			matrix.reset();
			pages.get(curpageId).image.setImageMatrix(matrix);

			// start load image
			pages.get(curpageId).loadPic(true);
			
//			long tt2 = System.currentTimeMillis() - t; // DEBUG
//			long tt3 = 0;// DEBUG

			 // preload next image
			int nextpage = curpageId + 1;
			if (nextpage > 2) {
				nextpage = 0;
			}
			
			if (submissions_index < submissions_list.size()-1) {
				pages.get(nextpage).setSubmission(submissions_list.get(submissions_index+1));
//			 tt3 = System.currentTimeMillis() - t; // DEBUG
				pages.get(nextpage).loadPic(false); // preload file, do not load bitmap
			} else {
				// fetch next page 
				// TODO Add code to load next page (pass man from GalleryArt?)
//				man.forceLoadNext(); // can't pass man through intent :(
			}
			
//			 long tt4 = System.currentTimeMillis() - t; // DEBUG
	        // Get a reference to the ViewFlipper
	        ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipper1);
	         // Set the animation
	         vf.setInAnimation(aRightIn);
	         vf.setOutAnimation(aLeftOut);
	          // Flip!
	         vf.showNext();
	         
			 pages.get(oldpageId).unloadPic();
			
//			 long tt5 = System.currentTimeMillis() - t; // DEBUG
//			 t = tt1 + tt2 + tt3 + tt4 + tt5; // DEBUG
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
				pages.get(prevpage).setSubmission(submissions_list.get(submissions_index-1));
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
//    	centerview.layout(dX, centerview.getTop(), getApplicationContext().getResources().getDisplayMetrics().widthPixels + dX, centerview.getBottom());
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

	public float centerImage(Boolean forceCenter, Boolean doFit, Boolean rescale) {
//    	View pageView = pages.get(curpageId).myview;
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
    		float scale = Math.min( (float) pageView.getWidth() / bmp.getWidth(), (float) pageView.getHeight() / bmp.getHeight() );
    		if ((scale > 0 )&&(scale < 1)) // do not enlarge
    			matrix.postScale(scale, scale);
	    	r.set(0,0,bmp.getWidth(),bmp.getHeight());
	    	matrix.mapRect(r);
    	}
    	
    	float dx = 0;
    	float dy = 0;
    	if (forceCenter) {
        	dx = pageView.getWidth()/2 - r.centerX();
        	dy = pageView.getHeight()/2 - r.centerY();
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
//    	View pageView = pages.get(curpageId).myview;
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

//    	if ( (r.left > 0)&&(r.right < pageView.getWidth()) ) {
       	if ( (r.width() <= pageView.getWidth()) ) {
    		delta.x = (r.left + r.right - pageView.getWidth())/2;
    	} else if (r.left > 0) {
    		delta.x = r.left;
    	} else if (r.right < pageView.getWidth())
    		delta.x = r.right - pageView.getWidth();

//    	if ( (r.top > 0)&&(r.bottom < pageView.getHeight()) ) {
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
//            		midPoint.set(arg1.getX(0), arg1.getY(0));
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
//                int longflipLength = getApplicationContext().getResources().getDisplayMetrics().widthPixels / 3;
                int longflipLength = pages.get(curpageId).myview.getWidth() / 3;
//                int shortflipLength = 15;
                int MinFlipVelocity = 40; 

                // read drag speed
                VTracker.addMovement(arg1);
                VTracker.computeCurrentVelocity(100); // pixels per last 100ms
                float vx = VTracker.getXVelocity();
                VTracker.recycle();

                // CLICK
//                if ( (clickDuration < clickTime) && (Math.abs(dX) < shortflipLength) ) {
                if ( (clickDuration < maxClickTime) && (Math.abs(dX) < maxClickLength) && (Math.abs(vx) < maxClickVelocity) ) {
                	// click
                	
//                    doHdView(pages.get(curpageId).submission);
                	if (clickTime - prevClickTime <= doubleClickDuration) {
                		// double click
                		mHasDoubleClicked = true;
                		
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
                                	  doHdView(pages.get(curpageId).submission);
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
                // very long drag || fast long drag
//                if ( (Math.abs(dX) > longflipLength) || ( (clickDuration < clickTime) && (Math.abs(dX) >= shortflipLength) )) {

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
            	centerImage(false, false, true);
                setViewPosition(0, 0);
                
                break;
            }
            case MotionEvent.ACTION_MOVE:
            {
                VTracker.addMovement(arg1);
                
            	switch (mode) {
            		case DRAG: {
//                    	int dX = (int) (arg1.getX() - downPoint.x);
//                    	setViewPosition(dX, 0);

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
}
