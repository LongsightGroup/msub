package com.rsmart.sakai.providers;

import com.rsmart.sakai.providers.util.UserDisplayQueries;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.ContextualUserDisplayService;
import org.sakaiproject.user.api.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.MissingFormatArgumentException;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Mar 28, 2011
 * Time: 10:25:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class UserPropertyDisplayAdvisor extends BaseContextualUserDisplayService implements ContextualUserDisplayService {
    private static final Log log = LogFactory.getLog(UserPropertyDisplayAdvisor.class);
    protected ToolManager toolManager;
    protected ServerConfigurationService serverConfigurationService;
    private String[] toolsForDisplayId = null;
    private String[] propertyNamesForDisplayId;
    private String formatForDisplayId;
    private String formatForDisplayIdGB;
    private List toolListForDisplayId = new ArrayList();

    private static final String USER_PROPERTY_DISPLAY_ADVISOR_TOOL = "userPropertyDisplayAdvisor.tool";
    private static final String USER_PROPERTY_DISPLAY_ADVISOR_PROPERTY_NAME = "userPropertyDisplayAdvisor.propertyName";
    private static final String USER_PROPERTY_DISPLAY_ADVISOR_FORMAT = "userPropertyDisplayAdvisor.format";

    private String[] toolsForDisplayName = null;
    private String[] propertyNamesForDisplayName;
    private String formatForDisplayName;
    private List toolListForDisplayName = new ArrayList();

    private static final String USER_PROPERTY_DISPLAY_ADVISOR_TOOL_FOR_DISPLAY_NAME = "userPropertyDisplayAdvisor.toolForDisplayName";
    private static final String USER_PROPERTY_DISPLAY_ADVISOR_PROPERTY_NAME_FOR_DISPLAY_NAME = "userPropertyDisplayAdvisor.propertyNameForDisplayName";
    private static final String USER_PROPERTY_DISPLAY_ADVISOR_FORMAT_FOR_DISPLAY_NAME = "userPropertyDisplayAdvisor.formatForDisplayName";
    private static final String USER_PROPERTY_DISPLAY_ADVISOR_FORMAT_FOR_DISPLAY_ID_GB = "userPropertyDisplayAdvisor.formatForDisplayIdGB";

    private UserDisplayQueries userDisplayQueries;
    private SiteService siteService;


    public void init() {
        if (serverConfigurationService.getStrings(USER_PROPERTY_DISPLAY_ADVISOR_TOOL) != null) {
            toolsForDisplayId = serverConfigurationService.getStrings(USER_PROPERTY_DISPLAY_ADVISOR_TOOL);
            toolListForDisplayId.addAll(Arrays.asList(toolsForDisplayId));
        }
        if (serverConfigurationService.getStrings(USER_PROPERTY_DISPLAY_ADVISOR_PROPERTY_NAME) != null) {
            propertyNamesForDisplayId = serverConfigurationService.getStrings(USER_PROPERTY_DISPLAY_ADVISOR_PROPERTY_NAME);
        }
        formatForDisplayId = serverConfigurationService.getString(USER_PROPERTY_DISPLAY_ADVISOR_FORMAT, "%s");

        formatForDisplayIdGB = serverConfigurationService.getString(USER_PROPERTY_DISPLAY_ADVISOR_FORMAT_FOR_DISPLAY_ID_GB, "%s");

        if (serverConfigurationService.getStrings(USER_PROPERTY_DISPLAY_ADVISOR_TOOL_FOR_DISPLAY_NAME) != null) {
            toolsForDisplayName = serverConfigurationService.getStrings(USER_PROPERTY_DISPLAY_ADVISOR_TOOL_FOR_DISPLAY_NAME);
            toolListForDisplayName.addAll(Arrays.asList(toolsForDisplayName));
        }
        if (serverConfigurationService.getStrings(USER_PROPERTY_DISPLAY_ADVISOR_PROPERTY_NAME_FOR_DISPLAY_NAME) != null) {
            propertyNamesForDisplayName = serverConfigurationService.getStrings(USER_PROPERTY_DISPLAY_ADVISOR_PROPERTY_NAME_FOR_DISPLAY_NAME);
        }
        formatForDisplayName = serverConfigurationService.getString(USER_PROPERTY_DISPLAY_ADVISOR_FORMAT_FOR_DISPLAY_NAME, "%s");


    }

    public String getUserDisplayId(User user) {
        ResourceProperties props = user.getProperties();
        if (toolsForDisplayId != null && toolsForDisplayId.length != 0 && currentToolIsSelectedForDisplayId()) {
            if (props != null) {
                String[] args = new String[propertyNamesForDisplayId.length];

                for (int i = 0; i < propertyNamesForDisplayId.length; i++) {
                    String propertyName = propertyNamesForDisplayId[i];
                    Object propertyValue = props.get(propertyName);
                    if (propertyValue != null && propertyValue instanceof String) {
                        args[i] = (String) propertyValue;
                    } else {
                        args[i] = "";
                    }
                }

                if (serverConfigurationService.getBoolean("grade.basis.appended", false)) {
                    return appendGradeBasis(args, user);
                } else {
                    return String.format(formatForDisplayId, args);
                }
            }
        }

        return user.getEid();
    }

    protected String appendGradeBasis(String[] args, User user) {
        /**
         * This code appends Grade Basis to the user display ID column
         * grade.basis.appended set to true in the local.properties file
         */
        Site site = null;
        String gradeBasisValue = null;
        String appendedGradeBasis = null;
        String formatStringReturn = null;

        String siteId = toolManager.getCurrentPlacement().getContext();
        if (siteId == null) {
            log.debug("Can not retrieve site id from tool manager");
        }

        try {
            site = siteService.getSite(siteId);
            if (site == null) {
                log.debug("Attempting to retrieve site object from site service and it failed on" + siteId);
            }
        } catch (IdUnusedException e) {
            log.debug("Site id: " + siteId + " is null for UserID: " + user.getEid(), e);
        }
        gradeBasisValue = userDisplayQueries.getEnrollmentSections(siteId, user.getEid());
        if (gradeBasisValue == null) {
            log.info("There is no grading scheme for User Id" + user.getEid());

        } else {
            int index = formatForDisplayIdGB.lastIndexOf(String.valueOf(formatForDisplayIdGB.charAt(formatForDisplayIdGB.length() - 1)));

            appendedGradeBasis = formatForDisplayIdGB.substring(0, index + 1);
            if (appendedGradeBasis == null) {
                log.debug("The appendedGradeBasis is null, please check the display format in the properties file.");
                return user.getEid();
            }

            log.debug("The format being used in the UI from the properties file" + appendedGradeBasis);
            // appendedGradeBasis = formatForDisplayId.substring(0,  formatForDisplayId.length());
            String[] finalArgs = combineArrays(args, gradeBasisValue);

            log.debug("Grading Scheme for" + user.getEid() + ":" + gradeBasisValue + "in site " + siteId);
            try {
                formatStringReturn = String.format(appendedGradeBasis, finalArgs);
            } catch (MissingFormatArgumentException e) {
                log.debug("The same number of '%s' in the format string,  the String Array needs the same amount of arguements", e);
            }
            return formatStringReturn == null ? "" : formatStringReturn;
        }

        return String.format(formatForDisplayId, args);
    }


    private  String[] combineArrays(String[] args, String gradeBasis) {
        String[] finalArray = new String[args.length + 1];
        int i = 0;
        for (; i < args.length; i++) {
            finalArray[i] = args[i];
        }
        finalArray[i++] = gradeBasis;
        return finalArray;
    }

    public String getUserDisplayName(User user) {
        ResourceProperties props = user.getProperties();
        if (toolsForDisplayName != null && toolsForDisplayName.length != 0 && currentToolIsSelectedForDisplayName() ) {
            if (props != null) {
                String[] args = new String[propertyNamesForDisplayName.length];
                for (int i=0; i< propertyNamesForDisplayName.length;i++){
                    String propertyName = propertyNamesForDisplayName[i];
                    Object propertyValue = props.get(propertyName);
                    if (propertyValue != null && propertyValue instanceof String) {
                        args[i] = (String) propertyValue;
                    } else {
                        args[i] = "";
                    }
                }
                return String.format(formatForDisplayName, args);
            }
        }

        return super.getUserDisplayName(user);
    }


    private boolean currentToolIsSelectedForDisplayId() {
        if (toolManager.getCurrentTool() == null) {
            return false;
        }
        return toolListForDisplayId.contains(toolManager.getCurrentTool().getId());
    }

    private boolean currentToolIsSelectedForDisplayName() {
        if (toolManager.getCurrentTool() == null) {
            return false;
        }
        return toolListForDisplayName.contains(toolManager.getCurrentTool().getId());
    }



    public ToolManager getToolManager() {
        return toolManager;
    }

    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }

    public ServerConfigurationService getServerConfigurationService() {
        return serverConfigurationService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setUserDisplayQueries(UserDisplayQueries userDisplayQueries) {
        this.userDisplayQueries = userDisplayQueries;
    }

}
