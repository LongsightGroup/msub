package org.sakaiproject.coursemanagement.impl.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.List;
import java.util.Iterator;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.sakaiproject.coursemanagement.impl.CourseSetCmImpl;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CanonicalCourse;
import org.sakaiproject.coursemanagement.api.CourseManagementAdministration;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.CourseSet;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Meeting;
import org.sakaiproject.coursemanagement.api.Membership;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;

/**
 * This class simply tries to load a URL first before it gets the resource from
 * the classloader. It also overrides updateCourseOfferingMembership!
 * 
 * @author dtabrams
 * 
 */
public class UDCMSyncJob extends ClassPathCMSyncJob
{
    private static final Log log = LogFactory.getLog(UDCMSyncJob.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getXmlInputStream()
    {
        InputStream is = null;
        //URL url;
        log.error("NOT ERROR: loading file: " + this.classPathToXml);
        try
        {
            File file = new File(this.classPathToXml);
            is = new FileInputStream(file);
            //url = new URL(this.classPathToXml);
            //log.error("NOT ERROR: url is well formed, loading");
            //is = url.openStream();
            //log.error("NOT ERROR: stream opened! (is file loaded?");
        }/*
        catch (MalformedURLException e1)
        {
            log.error("NOT ERROR: url is badly formed", e1);
        }*/
        catch (IOException e)
        {
            log.error("IO Exception loading xml input stream:", e);
        }
        return is;
    }

    @Override
    protected void updateCourseOfferingMembers(Element membersElement,
            CourseOffering courseOffering)
    {
        if (log.isInfoEnabled()) 
            log.info("skipping course offering updates since they are handled sakai-side only!");
    }
    /*
    protected void reconcileAcademicSessions(Document doc) {
        if (log.isInfoEnabled()) 
            log.info("skipping academic session syncing to enable manual " +
                    "updates to work around course showing timing issues!");
    }*/

    protected void reconcileCourseSetLinking(Document doc)
    {

        long start = System.currentTimeMillis();
        if(log.isInfoEnabled()) log.info("Reconciling CourseSet Links");
        
        try {
            if(log.isInfoEnabled()) log.info("Reconciling CourseSet - Canonical Course Links");
            XPath docsPath = XPath.newInstance("/cm-data/canonical-course-set-links/canonical-course-set-link");
            List items = docsPath.selectNodes(doc);
            if(log.isInfoEnabled()) log.info("Found " + items.size() + " canonical course->sets to reconcile");

            // Add or update each of the course offerings specified in the xml
            for(Iterator iter = items.iterator(); iter.hasNext();)
            {
                Element element = (Element)iter.next();
                String canEid = element.getChildText("canonical-course-eid");
                String csEid = element.getChildText("course-set-eid");
                if(log.isDebugEnabled()) log.debug("Linking canonical course " + 
                        canEid + " to course set " + csEid);
                
                if(!cmService.isCourseSetDefined(csEid))
                {
                    if(log.isWarnEnabled()) log.warn("Course Set not found: " + csEid);;
                    continue;
                }
                if (!cmService.isCanonicalCourseDefined(canEid))
                {
                    if(log.isWarnEnabled()) log.warn("Canonical course not found: " + canEid);;
                    continue;
                }
                CanonicalCourse can = cmService.getCanonicalCourse(canEid);
                Set<CanonicalCourse> cans = cmService.getCanonicalCourses(csEid);
                if (!cans.contains(can))
                    cmAdmin.addCanonicalCourseToCourseSet(csEid,canEid);
            }
            
            if(log.isInfoEnabled()) log.info("Finished Reconciling CourseSet - Canonical Course Links");
            
            if(log.isInfoEnabled()) log.info("Reconciling CourseSet -Course Offering Links");
            
            docsPath = XPath.newInstance("/cm-data/course-offering-set-links/course-offering-set-link");
            items = docsPath.selectNodes(doc);
            if(log.isInfoEnabled()) log.info("Found " + items.size() + " course offering->sets to reconcile");

            // Add or update each of the course offerings specified in the xml
            for(Iterator iter = items.iterator(); iter.hasNext();)
            {
                Element element = (Element)iter.next();
                String coEid = element.getChildText("course-offering-eid");
                String csEid = element.getChildText("course-set-eid");
                if(log.isDebugEnabled()) log.debug("Linking course offering " + 
                        coEid + " to course set " + csEid);             
                try
                {
                    if (!cmService.isCourseOfferingInCourseSet(csEid,coEid))
                        cmAdmin.addCourseOfferingToCourseSet(csEid,coEid);
                }
                catch (IdNotFoundException e)
                {
                    if(log.isWarnEnabled()) 
                        log.warn("Course Set not found: " + csEid + 
                                " or Course Offering not found: " + coEid, e);
                }
            }
            if(log.isInfoEnabled()) log.info("Finished Reconciling CourseSet -Course Offering Links");
            
        } catch (JDOMException jde) {
            log.error(jde);
        }
        if(log.isInfoEnabled()) log.info("Finished reconciling CourseSet Links in " + (System.currentTimeMillis()-start) + " ms");
    }
}