package com.sofurry.chat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.util.Authentication;
import com.sofurry.util.HttpRequest;

public class ChatActivity extends Activity {
	protected TextView chatView;
	private int chatSequence = 0;
	private int roomId = 1;
	protected ChatPollThread chatPollThread;
	String requestUrl = AppConstants.SITE_URL + AppConstants.SITE_REQUEST_SCRIPT;
	LinkedList<String> chatBuffer = new LinkedList<String>();
	protected final Handler updateHandler = new Handler();
	protected String lastServerResponse = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		chatView = new TextView(this);
//		chatView.setSingleLine(false);
//		setContentView(chatView);
		setContentView(R.layout.chatlayout);
		chatView = (TextView) findViewById(R.id.chatview);
		chatView.setMaxHeight(200);

		
		chatPollThread = new ChatPollThread(this.roomId);
		chatPollThread.start();;
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

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	protected void updateChatView(String serverResponse) {
		//Parse server response
		try {
			JSONObject jsonParser = new JSONObject(serverResponse);
			JSONArray items = new JSONArray(jsonParser.getString("data"));
			int numResults = items.length();
			for (int i = 0; i < numResults; i++) {
				JSONObject jsonItem = items.getJSONObject(i);
				String id = jsonItem.getString("id");
				String fromUserName = jsonItem.getString("fromUserName");
				String date = jsonItem.getString("timestamp");
				String toUserName = jsonItem.getString("toUserName");
				String message = jsonItem.getString("message");
				if (Integer.parseInt(id) > chatSequence) {
					chatSequence = Integer.parseInt(id);
				}
				chatBuffer.add(fromUserName+": "+message+"\n");
				if (chatBuffer.size() > 500) {
					chatBuffer.removeFirst();
				}
			}
			
			StringBuilder resultText = new StringBuilder();
			for (String chatMessage : chatBuffer) {
				resultText.append(chatMessage);
			}
			
			chatView.setText(resultText);
		} catch (JSONException e) {
			Log.e("CHAT.parse", e.getMessage());
			e.printStackTrace();
		}
	}

	// Create runnable for updating list
	protected final Runnable updateViewRunnable = new Runnable() {
		public void run() {
			updateChatView(lastServerResponse);
		}
	};

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
				lastServerResponse = pollChat(roomId);
				updateHandler.post(updateViewRunnable);
				try {
					Thread.sleep(3000);
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
	
	@Override
	public void finish() {
		super.finish();
		if (chatPollThread != null)
			chatPollThread.stopThread();
	}

}