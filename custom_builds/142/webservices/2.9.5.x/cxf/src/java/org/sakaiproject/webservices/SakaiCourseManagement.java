package org.sakaiproject.webservices;


import com.rsmart.customer.integration.cm.CrossListingHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseManagementAdministration;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitemanage.api.SiteInfoComposer;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 9/19/11
 * Time: 10:21 AM
 * To change this template use File | Settings | File Templates.
 */

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
public class SakaiCourseManagement extends AbstractWebService {
    private static final Log LOG = LogFactory.getLog(SakaiCourseManagement.class);

    protected CourseManagementService cmService;
    protected CourseManagementAdministration cmAdmin;
    private GroupProvider groupProvider;
    private SiteInfoComposer siteInfoComposer;
    private CrossListingHelper crossListingHelper;

    @WebMethod
    @Path("/getTermName")
    @Produces("text/plain")
    @GET
    public String getTermName(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "termEid", partName = "termEid") @QueryParam("termEid") String termEid) {
        Session s = establishSession(sessionid);
        List<AcademicSession> academicSessions = cmService.getAcademicSessions();

        for (AcademicSession academicSession : academicSessions) {
            //we found a matching academicSession, success
            if (academicSession.getEid().equals(termEid)) {
                return academicSession.getTitle();
            }
        }
        LOG.warn("can't find a matching term with eid=[" + termEid + "] check the sakai.properties files for proper term configuration");
        return null;

    }


    @WebMethod
    @Path("/getCurrentTerms")
    @Produces("text/plain")
    @GET
    public String getCurrentTerms(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid) {
        Session s = establishSession(sessionid);

        Document dom = Xml.createDocument();
        Node list = dom.createElement("list");
        dom.appendChild(list);

        try {
            List academicSessions = cmService.getCurrentAcademicSessions();
            if (academicSessions != null && academicSessions.size() > 0) {
                for (Iterator i = academicSessions.iterator(); i.hasNext(); ) {
                    AcademicSession as = (AcademicSession) i.next();
                    Node item = dom.createElement("academicSession");
                    Node eId = dom.createElement("eid");
                    eId.appendChild(dom.createTextNode(as.getEid()));
                    Node title = dom.createElement("title");
                    title.appendChild(dom.createTextNode(as.getTitle()));
                    Node description = dom.createElement("description");
                    description.appendChild(dom.createTextNode(as.getDescription()));
                    Node startDate = dom.createElement("startDate");
                    startDate.appendChild(dom.createTextNode(as.getStartDate().toString()));
                    Node endDate = dom.createElement("endDate");
                    endDate.appendChild(dom.createTextNode(as.getEndDate().toString()));
                    Node sortOrder = dom.createElement("sortOrder");
                    sortOrder.appendChild(dom.createTextNode(as.getSortOrder().toString()));

                    item.appendChild(eId);
                    item.appendChild(title);
                    item.appendChild(description);
                    item.appendChild(startDate);
                    item.appendChild(endDate);
                    item.appendChild(sortOrder);
                    list.appendChild(item);
                }
            }
            return Xml.writeDocumentToString(dom);
        } catch (Exception e) {
            LOG.error("WS getTerms(): " + e.getClass().getName() + " : " + e.getMessage(), e);
            return null;
        }
    }

    @WebMethod
    @Path("/getAllTerms")
    @Produces("text/plain")
    @GET
    public String getAllTerms(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid) {
        Session s = establishSession(sessionid);

        Document dom = Xml.createDocument();
        Node list = dom.createElement("list");
        dom.appendChild(list);

        try {
            List academicSessions = cmService.getAcademicSessions();
            if (academicSessions != null && academicSessions.size() > 0) {
                Date now = new Date();
                for (Iterator i = academicSessions.iterator(); i.hasNext(); ) {
                    AcademicSession as = (AcademicSession) i.next();
                    Node item = dom.createElement("academicSession");
                    Node eId = dom.createElement("eid");
                    eId.appendChild(dom.createTextNode(as.getEid()));
                    Node title = dom.createElement("title");
                    title.appendChild(dom.createTextNode(as.getTitle()));
                    Node description = dom.createElement("description");
                    description.appendChild(dom.createTextNode(as.getDescription()));
                    Node current = dom.createElement("current");
                    current.appendChild(dom.createTextNode(String.valueOf(as.getStartDate().before(now) && as.getEndDate().after(now))));
                    Node startDate = dom.createElement("startDate");
                    startDate.appendChild(dom.createTextNode(as.getStartDate().toString()));
                    Node endDate = dom.createElement("endDate");
                    endDate.appendChild(dom.createTextNode(as.getEndDate().toString()));
                    item.appendChild(eId);
                    item.appendChild(title);
                    item.appendChild(description);
                    item.appendChild(current);
                    item.appendChild(startDate);
                    item.appendChild(endDate);
                    list.appendChild(item);
                }
            }
            return Xml.writeDocumentToString(dom);
        } catch (Exception e) {
            LOG.error("WS getAllTerms(): " + e.getClass().getName() + " : " + e.getMessage(), e);
            return null;
        }
    }

    @WebMethod
    @Path("/deleteAcademicSession")
    @Produces("text/plain")
    @GET
    public boolean deleteAcademicSession(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid) {
        Session s = establishSession(sessionid);
        if (!securityService.isSuperUser(s.getUserId())) {
            LOG.warn("NonSuperUser trying to delete academicSession: " + s.getUserId());
            throw new RuntimeException("NonSuperUser trying to delete academicSession: " + s.getUserId());
        }
        try {
            cmAdmin.removeAcademicSession(eid);
            return true;
        } catch (Exception e) {
            LOG.error("WS deleteAcademicSession(): " + e.getClass().getName() + " : " + e.getMessage(), e);
            return false;
        }
    }

    @WebMethod
    @Path("/updateSortOrderForAcademicSession")
    @Produces("text/plain")
    @GET
    public boolean updateSortOrderForAcademicSession(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "sortOrder", partName = "sortOrder") @QueryParam("sortOrder") int sortOrder) {
        Session s = establishSession(sessionid);
        try {
            AcademicSession as = cmService.getAcademicSession(eid);
            as.setSortOrder(sortOrder);
            cmAdmin.updateAcademicSession(as);
            return true;
        } catch (Exception e) {
            LOG.error("WS updateSortOrderForAcademicSession(): " + e.getClass().getName() + " : " + e.getMessage(), e);
            return false;
        }

    }

    @WebMethod
    @Path("/saveAcademicSession")
    @Produces("text/plain")
    @GET
    public boolean saveAcademicSession(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "title", partName = "title") @QueryParam("title") String title,
            @WebParam(name = "description", partName = "description") @QueryParam("description") String description,
            @WebParam(name = "startDate", partName = "startDate") @QueryParam("startDate") String startDate,
            @WebParam(name = "endDate", partName = "endDate") @QueryParam("endDate") String endDate) {

        Session s = establishSession(sessionid);
        if (!securityService.isSuperUser(s.getUserId())) {
            LOG.warn("NonSuperUser trying to delete academicSession: " + s.getUserId());
            throw new RuntimeException("NonSuperUser trying to delete academicSession: " + s.getUserId());
        }
        try {
            DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
            AcademicSession as = cmAdmin.createAcademicSession(eid, title, description, dateformat.parse(startDate),
                    dateformat.parse(endDate));
            LOG.error("saved academic session" + as.getEid());
            setCurrentStatus(as);
            return true;

        } catch (Exception e) {
            LOG.error("WS saveAcademicSessions(): " + e.getClass().getName() + " : " + e.getMessage(), e);
            return false;
        }
    }

    @WebMethod
    @Path("/updateAcademicSession")
    @Produces("text/plain")
    @GET
    public boolean updateAcademicSession(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "title", partName = "title") @QueryParam("title") String title,
            @WebParam(name = "description", partName = "description") @QueryParam("description") String description,
            @WebParam(name = "startDate", partName = "startDate") @QueryParam("startDate") String startDate,
            @WebParam(name = "endDate", partName = "endDate") @QueryParam("endDate") String endDate) {
        Session s = establishSession(sessionid);
        DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            AcademicSession as = cmService.getAcademicSession(eid);
            as.setTitle(title);
            as.setDescription(description);
            as.setStartDate(dateformat.parse(startDate));
            as.setEndDate(dateformat.parse(endDate));
            cmAdmin.updateAcademicSession(as);
            setCurrentStatus(as);
            return true;
        } catch (Exception e) {
            LOG.error("WS updateAcademicSessions(): " + e.getClass().getName() + " : " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * return xml of sites found that have the section associated with the given sectionEid
     *
     * @param sessionid
     * @param sectionEid
     * @return
     * @
     */
    @WebMethod
    @Path("/findExistingSitesForSection")
    @Produces("text/plain")
    @GET
    public String findExistingSitesForSection(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "sectionEid", partName = "sectionEid") @QueryParam("sectionEid") String sectionEid) {
        LOG.debug("findExistingSitesForSection(" + sectionEid + "," + sectionEid + ")");
        Session s = establishSession(sessionid);
        Set authzGroupIds = authzGroupService.getAuthzGroupIds(sectionEid);
        if (authzGroupIds == null || authzGroupIds.isEmpty()) {
            return null;
        }

        Document dom = Xml.createDocument();
        Node list = dom.createElement("list");
        dom.appendChild(list);

        for (Object siteRef : authzGroupIds) {
            String siteId = ((String) siteRef).replaceFirst("/site/", "");
            try {
                Site site = siteService.getSite(siteId);
                if (site != null) {
                    Node item = dom.createElement("site");
                    Node siteIdNode = dom.createElement("siteId");
                    siteIdNode.appendChild(dom.createTextNode(site.getId()));
                    Node siteTitle = dom.createElement("siteTitle");
                    siteTitle.appendChild(dom.createTextNode(site.getTitle()));
                    item.appendChild(siteIdNode);
                    item.appendChild(siteTitle);
                    if (site.getProperties() != null) {
                        for (Iterator j = site.getProperties().getPropertyNames(); j.hasNext(); ) {
                            String name = (String) j.next();
                            Node siteProperty = dom.createElement(name);
                            siteProperty.appendChild(dom.createTextNode((String) site.getProperties().get(name)));
                            item.appendChild(siteProperty);
                        }
                    }
                    list.appendChild(item);
                }
            } catch (IdUnusedException e) {
                LOG.debug(siteId + " is not a siteId, which means its probably a group, ignoring");
            }
        }

        return Xml.writeDocumentToString(dom);

    }

    private void setCurrentStatus(AcademicSession session) {
        try {
            List<AcademicSession> currentSessions = cmService.getCurrentAcademicSessions();
            List<String> currentTerms = new ArrayList<String>();

            // initialize the array with the current sessions
            for (AcademicSession s : currentSessions) {
                currentTerms.add(s.getEid());
            }

            // add this session if its end date is after today
            if (session.getEndDate().after(new Date())) {
                if (!currentTerms.contains(session.getEid().toString())) {
                    currentTerms.add(session.getEid());
                }
                // otherwise remove this session
            } else {
                if (currentTerms.contains(session.getEid().toString())) {
                    currentTerms.remove(session.getEid().toString());
                }
            }

            cmAdmin.setCurrentAcademicSessions(currentTerms);
        } catch (Exception e) {
            LOG.error("WS setCurrentStatus(): " + e.getClass().getName() + " : " + e.getMessage(), e);
        }
    }

    @WebMethod
    @Path("/updateSiteTitle")
    @Produces("text/plain")
    @GET
    public String updateSiteTitle(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteId", partName = "siteId") @QueryParam("siteId") String siteId) {
        LOG.debug("updateSiteTitle(" + sessionid + "," + siteId + ")");
        Session s = establishSession(sessionid);
        try {
            Site site = siteService.getSite(siteId);
            siteInfoComposer.updateSiteTitle(site);
            return site.getTitle();
        } catch (IdUnusedException e) {
            LOG.debug("can't find site with id:" + siteId);
            return "site not found";
        }
    }


    @WebMethod
    @Path("/addSectionToSite")
    @Produces("text/plain")
    @GET
    public String addSectionToSite(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "sectionEid", partName = "sectionEid") @QueryParam("sectionEid") String sectionEid,
            @WebParam(name = "siteId", partName = "siteId") @QueryParam("siteId") String siteId) {
        LOG.debug("addSectionToSite(" + sessionid + "," + sectionEid + "," + siteId + ")");
        Session s = establishSession(sessionid);

        try {
            String realm = siteService.siteReference(siteId);
            AuthzGroup realmEdit = authzGroupService.getAuthzGroup(realm);
            String providerRealm = addToExternalRealm(sectionEid, StringUtils.trimToNull(realmEdit.getProviderGroupId()));
            realmEdit.setProviderGroupId(providerRealm);
            authzGroupService.save(realmEdit);
        } catch (Exception e) {
            throw new RuntimeException("", e);
        }

        return "success";
    }

    @WebMethod
    @Path("/getProviders")
    @Produces("text/plain")
    @GET
    public String getProviders(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid) {

        LOG.debug("getProviders(" + sessionid + "," + siteid + ")");
        Session s = establishSession(sessionid);

        Document dom = Xml.createDocument();
        Node providersNode = dom.createElement("providers");
        dom.appendChild(providersNode);

        try {
            String site = siteService.siteReference(siteid);
            Set<String> providers = authzGroupService.getProviderIds(site);

            for (String provider : providers) {
                Node node = dom.createElement("provider");
                node.appendChild(dom.createTextNode(provider));
                providersNode.appendChild(node);
            }

            return Xml.writeDocumentToString(dom);
        } catch (Exception e) {
            LOG.error("WS getProviders(): " + e.getClass().getName() + " : " + e.getMessage(), e);
            return null;
        }
    }

    @WebMethod
    @Path("/getSection")
    @Produces("text/plain")
    @GET
    public String getSection(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "sectionEid", partName = "sectionEid") @QueryParam("sectionEid") String sectionEid) {
        Session s = establishSession(sessionid);

        Document dom = Xml.createDocument();
        Node sectionNode = dom.createElement("section");
        dom.appendChild(sectionNode);

        try {
            if (cmService.isSectionDefined(sectionEid)) {
                Section section = cmService.getSection(sectionEid);
                Node eid = dom.createElement("eid");
                eid.appendChild(dom.createTextNode(section.getEid()));
                sectionNode.appendChild(eid);
                Node title = dom.createElement("title");
                title.appendChild(dom.createTextNode(section.getTitle()));
                sectionNode.appendChild(title);
                Node description = dom.createElement("description");
                description.appendChild(dom.createTextNode(section.getDescription()));
                sectionNode.appendChild(description);
                Node courseOfferingEid = dom.createElement("courseOfferingEid");
                courseOfferingEid.appendChild(dom.createTextNode(section.getCourseOfferingEid()));
                sectionNode.appendChild(courseOfferingEid);
                Node category = dom.createElement("category");
                category.appendChild(dom.createTextNode(section.getCategory()));
                sectionNode.appendChild(category);
                Node maxSize = dom.createElement("maxSize");
                maxSize.appendChild(dom.createTextNode(String.valueOf(section.getMaxSize())));
                sectionNode.appendChild(maxSize);
                Node authority = dom.createElement("authority");
                authority.appendChild(dom.createTextNode(String.valueOf(section.getAuthority())));
                sectionNode.appendChild(authority);


                sectionNode.appendChild(getCourseOfferingNode(section.getCourseOfferingEid(), dom));

            }

            return Xml.writeDocumentToString(dom);
        } catch (Exception e) {
            LOG.error("WS getSection(): " + e.getClass().getName() + " : " + e.getMessage(), e);
            return null;
        }
    }

    private String getCrossListId(String eid) {
        return crossListingHelper.getCrossListingId(eid);
    }

    private Node getCourseOfferingNode(String courseOfferingEid, Document dom) {
        Node coNode = dom.createElement("courseOffering");

        if (cmService.isCourseOfferingDefined(courseOfferingEid)) {
            CourseOffering co = cmService.getCourseOffering(courseOfferingEid);
            Node eid = dom.createElement("eid");
            eid.appendChild(dom.createTextNode(co.getEid()));
            coNode.appendChild(eid);
            Node title = dom.createElement("title");
            title.appendChild(dom.createTextNode(co.getTitle()));
            coNode.appendChild(title);
            Node description = dom.createElement("description");
            title.appendChild(dom.createTextNode(co.getDescription()));
            coNode.appendChild(description);
            Node endDate = dom.createElement("endDate");
            endDate.appendChild(dom.createTextNode(String.valueOf(co.getEndDate().toString())));
            coNode.appendChild(endDate);
            Node startDate = dom.createElement("startDate");
            startDate.appendChild(dom.createTextNode(String.valueOf(co.getStartDate().toString())));
            coNode.appendChild(startDate);
            coNode.appendChild(getTermNode(co.getAcademicSession(), dom));
            Node crossListing = dom.createElement("crossListing");
            crossListing.appendChild(dom.createTextNode(getCrossListId(courseOfferingEid)));
            coNode.appendChild(crossListing);

        }
        return coNode;
    }

    protected Node getTermNode(AcademicSession term, Document dom) {
        Date now = new Date();
        Node item = dom.createElement("academicSession");
        Node eId = dom.createElement("eid");
        eId.appendChild(dom.createTextNode(term.getEid()));
        Node title = dom.createElement("title");
        title.appendChild(dom.createTextNode(term.getTitle()));
        Node description = dom.createElement("description");
        description.appendChild(dom.createTextNode(term.getDescription()));
        Node current = dom.createElement("current");
        current.appendChild(dom.createTextNode(String.valueOf(term.getStartDate().before(now) && term.getEndDate().after(now))));
        Node startDate = dom.createElement("startDate");
        startDate.appendChild(dom.createTextNode(term.getStartDate().toString()));
        Node endDate = dom.createElement("endDate");
        endDate.appendChild(dom.createTextNode(term.getEndDate().toString()));
        item.appendChild(eId);
        item.appendChild(title);
        item.appendChild(description);
        item.appendChild(current);
        item.appendChild(startDate);
        item.appendChild(endDate);
        return item;
    }

    @WebMethod
    @Path("/deleteSectionFromSite")
    @Produces("text/plain")
    @GET
    public String deleteSectionFromSite(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "sectionEid", partName = "sectionEid") @QueryParam("sectionEid") String sectionEid,
            @WebParam(name = "siteId", partName = "siteId") @QueryParam("siteId") String siteId) {
        LOG.debug("deleteSectionFromSite(" + sectionEid + "," + sectionEid + "," + siteId + ")");
        Session s = establishSession(sessionid);

        try {
            //String realm = siteService.siteReference(siteId);
            //AuthzGroup realmEdit = authzGroupService.getAuthzGroup(realm);
            //String providerRealm = removeFromExternalRealm(sectionEid, StringUtils.trimToNull(realmEdit.getProviderGroupId()));
            //realmEdit.setProviderGroupId(providerRealm);
            //authzGroupService.save(realmEdit);
            Site site = siteService.getSite(siteId);
            for (Group group : site.getGroups()) {
                if (group.getId().equals(sectionEid)) {
                    site.removeGroup(group);
                }
            }
            siteService.save(site);

        } catch (Exception e) {
            throw new RuntimeException("", e);
        }

        return "success";
    }

    private String removeFromExternalRealm(String sectionEid, String existingProviderIdString) {
        return buildExternalRealm(sectionEid, existingProviderIdString, false);
    }

    private String addToExternalRealm(String sectionEid, String existingProviderIdString) {
        return buildExternalRealm(sectionEid, existingProviderIdString, true);
    }


    private String buildExternalRealm(String sectionEid, String existingProviderIdString, boolean add) {

        List<String> allProviderIdList = new ArrayList<String>();

        if (existingProviderIdString != null) {
            allProviderIdList.addAll(Arrays.asList(groupProvider.unpackId(existingProviderIdString)));
        }

        // update the list with newly added provider
        if (add) {
            allProviderIdList.add(sectionEid);
        } else {
            allProviderIdList.remove(sectionEid);
        }

        if (allProviderIdList == null || allProviderIdList.size() == 0)
            return null;

        String[] providers = new String[allProviderIdList.size()];
        providers = (String[]) allProviderIdList.toArray(providers);

        String providerId = groupProvider.packId(providers);
        return providerId;

    } // buildExternalRealm

    @WebMethod(exclude = true)
    public void setCmService(CourseManagementService cmService) {
        this.cmService = cmService;
    }

    @WebMethod(exclude = true)
    public void setCmAdmin(CourseManagementAdministration cmAdmin) {
        this.cmAdmin = cmAdmin;
    }

    @WebMethod(exclude = true)
    public void setGroupProvider(GroupProvider groupProvider) {
        this.groupProvider = groupProvider;
    }

    @WebMethod(exclude = true)
    public void setSiteInfoComposer(SiteInfoComposer siteInfoComposer) {
        this.siteInfoComposer = siteInfoComposer;
    }

    @WebMethod(exclude = true)
    public void setCrossListingHelper(CrossListingHelper crossListingHelper) {
        this.crossListingHelper = crossListingHelper;
    }
}