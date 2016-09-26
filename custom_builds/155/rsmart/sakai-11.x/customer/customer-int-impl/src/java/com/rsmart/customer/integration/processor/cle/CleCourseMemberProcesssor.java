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

import java.util.*;

import com.rsmart.customer.integration.processor.ProcessorState;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.component.cover.ComponentManager;

import com.rsmart.customer.integration.processor.ServiceProcessor;
import com.rsmart.customer.integration.dao.CleMembershipDao;
import com.rsmart.customer.integration.model.CleMembership;
import com.rsmart.customer.integration.util.SiteHelper;
import com.rsmart.customer.integration.MembershipFilter;

/**
 * CLE Course Member Processor
 *
 * @author dhelbert
 * @version $Revision$ $Date$
 */
public class CleCourseMemberProcesssor extends ServiceProcessor {

	/** Log */
	private static final Log logger = LogFactory.getLog(CleCourseMemberProcesssor.class);

	/** Site Service */
	private SiteService siteService;

	/** User Service */
	private UserDirectoryService uds;

	/** CLE Membership Dao */
	private CleMembershipDao cleMembershipDao;

	/** Remove Role List */
	private List <String>removeRoles = new ArrayList<String>();

    private String roles;

    private SiteHelper siteHelper;

   private Boolean extendedReporting = false;
   /**
    * optional array of reporting lines, based in extendedReporting property
    */
	private List <ExtendedReportingLine>extendedReportingLines;

   private class ExtendedReportingLine {
      String firstName;
      String lastName;
      String eid;
      String email;
      String siteId;
      String role;
      String notes;
      String nullSafe(String s) {
         if(s != null) return s;
         return "";
      }
/*      public ExtendedReportingLine(String f, String l, String eid, String email, String siteId, String role) {
         firstName=f; lastName=l; this.eid=eid; this.email=email; this.siteId=siteId; this.role=role;
      }
      */
      @Override
      public String toString() {
         String reportEmail = nullSafe(email);
         if(reportEmail.length()==0) {
            reportEmail = "No e-mail address available";
         }
         return nullSafe(firstName) + "\t" +
                 nullSafe(lastName) + "\t" +
                 nullSafe(eid) + "\t" +
                 reportEmail + "\t" +
                 nullSafe(siteId) + "\t" +
                 nullSafe(role) + "\t" +
                 nullSafe(notes);
      }
      public void setTitles() {
         this.firstName = "First Name"; this.lastName = "Last Name";
         this.eid = "Login ID"; this.email = "Email";
         this.siteId = "Site ID"; this.role = "Role"; this.notes = "Comments";
      }
      public void setFirstLastEidEmailSiteRole(String firstName, String lastName, String eid,
                                               String email, String siteId, String role) {
         this.firstName = firstName;
         this.lastName = lastName;
         this.eid = eid;
         this.email = email;
         this.siteId = siteId;
         this.role = role;
      }
   }

   /**
	 * Get Title
	 *
	 * @return String
	 */
	public String getProcessorTitle() {
		return "CLE Course Member Processor";
	}

    public ProcessorState init(Map config) {

        ProcessorState
            state = super.init(config);

        if (getRoles() != null && getRoles().length() > 0) {
            String[] items = roles.split(",");
            removeRoles.addAll(Arrays.asList(items));
        }
       if(isExtendedReporting()) {
          extendedReportingLines = new ArrayList<ExtendedReportingLine>();
          extendedReportingLines.add(createReportTitleLine("Begin Extended Membership Processing Report","\n"));
          ExtendedReportingLine titles = new ExtendedReportingLine();
          titles.setTitles();
          extendedReportingLines.add(titles);
       }

        return state;
    }

   protected ExtendedReportingLine createReportTitleLine(String line) {
      ExtendedReportingLine xrl = new ExtendedReportingLine();
      xrl.firstName = "\t\t" + line;
      xrl.email=" ";
      return xrl;
   }
   protected ExtendedReportingLine createReportTitleLine(String line1, String line2) {
      return createReportTitleLine(line1 + "\n" + createReportTitleLine(line2));
   }

   /**
	 * Process Rows
	 *
	 * @throws Exception
	 */
	public void processRows(ProcessorState state) throws Exception {
		List list = cleMembershipDao.listSections();
		String courseNum = null;
		Site site = null;
		CleMembership clem;
		Map<String,Boolean> usermap = null;
		Member member = null;
		User user = null;

		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				// Get next section
				courseNum = (String) list.get(i);

                try {
                    // Lookup external site id by section
                    site = siteHelper.findSite(courseNum);
                } catch (IdUnusedException e) {
                    site = null;
                }

				if (site != null) {
					// New hash for site
					usermap = new HashMap<String,Boolean>();

					// Search
					List members = cleMembershipDao.findCleMembership(courseNum);

					// Longsight custom get all members in one burst
					Set<String> eidsToFetch = new HashSet<String>();
					for (int j = 0; j < members.size(); j++) {
						clem = (CleMembership) members.get(j);
						eidsToFetch.add(clem.getUserName());
					}

					Map<String, User> eidToUserMap = new HashMap<String, User>();
					List<User> sakaiUsers = uds.getUsersByEids(eidsToFetch);
					for (User tempUser : sakaiUsers) {
						eidToUserMap.put(tempUser.getEid(), tempUser);
					}
					// End Longsight custom batch fetch

					for (int j = 0; j < members.size(); j++) {
                        ExtendedReportingLine xrl = new ExtendedReportingLine();
                        member = null;
                        user = null;
                        clem = (CleMembership) members.get(j);

                        if (site.getRole(clem.getRole()) == null)
                        {
                            logger.error("Skipping record: " + clem.getUserName() + ", " +"Site '" + courseNum + "' does not have a role named '" + clem.getRole() + "'");
                            continue;
                        }

                        // Longsight customization #47358 if user cant be fetched from LDAP do not drop the user
                        usermap.put(clem.getUserName().toLowerCase(), Boolean.TRUE);

                        if (eidToUserMap.containsKey(clem.getUserName())) {
                            user = eidToUserMap.get(clem.getUserName());
                        } else {
                                logger.error(clem.getUserName() + " Not A Valid User");
                                state.appendError(clem.getUserName() + " Not A Valid User");
                                state.incrementErrorCnt();
                                state.incrementProcessedCnt();
                                xrl.eid = clem.getUserName();
                                xrl.role = clem.getRole();
                                xrl.siteId = courseNum;
                                xrl.notes = "User not found";
                                if (isExtendedReporting()) extendedReportingLines.add(xrl);
                        }

                        if (user != null) {
                            xrl.setFirstLastEidEmailSiteRole(user.getFirstName(), user.getLastName(),
                                    user.getEid(),user.getEmail(),courseNum,clem.getRole());
                            state.incrementRecordCnt();

                            // Find membership
                            member = site.getMember(user.getId());

                            // If not member then add
                            if (member == null) {
                                if (processMembership(user, site, clem.getRole())){
                                    site.addMember(user.getId(),
                                            clem.getRole(), clem.isActive(), false);
                                    state.incrementInsertCnt();
                                    state.incrementProcessedCnt();
                                    xrl.notes = "Added to site";
                                    if(isExtendedReporting()) extendedReportingLines.add(xrl);
                                }
                            // If status changed remove and re-add to change status
                            // ...also, if role changed, remove and re-add (CLE-6097)
                            } else if (member.isActive() != clem.isActive() ||
                                       (member.getRole() != null && (!member.getRole().getId().equals(clem.getRole())))) {
                               site.removeMember(member.getUserId());
                               site.addMember(user.getId(),
                                       clem.getRole(), clem.isActive(), false);
                                member.setActive(clem.isActive());
                                state.incrementUpdateCnt();
                                state.incrementProcessedCnt();
                                xrl.notes = "Site active status changed to " + clem.isActive();
                               if(isExtendedReporting()) extendedReportingLines.add(xrl);
                            } else {
                               state.incrementIgnoreCnt();
                               state.incrementProcessedCnt();
                               xrl.notes = "No change";
                               if(isExtendedReporting()) extendedReportingLines.add(xrl);
                            }
                        }
					}

					Set set = site.getMembers();
					Iterator iter = set.iterator();

					// If not found in membership table and member of remove
					// role, remove membership
					while (iter.hasNext()) {
						member = (Member) iter.next();

						if (usermap.get(member.getUserEid().toLowerCase()) == null
								&& isRemoveRole(member.getRole().getId())) {
							site.removeMember(member.getUserId());
							state.incrementDeleteCnt();
                     if(isExtendedReporting()) {
                        ExtendedReportingLine xrl = new ExtendedReportingLine();
                        try {
                           user = uds.getUserByEid(member.getUserEid());
                           xrl.firstName = user.getFirstName();
                           xrl.lastName = user.getLastName();
                           xrl.email = user.getEmail();
                        } catch (UserNotDefinedException e) {
                           // ok, just won't have above properties filled in
                        }
                        xrl.eid = member.getUserEid();
                        xrl.role = member.getRole().getDescription();
                        xrl.siteId = courseNum;
                        xrl.notes = "Removed from site";
                        if(isExtendedReporting()) extendedReportingLines.add(xrl);  // save a little memory
                     }

						}
					}

					// Save Site Membership
					siteService.saveSiteMembership(site);
				}
				else {
					logger.error(courseNum + " Not A Valid Site");
					state.appendError(courseNum + " Not A Valid Site");
				}
			}
		}
	}

    protected boolean processMembership(User user, Site site, String role) {
        if (ComponentManager.get("com.rsmart.customer.integration.MembershipFilter") != null) {
            return ((MembershipFilter)ComponentManager.get("com.rsmart.customer.integration.MembershipFilter")).processMembership(user, site, role);
        }
        return true;
    }

    /**
	 * Is Removable Role
	 *
	 * @param roleId
	 * @return boolean
	 */
	public boolean isRemoveRole(String roleId) {
		if (removeRoles != null && removeRoles.size() > 0) {
			for (int i = 0; i < removeRoles.size(); i++) {
				if (removeRoles.get(i).toString().equals(roleId)) {
					return true;
				}
			}
		}

		return false;
	}

   /**
    * Get Report String
    *
    * @return String
    */
   @Override
   public String getReport(ProcessorState state) {
      String theReport = super.getReport(state);
      StringBuilder extendedReport = new StringBuilder();
      if (isExtendedReporting()) {
         if(extendedReportingLines.get(extendedReportingLines.size()-1).toString().indexOf("End Extended") == -1) {
            extendedReportingLines.add(createReportTitleLine("\n\n","End Extended Membership Processing Report"));
         }
         for (ExtendedReportingLine xrl : extendedReportingLines) {
            extendedReport.append(xrl.toString()).append('\n');
         }
         theReport = theReport + "\n\n" + extendedReport;
      }
      return theReport;
   }

   /**
	 * Get Site Service
	 *
	 * @return SiteService
	 */
	public SiteService getSiteService() {
		return siteService;
	}

	/**
	 * Set Site Service
	 *
	 * @param siteService
	 */
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	/**
	 * Get UDS
	 *
	 * @return
	 */
	public UserDirectoryService getUserDirectoryService() {
		return uds;
	}

	/**
	 * Set UDS
	 *
	 * @param uds
	 */
	public void setUserDirectoryService(UserDirectoryService uds) {
		this.uds = uds;
	}

	/**
	 * Get Dao
	 *
	 * @return CleMembershipDao
	 */
	public CleMembershipDao getCleMembershipDao() {
		return cleMembershipDao;
	}

	/**
	 * Set CleMembership Dao
	 *
	 * @param cleMembershipDao
	 */
	public void setCleMembershipDao(CleMembershipDao cleMembershipDao) {
		this.cleMembershipDao = cleMembershipDao;
	}

    /**
	 * Get Remove Role List
	 *
	 * @return List
	 */
	public List getRemoveRoles() {
		return removeRoles;
	}

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public SiteHelper getSiteHelper() {
        return siteHelper;
    }

    public void setSiteHelper(SiteHelper siteHelper) {
        this.siteHelper = siteHelper;
    }
   public Boolean isExtendedReporting() {
      return extendedReporting;
   }

   public void setExtendedReporting(Boolean extendedReporting) {
      this.extendedReporting = extendedReporting;
   }
}
