package com.sofurry.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;


public class PrivateMessage implements Serializable, IHasThumbnail {

	private static final long serialVersionUID = -237449121440709783L;

	private int id;
	private String fromUser;
	private int fromUserId;
	private String toUser;
	private int toUserId;
	private String date;
	private String subject;
	private String message;
	private String status;
	private Bitmap thumbnail;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getFromUser() {
		return fromUser;
	}
	public void setFromUser(String fromUser) {
		this.fromUser = fromUser;
	}
	public int getFromUserId() {
		return fromUserId;
	}
	public void setFromUserId(int fromUserId) {
		this.fromUserId = fromUserId;
	}
	public String getToUser() {
		return toUser;
	}
	public void setToUser(String toUser) {
		this.toUser = toUser;
	}
	public int getToUserId() {
		return toUserId;
	}
	public void setToUserId(int toUserId) {
		this.toUserId = toUserId;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	/**
	 * Fills the Private Message object with the data from the provided JSONOBject
	 * @param obj
	 * An Object containting the messagedata
	 * @throws JSONException
	 */
	public void populate(JSONObject obj) throws JSONException {
		String id = obj.getString("id");
		String fromUserName = obj.getString("fromUserName");
		String date = obj.getString("date");
		String subject = obj.getString("subject");
		String status = obj.getString("status");
		setFromUser(fromUserName);
		setId(Integer.parseInt(id));
		setDate(date);
		setSubject(subject);
		setStatus(status);
	}
	
	// TODO: Create item fetching here
	public Bitmap getThumbnail() {
		return null;
	}
	
	public void populateThumbnail() throws Exception {
	
	}
	
	public byte getThumbAttempts() {
		return 10;
	}
	
	
	
}
