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

import com.rsmart.customer.integration.model.CleCourse;
import com.rsmart.customer.integration.processor.BaseCsvFileProcessor;
import com.rsmart.customer.integration.processor.ProcessorState;
import com.rsmart.customer.integration.util.SiteHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ArrayUtil;

/**
 * Sis Course Processor
 *
 * @author dhelbert
 * @version $Revision$ $Date$
 */
public class CleCourseProcessor extends BaseCsvFileProcessor {
   private static final Log logger = LogFactory.getLog(CleCourseProcessor.class);


   /** Date Format */
	private SimpleDateFormat sdf = new SimpleDateFormat("d-MMM-yy");

	/** Site Service */
	private SiteService siteService;

	/** Master Site ID */
	private String masterSiteId;


	/** Date Format */
	private String dateFormat = "d-MMM-yy";

	/** Update Allowed Flag */
	private boolean updateAllowed = true;

    /**
     * set this to true to update the publish status even when updatedAllowed is false
     */
    private boolean publishOverride =false;

    /**
     * set this to false and updateAllowed to true to update all fields but the long description
     */
    private boolean updateLongDescription = true;

	/** Server Config */
	private ServerConfigurationService serverConfigurationService;

	/** Content Hosting Service */
	private ContentHostingService contentHostingService;

   private SiteHelper siteHelper;

   private UserDirectoryService userDirectoryService;

   //site is initially created with this user as the maintainer
   private String initialMaintainer;
      
   //site is updated with this user as the maintainer (removing the admin user!)
   private String updatedMaintainer;

    private CourseManagementService courseManagementService;

   private boolean extendedReporting = false;

    private AliasService aliasService;
   /**
    * optional array of reporting lines, based in extendedReporting property
    */
	private List <ExtendedReportingLine>extendedReportingLines;

    private boolean createAlias;

    private boolean generateSiteId = true;

   private class ExtendedReportingLine {
      String term;
      String title;
      String published;
      String template;
      String notes;
      String nullSafe(String s) {
         if(s != null) return s;
         return "";
      }

      public void setTitles() {
         this.term = "Term";
         this.title = "Course Title";
         this.published = "Published t/f";
         this.template = "Template ID";
         this.notes = "Comments";
      }
/*      public ExtendedReportingLine(String f, String l, String eid, String email, String siteId, String role) {
         firstName=f; lastName=l; this.eid=eid; this.email=email; this.siteId=siteId; this.role=role;
      }
      */
      @Override
      public String toString() {

         return nullSafe(term) + "\t" +
                 nullSafe(title) + "\t" +
                 nullSafe(published) + "\t" +
                 nullSafe(template) + "\t" +
                 nullSafe(notes);
      }

      public void setTermTitlePubTemplateNotes(String term, String title, String published,
                                               String template, String notes) {
         this.term = term;
         this.title = title;
         this.published = published;
         this.template = template;
         this.notes = notes;
      }
   }

   /**
    * Init Processor
    */
   @Override
   public ProcessorState init(Map config)
   {
      ProcessorState
        state = super.init(config);
       
      if(isExtendedReporting()) {
         extendedReportingLines = new ArrayList<ExtendedReportingLine>();
         extendedReportingLines.add(createReportTitleLine("Begin Extended Course Processing Report","\n"));
         ExtendedReportingLine titles = new ExtendedReportingLine();
         titles.setTitles();
         extendedReportingLines.add(titles);
      }

      return state;
   }

   protected ExtendedReportingLine createReportTitleLine(String line) {
      ExtendedReportingLine xrl = new ExtendedReportingLine();
      xrl.term = "\t\t" + line;
      return xrl;
   }
   protected ExtendedReportingLine createReportTitleLine(String line1, String line2) {
      return createReportTitleLine(line1 + "\n" + createReportTitleLine(line2));
   }
   /**
	 * Get Title
	 *
	 * @retun String
	 */
	public String getProcessorTitle() {
		return "SIS Course Processor";
	}

	/**
	 * Process Row
	 *
	 * @param data
	 */
	public void processRow(String[] data, ProcessorState state)
        throws Exception
    {
		CleCourse course = new CleCourse();

		course.setTerm(data[0]);
		course.setCourseNumber(data[1]);



		if (data[2] != null && data[2].length() > 0) {
			course.setStartDate(sdf.parse(data[2]));
		}

		course.setTitle(data[3]);



		course.setShortDescription(data[4]);
		course.setDescription(data[5]);
		course.setPublished(new Boolean("1".equals(data[6])));

		if (data[7] != null && data[7].length() > 0) {
			course.setEndDate(sdf.parse(data[7]));
		}

		course.setMasterSite(data[8]);

        if (data.length > 9) {
         course.setContactName(data[9]);
        }

        if (data.length > 10) {
         course.setContactEmail(data[10]);
        }

        if (data.length > 11) {
            course.setProperty1(data[11]);
        }
        if (data.length > 12) {
            course.setProperty2(data[12]);
        }
        if (data.length > 13) {
            course.setProperty3(data[13]);
        }
        if (data.length > 14) {
            course.setProperty4(data[14]);
        }
        if (data.length > 15) {
            course.setProperty5(data[15]);
        }

        //Course title length > 99
        if ( course.getTitle().length() > 99){
         logger.warn("Course Title length cannot be greater than 99");
         logger.warn("There is an error/format problem in the course csv file");
         logger.warn("Error Message: Course Title length cannot be greater than 99");
         logger.warn("Course Name:" + course.getTitle());
         logger.warn("Course Description:" + course.getDescription());
         logger.warn("Column Number:"+ getColumns());
         logger.warn("Record Count shows the line in which course title is >99" + state.getRecordCnt());
         logger.warn("Start Date" + state.getStartDate());
         logger.warn("End Date" + new Date());
         return;

        }

      processCleCourse(course, state);
	}

	/**
	 * Process CLE Course
	 *
	 * @param course
	 * @throws Exception
	 */
	private void processCleCourse(CleCourse course, ProcessorState state) throws Exception
    {
		// Site
		Site site = null;

		// Lookup site by external id attribute
		try {
			site = siteHelper.findSite(course.getCourseNumber());
		} catch (Exception ex) {
			// We had to do this because site not found throws exception
		}

		// If not found clone master site, otherwise update
		if (site == null) {
			Site masterSite;
			String sakaiProp;

			// If master defined, else use default
			if (course.getMasterSite() != null
					&& course.getMasterSite().length() > 0) {
				sakaiProp = serverConfigurationService
						.getString("cle.mastersiteid." + course.getMasterSite());
				masterSite = siteService.getSite(sakaiProp);
			} else {
				masterSite = siteService.getSite(masterSiteId);
			}

			if (masterSite != null) {
                String newSiteId = (generateSiteId) ?
                        IdManager.createUuid() : course.getCourseNumber();
                Site newSite = siteService.addSite( newSiteId,
						masterSite);

				newSite.setTitle(course.getTitle());
         
                if (course.getDescription() != null && course.getDescription().length() != 0) {
				    newSite.setDescription(course.getDescription());
                } else if (masterSite.getDescription() != null) {
                    newSite.setDescription(masterSite.getDescription());
                }
                if (course.getShortDescription() != null && course.getShortDescription().length() != 0) {
                    newSite.setShortDescription(course.getShortDescription());
                } else if (masterSite.getShortDescription() != null) {
                    newSite.setShortDescription(masterSite.getShortDescription());
                }
				newSite.setPublished(course.getPublished().booleanValue());

				ResourcePropertiesEdit rpe = newSite.getPropertiesEdit();

                //CLE-6094 - when copying template, need to remove template property!
                rpe.removeProperty("template");

                rpe.addProperty("term", getTermName(course.getTerm()));

				rpe.addProperty("term_eid", course.getTerm());
				rpe.addProperty("externalSiteId", course.getCourseNumber());


				if (course.getStartDate() != null) {
					rpe.addProperty("startDate", sdf.format(course
							.getStartDate()));
				}

				if (course.getEndDate() != null) {
					rpe.addProperty("endDate", sdf.format(course.getEndDate()));
				}


            if (course.getContactName() != null) {
               rpe.addProperty("contact-name", course.getContactName());
            }

            if (course.getContactEmail() != null) {
               rpe.addProperty("contact-email", course.getContactEmail());
            }

            updateExtraProperties(course, newSite);

            // Save Site
		    siteService.save(newSite);
            siteHelper.invalidateCache(course.getCourseNumber());
            newSite = siteService.getSite(newSiteId);

            if (createAlias) {
                aliasService.setAlias(course.getCourseNumber(), "/site/" + newSite.getId());
            }

            updateMaintainer(newSite, initialMaintainer);

            // Copy all resources
				copyResources(newSite, masterSite);

	        state.incrementInsertCnt();

            ExtendedReportingLine xrl = new ExtendedReportingLine();
            xrl.setTermTitlePubTemplateNotes(course.getTerm(), course.getTitle(),
                    course.getPublished().toString(), masterSite.getId(), "Course Added");
            if(isExtendedReporting()) extendedReportingLines.add(xrl);
			} else {
				throw new Exception("Master site '" + masterSiteId
						+ "' not found.");
			}
		} else {
            site = siteService.getSite(site.getId());
            site.loadAll();
            updateMaintainer(site, updatedMaintainer);

            ExtendedReportingLine xrl = new ExtendedReportingLine();
            xrl.setTermTitlePubTemplateNotes(course.getTerm(), course.getTitle(),
                    course.getPublished().toString(), "", "n/a");

            if (publishOverride && !updateAllowed) {
                site.setPublished(course.getPublished().booleanValue());
                siteService.save(site);
                state.incrementUpdateCnt();
            }

            if (updateAllowed) {
                site.setTitle(course.getTitle());
                if (publishOverride) {
                    site.setPublished(course.getPublished().booleanValue());
                }
                if (updateLongDescription) {
                    if (course.getDescription() != null && course.getDescription().length() != 0) {
                        site.setDescription(course.getDescription());
                    }
                }
                if (course.getShortDescription() != null && course.getShortDescription().length() != 0) {
                    site.setShortDescription(course.getShortDescription());
                }

                ResourcePropertiesEdit rpe = site.getPropertiesEdit();
                rpe.addProperty("term", getTermName(course.getTerm()));
                rpe.addProperty("term_eid", course.getTerm());
                rpe.addProperty("externalSiteId", course.getCourseNumber());

                if (course.getStartDate() != null) {
                    rpe.addProperty("startDate", sdf.format(course
                            .getStartDate()));
                }

                if (course.getEndDate() != null) {
                    rpe.addProperty("endDate", sdf.format(course.getEndDate()));
                }

                if (course.getContactName() != null) {
                    rpe.addProperty("contact-name", course.getContactName());
                }

                if (course.getContactEmail() != null) {
                    rpe.addProperty("contact-email", course.getContactEmail());
                }

                updateExtraProperties(course, site);

                if (createAlias) {
                    aliasService.setAlias(course.getCourseNumber(), "/site/" + site.getId());
                }

                siteService.save(site);

                state.incrementUpdateCnt();
                xrl.notes = "Updating Course";
            } else {
               xrl.notes="Course already exists";
            }
         if(isExtendedReporting()) {
             extendedReportingLines.add(xrl);
         }
		}
	}

    /**
     * Updates the Sakai UserEdit with properties in the CleUser.  This method does not
     * commit the changes
     * @param cleCourse
     * @param cleCourse
     */
    protected void updateExtraProperties(CleCourse cleCourse, Site site)  {
        String[] propertyNames = org.sakaiproject.component.cover.ServerConfigurationService.getStrings("course.sis.property");
        if (propertyNames != null && propertyNames.length > 0) {
            ResourcePropertiesEdit rpe = site.getPropertiesEdit();

            if (cleCourse.getProperty1() != null) {
                rpe.addProperty(propertyNames[0], cleCourse.getProperty1());
            }

            if (propertyNames.length > 1) {
                if (cleCourse.getProperty2() != null) {
                    rpe.addProperty(propertyNames[1], cleCourse.getProperty2());
                }
            }
            if (propertyNames.length > 2) {
                if (cleCourse.getProperty3() != null) {
                    rpe.addProperty(propertyNames[2], cleCourse.getProperty3());
                }
            }
            if (propertyNames.length > 3) {
                if (cleCourse.getProperty4() != null) {
                    rpe.addProperty(propertyNames[3], cleCourse.getProperty4());
                }
            }
            if (propertyNames.length > 4) {
                if (cleCourse.getProperty5() != null) {
                    rpe.addProperty(propertyNames[4], cleCourse.getProperty5());
                }
            }
        }
    }
    
    protected String getTermName(String termEid) {
           List<AcademicSession> academicSessions = courseManagementService.getAcademicSessions();

         for(AcademicSession academicSession : academicSessions) {
            //we found a matching academicSession, success
            if (academicSession.getEid().equals(termEid)){
                return academicSession.getTitle();
            }
         }
        logger.warn("can't find a matching term with eid=[" + termEid + "] check the sakai.properties files for proper term configuration");
        return null;
    }

    /**
    *  updates the maintainer of the site to specified user, and removes the current user's membership
    * @param site
    * @throws IdUnusedException
    * @throws PermissionException
    */
   protected void updateMaintainer(Site site, String maintainer) throws IdUnusedException, PermissionException {
      if (maintainer != null && maintainer.length() > 0) {
         try {
            boolean update = false;

            // Find membership
            Member member = site.getMember(userDirectoryService.getCurrentUser().getId());

            // remove old user
            if (member != null) {
               // clear the admin user from the site (or whatever the current user id is, the job is configured to be admin
               site.removeMember(userDirectoryService.getCurrentUser().getId());
               update = true;
               logger.info("removing " + userDirectoryService.getCurrentUser().getId() + " from site " + site.getId());
            }
            
            member = site.getMember(maintainer);

            // add user
            if (member == null) {
               site.addMember(maintainer, site.getMaintainRole(), true, false);
               update = true;
               logger.info("adding " + maintainer + " to site " + site.getId());               
            }

            if (update){
               siteService.save(site);
            }
         }
         catch (Exception e) {
            logger.warn("updateMaintainer() error updating the maintainer user", e);
         }
      }
   }

   /**
	 * Copy Resources
	 *
	 * @param newSite
	 * @param masterSite
	 */
	private void copyResources(Site newSite, Site masterSite) throws Exception {
		// import tool content
		List pageList = newSite.getPages();

        if (!((pageList == null) || (pageList.size() == 0))) {
        	boolean meleteFound = false;
			for (ListIterator i = pageList.listIterator(); i.hasNext();) {
				SitePage page = (SitePage) i.next();

				List pageToolList = page.getTools();
                if (pageToolList.isEmpty()){
                    //no tools on this page, don't blow up just continue
                    continue;
                }
				String toolId = ((ToolConfiguration) pageToolList.get(0))
						.getTool().getId();
				if (toolId.equalsIgnoreCase("sakai.resources")) {
					// handle resource
					// tool specially
					transferCopyEntities(toolId, contentHostingService
							.getSiteCollection(masterSite.getId()),
							contentHostingService.getSiteCollection(newSite
									.getId()));
				}
				else if("sakai.melete".equalsIgnoreCase(toolId))
				{
					meleteFound = true;
				}
				else {
					// other tools, except news and iframe, for some reason these gets copied twice

                    if (!(toolId.equals("sakai.news") || toolId.equals("sakai.iframe"))){
                        transferCopyEntities(toolId, masterSite.getId(), newSite
                                .getId());
                    }
                }
			}
			if(meleteFound)
			{
				transferCopyEntities("sakai.melete", masterSite.getId(), newSite.getId());
			}
		}
	}

	/**
	 * Transfer a copy of all entites from another context for any entity
	 * producer that claims this tool id.
	 *
	 * @param toolId
	 *            The tool id.
	 * @param fromContext
	 *            The context to import from.
	 * @param toContext
	 *            The context to import into.
	 */
	private void transferCopyEntities(String toolId, String fromContext,
			String toContext) throws Exception {
		for (Iterator i = EntityManager.getEntityProducers().iterator(); i
				.hasNext();) {
			EntityProducer ep = (EntityProducer) i.next();
			try
			{
			if (ep instanceof EntityTransferrer) {
				EntityTransferrer et = (EntityTransferrer) ep;

				// if this producer claims this tool id
				if (ArrayUtil.contains(et.myToolIds(), toolId)) {


                    ThreadLocalManager.set("sakai:ToolComponent:current.placement", new org.sakaiproject.util.Placement(null,toolId,null,null,toContext,null));
					et.transferCopyEntities(fromContext, toContext,
							new Vector());
				}
			}
			}
			catch(Exception ex)
			{
				logger.error("Failed transferCopyEntities for : " + ep.toString(), ex);
			}
		}
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
            extendedReportingLines.add(createReportTitleLine("\n\n","End Extended Course Processing Report"));
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
	 * @return
	 */
	private SiteService getSiteService() {
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
	 * Get Master Site ID
	 *
	 * @return
	 */
	public String getMasterSiteId() {
		return masterSiteId;
	}

	/**
	 * Set Master Site ID
	 *
	 * @param masterSiteId
	 */
	public void setMasterSiteId(String masterSiteId) {
		this.masterSiteId = masterSiteId;
	}

	/**
	 * Get Date Format - Default mm/dd/yyyy
	 *
	 * @return String
	 */
	public String getDatFormat() {
		return dateFormat;
	}

	/**
	 * Set Date Format
	 *
	 * @param dateFormat
	 */
	public void setDatFormat(String dateFormat) {
		this.dateFormat = dateFormat;
		sdf = new SimpleDateFormat(dateFormat);
	}

	/**
	 * Get Update Allowed
	 *
	 * @return boolean
	 */
	public boolean isUpdateAllowed() {
		return updateAllowed;
	}

	/**
	 * Set Updated ALlowed
	 *
	 * @param updateAllowed
	 */
	public void setUpdateAllowed(boolean updateAllowed) {
		this.updateAllowed = updateAllowed;
	}

	/**
	 * Get Server Conf Serv
	 *
	 * @return ServerConfigurationService
	 */
	public ServerConfigurationService getServerConfigurationService() {
		return serverConfigurationService;
	}

	/**
	 * Set Server Conf Serv
	 *
	 * @param serverConfigurationService
	 */
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	/**
	 * Get CHS
	 *
	 * @return
	 */
	public ContentHostingService getContentHostingService() {
		return contentHostingService;
	}

	/**
	 * Set CHS
	 *
	 * @param contentHostingService
	 */
	public void setContentHostingService(
			ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        setDatFormat(dateFormat);
    }

    public SiteHelper getSiteHelper() {
        return siteHelper;
    }

    public void setSiteHelper(SiteHelper siteHelper) {
        this.siteHelper = siteHelper;
    }

   public String getInitialMaintainer() {
      return initialMaintainer;
   }

   public void setInitialMaintainer(String initialMaintainer) {
      this.initialMaintainer = initialMaintainer;
   }

   public String getUpdatedMaintainer() {
      return updatedMaintainer;
   }

   public void setUpdatedMaintainer(String updatedMaintainer) {
      this.updatedMaintainer = updatedMaintainer;
   }

   public UserDirectoryService getUserDirectoryService() {
      return userDirectoryService;
   }

   public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
      this.userDirectoryService = userDirectoryService;
   }

    public CourseManagementService getCourseManagementService() {
        return courseManagementService;
    }

    public void setCourseManagementService(CourseManagementService courseManagementService) {
        this.courseManagementService = courseManagementService;
    }

    public boolean isPublishOverride() {
        return publishOverride;
    }

    public void setPublishOverride(boolean publishOverride) {
        this.publishOverride = publishOverride;
    }

    public boolean isExtendedReporting() {
       return extendedReporting;
    }

    public void setExtendedReporting(boolean extendedReporting) {
       this.extendedReporting = extendedReporting;
    }

    public void setAliasService(AliasService aliasService) {
        this.aliasService = aliasService;
    }

    public void setCreateAlias(boolean createAlias) {
        this.createAlias = createAlias;
    }

    public void setGenerateSiteId(boolean generateSiteId) {
        this.generateSiteId = generateSiteId;
    }

    public boolean isUpdateLongDescription() {
        return updateLongDescription;
    }

    public void setUpdateLongDescription(boolean updateLongDescription) {
        this.updateLongDescription = updateLongDescription;
    }
}
