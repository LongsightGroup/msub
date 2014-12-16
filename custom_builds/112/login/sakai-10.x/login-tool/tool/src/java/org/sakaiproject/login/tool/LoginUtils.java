package org.sakaiproject.login.tool;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.portable.ValueOutputStream;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserLockedException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;
import org.sakaiproject.user.cover.UserDirectoryService;

/*****************************************************************************
 * Functionality: Force a user to change its password if it has expired
 * Cooperates with: User Tool (For updating password change date)
 * Author: Lex de Ruijter
 * Company: Samoo
 *****************************************************************************/
public class LoginUtils {

       /** Our log (commons). */
       private static Log M_log = LogFactory.getLog(LoginUtils.class);

       private static LoginUtils instance = null;

       public static LoginUtils getInstance(){
               if(instance == null){
                       instance = new LoginUtils();
               }

               return instance;
       }

       public void init()
       {
               getInstance();
       }

       private static String PROPERTY_PW_MODIFIED_DATE = "password.modified.date";

       private PasswordRequirements passwordRequirements = PasswordRequirements.getInstance();

       /**
        * Get a user by eid
        * @param eid
        * @return
        */
       @SuppressWarnings("deprecation")
       private User getUserByEid(String eid) {
               User user;
               try {
                       user = UserDirectoryService.getUserByEid(eid);
               } catch (UserNotDefinedException e) {
                       user = null;
               }
               return user;
       }

       /**
        * Get an Edit user by user id
        * @param id
        * @return
        */
       @SuppressWarnings("deprecation")
       private UserEdit getEditUser(String id) {
               UserEdit edit = null;
               try {
                       edit = UserDirectoryService.editUser(id);
               } catch (UserNotDefinedException e) {
                       e.printStackTrace();
               } catch (UserPermissionException e) {
                       e.printStackTrace();
               } catch (UserLockedException e) {
                       e.printStackTrace();
               }
               return edit;
       }

       /**
        * Commit the changes made to a UserEdit object
        * @param userEdit The useredit to commit
        */
       @SuppressWarnings("deprecation")
       private void commitUserEdit(UserEdit userEdit) {

               try {
                       UserDirectoryService.commitEdit(userEdit);
               } catch (UserAlreadyDefinedException e) {
                       e.printStackTrace();
               }

       }

       /**
        * Set a new password of a user.
        * @param eid eid of the user
        * @param pw new password
        */
       public void setNewPasswordUser(String eid, String pw) {
               User user = getUserByEid(eid);
               elevatePermissions();

               UserEdit edit = getEditUser(user.getId());

               if (edit != null) {
                       edit.setPassword(pw);
                       updatePasswordModifiedDate(edit);
                       commitUserEdit(edit);
               }
               restorePermissions();
       }


       /**
        * Update the password modified date.
        * @param user User which changed its password
        */
       private void updatePasswordModifiedDate(User user) {

               String propertyValue = "" + (new Date().getTime());
               user.getProperties().addProperty(PROPERTY_PW_MODIFIED_DATE,
                               propertyValue);

       }

       /**
        * Temporarily Elevate the permissions so we can execute superUser tasks
        */
       @SuppressWarnings("deprecation")
       private void elevatePermissions() {
               Session session = SessionManager.getCurrentSession();
               session.setUserId("admin");
       }

       /**
        * Restore the elevated permissions.
        */
       @SuppressWarnings("deprecation")
       private void restorePermissions() {
               Session session = SessionManager.getCurrentSession();
               session.setUserId("");

       }

       /**
        * Check if a user must change its password.
        * 
        * @param eid
        *            The external ID of a user
        * @return True if the user must change its password, false if not.
        */
       public boolean userMustChangePassword(String eid) {
               boolean mustChangePassword = false;
               User user = getUserByEid(eid);

               if (user != null) {

                       if(SecurityService.isSuperUser(user.getId())) return false;

                       String resetAfterDaysString = ServerConfigurationService
                                       .getString("password.reset.after.days");

                       int resetAfterDays;
                       if (resetAfterDaysString != null
                                       && !resetAfterDaysString.equals("")) {
                               try {
                                       resetAfterDays = Integer.parseInt(resetAfterDaysString
                                                       .trim());
                               } catch (NumberFormatException nfe) {
                                       resetAfterDays = -1;
                                       M_log.warn("The 'password.reset.after.days' value in sakai.properties is no number.");
                               }
                       } else {
                               M_log.warn("The 'password.reset.after.days' property in sakai.properties is NOT set.");
                               resetAfterDays = -1;
                       }
                       if (resetAfterDays >= 0) {
                               String property = (String) user.getProperties().get(
                                               PROPERTY_PW_MODIFIED_DATE);
                               long lastPassChange;

                               if (property != null && !"".equals(property)) {
                                       try {
                                               lastPassChange = Long.parseLong(property.trim());

                                       } catch (NumberFormatException nfe) {
                                               lastPassChange = 0;
                                       }

                                       if (getDifferenceTimeInDays(lastPassChange,
                                                       new Date().getTime()) >= resetAfterDays) {
                                               mustChangePassword = true;
                                       }
                               } else {
                                       mustChangePassword = true;

                                       //Uncomment to not force newly registered users to change their password,
                                       //but instead let their password expire count from NOW on.
                                       // elevatePermissions();
                                       // updatePasswordModifiedDate(user);
                                       // restorePermissions();
                                       // mustChangePassword = false;
                               }
                       }
               }

               return mustChangePassword;
       }

       /**
        * Get the difference of time in days between the first and the second
        * dateTime
        * 
        * @param firstDate
        *            The first date, should be the date which is the most back in
        *            time.
        * @param secondDate
        *            The second date, should be the date which is most current.
        * @return amount of days between the first date and the second date.
        */
       public int getDifferenceTimeInDays(long firstDate, long secondDate) {
               long differenceInDays = (firstDate - secondDate)
                               / (24 * 60 * 60 * 1000);
               if (differenceInDays < 0)
                       differenceInDays = differenceInDays * -1;

               return Integer.parseInt(Long.toString(differenceInDays));
       }

       public boolean passwordMeetsRequirements(String password) {
               return passwordRequirements.passwordMeetsRequirements(password);
       }

       public int getPasswordMinimumLength() {
               return passwordRequirements.getPasswordMinimumLength();
       }

       public int getPasswordMinimumAmountOfUpperCaseLetters() {
               return passwordRequirements
                               .getPasswordMinimumAmountOfUpperCaseLetters();
       }

       public int getPasswordMinimumAmountOfLowerCaseLetters() {
               return passwordRequirements
                               .getPasswordMinimumAmountOfLowerCaseLetters();
       }

       public int getPasswordMinimumAmountOfNumbers() {
               return passwordRequirements.getPasswordMinimumAmountOfNumbers();
       }


}
