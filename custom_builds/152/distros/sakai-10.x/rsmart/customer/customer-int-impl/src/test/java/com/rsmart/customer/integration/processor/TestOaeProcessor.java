package com.rsmart.customer.integration.processor;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.imsglobal.basiclti.BasicLTIUtil;
import org.junit.Test;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.hybrid.util.XSakaiToken;


import java.net.URI;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 2/21/12
 * Time: 9:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestOaeProcessor {

    public final static void main(String[] args) {
        String contextid = "http-ucdavis-edu-course-offering-SAS-025-201201";
     String oaeHostname = "localhost";
     String oaePrincipal = "admin";
     String oaeGroupLookupUrl = "http://localhost:8080/system/userManager/group/";

     String isSectionJsonPathStr = "$['properties']['sakai:is-section']";
     JsonPath isSectionJsonPath;


        //XSakaiToken xSakaiToken = new XSakaiToken(ComponentManager.getInstance());
        isSectionJsonPath = JsonPath.compile(isSectionJsonPathStr);


        org.apache.http.client.HttpClient httpClient = new DefaultHttpClient();
        try {
            URI uri = new URI(oaeGroupLookupUrl + contextid + ".json");
            HttpGet httpget = new HttpGet(uri);
            // authenticate to Nakamura using x-sakai-token mechanism
           // String token = xSakaiToken.createToken(oaeHostname, oaePrincipal);
            //httpget.addHeader(XSakaiToken.X_SAKAI_TOKEN_HEADER, token);
            //
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpClient.execute(httpget, responseHandler);
            String isSection = isSectionJsonPath.read(responseBody);
            if (StringUtils.isNotBlank(isSection)) {
                System.out.print(Boolean.parseBoolean(isSection));
            }
        } catch (HttpResponseException e) {
            e.printStackTrace();
        } catch (Exception e) {
             e.printStackTrace();
        } finally {
            httpClient.getConnectionManager().shutdown();
        }




    }


    public void testLaunch() {


            String launch_url = "http://localhost:8888/imsblti/provider/sakai.resources";
            String key = "SUPER_SECRET";
            String secret ="SUPER_SECRET";
            String org_guid = "sakaiproject.org";
            String org_desc = "The Sakai Project";
            String org_url = null;
            //String contextid = "4743e1b8-7f81-4c15-8795-1e3f7fd63a66";
            String contextid = "eatmypoop2";

            String placementId = "5cb43b5b-4a8b-44e6-a294-0348d5b70eca";
            String userid = "admin";
            String role = "Instructor";
            String externalCourseId = "xxxxxxyy2";

            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(30000);

            try {
                Map<String, String>  postProps = new HashMap();

                postProps.put("context_label", "Testing Course 202");
                postProps.put("custom_course_id", externalCourseId);
                postProps.put("lis_person_name_family", "Administrator");
                postProps.put("resource_link_id", placementId);
                postProps.put("user_id", userid);
                postProps.put("resource_link_description", "test5");
                postProps.put("context_title", "Testing Course 202");
                postProps.put("context_type", "course");
                postProps.put("lis_person_name_full", "Sakai Administrator");
                postProps.put("context_id", contextid);
                postProps.put("roles", role);
                postProps.put("lis_person_name_given", "Sakai");
                postProps.put("launch_presentation_locale", "en_US");

                 Map<String, String> launch = BasicLTIUtil.signProperties(postProps, launch_url, "POST",
                        key, secret, org_guid, org_desc, org_url, null, "admin@sakaiproject.org <mailto:tool_consumer_instance_contact_email=admin@sakaiproject.org>");


                PostMethod post = new PostMethod(launch_url);

                for (Map.Entry<String, String> entry : launch.entrySet()) {
                    System.out.println(entry.getKey() + "=" + entry.getValue());
                    post.addParameter(entry.getKey(), entry.getValue());
                }



                int iGetResultCode = client.executeMethod(post);
                final String strGetResponseBody = post.getResponseBodyAsString();
                System.out.println(strGetResponseBody);
            }
            catch(Exception err) {
                err.printStackTrace();
            }


	}
}
