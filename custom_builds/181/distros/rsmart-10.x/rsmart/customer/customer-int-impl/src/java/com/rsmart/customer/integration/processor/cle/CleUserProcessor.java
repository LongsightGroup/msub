/*
 * Copyright 2008 The rSmart Group
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Contributor(s): jbush
 */

package com.rsmart.customer.integration.processor.cle;

import com.rsmart.customer.integration.model.CleUser;
import com.rsmart.customer.integration.processor.BaseCsvFileProcessor;
import com.rsmart.customer.integration.processor.ProcessorState;
import com.rsmart.userdataservice.UserDataService;
import com.rsmart.userdataservice.persistence.model.RsnUser;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.model.RenderedTemplate;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;
import org.sakaiproject.user.api.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * CLE User Processor
 * 
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class CleUserProcessor extends BaseCsvFileProcessor {

	/** UDS */
	private UserDirectoryService uds;

	/** Update Allowed Flag */
	private boolean updateAllowed = true;
	private boolean updatePassword = false;
	private static String NOTIFY_NEW_USER ="sitemanage.notifyNewUserEmail";
    private EmailTemplateService emailTemplateService;
	private EmailService emailService;
    private boolean emailNotification = true;
    private boolean generatePassword = false;
    private UserDataService userDataService;
    private boolean externallyProvided = false;

   /**
	 * Get Title
	 * 
	 * @return String
	 */
	public String getProcessorTitle() {
		return "SIS User Processor";
	}

	/**
	 * Process Row
	 * 
	 * @param data
	 */
	public void processRow(String[] data, ProcessorState state) throws Exception {
		CleUser user = new CleUser();
		user.setUserName(data[0]);
		user.setLastName(data[1]);
		user.setFirstName(data[2]);
		user.setEmailAddress(data[3]);
		user.setPassword(data[4]);
		user.setUserType(data[5]);
		user.setUserId(data[6]);

        if (data.length > 7) {
            user.setProperty1(data[7]);
        }
        if (data.length > 8) {
            user.setProperty2(data[8]);
        }
        if (data.length > 9) {
            user.setProperty3(data[9]);
        }
        if (data.length > 10) {
            user.setProperty4(data[10]);
        }
        if (data.length > 11) {
            user.setProperty5(data[11]);
        }
		processCleUser(user, state);
	}

	/**
	 * Process Sis User
	 * 
	 * @param user
	 * @throws Exception
	 */
	private void processCleUser(CleUser user, ProcessorState state) throws Exception {
		// User
		User u = null;

      // if we have a user id assume we are looking user up by this id
      if (user.getUserId() != null && user.getUserId().length() > 0) {
         // Lookup by user id
         try {
            u = uds.getUser(user.getUserId());
         }
         catch(UserNotDefinedException ex) {
         }
      } else {
         // Lookup by EID
         try {
            u = uds.getUserByEid(user.getUserName());
         }
         catch(UserNotDefinedException ex) {
         }
      }

      // Add new if not found, otherwise update user
		if( u == null && !externallyProvided) {
            if (user.getUserId() != null && user.getUserId().length() == 0) {
                user.setUserId(null);
            }
            if (generatePassword) {
                user.setPassword(generatePassword());
            }
            User newUser = uds.addUser(user.getUserId(), user.getUserName(), user.getFirstName(), user.getLastName(), user.getEmailAddress(), user.getPassword(), user.getUserType(), null);
            updateExtraProperties(user, newUser);

            notifyNewUserEmail(newUser, user.getPassword());
			state.incrementInsertCnt();
		}
        else if (externallyProvided){
            RsnUser rsnUser = new RsnUser();
            rsnUser.setFirstName(user.getFirstName());
            rsnUser.setLastName(user.getLastName());
            rsnUser.setEid(u.getEid());
            rsnUser.setEmail(user.getEmailAddress());
            rsnUser.setType(user.getUserType());
            rsnUser.setCreatedBy("admin");
            rsnUser.setModifiedBy("admin");
            rsnUser.setCreatedOn(getStartTimestamp());
            rsnUser.setModifiedOn(getStartTimestamp());
            rsnUser.setPw("");
            rsnUser.setEmailLc(user.getEmailAddress());
            rsnUser = updateExtraPropertiesWithEdit(user, rsnUser);
            userDataService.saveRsnUser(rsnUser);

            state.incrementIgnoreCnt();
        }
		else if( updateAllowed ) {
			UserEdit ue = uds.editUser(u.getId());
			
			ue.setFirstName(user.getFirstName());
			ue.setLastName(user.getLastName());
			ue.setEid(user.getUserName());
			ue.setEmail(user.getEmailAddress());
			ue.setType(user.getUserType());
            updateExtraPropertiesWithEdit(user, ue);


             if (isUpdatePassword()) {
                ue.setPassword(user.getPassword());
             }

         // Commit Edit
			uds.commitEdit(ue);
			
			state.incrementUpdateCnt();
		}
		else {
			state.incrementIgnoreCnt();
		}
	}


    private final String getStartTimestamp()
       {
           final Date
               now = new Date();
           final SimpleDateFormat
               dateFormat = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");

           return dateFormat.format(now);
       }


    protected String generatePassword() {
        // set password to a positive random number
        Random generator = new Random(System
                .currentTimeMillis());
        Integer num = Integer.valueOf(generator
                .nextInt(Integer.MAX_VALUE));
        if (num.intValue() < 0)
            num = Integer.valueOf(num.intValue() * -1);
        return num.toString();
    }


    /**
     * returns true if the user is provided or does not exist
     * Since there is no way to test if a user is created, we must determine it by attempting to edit,
     * this method assumes the user had been found already, do not call this without first looking up the user
     * @param userId
     * @return
     * @throws Exception
     */
    protected boolean isProvided(String userId) throws Exception{
        UserEdit ue = null;
        try {
            ue = uds.editUser(userId);
        } catch (UserNotDefinedException e){
            return true;
        } catch (UserLockedException e) {
            return false;
        } finally {
            if (ue != null) uds.cancelEdit(ue);
        }


        return false;
    }





    /**
     * Updates the Sakai UserEdit with properties in the CleUser.  This method does not
     * commit the changes
     * @param user
     * @param
     * @throws UserNotDefinedException
     * @throws UserPermissionException
     * @throws UserLockedException
     * @throws UserAlreadyDefinedException
     */
    protected RsnUser updateExtraPropertiesWithEdit(CleUser user, RsnUser rsnUser) throws UserNotDefinedException, UserPermissionException, UserLockedException, UserAlreadyDefinedException {
        String[] propertyNames = ServerConfigurationService.getStrings("user.sis.property");
        Map values = new HashMap();
        if (propertyNames != null && propertyNames.length > 0) {

            if (user.getProperty1() != null) {
                values.put(propertyNames[0], user.getProperty1());
                rsnUser.setProperties(values);
            }

            if (propertyNames.length > 1) {
                if (user.getProperty2() != null) {
                    values.put(propertyNames[1], user.getProperty2());
                    rsnUser.setProperties(values);
                }
            }

            if (propertyNames.length > 2) {
                if (user.getProperty3() != null) {
                    values.put(propertyNames[2], user.getProperty3());
                    rsnUser.setProperties(values);
                }
            }

            if (propertyNames.length > 3) {
                if (user.getProperty4() != null) {
                    values.put(propertyNames[3], user.getProperty4());
                    rsnUser.setProperties(values);
                }
            }

            if (propertyNames.length > 4) {
                if (user.getProperty5() != null) {
                    values.put(propertyNames[4], user.getProperty5());
                    rsnUser.setProperties(values);
                }
            }
        }
        return rsnUser;

    }



    /**
     * Updates the Sakai UserEdit with properties in the CleUser.  This method does not
     * commit the changes
     * @param user
     * @param ue
     * @throws UserNotDefinedException
     * @throws UserPermissionException
     * @throws UserLockedException
     * @throws UserAlreadyDefinedException
     */
    protected void updateExtraPropertiesWithEdit(CleUser user, UserEdit ue) throws UserNotDefinedException, UserPermissionException, UserLockedException, UserAlreadyDefinedException {
        String[] propertyNames = ServerConfigurationService.getStrings("user.sis.property");
        if (propertyNames != null && propertyNames.length > 0) {

            if (user.getProperty1() != null) {
                ue.getProperties().addProperty(propertyNames[0], user.getProperty1());
            }

            if (propertyNames.length > 1) {
                if (user.getProperty2() != null) {
                    ue.getProperties().addProperty(propertyNames[1], user.getProperty2());
                }
            }
            if (propertyNames.length > 2) {
                if (user.getProperty3() != null) {
                    ue.getProperties().addProperty(propertyNames[2], user.getProperty3());
                }
            }
            if (propertyNames.length > 3) {
                if (user.getProperty4() != null) {
                    ue.getProperties().addProperty(propertyNames[3], user.getProperty4());
                }
            }
            if (propertyNames.length > 4) {
                if (user.getProperty5() != null) {
                    ue.getProperties().addProperty(propertyNames[4], user.getProperty5());
                }
            }
        } 
    }

    /**
     * Updates the Sakai user with properties in the CleUser
     * @param cleUser
     * @param user
     * @throws UserNotDefinedException
     * @throws UserPermissionException
     * @throws UserLockedException
     * @throws UserAlreadyDefinedException
     */
    protected void updateExtraProperties(CleUser cleUser, User user) throws UserNotDefinedException, UserPermissionException, UserLockedException, UserAlreadyDefinedException {
        UserEdit ue = uds.editUser(user.getId());
        updateExtraPropertiesWithEdit(cleUser, ue);
        uds.commitEdit(ue);
    }

    private String getSetupRequestEmailAddress() {
      String from = ServerConfigurationService.getString("setup.request",
            null);
      if (from == null) {
         from = "postmaster@".concat(ServerConfigurationService.getServerName());
      }
      return from;
   }

   public void notifyNewUserEmail(User user, String newUserPassword) {
      if (!isEmailNotification()) return;

      String from = getSetupRequestEmailAddress();
      String productionSiteName = ServerConfigurationService.getString(
            "ui.service", "");

      String newUserEmail = user.getEmail();
      String to = newUserEmail;
      String headerTo = newUserEmail;
      String replyTo = newUserEmail;


      String content = "";


      if (from != null && newUserEmail != null) {
         /*
             * $userName
             * $localSakaiName
             * $currentUserName
             * $localSakaiUrl
             */
         Map<String, String> replacementValues = new HashMap<String, String>();
         replacementValues.put("userName", user.getDisplayName());
         replacementValues.put("userEid", user.getEid());
         replacementValues.put("localSakaiName", ServerConfigurationService.getString(
               "ui.service", ""));
         replacementValues.put("currentUserName", uds.getCurrentUser().getDisplayName());
         replacementValues.put("localSakaiUrl", ServerConfigurationService.getPortalUrl());
         replacementValues.put("newPassword", newUserPassword);
         replacementValues.put("productionSiteName", productionSiteName);
         RenderedTemplate template = emailTemplateService.getRenderedTemplateForUser(NOTIFY_NEW_USER, user.getReference(), replacementValues);
         if (template == null)
            return;
         content = template.getRenderedMessage();

         String message_subject = template.getRenderedSubject();
         List headers = new ArrayList();
         headers.add("Precedence: bulk");
         emailService.send(from, to, message_subject, content, headerTo,
               replyTo, headers);
      }
   }

   /**
	 * Get User Directory Service
	 * 
	 * @return
	 */
	public UserDirectoryService getUserDirectoryService() {
		return this.uds;
	}

	/**
	 * Set User Directory Service
	 * 
	 * @param uds
	 */
	public void setUserDirectoryService(UserDirectoryService uds) {
		this.uds = uds;
	}

	/**
	 * Update Allowed Flag
	 * 
	 * @return boolean
	 */
	public boolean isUpdateAllowed() {
		return updateAllowed;
	}

	/**
	 * Set Update Allowed
	 * 
	 * @param updateAllowed
	 */
	public void setUpdateAllowed(boolean updateAllowed) {
		this.updateAllowed = updateAllowed;
	}


   public boolean isUpdatePassword() {
      return updatePassword;
   }

   public void setUpdatePassword(boolean updatePassword) {
      this.updatePassword = updatePassword;
   }

   public EmailService getEmailService() {
      return emailService;
   }

   public void setEmailService(EmailService emailService) {
      this.emailService = emailService;
   }

   public EmailTemplateService getEmailTemplateService() {
      return emailTemplateService;
   }

   public void setEmailTemplateService(EmailTemplateService emailTemplateService) {
      this.emailTemplateService = emailTemplateService;
   }

   public boolean isEmailNotification() {
      return emailNotification;
   }

   public void setEmailNotification(boolean emailNotification) {
      this.emailNotification = emailNotification;
   }

    public void setGeneratePassword(boolean generatePassword) {
        this.generatePassword = generatePassword;
    }

    public boolean isExternallyProvided() {
        return externallyProvided;
}

    public void setExternallyProvided(boolean externallyProvided) {
        this.externallyProvided = externallyProvided;
    }

    public UserDataService getUserDataService() {
        return userDataService;
    }

    public void setUserDataService(UserDataService userDataService) {
        this.userDataService = userDataService;
    }
}
