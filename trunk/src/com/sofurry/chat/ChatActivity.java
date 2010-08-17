package com.sofurry.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.util.Authentication;
import com.sofurry.util.HttpRequest;

public class ChatActivity extends Activity {
    private ScrollView scrollView;
    private TextView chatView;
    private EditText chatEntry;
    private Button sendButton;
	private int chatSequence = 0;
	private int roomId = 1;
	protected ChatPollThread chatPollThread;
	protected ChatSendThread chatSendThread;
	String requestUrl = AppConstants.SITE_URL + AppConstants.SITE_REQUEST_SCRIPT;
	protected Handler chatHandler;
	protected LinkedBlockingQueue<String> chatSendQueue;
	
	private String MESSAGETYPE_MESSAGE = "message";
	private String MESSAGETYPE_WHISPER = "whisper";
		

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chatlayout);
		chatView = (TextView) findViewById(R.id.chatview);
        scrollView = (ScrollView) findViewById(R.id.scrollview);
        chatEntry = (EditText) findViewById(R.id.chatentry);
        sendButton = (Button) findViewById(R.id.send);

        /* Create text handler */
        chatHandler = new Handler() {
                @Override public void handleMessage(Message msg)
                {
            		//Parse server response
            		try {
            			if (msg.obj == null)
            				return;
                        CharSequence serverResponse = (CharSequence) msg.obj.toString();
            			JSONObject jsonParser = new JSONObject(serverResponse.toString());
            			JSONArray items = new JSONArray(jsonParser.getString("data"));
            			if (items == null)
            				return;
            			int numResults = items.length();
            			for (int i = 0; i < numResults; i++) {
            				JSONObject jsonItem = items.getJSONObject(i);
            				String id = jsonItem.getString("id");
            				String fromUserName = jsonItem.getString("fromUserName");
            				String type = jsonItem.getString("type"); //can be message or whisper
            				String date = jsonItem.getString("timestamp");
            				String toUserName = jsonItem.getString("toUserName");
            				String message = jsonItem.getString("message");
            				if (Integer.parseInt(id) > chatSequence) {
            					chatSequence = Integer.parseInt(id);
            				}
            				chatView.append(fromUserName);
            				chatView.append(": ");
                            SpannableString str = colorText(message, type);
                            chatView.append(str);
                            Linkify.addLinks(chatView, Linkify.ALL);
                            chatView.append("\n");
            			}

            			scrollView.scrollTo(0, chatView.getHeight());

            		} catch (Exception e) {
            			Log.e("SF CHAT", e.getMessage());
            			e.printStackTrace();
            		}
                }
        };

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

            
		
		chatPollThread = new ChatPollThread(this.roomId);
		chatPollThread.start();
		chatSendQueue = new LinkedBlockingQueue<String>();
		chatSendThread = new ChatSendThread(this.roomId);
		chatSendThread.start();
	}

	protected String pollChat(int roomId) {
		//Send chat poll request, return result
		Map<String, String> requestParameters = new HashMap<String, String>();
		requestParameters.put("f", "chatfetch");
		requestParameters.put("lastid", ""+chatSequence);
		requestParameters.put("roomid", ""+roomId);

		try {
			// add authentication parameters to the request
			Map<String, String> authRequestParameters = Authentication.addAuthParametersToQuery(requestParameters);
			HttpResponse response = HttpRequest.doPost(requestUrl, authRequestParameters);
			String httpResult = EntityUtils.toString(response.getEntity());
			ArrayList<String> resultList = new ArrayList<String>();
			if (Authentication.parseResponse(httpResult) == false) {
				// Retry request with new otp sequence if it failed for the first time
				authRequestParameters = Authentication.addAuthParametersToQuery(requestParameters);
				response = HttpRequest.doPost(requestUrl, authRequestParameters);
				httpResult = EntityUtils.toString(response.getEntity());
			}
			String errorMessage = parseErrorMessage(httpResult);
			if (errorMessage == null) {
				return httpResult;
			}

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
					Message msg = chatHandler.obtainMessage();
					msg.obj = result;
					chatHandler.sendMessage(msg);
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

		public void run() {
			while (keepRunning) {
				String message = chatSendQueue.poll();
				if (message != null) {
					//Build the request and send it
					Map<String, String> requestParameters = new HashMap<String, String>();
					
					// Check if message is meant to be a command and set the function accordingly
					if(message.substring(0,1).contains("/")) {
						requestParameters.put("f", "chatcommand");
					}
					else {
						requestParameters.put("f", "chatpost");
					}

					requestParameters.put("message", ""+message);
					requestParameters.put("roomid", ""+roomId);

					try {
						// add authentication parameters to the request
						Map<String, String> authRequestParameters = Authentication.addAuthParametersToQuery(requestParameters);
						HttpResponse response = HttpRequest.doPost(requestUrl, authRequestParameters);
						String httpResult = EntityUtils.toString(response.getEntity());
						ArrayList<String> resultList = new ArrayList<String>();
						if (Authentication.parseResponse(httpResult) == false) {
							// Retry request with new otp sequence if it failed for the first time
							authRequestParameters = Authentication.addAuthParametersToQuery(requestParameters);
							response = HttpRequest.doPost(requestUrl, authRequestParameters);
							httpResult = EntityUtils.toString(response.getEntity());
						}
						String errorMessage = parseErrorMessage(httpResult);
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

	
	protected String parseErrorMessage(String httpResult) {
		try {
			// check for json error message and parse it
			Log.d("Chat.parseErrorMessage", "response: " + httpResult);
			JSONObject jsonParser;
			jsonParser = new JSONObject(httpResult);
			int messageType = jsonParser.getInt("messageType");
			if (messageType == AppConstants.AJAXTYPE_APIERROR) {
				String error = jsonParser.getString("error");
				Log.e("ChatList.parseErrorMessage", "Error: " + error);
				return error;
			}
		} catch (JSONException e) {
			Log.e("Chat.parseResponse", e.toString());
		}

		return null;

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
	public void finish() {
		super.finish();
		if (chatPollThread != null)
			chatPollThread.stopThread();
		if (chatSendThread != null)
			chatSendThread.stopThread();
	}

}
