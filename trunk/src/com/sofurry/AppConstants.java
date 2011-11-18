package com.sofurry;

/**
 * Class description
 *
 */
public class AppConstants {
    /**
     * Used in the construction of MP3 download links, and this is the end marker
     */
    public static final String MP3_DOWNLOAD_LINK_END_MARKER = "\">Download as MP3</a>";

    /**
     * Used in the construction of MP3 download links, and this is the opening marker
     */
    public static final String MP3_DOWNLOAD_LINK_START_MARKER = "<a href=\"";

    /**
     * Private Message contents needs to be wrapped, and this is the ending wrapper
     */
    public static final String PM_CONTENTS_POSTFIX = "</p></body></html>";

    /**
     * Private Message contents need to be wrapped, and  this is the opening wrapper
     */
    public static final String PM_CONTENTS_PREFIX = "<html><head>"
                                                    + "<style>A:link {color: #3BB9FF} A:visited {color: #3BB9FF}"
                                                    + "</style></head><body><p style=\"color: #FFFFFF\">";

    /**
     * RegEx string used to find links in Private Messages, before replacing with {@link #PM_CONTENTS_URL_TEMPLATE}
     */
    public static final String PM_CONTENTS_URL_REGEX = "(?i)([^\"\\>])(https?://[^\\s<>\"`{}\\[\\]\\\\]+)";

    /**
     * Replace string, marking what to put where after using {@link #PM_CONTENTS_URL_REGEX}
     */
    public static final String PM_CONTENTS_URL_TEMPLATE = "$1<a href=\"$2\">$2</a>";

    /**
     * Preferences
     */
    public static final String PREFERENCE_IMAGE_FILE_NAME_TMPL      = "imgFileNameTemplate";
    public static final String PREFERENCE_IMAGE_TMPL_USE_ONLY_ADULT = "imgFileNameTemplateUseAdultForExtreme";
    public static final String PREFERENCE_IMAGE_CHECK_SAVED         = "imgCheckSaved";
    public static final String PREFERENCE_IMAGE_USE_LIB         	= "imgLoadFromLib";
    public static final String PREFERENCE_IMAGE_MENU_POSITION       = "imgMenuPosition";
    public static final String PREFERENCE_USE_HD_IMAGES      		= "useHDImg";
    public static final String PREFERENCE_LAST_LAUNCH_VERSION       = "lastLaunchVersion";
    public static final String PREFERENCE_LAST_PM_CHECK_TIME        = "lastpmchecktime";
    public static final String PREFERENCE_PASSWORD                  = "password";
    public static final String PREFERENCE_PM_CHECK_INTERVAL         = "pmCheckInterval";
    public static final String PREFERENCE_PM_ENABLE_CHECKS          = "pmEnableChecks";
    public static final String PREFERENCE_SALT                      = "salt";
    public static final String PREFERENCE_USERNAME                  = "username";
    public static final String PREFERENCE_THUMB_SIZE                = "art_gallery_thumbnail_size";
    

    /**
     * Name of the preference storage
     */
    public static final String PREFS_NAME = "SoFurryPreferences";

    /**
     * Path to the API request script
     */
    public static final String SITE_REQUEST_SCRIPT = "/ajaxfetch.php";

    /**
     * URL of the SoFurry API site
     */
    public static final String SITE_URL = "http://chat.sofurry.com";

    /**
     * Tag String for use in Log.* method calls
     */
    public static final String TAG_STRING = "[SoFurry]";

    /**
     * One of several Activity IDs
     */
    public static final int ACTIVITY_ACCOUNT      = 8;
    public static final int ACTIVITY_GALLERYART   = 4;
    public static final int ACTIVITY_JOURNALSLIST = 3;
    public static final int ACTIVITY_MUSICLIST    = 7;
    public static final int ACTIVITY_PMLIST       = 1;
    public static final int ACTIVITY_RATE         = 7;
    public static final int ACTIVITY_SETTINGS     = 2;
    public static final int ACTIVITY_STORIESLIST  = 0;
    public static final int ACTIVITY_TAGS         = 6;
    public static final int ACTIVITY_VIEWPM       = 5;

    /**
     * TODO: Document this!
     */
    public static final int AJAXTYPE_APIERROR = 5;

    /**
     * TODO: Document this!
     */
    public static final int AJAXTYPE_OTPAUTH = 6;

    /**
     * One of several Content Types
     */
    public static final int CONTENTTYPE_ART      = 1;
    public static final int CONTENTTYPE_JOURNALS = 3;
    public static final int CONTENTTYPE_MUSIC    = 2;
    public static final int CONTENTTYPE_STORIES  = 0;

    /**
     * Dialog ID: Error Dialog
     */
    public static final int DIALOG_ERROR_ID = 0;

    /**
     * Number of entries per page in a gallery listing
     */
    public static final int ENTRIESPERPAGE_GALLERY = 40;

    /**
     * Number of entries per page in a non-gallery listing
     */
    public static final int ENTRIESPERPAGE_LIST = 40;

    /**
     * One of many Menu entry IDs
     */
    public static final int MENU_ADDFAV                    = 4201;
    public static final int MENU_CHGROOM                   = 4401;
    public static final int MENU_CUM                       = 4203;
    public static final int MENU_FILTER_ALL                = 9090900;
    public static final int MENU_FILTER_FAVORITES          = 9090902;
    public static final int MENU_FILTER_FEATURED           = 9090901;
    public static final int MENU_FILTER_GROUP              = 9090904;
    public static final int MENU_FILTER_KEYWORDS           = 4204;
    public static final int MENU_FILTER_WATCHLIST          = 9090903;
    public static final int MENU_FILTER_WATCHLIST_COMBINED = 9090905;
    public static final int MENU_HD                        = 4204;
    public static final int MENU_REFRESH                   = 4213;
    public static final int MENU_PLAY                      = 4205;
    public static final int MENU_RATE                      = 4300;
    public static final int MENU_RATE1                     = 4301;
    public static final int MENU_RATE2                     = 4302;
    public static final int MENU_RATE3                     = 4303;
    public static final int MENU_RATE4                     = 4304;
    public static final int MENU_RATE5                     = 4305;
    public static final int MENU_REMFAV                    = 4202;
    public static final int MENU_SAVE                      = 4306;
    public static final int MENU_UNWATCH                   = 4207;
    public static final int MENU_USERS                     = 4402;
    public static final int MENU_USERSART                  = 4211;
    public static final int MENU_USERSMUSIK                = 4212;
    public static final int MENU_USERSSTORIES              = 4210;
    public static final int MENU_WATCH                     = 4206;

    /**
     * Notification ID for Private Messages
     */
    public static final int NOTIFICATION_ID_PM = 5000;

    /**
     * Private Message Status
     */
    public static final int PM_STATUS_NEW     = 0;
    public static final int PM_STATUS_READ    = 1;
    public static final int PM_STATUS_REPLIED = 2;

    /**
     * Request ID
     */
    public static final int REQUEST_ID_CUM                 = 4508;
    public static final int REQUEST_ID_DOWNLOADFILE        = 4510;
    public static final int REQUEST_ID_DOWNLOADIMAGE       = 4511;
    public static final int REQUEST_ID_ASYNCDOWNLOADIMAGE  = 4514;
    public static final int REQUEST_ID_FAV                 = 4506;
    public static final int REQUEST_ID_FETCHCONTENT        = 4503;
    public static final int REQUEST_ID_FETCHDATA           = 4505;
    public static final int REQUEST_ID_FETCHSUBMISSIONDATA = 4504;
    public static final int REQUEST_ID_RATE                = 4509;
    public static final int REQUEST_ID_ROOMLIST            = 4501;
    public static final int REQUEST_ID_SEND                = 4513;
    public static final int REQUEST_ID_UNFAV               = 4507;
    public static final int REQUEST_ID_USERLIST            = 4502;
    public static final int REQUEST_ID_WATCH               = 4512;

    /**
     * ViewSource
     */
    public static final int VIEWSOURCE_ALL                = 0;
    public static final int VIEWSOURCE_FAVORITES          = 1;
    public static final int VIEWSOURCE_FEATURED           = 8;
    public static final int VIEWSOURCE_GROUP              = 10;
    public static final int VIEWSOURCE_SEARCH             = 5;
    public static final int VIEWSOURCE_USER               = 7;
    public static final int VIEWSOURCE_WATCHLIST          = 2;
    public static final int VIEWSOURCE_WATCHLIST_COMBINED = 11;

    /**
     * Initial delay after boot before checking for PMs the first time
     */
    public static final long ALARM_CHECK_DELAY_FIRST = 20000;

    /**
     * Delay between checking for PMs
     */
    public static final long ALARM_CHECK_DELAY_PERIOD = 900000;

    /**
     * Vibration pattern for Private Messages
     */
    public static final long[] VIBRATE_PM_INCOMING = {
        0, 150, 100, 150, 100, 150
    };


    //~--- get methods --------------------------------------------------------

    /**
     * Combines the site URL with the path to the request script
     *
     * @return The fully qualified fetch URL
     */
    public static String getFetchUrl() {
        return AppConstants.SITE_URL + AppConstants.SITE_REQUEST_SCRIPT;
    }
}
