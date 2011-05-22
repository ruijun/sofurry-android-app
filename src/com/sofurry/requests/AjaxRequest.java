package com.sofurry.requests;

//~--- imports ----------------------------------------------------------------

import com.sofurry.AppConstants;
import com.sofurry.base.interfaces.IRequestHandler;
import com.sofurry.util.Authentication;

import java.util.HashMap;
import java.util.Map;


//~--- classes ----------------------------------------------------------------

/**
 *  An Ajax requests contains the URL and the Parameters to perfom an Request against the AjaxAPI.
 *
 */
public class AjaxRequest {
    private Map<String, String> parameters     = null;    // The parameters to be passed
    private RequestThread       requestThread  = null;    // The thread that might currently be processing this request
    private String              url            = null;    // The requestURL
    private String              waitingMessage = null;    // The message to be displayed, while waiting for the request
    private int                 requestID      = 0;       /*
                                                           *  A choosable request ID that the handler might choose to
                                                           * interpret the returned data
                                                           */


    //~--- constructors -------------------------------------------------------

    /**
     * Creates an Ajax request with the Fetch URL defined in the AppConstants class
     * Parameters can then be added via addParameter
     */
    public AjaxRequest() {
        this.url        = AppConstants.getFetchUrl();
        this.parameters = new HashMap<String, String>();
    }

    /**
     * Creates an Ajax Request to be passed to a ContentRequestThread, with empty parameter list
     * addParameter can be used for extra parameters
     * @param url
     * The URL to query
     */
    public AjaxRequest(String url) {
        super();

        this.url        = url;
        this.parameters = new HashMap<String, String>();
    }

    /**
     * Creates an Ajax Request to be passed to a ContentRequestThread
     * @param url
     * The URL to query
     * @param parameters
     * The parameters to use
     */
    public AjaxRequest(String url, Map<String, String> parameters) {
        super();

        this.url        = url;
        this.parameters = parameters;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Creates a Parameter for the current request object. Parameters with the same Name
     * will be overwritten
     *
     * @param name Name of the Parameter
     * @param value Value of the Parameter
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
     *
     * @param handler The handler the requests are redirected to
     *
     * @return
     */
    public RequestThread execute(IRequestHandler handler) {
        requestThread = new RequestThread(handler, this);

        requestThread.start();

        return requestThread;
    }

    /**
     * Method description
     *
     *
     * @param handler
     */
    public void executeInline(IRequestHandler handler) {
        InlineRequest inlineRequest = new InlineRequest(handler, this);

        inlineRequest.execute();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Returns the parameters used for this request
     *
     * @return
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Returns the ID this request is to be recognized by
     *
     * @return
     */
    public int getRequestID() {
        return requestID;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns the waiting message for this request
     *
     * @return
     */
    public String getWaitingMessage() {
        return waitingMessage;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Sets an ID that is passed to the returning message
     *
     * @param id The ID of to recognize this request by
     */
    public void setRequestID(int id) {
        requestID = id;
    }

    /**
     * Sets the Waiting Message for this Request
     *
     * @param waitingMessage
     */
    public void setWaitingMessage(String waitingMessage) {
        this.waitingMessage = waitingMessage;
    }
}
