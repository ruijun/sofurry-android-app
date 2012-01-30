package com.sofurry.mobileapi;

import com.sofurry.mobileapi.core.Request;
import com.sofurry.mobileapi.core.Request.HttpMode;

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
		featured(8),  // Returns all featured
		group(10),
		search(5),	  // Returns everything with the tags specified in the "extra" field
		user(7),	  
		watchlist(2),
		watchlist_combined(11);
		
		public int value = 0;
		
		private ViewSource(int value) {
			this.value = value;
		}
	}

	
	/**
	 * The Contenttype parameter Presets
	 */
	public enum ContentType {
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
    
    public static final String API2_URL = "http://api2.sofurry.com/std/";

    /**
	
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
		req.setParamter("f", "browse");
		req.setParamter("viewSource", Integer.toString(source.value));
		if (extra != null)
		  if ((source != ViewSource.search) && (source != ViewSource.user))
			  throw new Exception("The extra parameter can only be used with the modes 'search' and 'user' otherwise it has to be null.");
		req.setParamter("contentType", Integer.toString(contentType.value));
		req.setParamter("entriesPerPage", Integer.toString(entriesPerPage));
		req.setParamter("page", Integer.toString(page));
		return req;
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
		req.setParamter("f", "getpagecontent");
		req.setParamter("pid", "" + pageID);
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
		req.setURL(API2_URL + "getSubmissionDetails/?id=" + pageID);
		req.setMode(HttpMode.get);
		//req.setParamter("id", "" + pageID);
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
		req.setParamter("f", "addfav");
		req.setParamter("pid", "" + pid);
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
		req.setParamter("f", "remfav");
		req.setParamter("pid", "" + pid);
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
		req.setParamter("f", "vote");
		req.setParamter("pid", "" + pid);
		req.setParamter("votevalue", "" + numberOfStars);
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
		req.setParamter("f", "cum");
		req.setParamter("pid", "" + pid);
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
		req.setURL(API_URL + DEFAULT_API);
		req.setParamter("f", "addwatch");
		req.setParamter("authorid", "" + authorid);
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
		req.setURL(API_URL + DEFAULT_API);
		req.setParamter("f", "remwatch");
		req.setParamter("authorid", "" + authorid);
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
		req.setParamter("f", "remwatch");
		req.setParamter("page", "" + page);
		req.setParamter("entriesPerPage", "" + entriesPerPage);
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
		req.setParamter("f", "sendpm");
		req.setParamter("toUserName", toUserName);
		req.setParamter("subject", subject);
		req.setParamter("message", message);
		req.setParamter("parendId", "" + parentId );
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
		req.setParamter("toUserId", "" + toUserId);
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
		req.setParamter("f", "unreadpmcount");
		if (since != -1)
		  req.setParamter("since", "" + since);
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
		req.setParamter("f", "pmcontent");
  	    req.setParamter("id", "" + pmid);
		return req;
	}


}
