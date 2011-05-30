package com.sofurry.requests;

//~--- imports ----------------------------------------------------------------

import android.util.Log;

import com.sofurry.AppConstants;
import com.sofurry.base.interfaces.IRequestHandler;
import com.sofurry.util.Authentication;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


//~--- classes ----------------------------------------------------------------

/**
 * Alternative to {@link com.sofurry.requests.RequestThread} that executes in the current thread instead of a separate
 * one. This is primarily used in {@link com.sofurry.services.PmNotificationService} as that is already executing in
 * its own thread.
 *
 */
public class InlineRequest {
    private AjaxRequest     request_ = null;
    private IRequestHandler handler_;


    //~--- constructors -------------------------------------------------------

    /**
     * Constructs ...
     *
     *
     * @param handler
     * @param request
     */
    public InlineRequest(IRequestHandler handler, AjaxRequest request) {
        handler_ = handler;
        request_ = request;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Standardized HTTP Request. Attempts authentication a second time, should it fail
     *
     * @param request The request to attempt
     *
     * @return Returns the result as plain text
     *
     * @throws Exception
     */
    public static String authenticatedHTTPRequest(AjaxRequest request) throws Exception {
        // add authentication parameters to the request
        request.authenticate();

        String       url        = HttpRequest.encodeURL(request.getUrl());
        HttpResponse response   = HttpRequest.doPost(url, request.getParameters());
        String       httpResult = EntityUtils.toString(response.getEntity());

        // Try authentication again, in case the first request fails
        if (!Authentication.parseResponse(httpResult)) {
            // Retry request with new OTP sequence if it failed for the first time
            request.authenticate();

            // requestParameters = Authentication.addAuthParametersToQuery(requestParameters);
            response   = HttpRequest.doPost(url, request.getParameters());
            httpResult = EntityUtils.toString(response.getEntity());

            if (!Authentication.parseResponse(httpResult)) {
                throw new Exception("Authentification Failed (2nd attempt).");    // Check the sequence reply
            }
        }

        return httpResult;
    }

    // Asynchronous http request and result parsing

    /**
     * Method description
     *
     */
    public void execute() {
        Object answer = null;    // Will contain the answer that is returned to the client
        
        try {
            // add authentication parameters to the request
            String httpResult = authenticatedHTTPRequest(request_);

            if ("".equals(httpResult)) {
                answer = new JSONObject();
            } else {
                try {
                    // Analyse results
                    JSONObject jsonParser = new JSONObject(httpResult);

                    // Check for error Message
                    parseErrorMessage(jsonParser);

                    answer = jsonParser;    // Return results to caller
                } catch (JSONException je) {
                    /*
                     * In case that no JSON data is returned, but a whole lot of text, we will just return the text
                     * instead, for better analysis.
                     */
                    answer = httpResult;
                }
            }
        } catch (ClientProtocolException e) {
            answer = e;
        } catch (IOException e) {
            answer = e;
        } catch (Exception e) {
            answer = e;
        }

        // Signal the result of the operation to our caller
        handler_.postMessageInline(request_.getRequestID(), answer);
    }

    /**
     * Parses returned HTML data, to determine if an Error Message was returned
     *
     * @param parsed
     *
     * @throws Exception
     */
    public static void parseErrorMessage(JSONObject parsed) throws Exception {
        try {
            // check for JSON error message and parse it
            int messageType = parsed.getInt("messageType");

            if (messageType == AppConstants.AJAXTYPE_APIERROR) {
                String error = parsed.getString("error");

                Log.d(AppConstants.TAG_STRING, "List.parseErrorMessage: " + error);

                throw new Exception(error);
            }
        } catch (JSONException e) {
            Log.d(AppConstants.TAG_STRING, "Auth.parseResponse: " + e.toString());
        }
    }
}
