/**********************************************************************************
 * Copyright (c) 2006 the r-smart group, inc.
 **********************************************************************************/
package com.rsmart.elluminate.adapter;

import org.apache.log4j.Logger;
import org.apache.velocity.context.Context;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.elluminate.vclassmanager.common.CalendarDay;
import com.elluminate.vclassmanager.common.ParamNames;
import com.elluminate.vclassmanager.common.SortOrder;
import com.elluminate.vclassmanager.common.UserProfile;
import com.elluminate.vclassmanager.controller.ControllerServlet;
import com.elluminate.vclassmanager.dataaccess.MeetNow;
import com.elluminate.vclassmanager.dataaccess.Meeting;
import com.elluminate.vclassmanager.dataaccess.Recording;
import com.elluminate.vclassmanager.dataaccess.SystemPreferences;
import com.elluminate.vclassmanager.dataaccess.SystemPreferencesData;
import com.elluminate.vclassmanager.dataaccess.User;
import com.elluminate.vclassmanager.dataaccess.comparator.recording.RecordingCreationDateComparator;
import com.elluminate.vclassmanager.dataaccess.comparator.recording.RecordingFacilitatorComparator;
import com.elluminate.vclassmanager.dataaccess.comparator.recording.RecordingMeetingNameComparator;
import com.elluminate.vclassmanager.dataaccess.comparator.recording.RecordingSizeComparator;
import com.elluminate.vclassmanager.manager.CalendarManager;
import com.elluminate.vclassmanager.manager.MeetingManager;
import com.elluminate.vclassmanager.manager.UserManager;
import com.elluminate.vclassmanager.service.ELMService;
import com.elluminate.vclassmanager.service.ELMServiceAdapter;
import com.elluminate.vclassmanager.service.ELMServiceInitializationException;
import com.elluminate.vclassmanager.service.ELMServiceProvider;
import com.elluminate.vclassmanager.service.InvalidServiceCallException;
import com.elluminate.vclassmanager.service.JNLPException;
import com.elluminate.vclassmanager.service.ServiceCall;
import com.elluminate.vclassmanager.service.encryption.EncryptionStrategy;
import com.elluminate.vclassmanager.util.CommonUtils;
import com.elluminate.vclassmanager.util.XMLUtils;




/**
 * This class provides sakai with access to the elluminate live server.
 * This adapter class will reside in a jar file on the elluminate live server.
 * Clients such as sakai make remote calls to the adapter by creating a java.net.URLConnection and writing to its output stream.
 * The format of such calls are:
 *     adapter=<adapter name>&command=<command name>&<additional command specific parameters>
 *
 *      where:
 *         1. adapter name is specified in the <elluminate_home>/manager/tomcat/webapps/ROOT/WEB-INF/resources/configuration.xml via the <name> attribute.
 *         2. command name is one of the elluminate supported commands, a list of which can be found in the GenericAdapter_dev_guide.pdf starting on page 4.
 *            this file comes as part of the elluminate sdk installation.
 *         3. additional command specific parameters are listed for each of the supported elluminate commands in the GenericAdapter_dev_guide.pdf.
 *
 * Example:
 * To create an hour long meeting called "MyFirstMeeting" which will start on June 1st, 2006 at 1:00pm which will be run by user "joneric",
 * you would create a url connection like so:
 *
 * URL           elluminateServerURL            = new URL("http://trgptest8:2089/service.html");
 * URLConnection elluminateLiveServerConnection = elluminateServerURL.openConnection();
 * elluminateLiveServerConnection.setDoOutput(true);
 *
 * PrintWriter out = new PrintWriter(elluminateLiveServerConnection.getOutputStream());
 * out.println("adapter=sakai_adapter&command=CreateMeeting&meetingName=MyFirstMeeting&startDate=01/06/2006 13:00:00 MST&endDate=01/06/2006 14:00:00 MST&facilitatorId=joneric");
 * out.close();
 *
 * // elluminate will invoke the method on its server and return the result in an xml document
 * BufferedReader in        = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 * String         inputLine = null;
 *
 * // here we are just printing the resulting xml out.  But you should probably put it into a jdom object.
 * while ((inputLine = in.readLine()) != null)
 *     System.out.println(inputLine);
 *
 * in.close();
 *
 * note: JNLP = Java Network Launch Protocol
 *       see http://java.sun.com/products/javawebstart/
 *           http://www.samspublishing.com/articles/article.asp?p=25043
 */
public class ElluminateAdapter implements ELMServiceAdapter
{
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss z");

    // elluminate live server command keywords
    private static final String COMMAND_BUILD_JNLP                = "buildJNLP";
    private static final String COMMAND_CREATE_MEETING            = "createMeeting";
    private static final String COMMAND_CREATE_MEETING_URL        = "createMeetingURL";
    private static final String COMMAND_CREATE_RECORDING_URL      = "createRecordingURL";
    private static final String COMMAND_CREATE_USER               = "createUser";
    private static final String COMMAND_DELETE_RECORDING          = "deleteRecording";
    private static final String COMMAND_GET_RECORDINGS_JNLP       = "getRecordingsJnlp";
    private static final String COMMAND_GET_MEETING               = "getMeeting";
    private static final String COMMAND_GET_USER_ID               = "getUserId";
    private static final String COMMAND_HELP_URL                  = "helpUrl";
    private static final String COMMAND_LIST_MEETINGS             = "listMeetings";
    private static final String COMMAND_LIST_RECORDINGS           = "listRecordings";
    private static final String COMMAND_MEETING_LOGIN             = "meetingLogin";
    private static final String COMMAND_RECORDING_LOGIN           = "recordingLogin";
    private static final String COMMAND_REMOVE_MEETING            = "removeMeeting";
    private static final String COMMAND_SERVER_TIMEZONE           = "serverTimeZone";
    private static final String COMMAND_SET_FACILITATOR           = "setFacilitator";
    private static final String COMMAND_TEST_ELM                  = "testELM";
    private static final String COMMAND_UPDATE_MEETING            = "updateMeeting";

    // parameter keywords that the elluminate live server places in the parameters Map for service calls
    private static final String PARAM_END_DATE                    = "endDate";
    private static final String PARAM_FACILITATOR_ID              = "facilitatorId";
    private static final String PARAM_FIRST_NAME                  = "firstName";
    private static final String PARAM_LAST_NAME                   = "lastName";
    private static final String PARAM_MEETING_ID                  = "meetingId";
    private static final String PARAM_MEETING_NAME                = "meetingName";
    private static final String PARAM_MEETING_PASSWORD            = "meetingPassword";
    private static final String PARAM_RECORDING_ID                = "recordingId";
    private static final String PARAM_START_DATE                  = "startDate";
    private static final String PARAM_USER_ID                     = "userId";
    private static final String PARAM_USER_NAME                   = "userName";

    // parameter keywords defined by rsmart
    private static final String PARAM_RSMART_AUTHENTICATION_TOKEN = "token";

    // results
    public static final String RESULT                        = "results";
    public static final String RESULT_MIMETYPE               = "mimetype";
    public static final String RESULT_HEADER                 = "header";

    // miscellaneous
    private static final String ADAPTER_VERSION              = "1.0.24";
    private static final String INVALIDCHARS                 = "<>&\\/\"\'%?#";

    // data members
    private Logger          logger;         // logger
    private DocumentBuilder builder;        // DOM xml document builder
    private ELMService      ELMServer;      // elluminate live server
    private String          adapterName;    // name of this adapter - it must be the same name specified in the <name>     tag for the <adapter> in the <elluminate_home>/manager/tomcat/webapps/ROOT/WEB-INF/resources/configuration.xml file.
    private String          templateName;   // template name        - it must be the same name specified in the <template> tag for the <adapter> in the <elluminate_home>/manager/tomcat/webapps/ROOT/WEB-INF/resources/configuration.xml file.
                                            //                        a corresponding .tmpl file with the given name must exist in the <elluminate_home>/server6_5/sessions/ directory.



    /**
     * default constructor.
     * <br/><br/>
     * @throws ParserConfigurationException
     */
    public ElluminateAdapter() throws ParserConfigurationException {
        logger = Logger.getLogger(getClass());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        builder = factory.newDocumentBuilder();
    }

    /**
     * This method is called by the elluminate live server to initialize the adapter.
     * <br/><br/>
     * @param ELMServer       a reference to the ELMService interface which will allow you to query and perform CRUD operations on meetings, recordings and users – as well as some URL and JNLP creation functionality.
     * @param adapterName     used by the elluminate live manager for addressing this adapter.  This ensures that http requests coming in are routed to the correct adapter (matching the adapter=<adapterName>).
     *                        it must be the same name specified in the <name>     tag for the <adapter> in the <elluminate_home>/manager/tomcat/webapps/ROOT/WEB-INF/resources/configuration.xml file.
     * @param templateName    it must be the same name specified in the <template> tag for the <adapter> in the <elluminate_home>/manager/tomcat/webapps/ROOT/WEB-INF/resources/configuration.xml file.
     *                        a corresponding .tmpl file with the given name must exist in the <elluminate_home>/server6_5/sessions/ directory.
     * <br/><br/>
     * @throws ELMServiceInitializationException   if the adapater can not be initialized.
     */
    public void initialize(ELMService ELMServer, String adapterName, String templateName) throws ELMServiceInitializationException  {
        this.ELMServer    = ELMServer;
        this.adapterName  = adapterName;
        this.templateName = templateName;

        logger.info("initialize(), adapter name: " + adapterName + ", adapter version: " + ADAPTER_VERSION + ", template name: " + templateName);
    }

    /**
     * process the elluminate live server's command.
     * This method will take a service call in the form of a URL request, which implements the parameters adapter, adapterName, and command.
     * The command parameters are put into a hash map and processRequest returns a XML document to the requesting adapter.
     * The custom LMS adapter can define commands so it is up to the adapter developer to determine what commands they want to process.
     */
    public Document processRequest(ServiceCall call) {
        // create a new xml document to hold the results results returned by the elluminate server
        Document results = builder.newDocument();

        // based on the service call command string, call the corresponding method
        // get the elluminate live server command and parameters that are being passed by the client
        String command    = call.getCommand();
        Map    parameters = call.getParameters();

        logger.info("processRequest(), command: " + command + ", parameters: " + (parameters == null ? 0 : parameters.size()));

        if (command == null || command.trim().length() == 0) {
           XMLUtils.appendFailureNode(results, "elluminate live server api command was null or empty.");
        } else {
           command = command.trim();

           if (COMMAND_BUILD_JNLP.equalsIgnoreCase(command))
           {
              try
              {
                 String jnlp = buildJNLP(parameters);
                 parameters.put(ControllerServlet.RESULT         , jnlp);
                 parameters.put(ControllerServlet.RESULT_MIMETYPE, "application/x-java-jnlp-file");
                 parameters.put(ControllerServlet.RESULT_HEADER  , "attachment; filename=\"meeting.jnlp\"");
                 results = null;
              } catch(JNLPException x) {
                 XMLUtils.appendFailureNode(results, x.getMessage());
              }
           }
           else if (COMMAND_HELP_URL            .equalsIgnoreCase(command)) {getHelpUrl        (            results);                 }
           else if (COMMAND_TEST_ELM            .equalsIgnoreCase(command)) {testElm           (            results);                 }
           else if (COMMAND_CREATE_MEETING      .equalsIgnoreCase(command)) {createMeeting     (parameters, results);                 }
           else if (COMMAND_SET_FACILITATOR     .equalsIgnoreCase(command)) {setFacilitator    (parameters, results);                 }
           else if (COMMAND_LIST_MEETINGS       .equalsIgnoreCase(command)) {listMeetings      (parameters, results);                 }
           else if (COMMAND_UPDATE_MEETING      .equalsIgnoreCase(command)) {updateMeeting     (parameters, results);                 }
           else if (COMMAND_REMOVE_MEETING      .equalsIgnoreCase(command)) {removeMeeting     (parameters, results);                 }
           else if (COMMAND_CREATE_MEETING_URL  .equalsIgnoreCase(command)) {createMeetingURL  (parameters, results);                 }
           else if (COMMAND_CREATE_RECORDING_URL.equalsIgnoreCase(command)) {createRecordingURL(parameters, results);                 }
           else if (COMMAND_LIST_RECORDINGS     .equalsIgnoreCase(command)) {listRecordings    (parameters, results);                 }
           else if (COMMAND_GET_RECORDINGS_JNLP .equalsIgnoreCase(command)) {getRecordingsJNLP (parameters, results);                 }
           else if (COMMAND_SERVER_TIMEZONE     .equalsIgnoreCase(command)) {serverTimeZone    (            results);                 }
           else if (COMMAND_DELETE_RECORDING    .equalsIgnoreCase(command)) {deleteRecording   (parameters, results);                 }
           else if (COMMAND_GET_MEETING         .equalsIgnoreCase(command)) {getMeeting        (parameters, results);                 }
           else if (COMMAND_GET_USER_ID         .equalsIgnoreCase(command)) {getUserId         (parameters, results);                 }
           else if (COMMAND_CREATE_USER         .equalsIgnoreCase(command)) {createUser        (parameters, results);                 }
           else if (COMMAND_MEETING_LOGIN       .equalsIgnoreCase(command)) {meetingLogin      (call               ); results = null; }
           else if (COMMAND_RECORDING_LOGIN     .equalsIgnoreCase(command)) {recordingLogin    (call               ); results = null; }
           else XMLUtils.appendFailureNode(results, "Unknown API Command [ " + command + " ]");
        }
        return results;
    }

    /**
     * Pings ELM to see if it is alive.
     * returns an xml document of the form:
     * <?xml version="1.0" encoding="UTF-8"?>
     * <results>
     *    <success>true</success>
     *    <version>[adapter version]</version>
     * </results>
     * <br/><br/>
     * where adapter version is the current code revision of the adapter.
     * <br/><br/>
     * @param results The xml document to append our response into
     */
    public void testElm(Document results) {
        logger.info("testElm()");

        Element root = results.createElement("results");

        Element flagNode = results.createElement("success");
        flagNode.appendChild(results.createTextNode(Boolean.toString(true)));
        root.appendChild(flagNode);

        Element versionNode = results.createElement("version");
        versionNode.appendChild(results.createTextNode(ADAPTER_VERSION));
        root.appendChild(versionNode);

        results.appendChild(root);
    }

    /**
     *
     */
    private void getHelpUrl(Document results) {
        logger.info("getHelpUrl()");

        String helpUrl = SystemPreferencesData.getInstance().getSystemPreferences().getHelpUrl();
        Element root = results.createElement("results");
        root.appendChild(results.createTextNode(helpUrl));
        results.appendChild(root);
    }

    public ELMService getProvider() {
       return this.ELMServer;
    }

    /**
     * Creates a new meeting, returning the meeting details back to the caller if successful, otherwise a failure node
     * is returned.  The following parameters are expected:
     *
     *     meetingName : The name of the meeting being created.
     *     startDate : The starting date for this meeting.
     *     endDate : The ending date for this meeting.
     *
     * @param parameters A map of parameters.
     * @param results The document to append resulting data into.
     */
    public void createMeeting(Map parameters, Document results) {
        logger.info("createMeeting()");

        // do a parameter validation check for standard elluminate parameters
        if (!parameters.containsKey(PARAM_MEETING_NAME) || !parameters.containsKey(PARAM_START_DATE) || !parameters.containsKey(PARAM_END_DATE) || (!parameters.containsKey(PARAM_FACILITATOR_ID) && !parameters.containsKey(PARAM_USER_NAME))) {
           XMLUtils.appendFailureNode(results, "Create Meeting Requires a Meeting Name, Start Date, End Date and either a Facilitator ID or Facilitator User Name");
           return;
        }

        // extract the standard elluminate parameters from url
        String meetingName = (String)parameters.get(PARAM_MEETING_NAME);
        if (!isValidMeetingName(meetingName))
        {
            XMLUtils.appendFailureNode(results, "Meeting Name contains illegal characters. The following characters are not allowed (" + INVALIDCHARS + ").");
            return;
        }

        String startDate = (String)parameters.get(PARAM_START_DATE);
        String endDate   = (String)parameters.get(PARAM_END_DATE);

        logger.info("createMeeting(), meeting name: " + meetingName + ", start: " + startDate + ", end: " + endDate);

        // create a new meeting based on provided params
        Date start = null;
        Date end = null;

        try {
           start = dateFormat.parse(startDate);
        } catch(ParseException px) {
            XMLUtils.appendFailureNode(results,"Invalid Start Date: " + startDate);
            return;
        }
        try {
           end = dateFormat.parse(endDate);
        } catch(ParseException px) {
            XMLUtils.appendFailureNode(results,"Invalid End Date: " + endDate);
            return;
        }

        Meeting meeting = new Meeting();
        meeting.setPassword("");
        meeting.setAdapter(getTemplateName());
        meeting.setName(meetingName);
        meeting.setStartDateTime(start.getTime());
        meeting.setEndDateTime(end.getTime());
        meeting.setPrivateMeeting(true);

        if (parameters.containsKey(PARAM_FACILITATOR_ID)) {
            String facilitatorId = (String)parameters.get(PARAM_FACILITATOR_ID);
            long facId;
            try {
               facId = Long.parseLong(facilitatorId);
            } catch (NumberFormatException nfe) {
                XMLUtils.appendFailureNode(results, "Invalid format for parameter " + PARAM_FACILITATOR_ID + "(" + facilitatorId + "). Must be a numerical value");
                return;
            }
            User user = ELMServer.getUser(facId);
            if (user == null) {
                XMLUtils.appendFailureNode(results, facilitatorId + " not found in user datastore.");
                return;
            }
            meeting.setFacilitatorId(facId);
        } else if (parameters.containsKey(PARAM_USER_NAME)) {
            String userName = (String)parameters.get(PARAM_USER_NAME);
            User user = ELMServer.getUser(userName);
            if (user == null) {
                XMLUtils.appendFailureNode(results, userName + " not found in user datastore.");
                return;
            }
            meeting.setFacilitatorId(user.getId());
        }

        try {
           ELMServer.createMeeting(meeting);
        } catch(InvalidServiceCallException x) {
            XMLUtils.appendFailureNode(results, "Internal Error: " + x.getMessage());
            return;
        }

        // build a result xml document to return to caller
        Element root = results.createElement("results");
        XMLUtils.appendMeetingNode(root, results, meeting, dateFormat);
        results.appendChild(root);
    }

    /**
     * Lists a set of meetings, expects that there are two parameters (assumes filter is ignored for each parameter
     * that does not exist in this list):
     *
     *     courseId : The course id for this meeting.
     *     instructorId : The instructor id for this meeting.
     *
     * @param parameters A map of parameters.
     * @param results The document to append resulting data into.
     */
    public void listMeetings(Map parameters, Document results) {
        logger.info("listMeetings()");

        // get full list of all meetings
        List meetings = ELMServer.listMeetings();

        // build resulting xml document to return to caller
        Element root = results.createElement("results");
        for(int i = 0; i < meetings.size(); i++) {
            Meeting meeting = (Meeting) meetings.get(i);
            XMLUtils.appendMeetingNode(root, results, meeting, dateFormat);
        }
        results.appendChild(root);
    }

    /**
     * Updates the meeting given (id parameter) with the parameters found, parameters not found are ignored and the
     * cooresponding values left unchanged.
     *
     *     courseId : The course id for this meeting.
     *     instructorId : The instructor id for this meeting.
     *     meetingName : The name of the meeting being created.
     *     startDate : The starting date for this meeting.
     *     endDate : The ending date for this meeting.
     *
     * @param parameters A map of parameters.
     * @param results The document to append resulting data into.
     */
    public void updateMeeting(Map parameters, Document results) {
        logger.info("updateMeeting()");

        // do parameter validation check
        if (!parameters.containsKey(PARAM_MEETING_ID)) {
            XMLUtils.appendFailureNode(results, "You must provide a Meeting ID in order to update a meeting!");
            return;
        }

        // extract parameters from url
        String meetingName = (String)parameters.get(PARAM_MEETING_NAME);
        if (!isValidMeetingName(meetingName)) {
            XMLUtils.appendFailureNode(results, "Meeting Name contains illegal characters. The following characters are not allowed (" + INVALIDCHARS + ").");
            return;
        }

        String meetingId = (String)parameters.get(PARAM_MEETING_ID);
        String startDate = (String)parameters.get(PARAM_START_DATE);
        String endDate   = (String)parameters.get(PARAM_END_DATE);
        String userID    = (String)parameters.get(PARAM_FACILITATOR_ID);
        String userName  = (String)parameters.get(PARAM_USER_NAME);

        // create a new meeting based on provided params
        User user = null;
        long id;
        try {
           id = Long.parseLong(meetingId);
        } catch (NumberFormatException nfe) {
            XMLUtils.appendFailureNode(results, "Invalid format for parameter " + PARAM_MEETING_ID + "(" + meetingId + "). Must be a numerical value");
            return;
        }

        if (userName != null) {
            user = ELMServer.getUser(userName);
            if (user == null) {
                // No user found, so terminate with error
                XMLUtils.appendFailureNode(results, userName + " not found in user datastore.");
                return;
            }
        } else if (userID != null) {
            user = ELMServer.getUser(Long.parseLong(userID));
            if (user == null) {
                // No user found, so terminate with error
                XMLUtils.appendFailureNode(results, userID + " not found in user datastore.");
                return;
            }
        }
        Meeting meeting = ELMServer.getMeeting(id);
        if (meeting == null) {
            XMLUtils.appendFailureNode(results, "Meeting does not exsist: " +  meetingId);
            return;
        }

        if (startDate != null) {
            Date start = null;
            try {
               start = dateFormat.parse(startDate);
            } catch(ParseException px) {
                XMLUtils.appendFailureNode(results,"Invalid Start Date: " + startDate);
                return;
            }
            meeting.setStartDateTime(start.getTime());
        }
        if (endDate != null) {
            Date end = null;
            try {
               end = dateFormat.parse(endDate);
            } catch(ParseException px){
                XMLUtils.appendFailureNode(results,"Invalid End Date: " + endDate);
                return;
            }
            meeting.setEndDateTime(end.getTime());
        }
        if (meetingName != null) {
            try {
               meeting.setName(URLEncoder.encode(meetingName,"UTF-8"));
            } catch (UnsupportedEncodingException x) {
                XMLUtils.appendFailureNode(results, "Internal Error: " + x.getMessage());
                return;
            }
        }
        if (user != null)
        {
            // Update user id
            meeting.setFacilitatorId(user.getId());
        }

        // update meeting details
        try {ELMServer.updateMeeting(meeting);}
        catch(InvalidServiceCallException x)
        {
            XMLUtils.appendFailureNode(results, "Internal Error: " + x.getMessage());
            return;
        }

        // build a result xml document to return to caller
        Element root = results.createElement("results");
        XMLUtils.appendMeetingNode(root, results, meeting, dateFormat);
        results.appendChild(root);
    }

    /**
     * returns a list of recording links for the JNLPs to playback recordings for a given meeting (id parameter)
     *
     *     meetingId : The course id for this meeting.
     *
     * @param parameters A map of parameters.
     * @param results The document to append resulting data into.
     */
    public void getRecordingsJNLP(Map parameters, Document results) {
        logger.info("getRecordingsJNLP()");

        // do parameter validation check
        if (!parameters.containsKey(PARAM_MEETING_ID))
        {
            XMLUtils.appendFailureNode(results, "You must provide a Meeting ID in order to retrieve Recording Links for the meeting!");
            return;
        }

        // extract parameters from url
        String meetingId = (String)parameters.get(PARAM_MEETING_ID);

        // get full list of all recordings for the given date range
        long id = Long.parseLong(meetingId);
        List recordings = ELMServer.listRecordingsForMeeting(id);

        if (recordings.size() == 0)
        {
            XMLUtils.appendFailureNode(results, "No recordings found for meetingId: " +  meetingId + ".");
            return;
        }

        // build resulting xml document to return to caller, filter out on course and instructor id
        Element root = results.createElement("results");
        for(int i = 0; i < recordings.size(); i++)
        {
            // get the next recording and initialize flag to include this node
            Element recordingUrlNode = results.createElement("recordingURL");
            String recordingURL = null;
            try {
               recordingURL = ELMServer.createRecordingURL(((Recording)recordings.get(i)).getId());
            } catch (InvalidServiceCallException x) {
                XMLUtils.appendFailureNode(results, "Internal Error: " + x.getMessage());
                return;
            }
            try {
               recordingUrlNode.appendChild(results.createTextNode(URLEncoder.encode(recordingURL, "UTF-8")));
            } catch (UnsupportedEncodingException x) {
                XMLUtils.appendFailureNode(results, "Internal Error: " + x.getMessage());
                return;
            }
            root.appendChild(recordingUrlNode);
        }

        // add this recording to the list
        results.appendChild(root);
    }
    /**
     * Removes a meeting from the local file store.
     *
     *      meetingId : The unique meeting identifier.s
     *
     * @param parameters A map of parameters.
     * @param results The document to append resulting data into.
     */
    public void removeMeeting(Map parameters, Document results) {
        logger.info("removeMeeting()");

        // paramter validation check
        if (!parameters.containsKey(PARAM_MEETING_ID)) {
            XMLUtils.appendFailureNode(results, "You must provide a Meeting ID in order to remove a meeting!");
            return;
        }

        // get parameter values
        String meetingId = (String)parameters.get(PARAM_MEETING_ID);

        // remove the meeting form the data store
        long id;
        try {
           id = Long.parseLong(meetingId);
        } catch (NumberFormatException nfe) {
           XMLUtils.appendFailureNode(results, "Invalid format for parameter " + PARAM_MEETING_ID + "(" + meetingId + "). Must be a numerical value");
           return;
        }

        Meeting meeting;
        try {
           meeting = ELMServer.removeMeeting(id);
        } catch (InvalidServiceCallException x) {
           XMLUtils.appendFailureNode(results, "Internal Error: " + x.getMessage());
           return;
        }

        // build a result xml document to return to caller
        Element root = results.createElement("results");
        XMLUtils.appendMeetingNode(root, results, meeting, dateFormat);
        results.appendChild(root);
    }

    /**
     * Create meeting URL (or recording URL) based on the meetingID.  If the meetingID is numeric than assume it
     * is a meeting ID, otherwise assume we are being given a recording ID.
     *
     *      meetingId : The meeting id used for resolving JNLP details and building the JNLP URL.
     *
     * @param parameters A map of parameters.
     * @param results The document to append resulting data into.
     */
    public void createMeetingURL(Map parameters, Document results) {
       logger.info("createMeetingURL()");

       // parameter validation check
       if (!parameters.containsKey(PARAM_MEETING_ID)) {
           XMLUtils.appendFailureNode(results, "You must provide a Meeting ID in order to create a meeting url!");
           return;
       }
       if (!parameters.containsKey(PARAM_USER_NAME)) {
           XMLUtils.appendFailureNode(results, "You must provide a User Name in order to create a meeting url!");
           return;
       }
       if (!parameters.containsKey(PARAM_MEETING_NAME)) {
           XMLUtils.appendFailureNode(results, "You must provide the Meeting Name in order to create a meeting url!");
           return;
       }
       if (!parameters.containsKey(PARAM_START_DATE)) {
           XMLUtils.appendFailureNode(results, "You must provide the Meeting Start Date and Time in order to create a meeting url!");
           return;
       }
       if (!parameters.containsKey(PARAM_END_DATE)) {
           XMLUtils.appendFailureNode(results, "You must provide the Meeting End Date and Time in order to create a meeting url!");
           return;
       }

       // extract parameters
       String jnlpUrl;
       String meetingId   = (String) parameters.get(PARAM_MEETING_ID);
       String userName    = (String)parameters.get(PARAM_USER_NAME);
       String password    = (String)parameters.get(PARAM_MEETING_PASSWORD);
       String meetingName = (String)parameters.get(PARAM_MEETING_NAME);
       String startDate   = (String)parameters.get(PARAM_START_DATE);
       String endDate     = (String)parameters.get(PARAM_END_DATE);
       String isModerator = (String)parameters.get(PARAM_END_DATE);


       logger.info("createMeetingURL(), meeting id..: " + meetingId);
       logger.info("createMeetingURL(), meeting name: " + meetingName);
       logger.info("createMeetingURL(), start date..: " + startDate);
       logger.info("createMeetingURL(), end   date..: " + endDate);
       logger.info("createMeetingURL(), username....: " + userName);
       logger.info("createMeetingURL(), password....: " + password);

       // convert the parameters from strings to their native types
       long id;
       try {
          id = Long.parseLong(meetingId);
       } catch (NumberFormatException nfe) {
          XMLUtils.appendFailureNode(results, "Invalid format for parameter " + PARAM_MEETING_ID + "(" + meetingId + "). Must be a numerical value");
          return;
       }

       Date start = null;
       try {
          start = dateFormat.parse(startDate);
       } catch(ParseException px) {
          XMLUtils.appendFailureNode(results,"Invalid Start Date: " + startDate);
          return;
       }

       Date end = null;
       try {
          end = dateFormat.parse(endDate);
       } catch(ParseException px) {
          XMLUtils.appendFailureNode(results,"Invalid End Date: " + endDate);
          return;
       }

       // retrieve the meeting specified by the parameters
       Meeting meeting = ELMServer.getMeeting(id);
       if (meeting == null) {
           XMLUtils.appendFailureNode(results, "Meeting does not exsist: " +  meetingId);
           return;
       }

       // verify that this meeting is the meeting the user wants to join
       String meetingNameSaved = null;
       try {
          meetingNameSaved = URLDecoder.decode(meeting.getMeetingNamePadded().trim(), "UTF-8");
       } catch (UnsupportedEncodingException uee) {
          XMLUtils.appendFailureNode( results,"Could not decode the meeting name (" + meeting.getMeetingNamePadded().trim() + " using utf-8 for meeting: " +  Long.toString(id));
          return;
       }
       logger.info("createMeetingURL(), verifying meeting: " + meetingName + " == " + meetingNameSaved + " : " +  meetingNameSaved.equalsIgnoreCase(meetingName));

       if (meeting.getId() == id && meetingNameSaved.equalsIgnoreCase(meetingName) && meeting.getStartDateTime() == start.getTime() && meeting.getEndDateTime() == end.getTime()) {
          logger.info("createMeetingURL(), meeting verified");

           // Now check to see if the user is a moderator, then pass in the moderator password for the meeting.
           // todo: replace this with whatever we want to do in sakai
           if (userName.equals(meeting.getFacilitatorName())) {
               password = ELMServer.getUser(userName).getPassword();
           }

           // build a new JNLP url for a meeting
           try {
              jnlpUrl = ELMServer.createMeetingURL(id, userName, password);
           } catch (InvalidServiceCallException x) {
              XMLUtils.appendFailureNode(results, "Internal Error: " + x.getMessage());
              return;
           }
       } else {
          XMLUtils.appendFailureNode(results, "Meeting does not exsist: " +  Long.toString(id));
          return;
       }

       // append the adapter information to the url
       EncryptionStrategy strategy = SystemPreferencesData.getInstance().getSystemPreferences().getEncryptionStrategy();
       jnlpUrl += "&adapter=" + strategy.encrypt(getAdapterName());

       // build a result document to return to caller
       Element root = results.createElement("results");
       Element jnlp = results.createElement("jnlp");
       jnlp.appendChild(results.createCDATASection(jnlpUrl));
       root.appendChild(jnlp);
       results.appendChild(root);
    }

    /**
     * Create recroding URL based on the recordingID.  .
     *
     *      recordingId : The recording id used for resolving JNLP details and building the JNLP URL.
     *
     * @param parameters A map of parameters.
     * @param results The document to append resulting data into.
     */
    public void createRecordingURL(Map parameters, Document results) {
       // parameter validation check
       if( !parameters.containsKey( PARAM_RECORDING_ID ) )
       {
           XMLUtils.appendFailureNode( results, "You must provide a Recording ID in order to create a recording url!" );
           return;
       }
       if( !parameters.containsKey( PARAM_MEETING_ID ) )
       {
           XMLUtils.appendFailureNode( results, "You must provide a Meeting ID in order to create a recording url!" );
           return;
       }

       // extract parameters
       String jnlpUrl;
       String meetingId = ( String )parameters.get( PARAM_MEETING_ID );
       String recordingFileName = ( String )parameters.get( PARAM_RECORDING_ID );

       if (recordingFileName.startsWith(meetingId + "_")) {
          try {
             jnlpUrl = ELMServer.createRecordingURL( recordingFileName );
          } catch (InvalidServiceCallException x) {
             XMLUtils.appendFailureNode( results, "Internal Error: " + x.getMessage() );
             return;
          }

          // append the adapter information to the url
          EncryptionStrategy strategy = SystemPreferencesData.getInstance( ).getSystemPreferences( ).getEncryptionStrategy( );
          jnlpUrl += "&adapter=" + strategy.encrypt( getAdapterName( ) );

          // build a result document to return to caller
          Element root = results.createElement( "results" );
          Element jnlp = results.createElement( "jnlp" );
          jnlp.appendChild( results.createCDATASection( jnlpUrl ) );
          root.appendChild( jnlp );
          results.appendChild( root );
       } else {
          XMLUtils.appendFailureNode( results, "Recording does not exsist: " +  recordingFileName + " for Meeting id " + meetingId + " .");
       }
    }

    /**
     * Lists recordings based on the start date, end date, course id and intructor id (the later two are extrapolated
     * from the cooresponding meeting ID).
     *
     *      startDate : Starting date for a date range filter.
     *      endDate : Ending date for a date range filter.
     *      courseId : The course ID to filter on.
     *      instructorId : The instructor Id to filter on.
     *
     * @param parameters A map of parameters.
     * @param results The document to append resulting data into.
     */
    public void listRecordings(Map parameters, Document results) {
        logger.info("listRecordings()");

        try
        {
            // get the start and end dates
            long meetingId = -1;
            if (parameters.containsKey(PARAM_MEETING_ID))
            {
                String meetId = (String) (String)parameters.get(PARAM_MEETING_ID);
                try {meetingId = Long.parseLong(meetId);}
                catch (NumberFormatException nfe)
                {
                    XMLUtils.appendFailureNode(results, "Invalid format for parameter " + PARAM_MEETING_ID + "(" + meetId + "). Must be a numerical value");
                    return;
                }
            }

            Date startDate = null;
            if (parameters.containsKey(PARAM_START_DATE))
            {
                String start = (String)parameters.get(PARAM_START_DATE);
                if (start != null)
                {
                    try {startDate = dateFormat.parse(start);}
                    catch(ParseException px)
                    {
                        XMLUtils.appendFailureNode(results,"Invalid Start Date: " + start);
                        return;
                    }
                }
            }

            Date endDate = null;
            if (parameters.containsKey(PARAM_END_DATE))
            {
                String end = (String)parameters.get(PARAM_END_DATE);
                if (end != null)
                {
                    try {endDate = dateFormat.parse(end);}
                    catch(ParseException px)
                    {
                        XMLUtils.appendFailureNode(results,"Invalid End Date: " + end);
                        return;
                    }
                }
            }

            // get full list of all recordings for the given date range
            List recordings = null;
            if (meetingId > 0) recordings = ELMServer.listRecordingsForMeeting(meetingId);
            else recordings = ELMServer.listRecordings(startDate, endDate);


            // build resulting xml document to return to caller, filter out on course and instructor id
            Element root = results.createElement("results");
            int included = 0;
            for(int i = 0; i < recordings.size(); i++)
            {
                Recording recording = (Recording) recordings.get(i);
                Date recStart = recording.getCreationDate();
                boolean include = true;
                if (startDate != null) include = include && recStart.after(startDate);
                if (endDate != null) include = include && recStart.before(endDate);
                if (include)
                {
                    // get the next recording and initialize flag to include this node
                    XMLUtils.appendRecordingNode(root, results, recording, dateFormat);
                    included++;
                }
            }

            if (included == 0)
            {
                XMLUtils.appendFailureNode(results,"No recordings found for your filter.");
                return;
            }

            // add this recording to the list
            results.appendChild(root);
        }
        catch(Exception x)
        {
            XMLUtils.appendFailureNode(results, x);
        }
    }

    /**
     * Build a JNLP and return to caller.
     *
     * @param parameters The parameter map containing parameters and used to store result.
     */
    private String buildJNLP(Map parameters) throws JNLPException {
       logger.info("buildJNLP()");

       String jnlp = null;
       String id   = (String)parameters.get(PARAM_MEETING_ID);

       try
       {
           long meetingId = Long.parseLong(id);

           // Now that we are here, validate the meeting is still in session.
           Meeting meeting   = ELMServer.getMeeting(meetingId);
           long    startTime = meeting.getStartDateTime() - (SystemPreferencesData.getInstance().getSystemPreferences().getThresholdValue() * 60 * 1000);
           long    endTime   = meeting.getEndDateTime();
           long    curTime   = new Date().getTime();

           if ((curTime >= startTime) && (curTime <= endTime))
           {
               String user = (String)parameters.get(PARAM_USER_NAME);
               if (user.trim().length() == 0)
                   throw new JNLPException("Username can not be blank. Note: All leading and trailing spaces are trimmed.");

               String password = (String)parameters.get(PARAM_MEETING_PASSWORD);
               jnlp = ELMServer.buildMeetingJNLP(meetingId, user, password, templateName);
           } else {
               // We are not in session, so do not generate a jnlp.
               throw new JNLPException("The time for the session are:" + meeting.getFormattedStartDateTime() + " to "  + meeting.getFormattedEndDateTime()+ ".");
           }
       } catch(NumberFormatException x) {
           jnlp = ELMServer.buildRecordingJNLP(id);
       }
       return jnlp;
    }

    /**
     * retrieves the elluminate server's time zone.
     */
    public void serverTimeZone(Document results) {
       logger.info("serverTimeZone()");

       SystemPreferences preferences = SystemPreferencesData.getInstance().getSystemPreferences();
       TimeZone timeZone = preferences.getTimeZone().getActualTimeZone();
       String displayName = timeZone.getID();

       Element root = results.createElement("results");
       Element timeZoneNode = results.createElement("timezone");
       timeZoneNode.appendChild(results.createTextNode(displayName));
       root.appendChild(timeZoneNode);
       results.appendChild(root);
    }

    /**
     *  sets the facilitator for a given meeting.
     */
    public void setFacilitator(Map parameters, Document results) {
       logger.info("setFacilitator()");

       // parameter validation checks
       if (!parameters.containsKey(PARAM_MEETING_ID))
       {
           XMLUtils.appendFailureNode(results, "You must provide the meeting id in order to set the moderator.");
           return;
       }

       if (!parameters.containsKey(PARAM_USER_ID))
       {
           XMLUtils.appendFailureNode(results, "You must provide the user id in order to set the moderator.");
           return;
       }

       // extract parameters
       String meetingIdString = (String)parameters.get(PARAM_MEETING_ID);
       String userIdString    = (String)parameters.get(PARAM_USER_ID);

       // create a new meeting based on provided params
       long meetingId;
       try {
          meetingId = Long.parseLong(meetingIdString);
       } catch (NumberFormatException nfe) {
          XMLUtils.appendFailureNode(results, "Invalid format for parameter " + PARAM_MEETING_ID + "(" + meetingIdString + "). Must be a numerical value");
          return;
       }
       long userId;
       try {
          userId = Long.parseLong(userIdString);
       }
       catch (NumberFormatException nfe) {
           XMLUtils.appendFailureNode(results, "Invalid format for parameter " + PARAM_USER_ID + "(" + userIdString + "). Must be a numerical value");
           return;
       }

       // get the meeting and user details to ensure they exist
       // this is not needed since all the users are authenticated in the sakai database and do not exist on the elluminate server
/*     User user = ELMServer.getUser(userId);
       if (user == null) {
           // No user found, so terminate with error
           XMLUtils.appendFailureNode(results, userIdString + " not found in user datastore.");
           return;
       }
 */
       Meeting meeting = ELMServer.getMeeting(meetingId);
       if (meeting == null) {
           XMLUtils.appendFailureNode(results, "Meeting does not exsist: " + meetingIdString);
           return;
       }

       // set the facilitator for this meeting and update it
       meeting.setFacilitatorId(userId);
       if (meeting instanceof Meeting) {
          try {
             ELMServer.updateMeeting(meeting);
          } catch (InvalidServiceCallException x) {
             XMLUtils.appendFailureNode(results, "Internal Error: " + x.getMessage());
             return;
          }
       }
       else
       {
          try {
             ELMServer.updateMeetNow((MeetNow)meeting);
          } catch (InvalidServiceCallException x) {
             XMLUtils.appendFailureNode(results, "Internal Error: " + x.getMessage());
             return;
          }
       }

       // build a result xml document to return to caller
       Element root = results.createElement("results");
       XMLUtils.appendMeetingNode(root, results, meeting, dateFormat);
       results.appendChild(root);
    }

    /**
     * deletes a recording.
     */
    public void deleteRecording(Map parameters, Document results) {
       logger.info("deleteRecording()");

       // parameter validation check
       if (!parameters.containsKey(PARAM_RECORDING_ID))
       {
           XMLUtils.appendFailureNode(results, "You must provide the recording id in order to delete a recording.");
           return;
       }

       // get the successfully deleted recording
       String recordingId = (String)parameters.get(PARAM_RECORDING_ID);

       Recording recording;
       try {recording = ELMServer.deleteRecordings(recordingId );}
       catch (InvalidServiceCallException x)
       {
           XMLUtils.appendFailureNode(results, "Internal Error: " + x.getMessage());
           return;
       }

       if (recording != null)
       {
           // build a result document to return to caller
           Element root = results.createElement("results");
           XMLUtils.appendRecordingNode(root, results, recording, dateFormat);
           results.appendChild(root);
       }
       else
       {
           XMLUtils.appendFailureNode(results, "Unable to find the recording specified : " + recordingId);
       }
    }

    /**
     * Gets the meeting detail for the given meeting id.
     * <br/><br/>
     * @param parameters A map of parameters.
     * @param results    The document to append resulting data into.
     */
    public void getMeeting(Map parameters, Document results) {
       logger.info("getMeeting()");

        // do parameter validation check
        if( !parameters.containsKey(PARAM_MEETING_ID)) {
            XMLUtils.appendFailureNode( results, "You must provide a Meeting ID in order to retrieve a meeting!" );
            return;
        }

        String meetingId = (String)parameters.get(PARAM_MEETING_ID);

        // create a new meeting based on provided params
        long id;
        try {
           id = Long.parseLong(meetingId);
        } catch (NumberFormatException nfe) {
            XMLUtils.appendFailureNode(results, "Invalid format for parameter " + PARAM_MEETING_ID + "(" + meetingId + "). Must be a numerical value");
            return;
        }

        Meeting meeting = ELMServer.getMeeting(id);
        if (meeting == null) {
            XMLUtils.appendFailureNode(results, "Meeting does not exsist: " +  meetingId);
            return;
        }

        // build a result xml document to return to caller
        Element root = results.createElement("results");
        XMLUtils.appendMeetingNode(root, results, meeting, dateFormat);
        results.appendChild(root);
    }

    /**
     * Get the userId, for a give userName. Returning the user ID back to the caller if successful, otherwise a failure node
     * is returned.  The following parameters are expected:
     *
     *     userName : The userName of the user.
     *
     * @param parameters A map of parameters.
     * @param results The document to append resulting data into.
     */
    public void getUserId(Map parameters, Document results) {
        logger.info("getUserId()");

        // do a parameter validation check
        if (!parameters.containsKey(PARAM_USER_NAME)) {
            XMLUtils.appendFailureNode(results, "Get User ID Requires a User Name");
            return;
        }

        // extract parameters from url
        String userName = (String)parameters.get(PARAM_USER_NAME);
        User user = ELMServer.getUser(userName);

        if (user != null) {
            // build a result document to return to caller
            Element root = results.createElement("results");

            Element userIdNode = results.createElement("userId");
            userIdNode.appendChild(results.createCDATASection(user.getId() + ""));
            root.appendChild(userIdNode);
            results.appendChild(root);
        } else {
            XMLUtils.appendFailureNode(results, userName + " not found in user datastore.");
        }
        return;
    }

    /**
     * Creates a user, returning the user details back to the caller if successful, otherwise a failure node
     * is returned.  The following parameters are expected:
     *
     *     userID : The userId of the user.
     *     firstName : The First Name of the user.
     *     lastName : The Last Name date for this meeting.
     *
     * @param parameters A map of parameters.
     * @param results The document to append resulting data into.
     */
    public void createUser(Map parameters, Document results) {
        logger.info("createUser()");

        // do a parameter validation check
        if (!parameters.containsKey(PARAM_USER_NAME) || !parameters.containsKey(PARAM_FIRST_NAME) || !parameters.containsKey(PARAM_LAST_NAME)) {
            XMLUtils.appendFailureNode(results, "Create User Requires a User Name, First Name, Last Name");
            return;
        }

        // extract parameters from url
        String userName  = (String)parameters.get(PARAM_USER_NAME);
        String firstName = (String)parameters.get(PARAM_FIRST_NAME);
        String lastName  = (String)parameters.get(PARAM_LAST_NAME);

        // Check to make sure that the userName is not blank or filled with enpty strings.
        if (userName.trim().length() == 0) {
            XMLUtils.appendFailureNode(results, "Username can not be blank. Note: All leading and trailing spaces are trimmed.");
            return;
        }

        //Check to see if user already exsists
        User user = ELMServer.getUser(userName);
        if (user != null) {
            XMLUtils.appendFailureNode(results, "User: " + userName + " already exists in the database.");
            return;
        }

        // create a new user based on provided params
        user = new User(0, false, userName,userName, User.FACILITATOR_ROLE , firstName, lastName);
        ELMServer.createUser(user);

        // build a result xml document to return to caller
        Element root = results.createElement("results");

        Element userIdNode = results.createElement("userId");
        userIdNode.appendChild(results.createCDATASection(user.getId() + ""));
        root.appendChild(userIdNode);
        results.appendChild(root);
        return;
    }

    /**
     * Sets up the login page for the meeting.
     *
     *      This routine can only be called from the web page, so no need to return XML.
     *
     * @param call A map of parameters.
     */
    public void meetingLogin(ServiceCall call) {
        logger.info("meetingLogin()");

        HttpServletRequest request = call.getRequest();
        Context context = call.getContext();
        Map parameters = call.getParameters();
        CommonUtils.addErrorMessage(context, "Can not join a Generic meeting outside of the Generic LMS");

        request.getSession().removeAttribute("meetingView");
        CalendarManager.getInstance().provideCalendarMonth(request, context);
        Date [] dates = processDateRange(request, context);
        if (UserManager.getInstance().getCurrentUsersProfile(request) == null)
        {
            // We are not logged in, so redirect to ELM Proper
            MeetingManager.getInstance().provideMeetingViewList(request, context, dates[ 0 ], dates[ 1 ], false, true);
            CommonUtils.pageRedirect(context, parameters, "today.html");
        } else {
            //We are logged in, so redirect to listmeeting page

            UserProfile userProfile = UserManager.getInstance().getCurrentUsersProfile(request);
            if (userProfile.getUser().isFacilitator()) {
                MeetingManager.getInstance().provideMeetingViewListForCurrentFacilitator(request, context, dates[ 0 ], dates[ 1 ]);
            } else {
                MeetingManager.getInstance().provideMeetingViewList(request, context, dates[ 0 ], dates[ 1 ], true, true);
            }
            CommonUtils.pageRedirect(context, parameters, "manage_meetings.html");
        }
    }

    /**
     * Sets up the recording page for the meeting.
     *
     *      This routine can only be called from the web page, so no need to return XML.
     *
     * @param call A map of parameters.
     */
    public void recordingLogin(ServiceCall call) {
        logger.info("recordingLogin()");

        HttpServletRequest request = call.getRequest();
        Context context = call.getContext();
        Map parameters = call.getParameters();
        CommonUtils.addErrorMessage(context, "Can not playback a Generic recording outside of the Generic LMS");

        request.getSession().setAttribute("manage_recordings", new Boolean(false));
        CalendarManager.getInstance().provideCalendarMonth(request, context);
        Date [] dates = processDateRange(request, context);
        List recordingsList = ELMServiceProvider.getInstance().listRecordings(dates[ 0 ], dates[ 1 ]);
        if (UserManager.getInstance().getCurrentUsersProfile(request) == null)
        {
            // We are not logged in, so redirect to ELM Proper
            // toss the manage_recordings flag into session so recording_login.html knows where to go
            // get the recordings list

            // remove the password protected (closed) meetings, unless access has been overridden
            Iterator recordingIterator = recordingsList.iterator();
            while(recordingIterator.hasNext())
            {
                Recording recording = (Recording)recordingIterator.next();
                if (!recording.isDisplayable())
                {
                    recordingIterator.remove();
                }
            }

            // sort the collection
            String sortColumn = request.getParameter("sort_column");
            if (sortColumn == null)
            {
                sortColumn = "date";
            }

            Collections.sort(recordingsList, determineRecordingComparator(request.getSession(), sortColumn, request.getParameter("change_direction")));

            String sortString = (String)request.getSession().getAttribute("sort_order");
            context.put("recordingSortOrder", new SortOrder(sortColumn, sortString.substring(sortString.indexOf("/") + 1, sortString.length())));
            context.put("commonUtils", new CommonUtils());
            context.put("recordingsList", recordingsList);
            CommonUtils.pageRedirect(context, parameters, "recording.html");
        } else {
            //We are logged in, so redirect to managerecordings page

            // toss the manage_recordings flag into session so recording_login.html knows where to go
            request.getSession().setAttribute("manage_recordings", new Boolean(true));

            // add a reference to the private encryption strategy so we can make id's URL friendly
            context.put("commonUtils", new CommonUtils());

            List recordings = new ArrayList();

            // remove those recordings which are not for this facilitator
            User user = UserManager.getInstance().getCurrentUsersProfile(request).getUser();
            if (user.isFacilitator())
            {
                Iterator recordingIterator = recordingsList.iterator();
                while(recordingIterator.hasNext())
                {
                    Recording recording = (Recording)recordingIterator.next();
                    if (user.getLoginName().equals(recording.getFacilitator()))
                    {
                        recordings.add(recording);
                    }
                }
            }
            else
            {
                recordings = recordingsList;
            }

            // sort the collection
            String sortColumn = request.getParameter("sort_column");
            if (sortColumn == null)
            {
                sortColumn = "date";
            }

            Collections.sort(recordings, determineRecordingComparator(request.getSession(), sortColumn, request.getParameter("change_direction")));

            // put this recordings list within the context of this page
            String sortString = (String)request.getSession().getAttribute("sort_order");
            context.put("recordingSortOrder", new SortOrder(sortColumn, sortString.substring(sortString.indexOf("/") + 1, sortString.length())));
            context.put("recordingsList", recordings);
            CommonUtils.pageRedirect(context, parameters, "manage_recordings.html");
        }
    }

    /**
     * returns the configuration name used to identify this adapter.
     * It must be the same name specified in the <name> tag for the <adapter> in the <elluminate_home>/manager/tomcat/webapps/ROOT/WEB-INF/resources/configuration.xml file.
     */
    public String getAdapterName()
    {
        return adapterName;
    }

    /**
     * returns the configuration template name used to install this adapter on the elluminate server.
     * The template name must be the same name specified in the <template> tag for the <adapter> in the <elluminate_home>/manager/tomcat/webapps/ROOT/WEB-INF/resources/configuration.xml file.
     * A corresponding .tmpl file with the given name must exist in the <elluminate_home>/server6_5/sessions/ directory.
     */
    public String getTemplateName()
    {
        return templateName;
    }

    /**
     * helper method.
     */
    private Date[] processDateRange(HttpServletRequest request, Context context) {
        logger.info("processDateRange()");

        Date startDate = null, endDate = null;

        try
        {
            String dateString = request.getParameter(ParamNames.getStartDate());
            if (dateString == null)
            {
                dateString = CommonUtils.getWebDateFormat().format(new Date());
            }

            startDate = CommonUtils.getWebDateFormat().parse(dateString);
        }
        catch(ParseException x)
        {
            startDate = new Date();
        }

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);
        startCalendar.set(Calendar.HOUR, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);
        startDate = startCalendar.getTime();
        context.put("startDate", new CalendarDay(startDate.getTime()));

        try
        {
            String dateString = request.getParameter(ParamNames.getEndDate());
            if (dateString == null)
            {
                dateString = CommonUtils.getWebDateFormat().format(new Date());
            }

            endDate = CommonUtils.getWebDateFormat().parse(dateString);
        }
        catch(ParseException x)
        {
            endDate = new Date();
        }

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endDate);
        endCalendar.set(Calendar.HOUR, 23);
        endCalendar.set(Calendar.MINUTE, 59);
        endCalendar.set(Calendar.SECOND, 59);
        endDate = endCalendar.getTime();
        context.put("endDate", new CalendarDay(endDate.getTime()));

        return new Date[] { startDate, endDate };
    }

    /**
     * determines if a name is a valid elluminate meetting name.
     */
    private boolean isValidMeetingName(String txt) {
        logger.info("isValidMeetingName()");

        boolean retFlag = false;
        int n = INVALIDCHARS.length();
        int i = 0;
        while (!retFlag && i < n)
        {
            retFlag = (txt.indexOf(INVALIDCHARS.substring(i,i+1)) > 0);
            i++;
        }
        return !retFlag;
    }

    /**
     * helper method
     */
    private Comparator determineRecordingComparator(HttpSession session, String sort, String changeDirection)
    {
        if (sort.equals("date"))
        {
            return new RecordingCreationDateComparator(CommonUtils.buildOrdering(session, "date", changeDirection));
        }
        else
        if (sort.equals("facilitator"))
        {
            return new RecordingFacilitatorComparator(CommonUtils.buildOrdering(session, "facilitator", changeDirection));
        }
        else
        if (sort.equals("meeting"))
        {
            return new RecordingMeetingNameComparator(CommonUtils.buildOrdering(session, "meeting", changeDirection));
        }
        else
        {
            return new RecordingSizeComparator(CommonUtils.buildOrdering(session, "size", changeDirection));
        }
    }
}
