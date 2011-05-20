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
     * Activity ID for the {@link com.sofurry.gallery.GalleryArt} class
     */
    public static final int ACTIVITY_GALLERYART = 4;

    /**
     * Activity ID for the {@link com.sofurry.list.ListJournals} class
     */
    public static final int ACTIVITY_JOURNALSLIST = 3;

    /**
     * Activity ID for the {@link com.sofurry.list.ListMusic} class
     */
    public static final int ACTIVITY_MUSICLIST = 7;

    /**
     * Activity ID for the {@link com.sofurry.list.ListPM} class
     */
    public static final int ACTIVITY_PMLIST = 1;

    /**
     * Activity ID for the {@link com.sofurry.RateActivity} class
     */
    public static final int ACTIVITY_RATE = 7;

    /**
     * Activity ID for the {@link com.sofurry.AccountActivity} class
     */
    public static final int ACTIVITY_SETTINGS = 2;

    /**
     * Activity ID for the {@link com.sofurry.list.ListStories} class
     */
    public static final int ACTIVITY_STORIESLIST = 0;

    /**
     * Activity ID for the {@link com.sofurry.TagEditor} class
     */
    public static final int ACTIVITY_TAGS = 6;

    /**
     * Activity ID for the {@link com.sofurry.itemviews.ViewPMActivity} class
     */
    public static final int ACTIVITY_VIEWPM = 5;

    /**
     * TODO: Document this!
     */
    public static final int AJAXTYPE_APIERROR = 5;

    /**
     * TODO: Document this!
     */
    public static final int AJAXTYPE_OTPAUTH = 6;

    /**
     * Initial delay after boot before checking for PMs the first time
     */
    public static final int ALARM_CHECK_DELAY_FIRST = 30000;

    /**
     * Delay between checking for PMs
     */
    public static final int ALARM_CHECK_DELAY_PERIOD = 30000;

    /**
     * Content Type: Artwork
     */
    public static final int CONTENTTYPE_ART = 1;

    /**
     * Content Type: Journals
     */
    public static final int CONTENTTYPE_JOURNALS = 3;

    /**
     * Content Type: Music
     */
    public static final int CONTENTTYPE_MUSIC = 2;

    /**
     * Content Type: Stories
     */
    public static final int CONTENTTYPE_STORIES = 0;

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
     * Menu ID: Add Favorite
     */
    public static final int MENU_ADDFAV = 4201;

    /**
     * Menu ID: Change Room
     */
    public static final int MENU_CHGROOM = 4401;

    /**
     * Menu ID: Cum!
     */
    public static final int MENU_CUM = 4203;

    /**
     * Menu ID: Filter: All
     */
    public static final int MENU_FILTER_ALL = 9090900;

    /**
     * Menu ID: Filter: Favorites
     */
    public static final int MENU_FILTER_FAVORITES = 9090902;

    /**
     * Menu ID: Filter: Featured
     */
    public static final int MENU_FILTER_FEATURED = 9090901;

    /**
     * Menu ID: Filter: Group
     */
    public static final int MENU_FILTER_GROUP = 9090904;

    /**
     * Menu ID: Filter: Keywords
     */
    public static final int MENU_FILTER_KEYWORDS = 4204;

    /**
     * Menu ID: Filter: Watchlist
     */
    public static final int MENU_FILTER_WATCHLIST = 9090903;

    /**
     * Menu ID: Filter: Watchlist Combined
     */
    public static final int MENU_FILTER_WATCHLIST_COMBINED = 9090905;

    /**
     * TODO: Document this!
     */
    public static final int MENU_HD = 4204;

    /**
     * TODO: Document this!
     */
    public static final int MENU_PLAY = 4205;

    /**
     * TODO: Document this!
     */
    public static final int MENU_RATE = 4300;

    /**
     * TODO: Document this!
     */
    public static final int MENU_RATE1 = 4301;

    /**
     * TODO: Document this!
     */
    public static final int MENU_RATE2 = 4302;

    /**
     * TODO: Document this!
     */
    public static final int MENU_RATE3 = 4303;

    /**
     * TODO: Document this!
     */
    public static final int MENU_RATE4 = 4304;

    /**
     * TODO: Document this!
     */
    public static final int MENU_RATE5 = 4305;

    /**
     * TODO: Document this!
     */
    public static final int MENU_REMFAV = 4202;

    /**
     * TODO: Document this!
     */
    public static final int MENU_SAVE = 4306;

    /**
     * TODO: Document this!
     */
    public static final int MENU_UNWATCH = 4207;

    /**
     * TODO: Document this!
     */
    public static final int MENU_USERS = 4402;

    /**
     * TODO: Document this!
     */
    public static final int MENU_USERSART = 4211;

    /**
     * TODO: Document this!
     */
    public static final int MENU_USERSMUSIK = 4212;

    /**
     * TODO: Document this!
     */
    public static final int MENU_USERSSTORIES = 4210;

    /**
     * TODO: Document this!
     */
    public static final int MENU_WATCH = 4206;

    /**
     * Notification ID for Private Messages
     */
    public static final int NOTIFICATION_ID_PM = 5000;

    /**
     * Private Message Status: New Message
     */
    public static final int PM_STATUS_NEW = 0;

    /**
     * Private Message Status: Message Read
     */
    public static final int PM_STATUS_READ = 1;

    /**
     * Private Message Status: Message Replied To
     */
    public static final int PM_STATUS_REPLIED = 2;

    /**
     * Keeps track of the largest Request ID defined
     */
    public static final int REQUEST_ID_0LARGEST = 4513;

    /**
     * Request ID: Cum!
     */
    public static final int REQUEST_ID_CUM = 4508;

    /**
     * Request ID: Download File
     */
    public static final int REQUEST_ID_DOWNLOADFILE = 4510;

    /**
     * Request ID: Download Image
     */
    public static final int REQUEST_ID_DOWNLOADIMAGE = 4511;

    /**
     * Request ID: Favorite
     */
    public static final int REQUEST_ID_FAV = 4506;

    /**
     * Request ID: Fetch Content
     */
    public static final int REQUEST_ID_FETCHCONTENT = 4503;

    /**
     * Request ID: Fetch Data
     */
    public static final int REQUEST_ID_FETCHDATA = 4505;

    /**
     * Request ID: Fetch Submission Data
     */
    public static final int REQUEST_ID_FETCHSUBMISSIONDATA = 4504;

    /**
     * Request ID: Rate
     */
    public static final int REQUEST_ID_RATE = 4509;

    /**
     * Request ID: Roomlist
     */
    public static final int REQUEST_ID_ROOMLIST = 4501;

    /**
     * Request ID: Send
     */
    public static final int REQUEST_ID_SEND = 4513;

    /**
     * Request ID: Un-favorite
     */
    public static final int REQUEST_ID_UNFAV = 4507;

    /**
     * Request ID: Userlist
     */
    public static final int REQUEST_ID_USERLIST = 4502;

    /**
     * Request ID: Watch
     */
    public static final int REQUEST_ID_WATCH = 4512;

    /**
     * TODO: Document this!
     */
    public static final int VIEWSOURCE_ALL = 0;

    /**
     * TODO: Document this!
     */
    public static final int VIEWSOURCE_FAVORITES = 1;

    /**
     * TODO: Document this!
     */
    public static final int VIEWSOURCE_FEATURED = 8;

    /**
     * TODO: Document this!
     */
    public static final int VIEWSOURCE_GROUP = 10;

    /**
     * TODO: Document this!
     */
    public static final int VIEWSOURCE_SEARCH = 5;

    /**
     * TODO: Document this!
     */
    public static final int VIEWSOURCE_USER = 7;

    /**
     * TODO: Document this!
     */
    public static final int VIEWSOURCE_WATCHLIST = 2;

    /**
     * TODO: Document this!
     */
    public static final int VIEWSOURCE_WATCHLIST_COMBINED = 11;

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
