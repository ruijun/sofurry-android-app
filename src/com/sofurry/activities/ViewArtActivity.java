package com.sofurry.activities;

//~--- imports ----------------------------------------------------------------

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Bitmap;

import android.net.Uri;

import android.os.Bundle;
//import android.os.Debug;
//import android.os.Debug.MemoryInfo;

import android.preference.PreferenceManager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;

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
import com.sofurry.model.Submission;
import com.sofurry.requests.AsyncImageLoader;
import com.sofurry.storage.FileStorage;
import com.sofurry.storage.ImageStorage;

import java.io.File;
import java.util.ArrayList;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.lang.Math;


//~--- classes ----------------------------------------------------------------

/**
 * Class description
 *
 */
public class ViewArtActivity
        extends FavableActivity 
        implements OnTouchListener {
	

	// current page controls (change when page flips)
	private class PageHolder implements AsyncImageLoader.IImageLoadResult{
		private Bitmap    imageBitmap = null;
		private Boolean   imageLoaded = false;
		private ImageView image = null; 
		private ImageView savedIndicator = null;
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
//            InfoText.setText(MakeTitle()+" Dalvik: "+mi.dalvikPss+" Native: "+mi.nativePss+" Other: "+mi.otherPss);

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
            imageLoader = AsyncImageLoader.doLoad(context, this, submission, false, ! showImage);
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
            imageLoader = AsyncImageLoader.doLoad(context, this, submission, true, false);
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

			// if no bitmap loaded or error
			if ((obj == null)||(! (obj instanceof Bitmap))) {
				return;
			}

			// clean bitmap if other image already loaded
			unloadPic();

        	imageBitmap = (Bitmap) obj;
        	image.setImageBitmap(imageBitmap);
        	
        	imageLoaded = true;
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
	            File f = new File(ImageStorage.getSubmissionImagePath2(submission.getCacheName()));
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
	            onError(-1, e);
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
	private float downXValue; 
	private float downYValue;
	private long downTimer;
	private VelocityTracker VTracker = null;

	private Animation aLeftIn = null;
	private Animation aLeftOut = null;
	private Animation aRightIn = null;
	private Animation aRightOut = null;
	
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
    		f = new File(ImageStorage.getSubmissionImagePath2(s.getCacheName()));
    		if (!f.exists()) {
    			return;    // Until that file exists, there is nothing we can do really.
    		}
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

        // init pages for flipper
        curpage = new PageHolder(this);
        curpage.myview = (View) findViewById(R.id.page1);
        curpage.image           = (ImageView) findViewById(R.id.imagepreview1);
        curpage.InfoText = (TextView) findViewById(R.id.InfoText1);
        curpage.savedIndicator = (ImageView) findViewById(R.id.savedIndicator1);
        curpage.loadingIndicator = (View) findViewById(R.id.loadingIndicator1);
/*        curpage.image.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View arg0) {
                doHdView(pages.get(0).submission);
            }
        });/**/
//        curpage.image.setOnTouchListener((OnTouchListener) this);
        pages.add(curpage);

        curpage = new PageHolder(this);
        curpage.myview = (View) findViewById(R.id.page2);
        curpage.image           = (ImageView) findViewById(R.id.imagepreview2);
        curpage.InfoText = (TextView) findViewById(R.id.InfoText2);
        curpage.savedIndicator = (ImageView) findViewById(R.id.savedIndicator2);
        curpage.loadingIndicator = (View) findViewById(R.id.loadingIndicator2);
/*        curpage.image.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View arg0) {
                doHdView(pages.get(1).submission);
            }
        });/**/
//        curpage.image.setOnTouchListener((OnTouchListener) this);
        pages.add(curpage);

        curpage = new PageHolder(this);
        curpage.myview = (View) findViewById(R.id.page3);
        curpage.image           = (ImageView) findViewById(R.id.imagepreview3);
        curpage.InfoText = (TextView) findViewById(R.id.InfoText3);
        curpage.savedIndicator = (ImageView) findViewById(R.id.savedIndicator3);
        curpage.loadingIndicator = (View) findViewById(R.id.loadingIndicator3);
/*        curpage.image.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View arg0) {
                doHdView(pages.get(2).submission);
            }
        });/**/
//        curpage.image.setOnTouchListener((OnTouchListener) this);
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

        } else {
        	// load saved object

            submissions_list = (ArrayList<Submission>)  retrieveObject("list");
            submissions_index = (Integer) retrieveObject("listId");
            
            pages.get(0).retreive("0");
            pages.get(1).retreive("1");
            pages.get(2).retreive("2");
            
            curpageId = (Integer) retrieveObject("pageId");
            FixedViewFlipper vf = (FixedViewFlipper) findViewById(R.id.viewFlipper1);
            vf.setDisplayedChild(curpageId);
        }
        
        // init dragging
        FixedViewFlipper layMain = (FixedViewFlipper) findViewById(R.id.viewFlipper1);
        layMain.setOnTouchListener((OnTouchListener) this);
        layMain.captureAllTouch = true;
        VTracker = VelocityTracker.obtain();
        
        // prepare animation
    	aLeftIn = AnimationUtils.loadAnimation(this, R.anim.push_left_in);
    	aLeftOut = AnimationUtils.loadAnimation(this, R.anim.push_left_out);
    	aRightIn = AnimationUtils.loadAnimation(this, R.anim.push_right_in);
    	aRightOut = AnimationUtils.loadAnimation(this, R.anim.push_right_out);
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
        
        super.onSaveInstanceState(outState);
    }

/*
    // destroy image viewer on back button
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }/**/

	@Override
	public void save() {
		pages.get(curpageId).save();
	}

	public boolean showNext() {
		if (submissions_index < submissions_list.size()-1) {
			long t = System.currentTimeMillis(); // DEBUG
			
			submissions_index ++;
			assignSubmission(submissions_list.get(submissions_index));

			int oldpageId = curpageId;
			
			curpageId ++;
			if (curpageId > 2) {
				curpageId = 0;
			}
			long tt1 = System.currentTimeMillis() - t; // DEBUG

			pages.get(curpageId).loadPic(true);
			
			long tt2 = System.currentTimeMillis() - t; // DEBUG
			long tt3 = 0;// DEBUG

			 // preload next image
			int nextpage = curpageId + 1;
			if (nextpage > 2) {
				nextpage = 0;
			}
			
			if (submissions_index < submissions_list.size()-1) {
				pages.get(nextpage).setSubmission(submissions_list.get(submissions_index+1));
			 tt3 = System.currentTimeMillis() - t; // DEBUG
				pages.get(nextpage).loadPic(false); // preload file, do not load bitmap
			} else {
				// fetch next page 
//				man.forceLoadNext(); // can't pass man through intent :(
			}
			
			 long tt4 = System.currentTimeMillis() - t; // DEBUG
	        // Get a reference to the ViewFlipper
	        ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipper1);
	         // Set the animation
	         vf.setInAnimation(aRightIn);
	         vf.setOutAnimation(aLeftOut);
	          // Flip!
	         vf.showNext();
	         
			 pages.get(oldpageId).unloadPic();
			
			 long tt5 = System.currentTimeMillis() - t; // DEBUG
			 t = tt1 + tt2 + tt3 + tt4 + tt5; // DEBUG
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

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
        // Get the action that was done on this touch event
        switch (arg1.getAction())
        {
        	
            case MotionEvent.ACTION_DOWN:
            {
                // store the X value when the user's finger was pressed down
                downXValue = arg1.getX();
                downYValue = arg1.getY();
                downTimer = System.currentTimeMillis();
                VTracker.clear();
                VTracker.addMovement(arg1);
                break;
            }

            case MotionEvent.ACTION_UP:
            {
                float currentX = arg1.getX();
                float dX = downXValue - currentX;
                long clickDuration = System.currentTimeMillis() - downTimer; 

                int clickTime = 200;
                int longflipLength = getApplicationContext().getResources().getDisplayMetrics().widthPixels / 3;
                int shortflipLength = 15;
                int MinFlipVelocity = 10; 

                // WIP: replacing drag length flipping with drag speed
                VTracker.addMovement(arg1);
                VTracker.computeCurrentVelocity(100);
                float vx = VTracker.getXVelocity();
                VTracker.recycle();
                
//                if ( (clickDuration < clickTime) && (Math.abs(dX) < shortflipLength) ) {
                if ( (clickDuration < clickTime) && (Math.abs(vx) < MinFlipVelocity) ) {
                	// click
                    doHdView(pages.get(curpageId).submission);
                    return true;
                }
                
                // very long drag || fast long drag
//                if ( (Math.abs(dX) > longflipLength) || ( (clickDuration < clickTime) && (Math.abs(dX) >= shortflipLength) )) {

                // very long drag || fast drag
                if ( (Math.abs(dX) > longflipLength) || ( (Math.abs(vx) >= MinFlipVelocity) )) {
                	// flip

                	// going backwards: pushing stuff to the right
//                    if (downXValue < currentX)
                    if (vx > 0)
                    {
                    	if (showPrev()) return true;
                    }

                    // going forwards: pushing stuff to the left
//                    if (downXValue > currentX)
                    if (vx < 0)
                    {
                    	if (showNext()) return true;
                    }
                }
                
                // return to center animation
                setViewPosition(0, 0);
                
                break;
            }
            case MotionEvent.ACTION_MOVE:
            {
            	int dX = (int) (arg1.getX() - downXValue);
            	setViewPosition(dX, 0);
                VTracker.addMovement(arg1);
                
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            {	
//                VTracker.recycle();
            	break;
            }
        }

        // if you return false, these actions will not be recorded
        return true;
	}

	@Override
	protected void onResume() {
		super.onResume();

/*		pages.get(0).loadPic();
		pages.get(1).loadPic();
		pages.get(2).loadPic(); /**/
		pages.get(curpageId).loadPic(true);
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

}
