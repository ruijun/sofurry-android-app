package com.sofurry.requests;

import java.util.HashMap;
import java.util.Map;

import com.sofurry.AppConstants;
import com.sofurry.util.Authentication;

/**
 *  An Ajax requests contains the URL and the Parameters to perfom an Request against the AjaxAPI.
 *
 */
public class AjaxRequest {

	private RequestThread requestThread = null; // The thread that might currently be proessing this request
	private String url = null; // The requestURL
	private Map<String,String> parameters = null; // The parameters to be passed
	private String waitingMessage = null; // The message to be displayed, while waiting for the request
	
	/**
	 * Creates an Ajax Request to be passed to a ContentRequestThread
	 * @param url
	 * The URL to query
	 * @param parameters
	 * The parameters to use
	 */
	public AjaxRequest(String url, Map<String, String> parameters) {
		super();
		this.url = url;
		this.parameters = parameters;
	}

	/**
	 * Creates an Ajax Request to be passed to a ContentRequestThread, with empty parameter list
	 * addParameter can be used for extra parameters
	 * @param url
	 * The URL to query
	 */
	public AjaxRequest(String url) {
		super();
		this.url = url;
		this.parameters = new HashMap<String, String>();
	}
	
	/**
	 * Creates an Ajax request with the Fetch URL defined in the AppConstants class
	 * Parameters can then be added via addParameter
	 */
	public AjaxRequest() {
		this.url = AppConstants.getFetchUrl();
		this.parameters = new HashMap<String, String>();
	}

	public String getUrl() {
		return url;
	}

	
	/**
	 * Returns the waiting message for this request
	 * @return
	 */
	public String getWaitingMessage() {
		return waitingMessage;
	}

	/**
	 * Sets the Waiting Message for this Request
	 * @param waitingMessage
	 */
	public void setWaitingMessage(String waitingMessage) {
		this.waitingMessage = waitingMessage;
	}

	/**
	 * Returns the parameters used for this request
	 * @return
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}
	
	/**
	 * Creates a Parameter for the current request object. Parameters with the same Name
	 * will be overwritten
	 * @param name
	 * Name of the Parameter
	 * @param value
	 * Value of the Parameter
	 */
	public void addParameter(String name, String value) {
		parameters.put(name, value);
	}
	
	/**
	 * Adds or updates authentification information to this request
	 */
	public void authenticate() {
		Authentication.addAuthParametersToQuery(parameters);
	}
	
	/**
	 * Executes this Ajax Request, and delegates the resulting data to the provided Handler
	 * @param handler
	 * The handler the requests are redirected to
	 */
	public void execute(IRequestHandler handler) {
		requestThread = new RequestThread(handler, this);
		requestThread.start();
	}
	
	

	
}
