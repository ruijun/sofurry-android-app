package com.sofurry.chat;

import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.requests.AjaxRequest;
import com.sofurry.requests.RequestHandler;
import com.sofurry.requests.RequestThread;
import com.sofurry.util.Authentication;
import com.sofurry.util.ErrorHandler;

public class ChatActivity extends Activity {
	// TODO: Implement show Userlist
    private ScrollView scrollView;
    private TextView chatView;
    private TextView roomView;
    private EditText chatEntry;
    private Button sendButton;
	private int chatSequence = 0;
	private int roomId = 1;
	
	private int roomIds[] = null;
	private String roomNames[] = null;
	private String userNames[] = null;
	private ProgressDialog pd;
	protected ChatPollThread chatPollThread;
	protected ChatSendThread chatSendThread;
	String requestUrl = AppConstants.SITE_URL + AppConstants.SITE_REQUEST_SCRIPT;
	protected LinkedBlockingQueue<String> chatSendQueue;
	
	//private static String MESSAGETYPE_MESSAGE = "message";
	private static String MESSAGETYPE_WHISPER = "whisper";
		
	/**
	 * The request handler to be used to handle the feedback from the AjaxRequest
	 */
	protected RequestHandler requesthandler = new RequestHandler() {
		
		@Override
		public void onError(int id,Exception e) {
			showError(e);
		}
		
		@Override
		public void onData(int id,JSONObject obj) {
			if (id == AppConstants.REQUEST_ID_ROOMLIST) {
				populateRoomList(obj);
			}
			if (id == AppConstants.REQUEST_ID_USERLIST) {
				populateUserList(obj);
			}
		}

		@Override
		public void refresh() {
			// Dito.
		}

		@Override
		public void onOther(int id,Object obj) throws Exception {
			// If the object is text, it will be handled by the texthandler
			if (String.class.isAssignableFrom(obj.getClass())) {
				addTextToChatLog((String)obj);
			} else
			    super.onOther(id,obj);
		}
		
		
		
	};
	
	/**
	 * Shows the progress Dialog
	 * @param msg
	 */
	private void showProgressDialog(String msg) {
		pd = ProgressDialog.show(this, msg, "Please wait", true, false);
	}
	
	/**
	 * Hides the progress Dialog
	 */
	private void hideProgressDialog() {
		if (pd != null && pd.isShowing())
			  pd.dismiss();
	}
	
	/**
	 * Displays errors as they occur
	 * @param e
	 */
	public void showError(Exception e) {
		ErrorHandler.showError(this, e);
	}
	
	/**
	 * Adds text that was received from the AJAX Api, to the displayed chatlog
	 * @param str
	 */
	public void addTextToChatLog(String str) throws JSONException {
        CharSequence serverResponse = (CharSequence) str;
		JSONObject jsonParser = new JSONObject(serverResponse.toString());
		JSONArray items = new JSONArray(jsonParser.getString("data"));

		int numResults = items.length();
		for (int i = 0; i < numResults; i++) {
			JSONObject jsonItem = items.getJSONObject(i);
			String id = jsonItem.getString("id");
			String fromUserName = jsonItem.getString("fromUserName");
			String type = jsonItem.getString("type"); //can be message or whisper
			//String date = jsonItem.getString("timestamp");
			//String toUserName = jsonItem.getString("toUserName");
			String message = jsonItem.getString("message");
			if (Integer.parseInt(id) > chatSequence) {
				chatSequence = Integer.parseInt(id);
			}
			chatView.append(fromUserName);
			chatView.append(": ");
            SpannableString sstr = colorText(message, type);
            chatView.append(sstr);
            Linkify.addLinks(chatView, Linkify.ALL);
            chatView.append("\n");
		}

		scrollView.scrollTo(0, chatView.getHeight());
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chatlayout);
		chatView = (TextView) findViewById(R.id.chatview);
		roomView = (TextView) findViewById(R.id.roomtextview);
        scrollView = (ScrollView) findViewById(R.id.scrollview);
        chatEntry = (EditText) findViewById(R.id.chatentry);
        sendButton = (Button) findViewById(R.id.send);

//        /* Create text handler */
//        chatHandler = new Handler() {
//                @Override public void handleMessage(Message msg)
//                {
//            		//Parse server response
//            		try {
//            			if (msg.obj == null)
//            				return;
//                        CharSequence serverResponse = (CharSequence) msg.obj.toString();
//            			JSONObject jsonParser = new JSONObject(serverResponse.toString());
//            			JSONArray items = new JSONArray(jsonParser.getString("data"));
//            			if (items == null)
//            				return;
//            			int numResults = items.length();
//            			for (int i = 0; i < numResults; i++) {
//            				JSONObject jsonItem = items.getJSONObject(i);
//            				String id = jsonItem.getString("id");
//            				String fromUserName = jsonItem.getString("fromUserName");
//            				String type = jsonItem.getString("type"); //can be message or whisper
//            				String date = jsonItem.getString("timestamp");
//            				String toUserName = jsonItem.getString("toUserName");
//            				String message = jsonItem.getString("message");
//            				if (Integer.parseInt(id) > chatSequence) {
//            					chatSequence = Integer.parseInt(id);
//            				}
//            				chatView.append(fromUserName);
//            				chatView.append(": ");
//                            SpannableString str = colorText(message, type);
//                            chatView.append(str);
//                            Linkify.addLinks(chatView, Linkify.ALL);
//                            chatView.append("\n");
//            			}
//
//            			scrollView.scrollTo(0, chatView.getHeight());
//
//            		} catch (Exception e) {
//            			Log.e("SF CHAT", e.getMessage());
//            			e.printStackTrace();
//            		}
//                }
//        };
        
        /* Create send button callback */
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onSend();
            }
        });

        /* Create 'return' key callback */
        chatEntry.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                /* 'Enter' pressed */
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    onSend();
                    return true;
                /* 'Tab' pressed */
                } else if ((event.isAltPressed() && keyCode == KeyEvent.KEYCODE_Q && event.getAction() == KeyEvent.ACTION_DOWN) || keyCode == KeyEvent.KEYCODE_TAB && event.getAction() == KeyEvent.ACTION_DOWN) {
                    return onTab();
                } else {
                    return false;
                }
            }
        });
        
        getRoomList(); // Get Room list will populate the room list, and start the room selection
	}

    /**
     * Kills all the threads for this chat session
     */
    private void killThreads() {
		if (chatPollThread != null)
			chatPollThread.stopThread();
		if (chatSendThread != null)
			chatSendThread.stopThread();
    }

	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 * 
	 * Creates the Context Menu for this Activity.
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0,AppConstants.MENU_CHGROOM,0,"Rooms");
		menu.add(0,AppConstants.MENU_USERS,0,"Users");
		return result;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 * 
	 * Handles feedback from the context menu
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case AppConstants.MENU_CHGROOM:
			roomSelect();
			return true;
		case AppConstants.MENU_USERS:
			getUserList();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	
	/**
	 * Requests the room list from the AJAX api, and will call the populateRoomList callback
	 */
	private void getRoomList() {
		// Do we need to fetch the room list?
		if (roomIds == null) {
			showProgressDialog("Fetching rooms");
			AjaxRequest getRooms = new AjaxRequest();
			getRooms.addParameter("f", "chatrooms");
			getRooms.setRequestID(AppConstants.REQUEST_ID_ROOMLIST); // Mark this request, so the return value handler knows what to do with the result
			getRooms.execute(requesthandler);
		} else { // No we already have it, call dialog directly
			roomSelect();
		}
	}

	/**
	 * Populates the roomlist, and starts the room selection dialog
	 * @param obj
	 */
	private void populateRoomList(JSONObject obj) {
		try {
			JSONArray items = obj.getJSONArray("items");
			int cnt = items.length();
			roomNames = new String[cnt];
			roomIds = new int[cnt];
			for (int i = 0; i < cnt; i++) {
				JSONObject item = items.getJSONObject(i);
				Log.d("item", item.getString("name") + " " + item.getString("id"));
				roomNames[i] = item.getString("name");
				roomIds[i] = Integer.parseInt(item.getString("id")); 
			}
			
			roomSelect();
			
		} catch (Exception e) {
			showError(e);
		} finally {
			hideProgressDialog();
		}
	}
	
	/**
	 * Shows a Room selection dialog to the user, using the global roomTitles and roomIds variables
	 * The user will be able to change to the desired room by clicking on it.
	 */
	private void roomSelect() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose your Room:");
		builder.setItems(roomNames, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	changeRoom(item);
		    }
		});
		AlertDialog roomchooser = builder.create();
		roomchooser.show();
	}
	
	/**
	 * Changes the room, according to the provided index (sets the id in roomIDs[])
	 * @param idx
	 * The index in the two arrays roomNames and roomIDs
	 */
	private void changeRoom(int idx) {
		killThreads(); // For recalls
		roomView.setText("Your Room:" + roomNames[idx]);
		this.roomId = roomIds[idx];
		chatView.setText(""); // Clear chat window
		
		// Start the polling for our new room
		chatPollThread = new ChatPollThread(this.roomId);
		chatPollThread.start();
		chatSendQueue = new LinkedBlockingQueue<String>();
		chatSendThread = new ChatSendThread(this.roomId);
		chatSendThread.start();
		
		// Let us know we changed the room some more
    	Toast.makeText(getApplicationContext(), "Selected:" + roomNames[idx] + "("+roomIds[idx]+")", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Requests a list of currently online users in that room
	 * Since this list is subject to change, it gets updated every time, and destroyed once the menu is not used anymore
	 */
	private void getUserList() {
		showProgressDialog("Fetching users");
		AjaxRequest getRooms = new AjaxRequest();
		getRooms.addParameter("f", "onlineUsers");
		getRooms.addParameter("roomid", "" + this.roomId);
		getRooms.setRequestID(AppConstants.REQUEST_ID_USERLIST); // Mark this request, so the return value handler knows what to do with the result
		getRooms.execute(requesthandler);
	}

	/**
	 * Populates the userlist with the data returned from the API.
	 * @param obj
	 */
	private void populateUserList(JSONObject obj) {
		try {
			JSONArray items = obj.getJSONArray("items");
			int cnt = items.length();
			userNames = new String[cnt];
			for (int i = 0; i < cnt; i++) {
				JSONObject item = items.getJSONObject(i);
				userNames[i] = item.getString("name");
			}
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Select user to add:");
			builder.setItems(userNames, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			    	addUserName(item);
			    }
			});
			AlertDialog userchooser = builder.create();
			userchooser.show();
			
		} catch (Exception e) {
			showError(e);
		} finally {
			hideProgressDialog();
		}
	}
	
	/**
	 * Adds a username from the user array to the textfield
	 * @param idx
	 * The index to the username.
	 */
	private void addUserName(int idx) {
		if ((chatEntry.length() > 0) && (!chatEntry.getText().toString().endsWith(" ")))
		  chatEntry.getText().append(" ");
		chatEntry.getText().append(userNames[idx]);
		userNames = null; // Since the list will be recreated, we don't need this anymore.
	}
	
	
	/**
	 * Polls the Server for new messages to display
	 * @param roomId
	 * The ID of the currently selected room
	 * @return
	 */
	protected String pollChat(int roomId) {
		//Send chat poll request, return result
		AjaxRequest req = new AjaxRequest(requestUrl);
		
		//Map<String, String> requestParameters = new HashMap<String, String>();
		req.addParameter("f", "chatfetch");
		req.addParameter("lastid", ""+chatSequence);
		req.addParameter("roomid", ""+roomId);

		try {
			String httpResult = RequestThread.authenticadedHTTPRequest(req);
			RequestThread.parseErrorMessage(new JSONObject(httpResult));
			return httpResult;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	//This is the main message polling thread. This is necessary because we can't block the UI with our http requests 
	private class ChatPollThread extends Thread {
		boolean keepRunning = true;
		int roomId;
		
		// Set saveUserAvatar to true to save the returned thumbnail as the submission's user avatar
		public ChatPollThread(int roomId) {
			this.roomId = roomId;
		}

		public void stopThread() {
			keepRunning = false;
		}

		public void run() {
			while (keepRunning) {
				String result = pollChat(roomId);
				if (result != null) {
					requesthandler.postMessage(result);
				}
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	//This is a thread for sending chat messages. It will work off messages off a queue 
	private class ChatSendThread extends Thread {
		boolean keepRunning = true;
		int roomId;
		
		// Set saveUserAvatar to true to save the returned thumbnail as the submission's user avatar
		public ChatSendThread(int roomId) {
			this.roomId = roomId;
		}

		public void stopThread() {
			keepRunning = false;
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 * 
		 * Checks the chat queue in intervals and sends messages to the chat handler
		 */
		public void run() {
			while (keepRunning) {
				String message = chatSendQueue.poll();
				if (message != null) {
					AjaxRequest req = new AjaxRequest();
					//Build the request and send it
					
					// Check if message is meant to be a command and set the function accordingly
					if(message.substring(0,1).contains("/")) {
						req.addParameter("f", "chatcommand");
					}
					else {
						req.addParameter("f", "chatpost");
					}

					req.addParameter("message", ""+message);
					req.addParameter("roomid", ""+roomId);

					try {
						String httpResult = RequestThread.authenticadedHTTPRequest(req);
						RequestThread.parseErrorMessage(new JSONObject(httpResult));
					} catch (Exception e) {
						e.printStackTrace();
					}
				
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	    /* called to colorize messages */
    private SpannableString colorText(CharSequence text, String type) {
        Spanned s = Html.fromHtml(text.toString());
        SpannableString str = new SpannableString(s.toString());
        int color;
        /* topic color */
        if (text.toString().startsWith("*** Topic is: "))
            color = Color.YELLOW;
        /* nicks list color */
        else if (text.toString().startsWith("*** Nicks are: "))
            color = Color.GREEN;
        /* action color */
        else if (text.toString().startsWith("***"))
            color = Color.LTGRAY;
        /* cite color */
        else if (!text.toString().startsWith(Authentication.getUsername()) && text.toString().toLowerCase().contains(Authentication.getUsername().toLowerCase())) {
            color = Color.CYAN;
            Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            v.vibrate(200);
        }
        /* privmsg color */
        else if (text.toString().startsWith("<"))
        {
            color = Color.WHITE;
            Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            v.vibrate(200);
        }
        /* default color */
        else if (type.equalsIgnoreCase(MESSAGETYPE_WHISPER)) {
        	color = Color.BLACK;
            str.setSpan(new BackgroundColorSpan(Color.YELLOW), 0, s.toString().length(), 0);
        } else {
            color = Color.WHITE;
        }
        
        str.setSpan(new ForegroundColorSpan(color), 0, s.toString().length(), 0);
        
        
        return str;
    }

    // username autocompletion when pressing TAB key 
    public boolean onTab() {
/*        LinkedList<String> available = new LinkedList<String>();
        String text = mEditText.getText().toString();
        String b = text.substring(text.lastIndexOf(' ') + 1, text.length());
        for (String nick : getUserList()) {
            if (nick.startsWith(b)) {
                available.add(nick);
            }
        }
        if (available.size() == 0)  nothing to do 
            return false;
        else if (available.size() == 1) {
             complete nickname 
            mEditText.append(available.getFirst().substring(b.length(), available.getFirst().length()) + ": ");
            return true;
        } else {
            String display = new String("*** ");
            for (String nick : available) {
                display = display + nick + " ";
            }
            SpannableString str = colorText(display);
            mTextView.append(str);
            mTextView.append("\n");
            mScrollView.scrollTo(0, mTextView.getHeight());

            String subnick = "";
            boolean found = false;
            for (int i = 0; !found; i++) {
                for (String nick : available) {
                    if (nick.length() < i || available.getFirst().charAt(i) != nick.charAt(i)) {
                        found = true;  no more found 
                        break;
                    }
                    else {
                        subnick = nick.substring(0, i);
                    }
                }
            }
            mEditText.append(subnick.substring(b.length(), subnick.length()));
            return true;
        }
*/
    		return false;
    }

    private void onSend() {
    	Log.i("SF chat", "onSend() called");
    	chatSendQueue.add(chatEntry.getText().toString());
    	chatEntry.setText("");
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
        	finish();
            //return true;
        }

        return super.onKeyDown(keyCode, event);
    }

	@Override
	public void finish() {
		super.finish();
		killThreads();
	}

}
