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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.user.api.DisplayAdvisorUDP;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserFactory;
import org.sakaiproject.user.api.UsersShareEmailUDP;

import org.apache.axis.encoding.XMLType;
import javax.xml.rpc.ParameterMode;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.message.MessageElement;
import javax.xml.namespace.QName;

import org.w3c.dom.*;

/**
 * <p>
 * iMISUserDirectoryProvider is a UserDirectoryProvider implementation.
 * </p>
 */
public class iMISUserDirectoryProvider extends org.apache.axis.client.Stub implements UserDirectoryProvider, UsersShareEmailUDP, DisplayAdvisorUDP
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
		if (userId == null) return false;
		//if (userId.startsWith("test")) return true;
		//if (m_info.containsKey(userId)) return true;

		return true;

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
	    M_log.info("getUser is being called! "+edit.getEid());
		if (edit == null) return false;
		if (!userExists(edit.getEid())) return false;

        MessageElement results = null;
		try {
		    OperationDesc oper = new org.apache.axis.description.OperationDesc();
		    oper.setName("ExecuteDatasetStoredProcedure");
		    ParameterDesc param = new org.apache.axis.description.ParameterDesc(
		      new javax.xml.namespace.QName(nsUrl+"DataAccess/", "securityPassword"),
		      org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
		      java.lang.String.class, false, false
		    );
		    param.setOmittable(true);
		    oper.addParameter(param);
		    param = new org.apache.axis.description.ParameterDesc(
		      new javax.xml.namespace.QName(nsUrl+"DataAccess/", "name"), 
		      org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
		      java.lang.String.class, false, false
		    );
		    param.setOmittable(true);
		    oper.addParameter(param);
		    param = new org.apache.axis.description.ParameterDesc(
		      new javax.xml.namespace.QName(nsUrl+"DataAccess/", "parameters"),
		      org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
		      java.lang.String.class, false, false
		    );
		    param.setOmittable(true);
		    oper.addParameter(param);
		    oper.setReturnType(new javax.xml.namespace.QName(nsUrl+"DataAccess/", ">>ExecuteDatasetStoredProcedureResponse>ExecuteDatasetStoredProcedureResult"));
		    oper.setReturnClass(org.sakaiproject.provider.user.ExecuteDatasetStoredProcedureResult.class);
		    oper.setReturnQName(new javax.xml.namespace.QName(nsUrl+"DataAccess/", "ExecuteDatasetStoredProcedureResult"));
		    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
		    oper.setUse(org.apache.axis.constants.Use.LITERAL);
		    
		    Service service = new Service();
		    org.apache.axis.client.Call _call = (Call) service.createCall();
            _call.setOperation(oper);
            _call.setUseSOAPAction(true);
            _call.setSOAPActionURI(nsUrl+"DataAccess/ExecuteDatasetStoredProcedure");
            _call.setTargetEndpointAddress(wsdlUrl+"DataAccess.asmx?wsdl");
            _call.setEncodingStyle(null);
            _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
            _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
            _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
            _call.setOperationName(new javax.xml.namespace.QName(nsUrl+"DataAccess/", "ExecuteDatasetStoredProcedure"));
            
            setRequestHeaders(_call);
            setAttachments(_call);
            
            M_log.info("Call is built.");
            
            try {        
              java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
                  spStoredProcedure, 
                  "iweb_sp_getExamsInformationByid_SAKAI", 
                  edit.getEid()
                });

              if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException)_resp;
              } else {
                extractAttachments(_call);
                try {
                     org.sakaiproject.provider.user.ExecuteDatasetStoredProcedureResult result = (org.sakaiproject.provider.user.ExecuteDatasetStoredProcedureResult) _resp;
                     for (MessageElement m : result.get_any()) {
                       results = m;
                     }
                } catch (java.lang.Exception _exception) {
                    org.sakaiproject.provider.user.ExecuteDatasetStoredProcedureResult result = (org.sakaiproject.provider.user.ExecuteDatasetStoredProcedureResult) org.apache.axis.utils.JavaUtils.convert(_resp, org.sakaiproject.provider.user.ExecuteDatasetStoredProcedureResult.class);
                    for (MessageElement m : result.get_any()) {
                      results = m;
                    }
                }
              }
            } catch (org.apache.axis.AxisFault axisFaultException) {
              throw axisFaultException;
            }
		    
		} catch (Exception e) {
		    System.out.println(e.getClass().getName() + " : " + e.getMessage());
		    return false;
		}	
		
		if (results == null)
		{
			edit.setFirstName(edit.getEid());
			edit.setLastName(edit.getEid());
			edit.setEmail(edit.getEid());
			edit.setPassword(edit.getEid());
			edit.setType("registered");
		}
		else
		{		    
		    try {
		      Element xml = results.getAsDOM();
		      Element first = (Element) xml.getElementsByTagName("first_name").item(0);
		      Element last = (Element) xml.getElementsByTagName("last_name").item(0);
		      Element email = (Element) xml.getElementsByTagName("email").item(0);
		      
		      String firstStr = "", lastStr = "", emailStr = "";
		      
		      if (first != null) {
		          firstStr = getCharacterDataFromElement(first);
		      } else {
		          M_log.warn("I can't find First Name in the xml for "+edit.getEid());
		      }
		      if (last != null) {
		          lastStr = getCharacterDataFromElement(last);
		      } else {
		          M_log.warn("I can't find Last Name in the xml for "+edit.getEid());
		      }
		      if (email != null) {
		          emailStr = getCharacterDataFromElement(email);
		      } else {
		          M_log.warn("I can't find email in the xml for "+edit.getEid());
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
		
		String results = null;
		try {
		    OperationDesc oper = new org.apache.axis.description.OperationDesc();
		    oper.setName("AuthenticateUser");
		    ParameterDesc param = new org.apache.axis.description.ParameterDesc(
	          new javax.xml.namespace.QName(nsUrl+"Authentication/", "securityPassword"),
	          org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
	          java.lang.String.class, false, false
	        );
	        param.setOmittable(true);
	        oper.addParameter(param);
	        param = new org.apache.axis.description.ParameterDesc(
	          new javax.xml.namespace.QName(nsUrl+"Authentication/", "username"), 
	          org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
	          java.lang.String.class, false, false
	        );
	        param.setOmittable(true);
	        oper.addParameter(param);
	        param = new org.apache.axis.description.ParameterDesc(
	          new javax.xml.namespace.QName(nsUrl+"Authentication/", "password"),
	          org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
	          java.lang.String.class, false, false
	        );
	        param.setOmittable(true);
	        oper.addParameter(param);
	        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
	        oper.setReturnClass(java.lang.String.class);
	        oper.setReturnQName(new javax.xml.namespace.QName(nsUrl+"Authentication/", "AuthenticateUserResult"));
	        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
	        oper.setUse(org.apache.axis.constants.Use.LITERAL);
	        
	        Service service = new Service();
	        org.apache.axis.client.Call _call = (Call) service.createCall();
	        _call.setOperation(oper);
	        _call.setUseSOAPAction(true);
	        _call.setSOAPActionURI(nsUrl+"Authentication/AuthenticateUser");
	        _call.setTargetEndpointAddress(wsdlUrl+"Authentication.asmx?wsdl");
	        _call.setEncodingStyle(null);
	        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
	        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
	        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
	        _call.setOperationName(new javax.xml.namespace.QName(nsUrl+"Authentication/", "AuthenticateUser"));
	        
	        setRequestHeaders(_call);
	        setAttachments(_call);
	        
	        try {
	            java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
	                spAuthenticateUser, userId, password
	            });
	            
	            if (_resp instanceof java.rmi.RemoteException) {
	                throw (java.rmi.RemoteException)_resp;
	            } else {
	                extractAttachments(_call);
	                try {
	                    results = (java.lang.String) _resp; 
	                } catch (java.lang.Exception _exception) {
	                    results = (java.lang.String) org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
	                }
	            }   
	        } catch (org.apache.axis.AxisFault axisFaultException) {
	            throw axisFaultException;
	        }
		} catch (Exception e) {
		    System.out.println(e.getClass().getName() + " : " + e.getMessage());
		    return false;
	    }
	    
        if (results.indexOf("Error") != -1) {
            M_log.error("Error: "+results);
            return false;
        }
        if (results.indexOf("User") != -1) {
            M_log.info("Success: "+results);
            return true;
        }
        
		return false;

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
}

