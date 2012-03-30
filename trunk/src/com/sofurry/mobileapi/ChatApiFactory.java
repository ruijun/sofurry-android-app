package com.sofurry.mobileapi;

import com.sofurry.mobileapi.core.Request;

/**
 * @author Rangarig
 * 
 * This class is used for all Chat-Related calls to the so-furry api.
 * 
 * See ApiFactory class for call examples.
 *
 */
public class ChatApiFactory {

	public static final String CHAT_API_URL = ApiFactory.API2_URL; 
	
	/**
	 * Joins a chatroom
	 * @param room
	 * The room to join
	 * @return
	 */
	public static Request createJoin(int room) {
		Request req = new Request();
		req.setURL(CHAT_API_URL + "/chat/feed/join");
		req.setParameter("format", "json"); // Currently not Supported
		req.setParameter("room", ""+ room);
		req.setParameter("character", "-1"); // Currently not supported
		return req;
	}
	
	/**
	 * Fetches a list of all available chatrooms
	 * @return
	 */
	public static Request createRoomList() {
		Request req = new Request();
		req.setURL(CHAT_API_URL + "/chat/api/roomlist");
		req.setParameter("format","json");
		return req;
	}
	
	/**
	 * Fetches a list of users connected to the specified room
	 * @param roomid
	 * The room id (as passed from list chatrooms
	 * @return
	 */
	public static Request createUserList(int roomid) {
		Request req = new Request();
		req.setURL(CHAT_API_URL + "/chat/feed/userlist");
		req.setParameter("room", "" + roomid);
		req.setParameter("format","json");
		return req;
	}
	
	/**
	 * Returns the chatroom events since the last chatfetch
	 * @param roomid
	 * The room id (as passed from listchatrooms)
	 * @param startID
	 * Sequence marker to identify what events to return
	 * @return
	 */
	public static Request createChatPoll(int startID,int roomid) {
		return createChatPoll(startID, new int[] {roomid});
	}

	/**
	 * Returns the chatroom events since the last chatfetch
	 * @param roomid
	 * The room id (as passed from listchatrooms)
	 * @param chatsequence
	 * Sequence marker to identify what events to return
	 * @return
	 */
	public static Request createChatPoll(int startID,int[] roomids) {
		Request req = new Request();
		req.setURL(CHAT_API_URL + "/chat/feed/backlog");
		req.setParameter("startID", "" + startID);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < roomids.length; i++) {
			if (sb.length() > 0) sb.append(",");
			sb.append(roomids[i]);
		}
		req.setParameter("rooms", sb.toString());
		return req;
	}

	
	/**
	 * Parses the string into the required number of parameters
	 * @param text
	 * The string to be parsed
	 * @param expected
	 * the expected number of items
	 * @param message
	 * the suffix to the error message Invalid number of parameters: ... suffix ...
	 * @return
	 * Returns an array containing the requested number of elements
	 */
	private static String[] parse(String text, int expected, String message) throws Exception{
		String[] buf = text.split(" ",expected);
		if (buf.length != expected) throw new Exception("Invalid syntax:" + message);
		return buf;
	}
	
	/**
	 * Removes the command in front and returns the rest of the line.
	 * @param text
	 * The text to remove the first word
	 * @return
	 */
	private static String restOfLine(String text) {
		int idx = text.indexOf(' ');
		if (idx == -1) return text;
		return text.substring(idx + 1);
	}
	
	
	/**
	 * Creates parameters for a status change
	 * @param req
	 * The request to set the parameters in
	 * @param status
	 * The status to set
	 */
	private static void createStatus(Request req, String status) {
		req.setURL(CHAT_API_URL + "/chat/feed/statusupdate");
		req.setParameter("status", status);
	}
	
	/**
	 * Posts a message into the specified room
	 * 
	 * WARNING: This request cannot be reused. (the request is build on context)
	 * 
	 * @param roomid
	 * The room id (as passed from listchatrooms)
	 * @param text
	 * The message to be posted into the room
	 * @return
	 */
	public static Request createSendMessage(String text,int roomid) throws Exception {
		Request req = new Request();
		req.setParameter("room", "" + roomid);
		
		// Parse the message for commands, and redirect to respective command header
		if (text.startsWith("/")) {
			String check = text.substring(1);
			
			// Invokes the help
			if (check.startsWith("help"))
			{
				req.setURL(CHAT_API_URL + "/chat/feed/help");
				return req;
			}
			
			// Allows to pose a character action
			if (check.startsWith("me")) {
				req.setURL(CHAT_API_URL + "/chat/feed/sendmessage");
				req.setParameter("action", "y");
				req.setParameter("text", text);
				return req;
			}

			// Talks to a user in private (whisper)
			if (check.startsWith("pm") || check.startsWith("whisper")) {
				req.setURL(CHAT_API_URL + "/chat/feed/sendwhisper");
				req.setParameter("text", text);
				return req;
			}
			
			// Bans a user
			if (check.startsWith("ban")) {
				req.setURL(CHAT_API_URL + "/chat/feed/modban");
				
				String[] param = parse(check,4,"ban username length reason");
				req.setParameter("username", param[1]);
				req.setParameter("banlength", param[2]);
				req.setParameter("reason", param[3]);
				return req;
			}
			
			// Bans a user with ageban
			if (check.startsWith("ageban")) {
				req.setURL(CHAT_API_URL + "/chat/feed/ageban");
				
				String[] param = parse(check,4,"ageban username length reason");
				req.setParameter("username", param[1]);
				req.setParameter("banlength", param[2]);
				req.setParameter("reason", param[3]);
				return req;
			}
			
			// Kicks a user from the chatroom
			if (check.startsWith("kick")) {
				req.setURL(CHAT_API_URL + "/chat/feed/kick");

				String[] param = parse(check,3,"kick username reason");
				req.setParameter("username", param[1]);
				req.setParameter("reason", param[3]);
				return req;
			}
			
			// Warn a user
			if (check.startsWith("warn")) {
				req.setURL(CHAT_API_URL + "/chat/feed/warn");

				String[] param = parse(check,3,"warn username reason");
				req.setParameter("username", param[1]);
				req.setParameter("reason", param[3]);
				return req;
			}
			
			// Mute a user (warning, not implemented yet)
			if (check.startsWith("mute")) {
				req.setURL(CHAT_API_URL + "/chat/feed/mute");

				String[] param = parse(check,3,"warn username reason");
				req.setParameter("username", param[1]);
				req.setParameter("reason", param[3]);
				return req;
			}
			
			// Change status to brb
			if (check.startsWith("brb")) {
				createStatus(req, "brb");
				return req;
			}

			// Change status to afk
			if (check.startsWith("afk") || check.startsWith("away")) {
				createStatus(req, "afk");
				return req;
			}

			// Change status to busy
			if (check.startsWith("busy")) {
				createStatus(req, "busy");
				return req;
			}

			// Change status to back
			if (check.startsWith("back")) {
				createStatus(req, "back");
				return req;
			}

			// Allows to block a user
			if (check.startsWith("block")) {
				req.setURL(CHAT_API_URL + "/chat/feed/block");
				req.setParameter("username", restOfLine(check));
				return req;
			}

			// Allows to unblock a user
			if (check.startsWith("unblock")) {
				req.setURL(CHAT_API_URL + "/chat/feed/unblock");
				req.setParameter("username", restOfLine(check));
				return req;
			}
			
			throw new Exception("Invalid command '" + check + "'");
		}
		
		// no special command found, so the text is simply posted
		req.setURL(CHAT_API_URL + "/chat/feed/sendmessage");
		req.setParameter("text", text);
		return req;
	}
	
}
