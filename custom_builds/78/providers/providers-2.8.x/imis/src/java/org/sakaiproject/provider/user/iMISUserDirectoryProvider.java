/**********************************************************************************
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.provider.user;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.user.api.DisplayAdvisorUDP;
import org.sakaiproject.user.api.ExternalUserSearchUDP;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserFactory;
import org.sakaiproject.user.api.UsersShareEmailUDP;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//import javax.xml.rpc.ParameterMode;
import com.isgsolutions.ibridge.authentication.*;
import com.isgsolutions.ibridge.dataaccess.*;
import com.isgsolutions.ibridge.dataaccess.ExecuteDatasetStoredProcedureResponse.ExecuteDatasetStoredProcedureResult;

import org.w3c.dom.*;

/**
 * <p>
 * iMISUserDirectoryProvider is a UserDirectoryProvider implementation.
 * </p>
 */
public class iMISUserDirectoryProvider implements UserDirectoryProvider, UsersShareEmailUDP, DisplayAdvisorUDP, ExternalUserSearchUDP
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(iMISUserDirectoryProvider.class);

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/
     
     protected String spAuthenticateUser = "password";
     protected String spStoredProcedure = "password";
     protected String wsdlUrl = "http://localhost/";
     protected String nsUrl = "http://localhost/";

     public void setSpAuthenticateUser(String password) {
        spAuthenticateUser = password;
     }
     public void setSpStoredProcedure(String password) {
        spStoredProcedure = password;
     }
     public void setWsdlUrl(String url) {
        wsdlUrl = url;
     }
     public void setNsUrl(String url) {
        nsUrl = url;
     }

	/***************************************************************************
	 * Init and Destroy
	 **************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
	    M_log.info("Initing the iMIS User Directory Provider!");

	}
	
	/**
	 * Returns to uninitialized state. You can use this method to release resources thet your Service allocated when Turbine shuts down.
	 */
	public void destroy()
	{

		M_log.info("destroy()");

	} // destroy

	/**********************************************************************************************************************************************************************************************************************************************************
	 * UserDirectoryProvider implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** A collection of user ids/names. */
	/*
	protected Hashtable m_info = null;
	protected Call authUser = null;
    protected Call dataAccess = null;

	protected class Info
	{
		public String id;

		public String firstName;

		public String lastName;

		public String email;

		public Info(String id, String firstName, String lastName, String email)
		{
			this.id = id;
			this.firstName = firstName;
			this.lastName = lastName;
			this.email = email;
		}

		public Info(String firstName, String lastName, String email)
		{
			this.firstName = firstName;
			this.lastName = lastName;
			this.email = email;
		}

	}*/ // class info

	/**
	 * Construct.
	 */
	public iMISUserDirectoryProvider()
	{
	}

	/**
	 * See if a user by this id exists.
	 * 
	 * @param userId
	 *        The user id string.
	 * @return true if a user by this id exists, false if not.
	 */
	protected boolean userExists(String userId)
	{
	    M_log.info("userExists is being called! "+userId);
		if (userId == null || userId.equals("null")) return false;
		
		DataAccessX0020WebX0020Service dataService = new DataAccessX0020WebX0020Service();
        DataAccessX0020WebX0020ServiceSoap dataPort = dataService.getDataAccessX0020WebX0020ServiceSoap();
        
        ExecuteDatasetStoredProcedureResult result = dataPort.executeDatasetStoredProcedure(
            spStoredProcedure, 
            "iweb_sp_getUsersByid_SAKAI", 
            "'"+userId+"'"
        );
		
		if (result == null)
		{
			return false;
		}
		else
		{		    
		    try {
		        Node diffgram = (Node) result.getAny();
                NodeList newDS = diffgram.getChildNodes();
                if (newDS.getLength() == 0) {
                	M_log.info("User "+userId+" doesn't exist!");
                	return false;
                }
                NodeList table = newDS.item(0).getChildNodes();
                
                for (int i=0; i<table.getLength(); i++) {
                    Node record = table.item(i);
                    if (record.getNodeName().equals("iBridge-Errors")) {
                        M_log.warn(userId+" is not a real user!");
                        return false;
                    } 
                }
		      return true;
		      
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}
		return false;

	} // userExists

	/**
	 * Access a user object. Update the object with the information found.
	 * 
	 * @param edit
	 *        The user object (id is set) to fill in.
	 * @return true if the user object was found and information updated, false if not.
	 */
	public boolean getUser(UserEdit edit)
	{
		if (edit == null) return false;
		String userId = edit.getEid();
		M_log.info("getUser is being called! "+userId);
		if (!userExists(userId)) return false;

        DataAccessX0020WebX0020Service dataService = new DataAccessX0020WebX0020Service();
        DataAccessX0020WebX0020ServiceSoap dataPort = dataService.getDataAccessX0020WebX0020ServiceSoap();
        
        ExecuteDatasetStoredProcedureResult result = dataPort.executeDatasetStoredProcedure(
            spStoredProcedure, 
            "iweb_sp_getUsersByid_SAKAI", 
            "'"+userId+"'"
        );
		
		if (result == null)
		{
			edit.setFirstName(userId);
			edit.setLastName(userId);
			edit.setEmail(userId);
			edit.setPassword(userId);
			edit.setType("registered");
		}
		else
		{		    
		    try {
		        Node diffgram = (Node) result.getAny();
                NodeList newDS = diffgram.getChildNodes();
                NodeList table = newDS.item(0).getChildNodes();	

                String firstStr = "", lastStr = "", emailStr = "";
                
                for (int i=0; i<table.getLength(); i++) {
                    Node record = table.item(i);
                    NodeList entries = record.getChildNodes();
                    for (int j=0; j<entries.getLength(); j++) {
                        //System.out.println(entries.item(j).getNodeName()+": "+entries.item(j).getTextContent());
                        if (firstStr.equals("") && entries.item(j).getNodeName().equals("first_name")) {
                            firstStr = entries.item(j).getTextContent();
                        }
                        else if (lastStr.equals("") && entries.item(j).getNodeName().equals("last_name")) {
                            lastStr = entries.item(j).getTextContent();
                        }
                        else if (emailStr.equals("") && entries.item(j).getNodeName().equals("email")) {
                            emailStr = entries.item(j).getTextContent();
                        }
                    }
                }
                
                if (firstStr.equals("")) {
		            M_log.warn("I can't find First Name in the xml for "+userId);
		        }
		        if (lastStr.equals("")) {
		             M_log.warn("I can't find Last Name in the xml for "+userId);
		        }
		        if (emailStr.equals("")) {
		            M_log.warn("I can't find email in the xml for "+userId);
		        }

		      edit.setFirstName(firstStr);
		      edit.setLastName(lastStr);
		      edit.setEmail(emailStr);
		      edit.setPassword(""); //TODO - does this have to be set correctly? Does it matter since authenticateUser will be consulted anyway?
		      edit.setType("student");
		      
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		    
		}
		
		return true;

	} // getUser

	/**
	 * Access a collection of UserEdit objects; if the user is found, update the information, otherwise remove the UserEdit object from the collection.
	 * 
	 * @param users
	 *        The UserEdit objects (with id set) to fill in or remove.
	 */
	public void getUsers(Collection users)
	{
		for (Iterator i = users.iterator(); i.hasNext();)
		{
			UserEdit user = (UserEdit) i.next();
			if (!getUser(user))
			{
				i.remove();
			}
		}
	}

	/**
	 * Find a user object who has this email address. Update the object with the information found. <br />
	 * Note: this method won't be used, because we are a UsersShareEmailUPD.<br />
	 * This is the sort of method to provide if your external source has only a single user for any email address.
	 * 
	 * @param email
	 *        The email address string.
	 * @return true if the user object was found and information updated, false if not.
	 */
	public boolean findUserByEmail(UserEdit edit, String email)
	{
		if ((edit == null) || (email == null)) return false;

		// assume a "@local.host"
		int pos = email.indexOf("@local.host");
		if (pos != -1)
		{
			String id = email.substring(0, pos);
			edit.setEid(id);
			return getUser(edit);
		}

		return false;

	} // findUserByEmail

	/**
	 * Find all user objects which have this email address.
	 * 
	 * @param email
	 *        The email address string.
	 * @param factory
	 *        Use this factory's newUser() method to create all the UserEdit objects you populate and return in the return collection.
	 * @return Collection (UserEdit) of user objects that have this email address, or an empty Collection if there are none.
	 */
	public Collection findUsersByEmail(String email, UserFactory factory)
	{
		Collection rv = new Vector();

		// get a UserEdit to populate
		UserEdit edit = factory.newUser();

		// assume a "@local.host"
		int pos = email.indexOf("@local.host");
		if (pos != -1)
		{
			String id = email.substring(0, pos);
			edit.setEid(id);
			if (getUser(edit)) rv.add(edit);
		}

		return rv;
	}

	/**
	 * Authenticate a user / password. If the user edit exists it may be modified, and will be stored if...
	 * 
	 * @param id
	 *        The user id.
	 * @param edit
	 *        The UserEdit matching the id to be authenticated (and updated) if we have one.
	 * @param password
	 *        The password.
	 * @return true if authenticated, false if not.
	 */
	public boolean authenticateUser(String userId, UserEdit edit, String password)
	{
	    M_log.info("authenticateUser is being called!");
		if ((userId == null) || (password == null)) return false;

		//if (userId.startsWith("test")) return userId.equals(password);
		//if (userExists(userId) && password.equals("sakai")) return true;
		
		String result = null;
		
		AuthenticationX0020WebX0020Service authService = new AuthenticationX0020WebX0020Service();
		AuthenticationX0020WebX0020ServiceSoap authPort = authService.getAuthenticationX0020WebX0020ServiceSoap();
		String authResult = authPort.authenticateUser(
		    spAuthenticateUser,
		    userId,
		    password
		);
		
		try {
		    InputStream res = new ByteArrayInputStream(authResult.getBytes("UTF-16"));
		    
		    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		    DocumentBuilder docBuilder = dbf.newDocumentBuilder();
		    Document doc = docBuilder.parse(res);
		    
		    NodeList userList = doc.getElementsByTagName("User");
		    if (userList == null || userList.getLength() == 0) {
		        M_log.error("Error: That user does not exist or you have the wrong pwd!");
		        M_log.error(result);
		        return false;
		    } else {
		        Element user = (Element) userList.item(0);
		        return true;
		    }
		    
		} catch (Exception e) {
		    System.out.println(e.getClass().getName() + " : " + e.getMessage());
		    return false;
	    }
        
	} // authenticateUser

	/**
	 * {@inheritDoc}
	 */
	public boolean authenticateWithProviderFirst(String id)
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean createUserRecord(String id)
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDisplayId(User user)
	{
		return user.getEid();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDisplayName(User user)
	{
		// punt
		return null;
	}
	
	public static String getCharacterDataFromElement(Element e) {
	    Node child = e.getFirstChild();
	    if (child instanceof CharacterData) {
	        CharacterData cd = (CharacterData) child;
	        return cd.getData();
	    }
	    return "?";
	}
	
	public List<UserEdit> searchExternalUsers(String criteria, int first, int last, UserFactory factory) {
	    
	    M_log.info("searchExternalUsers() is called!");
	    List<UserEdit> users = new ArrayList<UserEdit>();
	    
	    DataAccessX0020WebX0020Service dataService = new DataAccessX0020WebX0020Service();
	    DataAccessX0020WebX0020ServiceSoap dataPort = dataService.getDataAccessX0020WebX0020ServiceSoap();
	    ExecuteDatasetStoredProcedureResult result = dataPort.executeDatasetStoredProcedure(
	        spStoredProcedure,
	        "iweb_sp_getUsersByid_SAKAI",
	        "'"+criteria+"'"
	    );
		
		if (result != null)
		{		    
		    try {
		        
		        Node diffgram = (Node) result.getAny();
		        NodeList newDS = diffgram.getChildNodes();
		        NodeList table = newDS.item(0).getChildNodes();
		        
		        String firstStr = "", lastStr = "", emailStr = "";
		        
		        for (int i=0; i<table.getLength(); i++) {
		            Node record = table.item(i);
                    if (record.getNodeName().equals("iBridge-Errors")) {
                        M_log.warn("No Such User");
                        return users;
                    }
		            NodeList entries = record.getChildNodes();
		            for (int j=0; j<entries.getLength(); j++) {
		                //System.out.println(entries.item(j).getNodeName()+": "+entries.item(j).getTextContent());
		                if (firstStr.equals("") && entries.item(j).getNodeName().equals("first_name")) {
		                    firstStr = entries.item(j).getTextContent();
		                }
		                else if (lastStr.equals("") && entries.item(j).getNodeName().equals("last_name")) {
		                    lastStr = entries.item(j).getTextContent();
		                }
		                else if (emailStr.equals("") && entries.item(j).getNodeName().equals("email")) {
		                    emailStr = entries.item(j).getTextContent();
		                }
		            }
		        }
		        
		        if (firstStr.equals("")) {
		             M_log.warn("I can't find First Name in the xml for "+criteria);
		        }
		        if (lastStr.equals("")) {
		            M_log.warn("I can't find Last Name in the xml for "+criteria);
		        }
		        if (emailStr.equals("")) {
		            M_log.warn("I can't find email in the xml for "+criteria);
		        }
		      
		      UserEdit user = factory.newUser();
		      
		      user.setEid(criteria);
		      user.setFirstName(firstStr);
		      user.setLastName(lastStr);
		      user.setEmail(emailStr);
		      user.setPassword("ffdsfsgsjhdfvdsfvhdsvc"); //TODO - does this have to be set correctly? 
		      user.setType("student");
		      
		      users.add(user);
		      
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		    
		}
	    
	    return users;
	}
}

