package com.sofurry;

public class AppConstants {

	//Activity enumerations
	public static final int ACTIVITY_STORIESLIST = 0;
	public static final int ACTIVITY_PMLIST = 1;
	public static final int ACTIVITY_SETTINGS = 2;
	public static final int ACTIVITY_JOURNALSLIST = 3;
	public static final int ACTIVITY_GALLERYART = 4;
	public static final int ACTIVITY_VIEWPM = 5;
	public static final int ACTIVITY_TAGS = 6;
	public static final int ACTIVITY_MUSICLIST = 7;
	public static final int ACTIVITY_RATE = 7;
	
	
	public static final String TAG_STRING = "[SoFurry]";


	public static final int CONTENTTYPE_STORIES = 0;
	public static final int CONTENTTYPE_MUSIC = 2;
	public static final int CONTENTTYPE_JOURNALS = 3;
	public static final int CONTENTTYPE_ART = 1;
	
	// Dialog types
	public static final int DIALOG_ERROR_ID = 0;
	
	//Ajax message types
	public static final int AJAXTYPE_APIERROR = 5;
	public static final int AJAXTYPE_OTPAUTH = 6;
	
	public static final int MENU_FILTER_ALL = 9090900;
	public static final int MENU_FILTER_FEATURED = 9090901;
	public static final int MENU_FILTER_FAVORITES = 9090902;
	public static final int MENU_FILTER_WATCHLIST = 9090903;
	public static final int MENU_FILTER_GROUP = 9090904;
	public static final int MENU_FILTER_WATCHLIST_COMBINED = 9090905;
	public static final int MENU_FILTER_KEYWORDS = 4204;
	
	public static final int MENU_ADDFAV = 4201;
	public static final int MENU_REMFAV = 4202;
	public static final int MENU_CUM = 4203;
	public static final int MENU_HD = 4204;
	public static final int MENU_PLAY = 4205;
	public static final int MENU_WATCH = 4206;
	public static final int MENU_UNWATCH = 4207;
	public static final int MENU_USERSSTORIES = 4210;
	public static final int MENU_USERSART = 4211;
	public static final int MENU_USERSMUSIK = 4212;

	public static final int MENU_RATE = 4300;
	public static final int MENU_RATE1 = 4301;
	public static final int MENU_RATE2 = 4302;
	public static final int MENU_RATE3 = 4303;
	public static final int MENU_RATE4 = 4304;
	public static final int MENU_RATE5 = 4305;
	public static final int MENU_SAVE = 4306;

	public static final int MENU_CHGROOM = 4401;
	public static final int MENU_USERS = 4402;
	
	public static final int VIEWSOURCE_ALL = 0;	
	public static final int VIEWSOURCE_FEATURED = 8;	
	public static final int VIEWSOURCE_USER = 7;	
	public static final int VIEWSOURCE_FAVORITES = 1;	
	public static final int VIEWSOURCE_WATCHLIST = 2;	
	public static final int VIEWSOURCE_GROUP = 10;	
	public static final int VIEWSOURCE_WATCHLIST_COMBINED = 11;	
	public static final int VIEWSOURCE_SEARCH = 5;	
	
	public static final int REQUEST_ID_ROOMLIST = 4501;
	public static final int REQUEST_ID_USERLIST = 4502;
	public static final int REQUEST_ID_FETCHCONTENT = 4503;
	public static final int REQUEST_ID_FETCHSUBMISSIONDATA = 4504;
	public static final int REQUEST_ID_FETCHDATA = 4505;
	
	public static final int REQUEST_ID_FAV = 4506;
	public static final int REQUEST_ID_UNFAV = 4507;
	public static final int REQUEST_ID_CUM = 4508;
	public static final int REQUEST_ID_RATE = 4509;
	public static final int REQUEST_ID_WATCH = 4512;
	public static final int REQUEST_ID_SEND = 4513;

	public static final int REQUEST_ID_DOWNLOADFILE = 4510;
	public static final int REQUEST_ID_DOWNLOADIMAGE = 4511;

	
	public static final String PREFS_NAME = "SoFurryPreferences";
	public static final String SITE_URL = "http://chat.sofurry.com";
//	public static final String SITE_URL = "http://10.0.2.2";
	public static final String SITE_REQUEST_SCRIPT = "/ajaxfetch.php";
	
	public static final String MP3_DOWNLOAD_LINK_END_MARKER = "\">Download as MP3</a>";
	public static final String MP3_DOWNLOAD_LINK_START_MARKER = "<a href=\"";
	
	public static final String PM_CONTENTS_PREFIX = "<html><head><style>A:link {color: #3BB9FF} A:visited {color: #3BB9FF}</style></head><body><p style=\"color: #FFFFFF\">";
	public static final String PM_CONTENTS_POSTFIX = "</p></body></html>";
	public static final String PM_CONTENTS_URL_TEMPLATE = "$1<a href=\"$2\">$2</a>";
	public static final String PM_CONTENTS_URL_REGEX = "(?i)([^\"\\>])(https?://[^\\s<>\"`{}\\[\\]\\\\]+)";

	public static final int PM_STATUS_NEW = 0;
	public static final int PM_STATUS_READ = 1;
	public static final int PM_STATUS_REPLIED  = 2;
	
	public static final int ENTRIESPERPAGE_GALLERY = 40;
	public static final int ENTRIESPERPAGE_LIST = 40;
	
	/**
	 * Combines the site URL with the path to the request script
	 * 
	 * @return The fully qualified fetch URL
	 */
	public static String getFetchUrl() {
		return AppConstants.SITE_URL + AppConstants.SITE_REQUEST_SCRIPT;
	}


}
