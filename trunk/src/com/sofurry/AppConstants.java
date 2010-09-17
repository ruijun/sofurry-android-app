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


	public static final int CONTENTTYPE_STORIES = 0;
	public static final int CONTENTTYPE_MUSIC = 2;
	public static final int CONTENTTYPE_JOURNALS = 3;
	public static final int CONTENTTYPE_ART = 1;
	
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

	public static final String PREFS_NAME = "SoFurryPreferences";
	public static final String SITE_URL = "http://chat.sofurry.com";
//	public static final String SITE_URL = "http://10.0.2.2";
	public static final String SITE_REQUEST_SCRIPT = "/ajaxfetch.php";
	
	public static final String MP3DownloadLinkEndMarker = "\">Download as MP3</a>";
	public static final String MP3DownloadLinkStartMarker = "<a href=\"";
	
	public static String getFetchUrl() {
		return AppConstants.SITE_URL + AppConstants.SITE_REQUEST_SCRIPT;
	}


}
