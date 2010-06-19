package com.sofurry;

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

import com.sofurry.util.Authentication;
import com.sofurry.util.HttpRequest;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class ChatActivity extends Activity {
	private int chatSequence = 0;
	private int roomId = 1;
	TextView chatView;
	ChatPollTask chatPollTask;
	String requestUrl = AppConstants.SITE_URL + AppConstants.SITE_REQUEST_SCRIPT;
	LinkedList<String> chatBuffer = new LinkedList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		chatView = new TextView(this);
		chatView.setSingleLine(false);
		setContentView(chatView);
		chatPollTask = new ChatPollTask();
		chatPollTask.execute(new Integer(this.roomId));
	}

	protected String pollChat(int roomId) {
		// TODO: Send chat poll request, return result
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
				if (chatBuffer.size() > 15) {
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

	private class ChatPollTask extends AsyncTask<Integer, String, Boolean> {
		protected Boolean doInBackground(Integer... roomId) {
			boolean keepRunning = true;
			while (keepRunning) {
				String result = pollChat(roomId[0]);
				publishProgress(result);
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return true;
		}
		@Override
		protected void onProgressUpdate(String... data) {
			updateChatView(data[0]);
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

}