package com.sofurry.mobileapi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sofurry.base.classes.ActivityManager;
import com.sofurry.base.interfaces.IAddSubmission;
import com.sofurry.mobileapi.core.Request;
import com.sofurry.mobileapi.core.Request.HttpMode;
import com.sofurry.model.Submission;

/**
 * @author Rangarig
 * 
 * This is the main API Factory class. You can create Requests here, that can then be Executed either syncronous or asyoncronus with callbacks.
 * 
 * The simplest example is:
 * 
 * ApiFactory.createAddFav(1234).exec();
 * 
 * This will execute a syncronus call, and fav the submission 1234.
 * 
 * Asyncronous calls are a little bit more complicated and require an anonymous class for the callback.
 * 
 * Request request = ApiFactory.createAddFav(1234);
 * request.executeAsync(
 *   new CallBack(){ 
 *     public void success(JSONObject result){ mymethod(result); };
 *     public void fail(Exception e){ errormethod(e); };
 *   };
 * );
 *
 */
public class ApiFactory {

	/**
	 * The Viewsource Parameter presets
	 */
	public enum ViewSource {
		all(0), // Does not filter the submission type
		favorites(1), // Returns all favorites
		watchlist(2),
		folder(3),
		groups(4),
		search(5),	  // Returns everything with the tags specified in the "extra" field
		commission(6),
		user(7),	  
		featured(8),  // Returns all featured
		highlights(9),
		groupwatch(10),
		combinedwatch(11);
		
		public int value = 0;
		
		private ViewSource(int value) {
			this.value = value;
		}
	}

	
	/**
	 * The Contenttype parameter Presets
	 */
	public enum ContentType {
		all(-1),
		art(1), // Does not filter the submission type
		journals(3), // Returns all favorites
		music(2),  // Returns all featured
		stories(0);
		
		public int value = 0;
		
		private ContentType(int value) {
			this.value = value;
		}
		
	}

	/**
     * Path to the API request script
     */
    public static final String DEFAULT_API = "/ajaxfetch.php";

    /**
     * URL of the SoFurry API site
     */
    public static final String API_URL = "http://chat.sofurry.com";
    public static final String API2_URL = "http://api2.sofurry.com";

    /**
     * URL for all the resources like thumbnails, preview images, images
     */
    public static final String RESOURCE_URL = "http://www.sofurry.com/std";
    
    /**
     * Returns the url to the preview sized submission image
     * @param id
     * @return
     */
    public static String getPreviewURL(int id) {
		return RESOURCE_URL + "/preview?page="+id;
	}
	
	/**
	 * Returns the url to the full sized submission image
	 * @param id
	 * @return
	 */
	public static String getFullURL(int id) {
		return RESOURCE_URL + "/content?page="+id;
	}
	
	public static String getThumbURL(int id) {
		return RESOURCE_URL + "/thumb?page="+id;
	}
	
	public static String getUserIconURL(int id) {
		return RESOURCE_URL + "/avatar?user="+id;
	}
	
	
	/**
	 * Creates a Browse command. (the command will still have to be executed, either by execute or by executeasync.
	 * @param viewSource
	 * The viewSource
	 * @param extra
	 * extra parameters. 
	 * In viewsearch mode 'search': The tags to be searched for.
	 * In viewsearch mode 'user': The author id
	 * @param contentType
	 * The content type to filter by
	 * @param entriesPerPage
	 * The number of entries to return
	 * @param page
	 * The page to return
	 * @return
	 * Returns a Request object, ready to be executed
	 */
	public static Request createBrowse(ViewSource source, String extra, ContentType contentType, int entriesPerPage, int page) throws Exception {
		Request req = new Request();
		req.setURL(API_URL + DEFAULT_API);
		req.setParameter("f", "browse");
		req.setParameter("viewSource", Integer.toString(source.value));
		
		switch (source) {
		case favorites: { 
			if ( (extra != null) && (extra.length() > 0)) {
			    req.setParameter("viewUserID", extra);
			} else {
				throw new Exception("Favorites require viewUserID parameter");
			}
		}
		case watchlist: { 
			if ( (extra != null) && (extra.length() > 0)) {
			    req.setParameter("viewUserID", extra);
			} else {
				throw new Exception("Watchlist require viewUserID parameter");
			}
		}
		case folder: { 
			if ( (extra != null) && (extra.length() > 0)) {
			    req.setParameter("folderID", extra);
			} else {
				throw new Exception("Folder require folderID parameter");
			}
		}
		case groups: { 
			if ( (extra != null) && (extra.length() > 0)) {
			    req.setParameter("gid", extra);
			} else {
				throw new Exception("Groups require groupID parameter");
			}
		}
		case search: { 
			if ( (extra != null) && (extra.length() > 0)) {
			    req.setParameter("search", extra);
			} else {
				throw new Exception("Search require search parameter with comma separated tags");
			}
		}
		case commission: { 
			if ( (extra != null) && (extra.length() > 0)) {
			    req.setParameter("cid", extra);
			} else {
				throw new Exception("Commission require commission ID parameter");
			}
		}
		case user: { 
			if ( (extra != null) && (extra.length() > 0)) {
			    req.setParameter("viewUserID", extra);
			} else {
				throw new Exception("User require viewUserID parameter");
			}
		}
		case highlights: { 
			if ( (extra != null) && (extra.length() > 0)) {
			    req.setParameter("viewUserID", extra);
			} else {
				throw new Exception("Highlights require viewUserID parameter");
			}
		}
		case groupwatch: { 
			if ( (extra != null) && (extra.length() > 0)) {
			    req.setParameter("viewUserID", extra);
			} else {
				throw new Exception("Group watchlist require viewUserID parameter");
			}
		}
		case combinedwatch: { 
			if ( (extra != null) && (extra.length() > 0)) {
			    req.setParameter("viewUserID", extra);
			} else {
				throw new Exception("Combined watchlist require viewUserID parameter");
			}
		}
		}
		/*
		if (extra != null) {
		  if ((source != ViewSource.search) && (source != ViewSource.user))
			  throw new Exception("The extra parameter can only be used with the modes 'search' and 'user' otherwise it has to be null.");
		  if (source == ViewSource.user)
		    req.setParameter("authorid", extra);
		  if (source == ViewSource.search)
			req.setParameter("search", extra);
		}
		*/
		req.setParameter("contentType", Integer.toString(contentType.value));
		req.setParameter("entriesPerPage", Integer.toString(entriesPerPage));
		req.setParameter("page", Integer.toString(page));
		return req;
	}
	
	/**
	 * Parse browse responce and add Submissions
	 * should return both number of items loaded and total amount. But returning only num items loaded
	 * @param obj - responce to prase
	 * @param addsub - add submission callback
	 * @throws JSONException
	 * return number of items readed from server
	 */
	public static int ParseBrowse(JSONObject obj, IAddSubmission addsub) throws JSONException {
		JSONArray pagecontents = new JSONArray(obj.getString("pagecontents"));
		JSONArray items = new JSONArray(pagecontents.getJSONObject(0).getString("items"));
		for (int i = 0; i < items.length(); i++) {
			Submission s = new Submission();
			s.populate(items.getJSONObject(i));
			addsub.AddSubmission(s);
		}
//		return Integer.parseInt(obj.getString("totalpages"));
		return items.length();
	}
	
	/**
	 * Creates a search command, allowing to browse for keywoards
	 * @param searchparameter
	 * The keywords, separated by komma ','
	 * @param contentType
	 * The type of content to be returned
	 * @param entriesPerPage
	 * The entries per returned page
	 * @param page
	 * The page to fetch
	 * @return
	 * @throws Exception
	 */
	public static Request createSearch(String searchparameter, ContentType contentType, int entriesPerPage, int page) throws Exception {
		return createBrowse(ViewSource.search, searchparameter, contentType, entriesPerPage, page);
	}
	
	/**
	 * Fetches a content of a page
	 * @param pageID
	 * The pageid of the page to be fetched
	 * @return
	 */
	public static Request createGetPageContent(int pageID) {
		Request req = new Request();
		req.setURL(API_URL + DEFAULT_API);
		req.setParameter("f", "getpagecontent");
		req.setParameter("pid", "" + pageID);
		return req;
	}
	
	/**
	 * Returns data about a submission
	 * @param pageID
	 * The page ID of the submission to return the data for.
	 * @return
	 */
	public static Request createGetSubmissionData(int pageID) {
		Request req = new Request();
//		req.setURL(API2_URL);//   /?id=" + pageID);
//		req.setParameter("id", "" + pageID);
//		req.setParameter("r", "std/getSubmissionDetails");
		req.setURL(API2_URL + "/std/getSubmissionDetails"); //"/?id=" + pageID);
//		req.setMode(HttpMode.get);
		req.setParameter("id", "" + pageID);
		return req;
	}
	
	/**
	 * Adds a submission to the users favorites
	 * @param pid
	 * The page ID of the submission
	 * @return
	 */
	public static Request createAddFav(int pid) {
		Request req = new Request();
		req.setURL(API_URL + DEFAULT_API);
		req.setParameter("f", "addfav");
		req.setParameter("pid", "" + pid);
		return req;
	}
	
	/**
	 * Adds a submission to the users favorites
	 * @param pid
	 * The page ID of the submission
	 * @return
	 */
	public static Request createRemFav(int pid) {
		Request req = new Request();
		req.setURL(API_URL + DEFAULT_API);
		req.setParameter("f", "remfav");
		req.setParameter("pid", "" + pid);
		return req;
	}
	
	/**
	 * Sets a stars rating for the submission indicated by the pid
	 * @param pid
	 * The page ID of the submission
	 * @param numberOfStars
	 * The number of stars to be set
	 * @return
	 */
	public static Request createSetStars(int pid,int numberOfStars) {
		Request req = new Request();
		req.setURL(API_URL + DEFAULT_API);
		req.setParameter("f", "vote");
		req.setParameter("pid", "" + pid);
		req.setParameter("votevalue", "" + numberOfStars);
		return req;
	}
	
	/**
	 * Sets a 'cum' flag for the indicated submission
	 * @param pid
	 * The page ID of the submission
	 * @return
	 */
	public static Request createCum(int pid) {
		Request req = new Request();
		req.setURL(API_URL + DEFAULT_API);
		req.setParameter("f", "cum");
		req.setParameter("pid", "" + pid);
		return req;
	}
	
	/**
	 * Adds the author indicated by the authorid to the currentusers watchlist 
	 * @param authorid
	 * The authorid to add to the watchlist
	 * @return
	 */
	public static Request createWatch(int authorid) {
		Request req = new Request();
		req.setURL(API2_URL + "/user/action/watch");
		req.setParameter("id", "" + authorid);
/*
		req.setURL(API_URL + DEFAULT_API);
		req.setParameter("f", "addwatch");
		req.setParameter("authorid", "" + authorid);
*/
		return req;
	}
	
	/**
	 * Removes the author indicated by the authorid from the currentusers watchlist 
	 * @param authorid
	 * The authorid to remove from the watchlist
	 * @return
	 */
	public static Request createUnWatch(int authorid) {
		Request req = new Request();
		req.setURL(API2_URL + "/user/action/unwatch");
		req.setParameter("id", "" + authorid);
/*
		req.setURL(API_URL + DEFAULT_API);
		req.setParameter("f", "remwatch");
		req.setParameter("authorid", "" + authorid);
*/
		return req;
	}
	
	/**
	 * Returns the list of PersonalMessage's for the current User
	 * @param page
	 * The Page to be fetched
	 * @param entriesPerPage
	 * The number of entries per page
	 * @return
	 */
	public static Request createListPMs(int page, int entriesPerPage) {
		Request req = new Request();
		req.setURL(API_URL + DEFAULT_API);
		req.setParameter("f", "pm");
		req.setParameter("page", "" + page);
		req.setParameter("entriesPerPage", "" + entriesPerPage);
		return req;
	}
	
	/**
	 * Sends a Personal message
	 * @param toUserName
	 * The user to send the PM to
	 * @param subject
	 * The Subject line of the message
	 * @param message
	 * The textbody of the message
	 * @param parentId
	 * ID of the message we are replying to
	 * @return
	 */
	public static Request createSendPM(String toUserName, String subject, String message, int parentId) {
		Request req = new Request();
		req.setURL(API_URL + DEFAULT_API);
		req.setParameter("f", "sendpm");
		req.setParameter("toUserName", toUserName);
		req.setParameter("subject", subject);
		req.setParameter("message", message);
		req.setParameter("parendId", "" + parentId );
		return req;
	}
		
	
	/**
	 * Sends a Personal message
	 * @param toUserName
	 * The user to send the PM to
	 * @param toUserId
	 * the userID to send the PM to
	 * @param subject
	 * the Subject line of the message
	 * @param message
	 * The body of the message
	 * @param parentId
	 * ID of the message we are replying to
	 * @return
	 */
	public static Request createSendPM(String toUserName, int toUserId, String subject, String message, int parentId) {
		Request req = createSendPM(toUserName,subject,message,parentId);
		req.setParameter("toUserId", "" + toUserId);
		return req;
	}
	
	/**
	 * Returns the count of the unread personal messages since the last check
	 * @param since
	 * A long containing currentTimeMillis at the last check devided by a thousand. 
	 * (System.currentTimeMillis() / 1000)
	 * 
	 * @return
	 */
	public static Request createUnreadPMCount(long since) {
		Request req = new Request();
		req.setURL(API_URL + DEFAULT_API);
		req.setParameter("f", "unreadpmcount");
		if (since != -1)
		  req.setParameter("since", "" + since);
		return req;
	}

	/**
	 * Returns the count of the unread personal messages
	 * @param since
	 * 
	 * @return
	 */
	public static Request createUnreadPMCount() {
		return createUnreadPMCount(-1);
	}
	
	/**
	 * Provides the content of a Personal Message
	 * @param pmid
	 * The id of the Personal Message to return
	 * @return
	 */
	public static Request createGetPmContent(int pmid) {
		Request req = new Request();
		req.setURL(API_URL + DEFAULT_API);
		req.setParameter("f", "pmcontent");
  	    req.setParameter("id", "" + pmid);
		return req;
	}
	
	/**
	 * Returns the profile information of a user
	 * @param userid
	 * The user's userid. If the userid is your own, extra parameters are passed.
	 * @return
	 */
	public static Request createGetUserProfile(int userid) {
		Request req = new Request();
		req.setURL(API2_URL + "/std/getUserProfile");
		req.setParameter("id", "" + userid);
		return req;
	}

	/**
	 * Returns the profile information of a current user
	 * @param userid
	 * The user's userid. If the userid is your own, extra parameters are passed.
	 * @return
	 */
	public static Request createGetUserProfile() {
		Request req = new Request();
		req.setURL(API2_URL + "/std/getUserProfile");
//		req.setParameter("id", "" + userid);
		return req;
	}


}
