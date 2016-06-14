package com.rsmart.customer.integration.oae;

import com.jayway.jsonpath.JsonPath;
import com.rsmart.customer.integration.util.SiteHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.imsglobal.basiclti.BasicLTIConstants;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.lti.api.AbstractBLTIProcessor;
import org.sakaiproject.lti.api.LTIException;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.hybrid.util.XSakaiToken;
import org.sakaiproject.section.api.SectionManager;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;

/**
 * Hooks into Sakai's basic lti provider in order to modify the site after creation to
 * synchronize SIS data between oae and cle.
 *
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 2/17/12
 * Time: 3:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class OAEBasicLTIProcessor extends AbstractBLTIProcessor {
    private final static String EXTERNAL_COURSE_ID = "custom_external_course_id";

    private static Log log = LogFactory.getLog(OAEBasicLTIProcessor.class);
    private SiteHelper siteHelper;
    private SiteService siteService;
    private SectionManager sectionManager;
    private boolean enabled = true;
    protected transient XSakaiToken xSakaiToken;
    private String oaeHostname = "localhost";
    private String oaePrincipal = "admin";
    private String oaeGroupLookupUrl = "http://localhost:8080/system/userManager/group/";

    private String isSectionJsonPathStr = "$['properties']['sakai:is-section']";
    private JsonPath isSectionJsonPath;

    public void init(){
        xSakaiToken = new XSakaiToken(ComponentManager.getInstance());
        isSectionJsonPath = JsonPath.compile(isSectionJsonPathStr);
    }

    public void afterValidation(Map payload, boolean trustedConsumer) throws LTIException {

        String externalCourseId = getExternalCourseId(payload);

        if (externalCourseId != null) {
            try {
                Site existingSite = siteHelper.findSite(externalCourseId);

                String payloadContextId = (String) payload.get(BasicLTIConstants.CONTEXT_ID);
                Site launchCreatedSite = siteHelper.findSiteByLTIContextId(payloadContextId);

                if (existingSite != null) {
                    String ltiContextId = existingSite.getProperties().getProperty(SiteHelper.LTI_CONTEXT_ID);

                    if (StringUtils.isEmpty(ltiContextId) ) {
                        log.debug("can't find property with name " + SiteHelper.LTI_CONTEXT_ID + " in site " + existingSite.getId());

                        if (launchCreatedSite != null){
                            if (!existingSite.getId().equals(launchCreatedSite.getId())){
                                String launchCreatedSiteLtiContextId = launchCreatedSite.getProperties().getProperty(SiteHelper.LTI_CONTEXT_ID);
                                log.debug("found existing site: " + existingSite.getId() + " with " + EXTERNAL_COURSE_ID + ":" + externalCourseId +
                                        " which does not have the lti_context_id set, found site:" + launchCreatedSite.getId() +
                                        " with existing lti_context_id:" + launchCreatedSiteLtiContextId);
                                // kick out because we don't want to set the lti_context_id in this site, its set somewhere else already
                                return;
                            } else {
                                log.debug("found existing site: " + existingSite.getId() + " with " + EXTERNAL_COURSE_ID + ":" + externalCourseId +
                                        " and context_id matches the lti_context_id in the site");
                                return;
                            }
                        }  else {
                            // SIS-9 if you are a section and the external course id is found but that site does not have an lti_context_id
                            // throw exception, you need to launch into course site first.
                            if (isSection(payloadContextId)){
                                throw new LTIException("LAUNCHING_INTO_SECTION_WORLD_AHEAD_OF_COURSE_WORLD_NOT_ALLOWED",
                                        "you can not launch into a section world ahead of a course world, please find your course world and launch into it first.");
                            }


                            // if a launch does not exist, set the lti_context_id so rest of processing can handle the work
                            log.debug("found course with " + EXTERNAL_COURSE_ID  + ":" + externalCourseId +
                                    " , but no existing launch created site detected for context_id: " + payload.get(BasicLTIConstants.CONTEXT_ID));
                            try {

                                // bypass security when saving sites
                                SecurityService.pushAdvisor(new SecurityAdvisor() {
                                    public SecurityAdvice isAllowed(String userId, String function, String reference) {
                                        return SecurityAdvice.ALLOWED;
                                    }
                                });

                                existingSite.getPropertiesEdit().addProperty(SiteHelper.LTI_CONTEXT_ID, payloadContextId);

                                siteService.save(existingSite);

                                // reload site, otherwise bad stuff happens
                                existingSite = siteService.getSite(existingSite.getId());

                            } finally {
                                SecurityService.popAdvisor();
                            }

                        }

                    } else {
                        if (!ltiContextId.equals(payload.get(BasicLTIConstants.CONTEXT_ID))){
                            log.debug("found existing site: " + existingSite.getId() + " with " + EXTERNAL_COURSE_ID + ":" + externalCourseId +
                                    " therefore swapping out lti_context_id in payload to " + ltiContextId + " so that we launch to it");

                            payload.put(BasicLTIConstants.CONTEXT_ID, ltiContextId);
                        }
                    }
                }

            } catch (Exception e) {
                if (e instanceof LTIException) {
                    throw (LTIException) e;
                }
                log.error("problem looking up existing site: " + e.getMessage(), e);
            }
        }
    }

    protected boolean isSection(String payloadContextId) {

        HttpClient httpClient = new DefaultHttpClient();
        try {
            URI uri = new URI(oaeGroupLookupUrl + payloadContextId + ".json");
            HttpGet httpget = new HttpGet(uri);
            // authenticate to Nakamura using x-sakai-token mechanism
            String token = xSakaiToken.createToken(oaeHostname, oaePrincipal);
            httpget.addHeader(XSakaiToken.X_SAKAI_TOKEN_HEADER, token);
            //
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpClient.execute(httpget, responseHandler);
            String isSection = isSectionJsonPath.read(responseBody);
            if (StringUtils.isNotBlank(isSection)) {
                return Boolean.parseBoolean(isSection);
            }
        } catch (HttpResponseException e) {
            // usually a 404 error - could not find cookie / not valid
            if (log.isDebugEnabled()) {
                log.debug("HttpResponseException: " + e.getMessage() + ": "
                        + e.getStatusCode() + ": " + oaeGroupLookupUrl + payloadContextId + ".json");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return false;

    }


    @Override
    public void afterSiteCreation(Map payload, boolean trustedConsumer, User user, Site oaeLinkedSite) throws LTIException {
        // bypass code all together if not an oae call, since nothing in paylod to id oae call, just add feature
        // disable altogether, not likely two systems will need one cle instance as a provider.
        if (!enabled) {
            log.info("not enabled skipping processing");
            return;
        }

        Site existingSite = null;
        String externalCourseId = getExternalCourseId(payload);
        String oaeLinkedSiteId = null;
        String ltiContextId = (String) payload.get(BasicLTIConstants.CONTEXT_ID);

        if (oaeLinkedSite != null) {
            oaeLinkedSiteId = oaeLinkedSite.getId();
        } else {
            log.error("no oae linked site, halting processing");
            return;
        }

        if (ltiContextId == null) {
            log.error("can't link cle site to oaeLinkedSite=" + oaeLinkedSiteId + " no " +
                    BasicLTIConstants.CONTEXT_ID + " parameter found in payload");
            return;
        }

        if (StringUtils.isEmpty(externalCourseId)) {
            // SIS-2, SIS-3: OAE world exists with no external course id
            // nothing more to do site has been created, don't have an external course id yet
            log.debug(EXTERNAL_COURSE_ID + " not found in payload, no oae SIS data available yet.");
            return;
        }

        try {
            existingSite = siteHelper.findSite(externalCourseId);


            if (existingSite == null) {
                // SIS-1, SIS-5: no existing site in cle with externalCourseId, update property

                log.debug("for " + BasicLTIConstants.CONTEXT_ID + "=" + ltiContextId +
                        ": saving " + SiteHelper.EXTERNAL_SITE_ID + "=" + externalCourseId + " in site=" + oaeLinkedSiteId);
                oaeLinkedSite.getPropertiesEdit().addProperty(SiteHelper.EXTERNAL_SITE_ID, externalCourseId);
                siteService.save(oaeLinkedSite);
                // reload site, otherwise bad stuff happens
                oaeLinkedSite = siteService.getSite(oaeLinkedSite.getId());
                return;
            }

            String existingSiteLtiContextId = existingSite.getProperties().getProperty(SiteHelper.LTI_CONTEXT_ID);

            if (ltiContextId.equals(existingSiteLtiContextId)) {
                // SIS-6:  found existing site and its lti_context_id matches the oae linked site

                // nothing to do life is good, this is just a normal launch
                log.debug("for " + BasicLTIConstants.CONTEXT_ID + "=" + ltiContextId +
                        ": proper CLE linking detected " + SiteHelper.EXTERNAL_SITE_ID + "=" + externalCourseId +
                        " in site=" + oaeLinkedSiteId);
                return;
            } else {
                // SIS-4:  means cle provisioned site ahead of oae linking
                // move memberships from existingSite to the oaeLinkedSite and move external course id flag
                log.debug("for " + BasicLTIConstants.CONTEXT_ID + "=" + ltiContextId +
                        ": moving " + SiteHelper.EXTERNAL_SITE_ID + "=" + externalCourseId + " from oldsite=" +
                        existingSite.getId() + " to newsite=" + oaeLinkedSite.getId());

                try {
                     // bypass security when modifying sites
                    SecurityService.pushAdvisor(new SecurityAdvisor() {
                        public SecurityAdvice isAllowed(String userId, String function, String reference) {
                            return SecurityAdvice.ALLOWED;
                        }
                    });
                    existingSite.getPropertiesEdit().removeProperty(SiteHelper.EXTERNAL_SITE_ID);
                    existingSite.getPropertiesEdit().addProperty("removedExternalCourseId", externalCourseId);
                    siteService.save(existingSite);

                    // reload site, otherwise bad stuff happens
                    existingSite = siteService.getSite(existingSite.getId());


                    oaeLinkedSite.getPropertiesEdit().addProperty(SiteHelper.EXTERNAL_SITE_ID, externalCourseId);
                    siteService.save(oaeLinkedSite);

                    // reload site, otherwise bad stuff happens
                    oaeLinkedSite = siteService.getSite(oaeLinkedSite.getId());

                    moveMemberships(oaeLinkedSite, existingSite);
                } finally {
                     SecurityService.popAdvisor();
                }

            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }


    }

    protected void moveMemberships(Site oaeLinkedSite, Site existingSite) throws IdUnusedException, PermissionException {
        for (Member member : existingSite.getMembers())  {
            oaeLinkedSite.addMember(member.getUserId(), member.getRole().getId(), member.isActive(), member.isProvided());
        }

        // in the unlikely case the site types are different do not attempt to copy memberships, roles will not match
        // in such a case another run of the SIS job would deal with the memberships

        if (oaeLinkedSite.getType().equals(existingSite.getType())) {

            siteService.saveSiteMembership(oaeLinkedSite);

            log.debug("completed saving memberships to oae linked site:" + oaeLinkedSite.getId());

            for (CourseSection section : sectionManager.getSections(existingSite.getReference()) ) {

                CourseSection newSection = sectionManager.addSection(oaeLinkedSite.getReference(), section.getTitle(), section.getCategory(), section.getMaxEnrollments(), "", null, null, false, false, false, false, false, false,false);
                log.debug("added section: " + newSection.getUuid() + " to oae linked site:" + oaeLinkedSite.getId());

                moveExternalSectionIdProperty(oaeLinkedSite, existingSite, newSection);

                moveSectionMemberships(newSection, section);
            }

            for (Member member : existingSite.getMembers())  {
                existingSite.removeMember(member.getUserId());
            }

            siteService.saveSiteMembership(existingSite);
            log.debug("removed memberships from existing site:" + existingSite.getId());

        }
    }

    protected void moveSectionMemberships(CourseSection newSection, CourseSection oldSection) {
        //TODO deal if roles don't match
        for (Iterator<ParticipationRecord> k=sectionManager.getSectionTeachingAssistants(oldSection.getUuid()).iterator();k.hasNext();){
            ParticipationRecord pRecord = k.next();
            try {
                log.debug("adding user: " + pRecord.getUser().getUserUid() + " to section: " + newSection.getUuid() + " as role: " + pRecord.getUser().getUserUid());
                sectionManager.addSectionMembership(pRecord.getUser().getUserUid(), pRecord.getRole(), newSection.getUuid());
            } catch (Exception e) {
                log.error("problem adding section membership", e);
            }
        }

        for (Iterator<EnrollmentRecord> k=sectionManager.getSectionEnrollments(oldSection.getUuid()).iterator();k.hasNext();){
            EnrollmentRecord eRecord = k.next();
            try {
                log.debug("adding user: " + eRecord.getUser().getUserUid() + " to section: " + newSection.getUuid() + " as role: " + eRecord.getUser().getUserUid());
                sectionManager.addSectionMembership(eRecord.getUser().getUserUid(), eRecord.getRole(), newSection.getUuid());
            } catch (Exception e) {
                log.error("problem adding section membership", e);
            }
        }

         sectionManager.disbandSection(oldSection.getUuid());
    }

    protected void moveExternalSectionIdProperty(Site oaeLinkedSite, Site existingSite, CourseSection newSection) throws IdUnusedException, PermissionException {
        //reload site, and set externalSectionid
        oaeLinkedSite = siteService.getSite(oaeLinkedSite.getId());

        String externalSectionId = null;

        for (Iterator<Group> k=existingSite.getGroups().iterator(); k.hasNext(); ) {
            Group group = k.next();
            if (newSection.getUuid().equals(group.getReference())){
                externalSectionId = group.getProperties().getProperty("externalSectionId");
                break;
            }
        }

        if (externalSectionId != null) {
            for (Iterator<Group> k = oaeLinkedSite.getGroups().iterator(); k.hasNext(); ) {
                Group group = k.next();
                if (newSection.getUuid().equals(group.getReference())) {
                    group.getProperties().addProperty("externalSectionId", externalSectionId);
                    log.debug("adding externalSectionId property to group:" + group.getId());
                    siteService.save(oaeLinkedSite);
                    // reload site, otherwise bad stuff happens
                    oaeLinkedSite = siteService.getSite(oaeLinkedSite.getId());
                    break;
                }
            }
        }

    }




    protected String getExternalCourseId(Map payload) {
        return (String) payload.get(EXTERNAL_COURSE_ID);
    }

    protected String getOaeId(Map payload) {
        return (String) payload.get(BasicLTIConstants.CONTEXT_ID);
    }

    public void setSiteHelper(SiteHelper siteHelper) {
        this.siteHelper = siteHelper;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setSectionManager(SectionManager sectionManager) {
        this.sectionManager = sectionManager;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setOaeHostname(String oaeHostname) {
        this.oaeHostname = oaeHostname;
    }

    public void setOaePrincipal(String oaePrincipal) {
        this.oaePrincipal = oaePrincipal;
    }

    public void setOaeGroupLookupUrl(String oaeGroupLookupUrl) {
        oaeGroupLookupUrl = oaeGroupLookupUrl.trim();
        if (!oaeGroupLookupUrl.endsWith("/")) {
            oaeGroupLookupUrl += "/";
        }
        this.oaeGroupLookupUrl = oaeGroupLookupUrl;
    }
}
