package com.sofurry.requests;

import android.os.Handler;
import android.os.Message;

import com.sofurry.base.interfaces.ICanHandleFeedback;
import com.sofurry.base.interfaces.IRequestHandler;
import com.sofurry.helpers.RefreshRequest;

/**
 * @author Rangarig
 *
 * A Request handler class handles all feedback coming from request threads
 */
public class RequestHandler implements IRequestHandler {
    private ICanHandleFeedback feedback = null;
    private boolean            killed   = false;

    /**
     * Handler to handle AjaxRequestFeedback
     */
    protected Handler requesthandler = new Handler() {
        public void handleMessage(Message msg) {
            try {
                
                if (msg.obj == null) {
                    onError(new RequestHandlerException(msg.arg1, new Exception("Null return value on requestHandler")));
                    return;
                }
                
                if (msg.obj instanceof Exception) {
                    onError(new RequestHandlerException(msg.arg1, (Exception) msg.obj));

                    return;
                }

                // Redirects a method call via the request handler to decouple gui and workerthreads
                if (msg.obj instanceof DataCall) {
                	((DataCall)msg.obj).call(); // Potential exceptions are handled by the exception handler below
                	return;
                }

                
                if (msg.obj instanceof RefreshRequest) {
                    refresh();

                    return;
                }
                

//                if (msg.obj instanceof JSONObject) {
//                    onData(msg.arg1, (JSONObject) msg.obj);
//
//                    return;
//                }

//                if (msg.obj instanceof ProgressSignal) {
//                    onProgress(msg.arg1, (ProgressSignal) msg.obj);
//
//                    return;
//                }

                onOther(msg.arg1, msg.obj);

                // onError(new Exception("Unknown return value on requestHandler ("+msg.obj.getClass().getName()+")"));
            } catch (Exception e) {
                onError(new RequestHandlerException(msg.arg1, e));
            }
        }
    };

    /**
     * Constructor that will allow the request handler to be joined with an activity that know how to handle the
     * feedback (implemented CanHandleFeedback interface)
     * @param feedback
     */
    public RequestHandler(ICanHandleFeedback feedback) {
        setFeedbackReceive(feedback);
    }

    /**
     * Sets the "killed" boolean to true, which is polled by running threads and instructs them to terminate.
     */
    public void killThreads() {
        killed = true;
    }

    
//    /**
//     * See if this can be replaced with DataCall instead.
//     * @param id
//     * @param obj
//     */
//    public void onData(int id, JSONObject obj) {
//        try {
//            feedback.onData(id, obj);
//        } catch (Exception e) {
//            feedback.onError(e);
//        }
//    }

    /*
     *  (non-Javadoc)
     * @see com.sofurry.requests.CanHandleFeedback#onError(int, java.lang.Exception)
     */
    public void onError(Exception e) {
        feedback.onError(e);
    }

    /*
     *  (non-Javadoc)
     * @see com.sofurry.requests.CanHandleFeedback#onOther(int, java.lang.Object)
     */
    public void onOther(int id, Object obj) throws Exception {
        try {
            feedback.onOther(id, obj);
        } catch (Exception e) {
            feedback.onError(e);
        }
    }

    /*
     *  (non-Javadoc)
     * @see com.sofurry.requests.CanHandleFeedback#onProgress(int, com.sofurry.requests.ProgressSignal)
     */

//    /**
//     * Method description
//     *
//     *
//     * @param id
//     * @param prg
//     */
//    public void onProgress(int id, ProgressSignal prg) {
//        try {
//            feedback.onProgress(id, prg);
//        } catch (Exception e) {
//            feedback.onError(e);
//        }
//    }
    
    /**
     * Posts an actual Message to the message handler
     * @param msg
     */
    public void postMessage(Message msg) {
    	requesthandler.sendMessage(msg);
    }

    /**
     * Envelops an object into a message and posts it to the MessageHandler
     * @param obj
     * The object to be passed as the message
     */
    public void postMessage(Object obj) {
    	if (obj instanceof Message)
    		requesthandler.sendMessage((Message)obj);
    	else
          postMessage(-1, obj);
    }

    /**
     * Envelops an object in a message and posts it to the MessageHandler
     * @param obj
     * The object to be passed as the message
     * @param id
     * The id to be passed to the callback. Usually this is the ajaxrequest's id
     */
    public void postMessage(int id, Object obj) {
        Message msg = requesthandler.obtainMessage();

        msg.arg1 = id;     // To recognize the message by
        msg.obj  = obj;    // The object containing data payload

        requesthandler.sendMessage(msg);
    }

//    /**
//     * Method description
//     *
//     *
//     * @param id
//     * @param obj
//     */
//    public void postMessageInline(int id, Object obj) {
//        try {
//            if (obj == null) {
//                onError(id, new Exception("Null return value on requestHandler"));
//
//                return;
//            }
//
//            if (obj instanceof Exception) {
//                onError(id, (Exception) obj);
//
//                return;
//            }
//
//            if (obj instanceof RefreshRequest) {
//                refresh();
//
//                return;
//            }
//
//            if (obj instanceof JSONObject) {
//                onData(id, (JSONObject) obj);
//
//                return;
//            }
//
//            if (obj instanceof ProgressSignal) {
//                onProgress(id, (ProgressSignal) obj);
//
//                return;
//            }
//
//            onOther(id, obj);
//
//            // onError(new Exception("Unknown return value on requestHandler ("+msg.obj.getClass().getName()+")"));
//        } catch (Exception e) {
//            onError(id, e);
//        }
//    }

    /*
     *  (non-Javadoc)
     * @see com.sofurry.requests.CanHandleFeedback#refresh()
     */

    /**
     * Method description
     *
     */
    public void refresh() {
        try {
            feedback.refresh();
        } catch (Exception e) {
            feedback.onError(e);
        }
    }

    /*
     *  (non-Javadoc)
     * @see com.sofurry.requests.IRequestHandler#getRequestHandler()
     * Returns the request handler for this session
     */

    /**
     * Method description
     *
     *
     * @return
     */
    public Handler getRequestHandler() {
        return requesthandler;
    }

    /*
     *  (non-Javadoc)
     * @see com.sofurry.requests.IRequestHandler#isKilled()
     *
     * Is called by running threads to see if they should terminate
     */

    /**
     * Method description
     *
     *
     * @return
     */
    public boolean isKilled() {
        return killed;
    }

    /**
     * Method description
     *
     *
     * @param feedback
     */
    public void setFeedbackReceive(ICanHandleFeedback feedback) {
        this.feedback = feedback;
    }
}
