package com.gradebook2.export.institutional.advisor;

import com.gradebook2.export.institutional.advisor.util.GradebookExportData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.gradebook.gwt.client.model.FinalGradeSubmissionResult;
import org.sakaiproject.gradebook.gwt.client.model.FinalGradeSubmissionStatus;
import org.sakaiproject.gradebook.gwt.sakai.InstitutionalAdvisor;
import org.sakaiproject.gradebook.gwt.sakai.model.UserDereference;
import org.sakaiproject.gradebook.gwt.server.model.FinalGradeSubmissionResultImpl;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;


public class GradebookExportPublisher extends JmsTemplate implements InstitutionalAdvisor {
    private static final Log log = LogFactory.getLog(PacificInstitutionalAdvisor.class);

    protected ObjectMapper mapper = new ObjectMapper();
    private ServerConfigurationService serverConfigurationService;
    private SiteService siteService;
    private UserDirectoryService userDirectoryService;
    private ToolManager toolManager;

    public List<String> getExportCourseManagementSetEids(Group group) {
         if (null == group) {
            log.error("ERROR : Group is null");
            return null;
        }
        if (null == group.getProviderGroupId()) {
            log.warn("Group Provider Id is null");
            return null;
        }
        return Arrays.asList(group.getProviderGroupId().split("\\+"));
    }

    public String getExportCourseManagementId(String userEid, Group group, List<String> enrollmentSetEids) {
         if (null == group) {
            log.error("ERROR : Group is null");
            return null;
        }

        if (null == group.getContainingSite()) {
            log.warn("Containing site is null");
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(group.getContainingSite().getTitle());
        stringBuilder.append(" : ");
        stringBuilder.append(group.getTitle());

        return stringBuilder.toString();
    }

    public String getExportUserId(UserDereference dereference) {
        return dereference.getEid();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getFinalGradeUserId(UserDereference dereference) {
        return dereference.getEid();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] getLearnerRoleNames() {
        String[] roleKeys = {"Student", "Open Campus", "access"};
        return roleKeys;
    }

    public boolean isExportCourseManagementIdByGroup() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

	public FinalGradeSubmissionResult submitFinalGrade(List<Map<Column, String>> studentDataList,
			String gradebookUid) {
        FinalGradeSubmissionResult finalGradeSubmissionResult = new FinalGradeSubmissionResultImpl();
		GradebookExportData data = new GradebookExportData();
        data.setGradebookId(gradebookUid);
        data.setServerId(serverConfigurationService.getServerId());

        if (toolManager.getCurrentPlacement() != null) {
            String siteId = toolManager.getCurrentPlacement().getContext();
            data.setSiteId(siteId);

            data.setGradebookToolPlacementId(toolManager.getCurrentPlacement().getId());
            try {
                Site site = siteService.getSite(siteId);
                data.setSiteTitle(site.getTitle());
                data.setSiteDescription(site.getDescription());
                data.setSiteShortDescription(site.getShortDescription());

                for (Iterator i = site.getProperties().getPropertyNames();i.hasNext();){
                    String propName = (String) i.next();
                    String propValue = (String) site.getProperties().get(propName);
                    data.getSiteProperties().put(propName, propValue);
                }
                Set<String> instIds = site.getUsersIsAllowed("section.role.instructor");

                //the site could contain references to deleted users
                List<User> activeUsers = userDirectoryService.getUsers(instIds);
                Set<String> ret = new HashSet<String>();
                for (int i = 0; i < activeUsers.size(); i++) {
                    User user = activeUsers.get(i);
                    Map<String, String> userProperties = new HashMap();
                    userProperties.put("email", user.getEmail());
                    userProperties.put("eid", user.getEid());
                    userProperties.put("displayName", user.getDisplayName());
                    userProperties.put("lastName", user.getLastName());
                    userProperties.put("firstName", user.getFirstName());
                    userProperties.put("userId", user.getId());

                    for (Iterator userPropertiesIterator = user.getProperties().getPropertyNames();userPropertiesIterator.hasNext();){
                        String propName = (String) userPropertiesIterator.next();
                        String propValue = (String) site.getProperties().get(propName);
                        userProperties.put(propName, propValue);
                    }

                    data.getInstructorList().put(user.getId(), userProperties);
                }

            } catch (IdUnusedException e) {
                logger.error("can't find site with id: " + siteId, e);
            }
        }
        User user = userDirectoryService.getCurrentUser();
        data.setInstructorEid(user.getEid());
        data.setInstructorEmail(user.getEmail());
        data.setInstructorUid(user.getId());
        data.setSubmissionDate(new Date());
        data.setServerUrl(serverConfigurationService.getServerUrl());
		//create JMS Que and Send it away to Mule
		sendGradebookExportMessage(data);

        return finalGradeSubmissionResult;
	}

    public String getDisplaySectionId(String sectionEid) {
        return "DisplayId for eid: " + sectionEid;
    }

    public String getPrimarySectionEid(List<String> eids) {
        if (null == eids || eids.isEmpty()) {
            return "";
        } else {
            return eids.get(0);
        }
    }

    public FinalGradeSubmissionStatus hasFinalGradeSubmission(String gradebookUid, boolean hasFinalGradeSubmission) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isValidOverrideGrade(String grade, String learnerEid, String learnerDisplayId, Gradebook gradebook, Set<String> scaledGrades) {
         if (scaledGrades.contains(grade))
            return true;

        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }



    protected void sendGradebookExportMessage(final GradebookExportData data)
    {
        log.debug("attempting to send jms message to " + this.getDefaultDestinationName());
        send(getDefaultDestinationName(),
                new MessageCreator() {
                    public Message createMessage(Session session) throws JMSException {
                        TextMessage message = session.createTextMessage();
                        String jsonMessage = MarshallToJson(data);

                        message.setText(jsonMessage);
                        return message;
                    }
                });
    }

    protected String MarshallToJson(GradebookExportData data)
    {
        StringWriter writer = new StringWriter();
        try {
            mapper.writeValue(writer, data);
        } catch (IOException e) {
            logger.error("problem marshalling gb2 export data into json" , e);
        }
        log.debug("marshalling message body into: " + writer.toString());
        return writer.toString();
    }


    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }
}