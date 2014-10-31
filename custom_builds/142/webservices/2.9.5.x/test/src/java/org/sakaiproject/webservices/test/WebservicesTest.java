package org.sakaiproject.webservices.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis.client.Service;
import org.apache.axis.client.Call;
import org.xml.sax.InputSource;

import javax.crypto.SecretKey;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.rpc.ServiceException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Dec 15, 2008
 * Time: 1:03:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class WebservicesTest {

   
   /* list of calls:
         $userService->changeUserType($sessionID, $userData->{username},
		      $userData->{user_type})
		   $userService->addMemberToSiteWithRole($sessionID, $siteID,
		      $userData->{username}, $userData->{role})
   */

   private static final String SIGNING_SUFFIX = "SakaiSigning.jws?wsdl";
   private static final String SITE_SUFFIX = "SakaiSite.jws?wsdl";
   private static final String SCRIPT_SUFFIX = "SakaiScript.jws?wsdl";
   private static final String LOGIN_SUFFIX = "SakaiLogin.jws?wsdl";
   

   protected static Log logger = LogFactory.getLog(WebservicesTest.class);
   private String wsHome;
   private String userSession;
   private String adminSession;
   private static final String A_USER = "blahdblah";
   private static final String A_USER_PASS = "blahd";
   private static final String A_SITE = "siteForWSTest";
   private SecretKey salt;
   private Service signingService;

   // run with -DlinktoolSalt=<path to salt file>
   protected void testLinktool() throws Exception {
      salt = readSecretKey(System.getProperty("linktoolSalt"), "HmacSHA1");
      signingService = new Service();

      adminSession = getSession("admin", "admin");
         
      // checkForUser
      Boolean exists = new Boolean(false);
      Call check = (Call) signingService.createCall();
      check.setTargetEndpointAddress(new java.net.URL(wsHome + SCRIPT_SUFFIX));
      check.setOperationName(new QName("checkForUser"));
      exists = (Boolean) check.invoke( new Object[] {adminSession,
         A_USER} );
      
      if (exists.booleanValue()) {
         logger.info("user already exists.");
      }
      else {
         // addNewUser
         Call addUser = (Call) signingService.createCall();
         addUser.setTargetEndpointAddress(new java.net.URL(wsHome + SCRIPT_SUFFIX));
         addUser.setOperationName(new QName("addNewUser"));
         // String sessionid, String eid, String firstname, String lastname, String email, String type, String password
         String ret = (String) addUser.invoke( new Object[] {adminSession,
            A_USER, "first", "last", "info@rsmart.com", "registered", A_USER_PASS} );
         
         if (!"success".equals(ret)) {
            logger.error("prob creating user", new Exception());
         }
      }
      
      // recheck for user
      exists = (Boolean) check.invoke( new Object[] {adminSession,
         A_USER} );

      if (!exists.booleanValue()) {
         logger.error("user should have been created", new Exception());
      }
      
      // checkForSite
      exists = new Boolean(false);
      check.setTargetEndpointAddress(new java.net.URL(wsHome + SCRIPT_SUFFIX));
      check.setOperationName(new QName("checkForSite"));
      exists = (Boolean) check.invoke( new Object[] {adminSession,
         A_SITE} );
      
      if (exists.booleanValue()) {
         logger.info("site already exists.");
      }
      else {
         // addNewSite
         addSite();
      }
      
      exists = (Boolean) check.invoke( new Object[] {adminSession,
         A_SITE} );

      if (!exists.booleanValue()) {
         logger.error("site should have been created", new Exception());
      }
      
      Call getUserId = (Call) signingService.createCall();
      getUserId.setTargetEndpointAddress(new java.net.URL(wsHome + SCRIPT_SUFFIX));
      getUserId.setOperationName(new QName("getUserID"));
      String userId = (String) getUserId.invoke( new Object[] {adminSession,
         A_USER} );
      
      userSession = getSession(A_USER, userId);
      
      if ("".equals(userSession)) {
         logger.error("failed to getSession for regular user", new Exception());
      }
      
      Call getSiteType = (Call) signingService.createCall();
      getSiteType.setTargetEndpointAddress(new java.net.URL(wsHome + SITE_SUFFIX));
      getSiteType.setOperationName(new QName("getSiteType"));
      String type = (String) getSiteType.invoke( new Object[] {adminSession,
         A_SITE} );
      type = getSiteType(type);
      if (!"project".equals(type)) {
         logger.error("incorrent site type: " + type, new Exception());
      }
      
   }

   private void addSite() throws Exception, MalformedURLException, RemoteException {
      Call addSite = (Call) signingService.createCall();
      addSite.setTargetEndpointAddress(new java.net.URL(wsHome + SCRIPT_SUFFIX));
      addSite.setOperationName(new QName("addNewSite"));
      // String sessionid, String siteid, String title, String description, String shortdesc, String iconurl, String infourl, boolean joinable, String joinerrole, boolean published, boolean publicview, String skin, String type
      String ret = (String) addSite.invoke( new Object[] {adminSession,
         A_SITE, "Test WS Site", "a test for the webservices", "a test for the webservices", "", "", new Boolean(false), "", new Boolean(true), new Boolean(false), "", "project"} );

      if (!"success".equals(ret)) {
         logger.error("prob creating site", new Exception());
      }
      
      addSite.setOperationName(new QName("addNewPageToSite"));
      // addNewPageToSite( String sessionid, String siteid, String pagetitle, int pagelayout)
      ret = (String) addSite.invoke( new Object[] {adminSession,
         A_SITE, "Home", new Integer(0)} );
      
      if (!ret.equals("success")) {
         throw new Exception("failed to add site page");
      }
      
      addSite.setOperationName(new QName("addNewPageToSite"));
      // addNewPageToSite( String sessionid, String siteid, String pagetitle, int pagelayout)
      ret = (String) addSite.invoke( new Object[] {adminSession,
         A_SITE, "Site Info", new Integer(0)} );
      
      if (!ret.equals("success")) {
         throw new Exception("failed to add site page");
      }
      
      addSite.setOperationName(new QName("addNewToolToPage"));
      //addNewToolToPage( String sessionid, String siteid, String pagetitle, String tooltitle, String toolid, String layouthints)
      ret = (String) addSite.invoke(new Object[] {adminSession,
         A_SITE, "Home", "web", "sakai.iframe.site", "0,0"});     
      
      if (!ret.equals("success")) {
         throw new Exception("failed to add tool to page");
      }
      addSite.setOperationName(new QName("addNewToolToPage"));
      //addNewToolToPage( String sessionid, String siteid, String pagetitle, String tooltitle, String toolid, String layouthints)
      ret = (String) addSite.invoke(new Object[] {adminSession,
         A_SITE, "Site Info", "siteInfo", "sakai.siteinfo", "0,0"});     
      
      if (!ret.equals("success")) {
         throw new Exception("failed to add tool to page");
      }
   }

   protected void testForMySakai() throws ServiceException, RemoteException, MalformedURLException {
      Call login = (Call) signingService.createCall();
      login.setTargetEndpointAddress(new java.net.URL(wsHome + LOGIN_SUFFIX));
      login.setOperationName(new QName("login"));
      
      try {
         String sessionId = (String) login.invoke( new Object[] {"admin",
            "wrongpassword"} );
         logger.error("incorrect admin login should have failed", new Exception());
      }
      catch (RemoteException e) {
         // this was meant to happen
      }
      
      adminSession = (String) login.invoke( new Object[] {"admin",
         "admin"} );
      
      Call changeUserType = (Call) signingService.createCall();
      changeUserType.setTargetEndpointAddress(new java.net.URL(wsHome + SCRIPT_SUFFIX));
      changeUserType.setOperationName(new QName("changeUserType"));
      
      changeUserType.invoke(new Object[] {adminSession, A_USER,
         "instructor"});
      
      Call addMemberToSiteWithRole = (Call) signingService.createCall();
      addMemberToSiteWithRole.setTargetEndpointAddress(new java.net.URL(wsHome + SCRIPT_SUFFIX));
      addMemberToSiteWithRole.setOperationName(new QName("addMemberToSiteWithRole"));
      
      addMemberToSiteWithRole.invoke(new Object[] {adminSession, A_SITE, A_USER,
         "Participant"});
   }
   
   protected String getSiteType(String siteXml) throws XPathExpressionException {
      XPath xpath = XPathFactory.newInstance().newXPath();
      InputSource is = new InputSource(new StringReader(siteXml));
      return xpath.evaluate("/site/type", is);
   }

   protected String getSession(String eid, String id) throws Exception {
      String signingObject = "currentuser&sign=" + sign("currentuser", salt);

      String command = "signedobject=" + URLEncoder.encode(signingObject) + 
             "&user=" + URLEncoder.encode(eid) + 
			    "&internaluser=" + URLEncoder.encode(id) + 
			    "&site=" + URLEncoder.encode("~admin") + 
			    "&role=" + URLEncoder.encode("maintainer") +
			    "&session=" + URLEncoder.encode("blah d blah") +
			    "&serverurl=" + URLEncoder.encode("http://localhost:8080") +
			    "&time=" + System.currentTimeMillis() +
			    "&placement=" + URLEncoder.encode("1234567890");

      String signature = sign(command, salt);
      command += "&sign=" + signature;

      Call call = (Call) signingService.createCall(); //new QName("SakaiSigning"), "testSign");
      call.setTargetEndpointAddress(new java.net.URL(wsHome + SIGNING_SUFFIX));
      call.setOperationName(new QName("getsession"));
      String ret = (String) call.invoke( new Object[] {command,
         signingObject} );

      if (ret.equalsIgnoreCase("")) {
         throw new Exception("Failed to verify signature.");
      }
      return ret;
   }

   // -DsakaiWSHome=http://localhost:8880/sakai-axis/
   public static void main(String[] args) {
      logger.info("starting tests");
      
      WebservicesTest test = new WebservicesTest();
      test.wsHome = System.getProperty("sakaiWSHome");
      try {
         test.testLinktool();
         test.testForMySakai();
      } catch (Exception e) {
         logger.error("error on tests", e);
      }
      
   }
   
	protected SecretKey readSecretKey(String filename, String alg) {
	    SecretKey privkey = null;
	    FileInputStream file = null;
	    try {
			file = new FileInputStream(filename);
			byte[] bytes = new byte[file.available()];
			file.read(bytes);
			privkey = new SecretKeySpec(bytes, alg);
		} catch (Exception ignore) {
			logger.error("Unable to read key from " + filename);
	    } finally {
	        if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    // tried
                }
	        }
	    }
        return privkey;
	}

   protected String sign(String data, SecretKey salt) throws Exception {
      Mac sig = Mac.getInstance("HmacSHA1");
      sig.init(salt);
      return byteArray2Hex(sig.doFinal(data.getBytes()));
   }

	private static String byteArray2Hex(byte[] ba){
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < ba.length; i++){
         int hbits = (ba[i] & 0x000000f0) >> 4;
         int lbits = ba[i] & 0x0000000f;
         sb.append("" + hexChars[hbits] + hexChars[lbits]);
      }
      return sb.toString();
	}

    private static char[] hexChars = {
	    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
	};
}
