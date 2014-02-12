package org.sakaiproject.tool.itunesuadmin;

import java.util.Collection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.util.SortedIterator;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import org.sakaiproject.tool.itunesu.api.*;

import uk.ac.cam.caret.sakai.rsf.producers.FrameAdjustingProducer;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UIOutputMultiline;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.evolvers.TextInputEvolver;
import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import uk.org.ponder.stringutil.StringList;

/**
 * 
 * @author
 *
 */
public class ITunesUAdminProducer implements ViewComponentProducer, DefaultView {
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(ITunesUAdminProducer.class);
	
    public ITunesUAdminHandler handler;
    public static final String VIEW_ID = "ITunesUAdmin";
    public MessageLocator messageLocator;
    public FrameAdjustingProducer frameAdjustingProducer;
    public SiteService siteService = null;

    public String getViewID() {
        return VIEW_ID;
    }
    
    private TargettedMessageList tml;
	public void setTargettedMessageList(TargettedMessageList tml) {
		this.tml = tml;
	}
	
	public SecurityService securityService;
	public void setSecurityService(SecurityService securityService)
	{
		this.securityService = securityService;
	}

    public void fillComponents(UIContainer arg0, ViewParameters arg1, ComponentChecker arg2) {
         
    	if (!securityService.isSuperUser())
    	{
    		UIBranchContainer errorRow = UIBranchContainer.make(arg0,"error-row:", "0");
    		UIMessage.make(errorRow, "error", messageLocator.getMessage("no_permission"));
    	}
    	else
	    {

    		//********** look up form ************/
        	UIForm lookupForm = UIForm.make(arg0, "lookup-form");

        	UIOutput.make(lookupForm, "lookup_instruction", messageLocator.getMessage("lookup_instruction"));
    		
        	//************ delete form ***********/
        	UIForm deleteForm = UIForm.make(arg0, "delete-form");
        	
        	// delete course
        	UIOutput.make(deleteForm, "deleteInstruction", messageLocator.getMessage("delete.instruction"));
        	UIInput.make(deleteForm, "deleteSiteIds", "#{ITunesUAdminHandler.deleteSiteIds}", handler.deleteSiteIds);
        	UIOutput.make(deleteForm, "deleteSiteIdsLabel", messageLocator.getMessage("delete.siteids.label"));

    	    UICommand.make(deleteForm, "delete", messageLocator.getMessage("delete"), "#{ITunesUAdminHandler.processDeleteCourse}");

        	//********* merge form **************/
        	// for source site id of merging 
        	UIOutput.make(lookupForm, "lookup_site_id_label", messageLocator.getMessage("lookup_site_id_label"));
    	    UIInput.make(lookupForm, "lookup_site_id", "#{ITunesUAdminHandler.lookupSiteId}","");
    	    UIOutput.make(lookupForm, "lookup_site_id_2", handler.getLookupSiteId());

    	    UICommand.make(lookupForm, "lookup", messageLocator.getMessage("lookup"), "#{ITunesUAdminHandler.processLookupCourse}");

        	UIOutput.make(lookupForm, "lookup_handle_label_1", messageLocator.getMessage("lookup_handle_label_1"));
        	UIOutput.make(lookupForm, "lookup_handle_label_2", messageLocator.getMessage("lookup_handle_label_2"));

        	UIOutput.make(lookupForm, "lookup_handle", handler.getLookupHandle());

    	    UIForm mergeForm = UIForm.make(arg0, "merge-form");
        	UIOutput.make(mergeForm, "merge_instruction", messageLocator.getMessage("merge_instruction"));
    		
        	// for source site id of merging 
        	UIOutput.make(mergeForm, "source_site_id_label", messageLocator.getMessage("source_site_id_label"));
    	    UIInput.make(mergeForm, "source_site_id", "#{ITunesUAdminHandler.sourceSiteId}","");
    		
    	    // for target site id of merging 
        	UIOutput.make(mergeForm, "target_site_id_label", messageLocator.getMessage("target_site_id_label"));
    	    UIInput.make(mergeForm, "target_site_id", "#{ITunesUAdminHandler.targetSiteId}","");
    		
    	    UICommand.make(mergeForm, "merge", messageLocator.getMessage("merge"), "#{ITunesUAdminHandler.processMergeCourse}");
 
    	    //********** look up orphaned page form ************/
        	UIForm lookupOrphanedPageForm = UIForm.make(arg0, "orphanedPageForm");

        	UIOutput.make(lookupOrphanedPageForm, "findOrphanedPageInstruction", messageLocator.getMessage("lookup_orphaned_page_instruction"));
        	
        	UICommand.make(lookupOrphanedPageForm, "lookupOrphanedPage", messageLocator.getMessage("lookup"), "#{ITunesUAdminHandler.processLookupOrphanedPage}");
        	
        	HashMap<String, String> oMap = handler.getOrphanedPages();
        	List<String> buffer = new Vector<String>();
        	if (oMap != null)
        	{
        		buffer.add("iTunesU Page Handler (site id)");
	        	for(Iterator<String> keys = oMap.keySet().iterator(); keys.hasNext();)
	        	{
	        		String key = keys.next();
	        		String value = oMap.get(key);
	        		buffer.add(key + "(" + value + ")");
	        	}
        	}
        	// construct rsf's StringList
        	StringList sList = new StringList(buffer);
        	UIOutputMultiline.make(lookupOrphanedPageForm,"orphanedPageIds", null, sList);
        	
    	    //********** add feed group form ************/
        	UIForm addFeedGroupForm = UIForm.make(arg0, "addFeedGroupForm");

        	UIOutput.make(addFeedGroupForm, "addFeedGroupInstruction", messageLocator.getMessage("addFeedGroup_instruction"));
        	UIOutput.make(addFeedGroupForm, "addFeedGroup_siteId_label", messageLocator.getMessage("addFeedGroup_siteId_label"));
        	UIInput.make(addFeedGroupForm, "addFeedGroup_siteId", "#{ITunesUAdminHandler.addFeedGroup_siteId}","");
        	UIOutput.make(addFeedGroupForm, "addFeedGroup_name_label", messageLocator.getMessage("addFeedGroup_name_label"));
        	UIInput.make(addFeedGroupForm, "addFeedGroup_name", "#{ITunesUAdminHandler.addFeedGroup_name}","");
        	UIOutput.make(addFeedGroupForm, "addFeedGroup_url_label", messageLocator.getMessage("addFeedGroup_url_label"));
        	UIInput.make(addFeedGroupForm, "addFeedGroup_url", "#{ITunesUAdminHandler.addFeedGroup_url}","");
        	UIOutput.make(addFeedGroupForm, "addFeedGroup_ownerEmail_label", messageLocator.getMessage("addFeedGroup_ownerEmail_label"));
        	UIInput.make(addFeedGroupForm, "addFeedGroup_ownerEmail", "#{ITunesUAdminHandler.addFeedGroup_ownerEmail}","");
        	
        	UICommand.make(addFeedGroupForm, "addFeedGroup", messageLocator.getMessage("addFeedGroup_add"), "#{ITunesUAdminHandler.processAddFeedGroup}");
        	
        	//************************* process any messages *****************/
	         if (tml.size() > 0) {
	 			for (int i = 0; i < tml.size(); i ++ ) {
	 				UIBranchContainer errorRow = UIBranchContainer.make(arg0,"error-row:", new Integer(i).toString());
	 				if (tml.messageAt(i).args != null ) {	    		
	 					UIMessage.make(errorRow,"error",tml.messageAt(i).acquireMessageCode(),(String[])tml.messageAt(i).args[0]);
	 				} else {
	 		    			UIMessage.make(errorRow,"error",tml.messageAt(i).acquireMessageCode());
	 				}
	 		    		
	 			}
	         }
	         
	         frameAdjustingProducer.fillComponents(arg0, "resize", "resetFrame");
    	}
    }
}
