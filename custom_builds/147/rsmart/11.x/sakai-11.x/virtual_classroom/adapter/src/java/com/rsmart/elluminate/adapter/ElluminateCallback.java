/**********************************************************************************
 * Copyright (c) 2006 the r-smart group, inc.
 **********************************************************************************/
package com.rsmart.elluminate.adapter;

import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

import com.elluminate.vclassmanager.dataaccess.MeetNow;
import com.elluminate.vclassmanager.dataaccess.Meeting;
import com.elluminate.vclassmanager.dataaccess.SystemPreferencesData;
import com.elluminate.vclassmanager.dataaccess.User;
import com.elluminate.vclassmanager.service.ELMBaseCallback;
import com.elluminate.vclassmanager.service.ELMServiceAdapter;
import com.elluminate.vclassmanager.service.ELMServiceProvider;
import com.elluminate.vclassmanager.service.UnparseableMeetingException;
import com.elluminate.vclassmanager.util.CommonUtils;



/**
 * This class provides authentication and authorization services for the elluminate server.
 * Whenever a user tries to access the elluminate server, the elluminate server will invoke methods in this class to authenticate
 * a user and to verify that they have appropriate permissions to execute the corresponding function on the elluminate server.
 */
public class ElluminateCallback extends ELMBaseCallback {
   // logger
// private static Logger logger = Logger.getLogger(ElluminateCallback.class);
   private Logger            logger;
   private ElluminateAdapter adapter;



   /**
    * default constructor.
    */
   public ElluminateCallback() {
      // no code necessary
      logger = Logger.getLogger(getClass());
   }

   public void initialize (ELMServiceAdapter associatedAdapter) {
      adapter = (ElluminateAdapter)associatedAdapter;
   }

   /**
    * Here we want to confirm if a specified room id w/specified password are valid in
    * the ELM context.  This method is invoked whenever the vClass server makes a callback
    * to ELM.
    *
    * Expected string is in the form: "roomgroup|roomname#roomid".
    *
    * @param parameters The parameter map containing parameters and used to store result.
    * @param output The stream to output our results too
    *
    * challenge: a prefix used to append to the user's password to prior to calculation of an MD5 digest.
    * response: The MD5 digest as calculated by server for this user's password.
    * name: loginName of the user that is requesting access to the meeting
    * conf: The meeting name and meeting id in the form "TemplateName"|"MeetingName"#"MeetingID"
    */
   protected void confirmAccess(Map parameters, PrintWriter output) {
      logger.info("confirmAccess()");

      try
      {
         // get the challenge/response and using the password ensure this is who they say they are :)
         String  challenge = ((String[])parameters.get("challenge"))[0];
         String  response  = ((String[])parameters.get("response" ))[0];
         String  user      = ((String[])parameters.get("name"     ))[0];
         Meeting meeting   = parseMeeting(parameters);
         long    startTime = meeting.getStartDateTime() - (SystemPreferencesData.getInstance().getSystemPreferences().getThresholdValue() * 60 * 1000);
         long    endTime   = meeting.getEndDateTime();
         long    curTime   = new Date().getTime();

         logger.info("confirmAccess(), user: " + user + ", challenge: " + challenge + ", response: " + response);

         if ((curTime >= startTime) && (curTime <= endTime)) {
            // check if this user is a moderator or not, and if so, ensure the moderator password is good!
            logger.info("confirmAccess(), meetingFacilitatorName: " + meeting.getFacilitatorName());
            if (user != null && user.equals(meeting.getFacilitatorName())) {
               logger.info("confirmAccess(), facilitator user");
               if (meeting instanceof MeetNow) {
                  // if this is a meet now meeting then the password is stored in the meeting itself
                  MeetNow meetnow = (MeetNow)meeting;
                  if (CommonUtils.isValid(challenge, response, meetnow.getFacilitatorPassword())) {
                     writeResult(output, "y");
                  } else {
                     writeResult(output, "n");
                  }
               } else {
                  // a regular meeting will use a defined user to authenticate against
//                User elmUser = UserData.getInstance().getUserByLoginName(user);
                  User elmUser = adapter.getProvider().getUser(user);
                  if (CommonUtils.isValid(challenge, response, elmUser.getPassword())) {
                     writeResult(output, "y");
                  } else {
                     writeResult(output, "n");
                  }
               }
            // user is just a regular user
            } else {
               logger.info("confirmAccess(), regular user");

               String password = meeting.getPassword();
               if (password == null || password.equals("") || CommonUtils.isValid(challenge, response, password)) {
                  writeResult(output, "y");
               } else {
                  writeResult(output, "n");
               }
            }
         } else {
            // Meeting is not in session.
            logger.info("confirmAccess(), meeting is not in session");
            writeResult(output, "n");
         }
      } catch(UnparseableMeetingException x) {
         logger.error("confirmAccess(), meeting parameter was not parseable", x);
         writeResult(output, "n");
      }
   }

   /**
    * Given a room id, return the moderator id for that room.
    * <br/><br/>
    * @param parameters The parameter map containing parameters and used to store result.
    * @param output The stream to output our results too
    */
   protected void findModeratorForRoom(Map parameters, PrintWriter output) {
      logger.info("findModeratorForRoom()");

      try {
         Meeting meeting = parseMeeting(parameters);
         writeResult(output, meeting.getFacilitatorName());
         logger.info("moderator: " + meeting.getFacilitatorName());
      } catch(UnparseableMeetingException x) {
         logger.error(x);
         writeResult(output, "?");
      }
   }

   /**
    * Determines if the specified user is a moderator for the given meeting.
    *
    * @param parameters The parameter map containing parameters and used to store result.
    * @param output The stream to output our results too
    */
   protected void isModeratorForRoom(Map parameters, PrintWriter output) {
      logger.info("isModeratorForRoom()");

      try
      {
         // parse out the room id from the roomId string (currently in the ?|?#? form), or fail if no # found
         String roomId = ((String[ ])parameters.get("conf"))[0];
         if (roomId.indexOf(ELMServiceProvider.MEETING_ROOM_ID_DELIMITER) < 0) {
             writeResult(output, "n");
             return;
         }

         // get the requesting user name and meeting
         String user = ((String[ ])parameters.get("name"))[0];
         Meeting meeting = parseMeeting(parameters);

         // validate that the user is the facilitator
         if (user != null && user.equals(meeting.getFacilitatorName())) {
            logger.info("moderator: " + user);
            writeResult(output, "y");
         } else {
            logger.info("participant: " + user);
            writeResult(output, "n");
         }
      } catch(Exception x) {
          logger.error(x);
          writeResult(output, "n");
      }
   }

   /**
    *
    */
   private Meeting parseMeeting(Map parameters) throws UnparseableMeetingException {
      logger.info("parseMeeting()");

      // parse out the room id from the roomId string (currently in the ?|?#? form), or fail if no # found
      String roomId = ((String[ ])parameters.get("conf"))[0];
      if (roomId.indexOf("#") < 0) {
          throw new UnparseableMeetingException("Meeting string does not contain a '#' and cannot be parsed!");
      }

      // using the room id get the room
      roomId = roomId.substring(roomId.lastIndexOf("#") + 1, roomId.length());
      Meeting result = adapter.getProvider().getMeeting(Long.parseLong(roomId));
//      Meeting result =  MeetingManager.getInstance().getMeeting(Long.parseLong(roomId));

      if (result == null) {
          throw new UnparseableMeetingException("Meeting was not found in the local file store!");
      }
      return result;
   }
}
