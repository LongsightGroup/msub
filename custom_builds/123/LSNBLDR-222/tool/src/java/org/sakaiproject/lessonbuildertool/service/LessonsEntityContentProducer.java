/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2013 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.lessonbuildertool.service;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.api.SearchUtils;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.util.HTMLParser;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;

//import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
//import uk.ac.cam.caret.sakai.rwiki.service.api.RenderService;
//import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiEntity;
//import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
//import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;

import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.LessonBuilderAccessAPI;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;

public class LessonsEntityContentProducer implements EntityContentProducer
{

	private static Log log = LogFactory.getLog(LessonsEntityContentProducer.class);
	
	static final String REFERENCE_ROOT = Entity.SEPARATOR + "lessonbuilder";

	private SearchService searchService = null;

	private SearchIndexBuilder searchIndexBuilder = null;

	private EntityManager entityManager = null;
	
	private SimplePageToolDao simplePageToolDao;
	
    private LessonBuilderAccessAPI lessonBuilderAccessAPI;
    
    private SecurityService securityService;
    private ToolManager toolManager;
    private SessionManager sessionManager;
	private SiteService siteService;
	
    public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setLessonBuilderAccessAPI(LessonBuilderAccessAPI lessonBuilderAccessAPI) {
		this.lessonBuilderAccessAPI = lessonBuilderAccessAPI;
	}

	public void setSimplePageToolDao(SimplePageToolDao simplePageToolDao) {
		this.simplePageToolDao = simplePageToolDao;
	}


	
	public void init()
	{
		try
		{
			ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
					.getInstance();
			log.info("init()");
			searchService = (SearchService) load(cm, SearchService.class.getName());
			searchIndexBuilder = (SearchIndexBuilder) load(cm, SearchIndexBuilder.class
					.getName());
			entityManager = (EntityManager) load(cm, EntityManager.class.getName());
			
    


			if ( "true".equals(ServerConfigurationService.getString(
					"search.enable", "false")))
			{

				searchService.registerFunction("lessonbuilder.create");
				searchService.registerFunction("lessonbuilder.update");
				searchService.registerFunction("lessonbuilder.delete");
				searchIndexBuilder.registerEntityContentProducer(this);
			}
		}
		catch (Throwable t)
		{
			log.error("Failed to init ", t);
		}

	}

	private Object load(ComponentManager cm, String name)
	{
		Object o = cm.get(name);
		if (o == null)
		{
			log.error("Cant find Spring component named " + name);
		}
		return o;
	}

	public boolean isContentFromReader(String cr)
	{
		return false;
	}

	public Reader getContentReader(String reference)
	{
		return null;
	}

	public String getContent(String reference)
	{
		
		long id = idFromRef(reference);
		
		SimplePageItem item = simplePageToolDao.findItem(id);
		if (item != null) {
			StringBuilder sb = new StringBuilder();
			for (HTMLParser hp = new HTMLParser(item.getHtml()); hp.hasNext();)
			{
				SearchUtils.appendCleanString(hp.next(), sb);
			}
			return sb.toString();
        }
		else {
			log.info("Could not getContent for reference  "+id);
		}
		
	    return "";
	}

	public String getTitle(String reference)
	{
	    
		long id = idFromRef(reference);
		
		SimplePageItem item = simplePageToolDao.findItem(id);
		String ret = "";
		if (item != null) { 
			ret = SearchUtils.appendCleanString(item.getName(), null).toString();
		}
		return ret;

	}

	public boolean matches(String reference)
	{
		try
		{
			Reference ref = getReference(reference);
			EntityProducer ep = ref.getEntityProducer();
			String className = ep.getClass().getName();
			return ("org.sakaiproject.lessonbuildertool.service.LessonBuilderEntityProducer".equals(className));
		}
		catch (Exception ex)
		{
			return false;
		}
	}
	
	public Integer getAction(Event event)
	{
		String eventName = event.getEvent();
		if ("lessonbuilder.create".equals(eventName)
				|| "lessonbuilder.update".equals(eventName))
		{
			return SearchBuilderItem.ACTION_ADD;
		}
		if ("lessonbuilder.delete".equals(eventName))
		{
			return SearchBuilderItem.ACTION_DELETE;
		}
		return SearchBuilderItem.ACTION_UNKNOWN;
	}

	public boolean matches(Event event)
	{
		return !SearchBuilderItem.ACTION_UNKNOWN.equals(getAction(event));
	}

	public String getTool()
	{
		return "lessons";
	}

	public String getUrl(String reference)
	{
		Reference ref = getReference(reference);
		//Need to implement public String getEntityUrl(Reference ref)

		if (ref != null && ref.getUrl() != null) {
			return ref.getUrl(); 
		}
		return "";
	}

	private String getSiteId(Reference ref)
	{
		String context = ref.getContext();
		if (context.startsWith("/site/"))
		{
			context = context.substring("/site/".length());
		}
		if (context.startsWith("/"))
		{
			context = context.substring(1);
		}
		int slash = context.indexOf("/");
		if (slash > 0)
		{
			context = context.substring(0, slash);
		}
		if (log.isDebugEnabled())
		{
			log.debug("Lessons.getSiteId" + ref + ":" + context);
		}
		return context;
	}

	public String getSiteId(String resourceName)
	{
		String r = getSiteId(entityManager.newReference(resourceName));
		if (log.isDebugEnabled())
		{
			log.debug("Lessons.getSiteId" + resourceName + ":" + r);
		}
		return r;
	}

	public String makeReference(String type, long pageId) {
		String ref = REFERENCE_ROOT + Entity.SEPARATOR + type + Entity.SEPARATOR + Long.toString(pageId);
		return ref;
	}
	
	public Iterator getSiteContentIterator(String context)
	{
		//Limit index to text items
		List <SimplePageItem> pages = simplePageToolDao.findTextItemsInSite(context);
		final Iterator<SimplePageItem> allPageItemsIterator = pages.iterator();
		return new Iterator() {
			public boolean hasNext() {
				return allPageItemsIterator.hasNext(); 
			}
			
			public Object next() {
				SimplePageItem pageItem = (SimplePageItem) allPageItemsIterator.next();
				return makeReference("item",pageItem.getId());
			}
			
			public void remove() {
				throw new UnsupportedOperationException("Remove not supported");
			}
		};
	}

	public boolean isForIndex(String reference)
	{
		//Not entirely sure why you wouldn't want these to be indexed . . . Maybe if they're not a lessons page type?
		long id = idFromRef(reference);
		
		SimplePageItem item = simplePageToolDao.findItem(id);
	
		if (item != null && (SimplePageItem.TEXT == item.getType() || SimplePageItem.PAGE == item.getType() || SimplePageItem.COMMENTS == item.getType() ||
				SimplePageItem.STUDENT_CONTENT == item.getType())) {
			return true;
		}
		return false;
	}

	public boolean canRead(String reference)
	{
		log.debug("canRead(" + reference);
		//Looks like /lessonbuilder/item/39
		long id = idFromRef(reference);
		SimplePageItem item = simplePageToolDao.findItem(id);
		boolean isVisible = false;
		if (item != null) {
			//Does this need to be synchronized?
			synchronized(this) {
				//This seems inefficient but it needs a new one otherwise the cache isn't reset
				
				SimplePageBean simplePageBean = new SimplePageBean();
				simplePageBean.setSecurityService(securityService);
				simplePageBean.setSiteService(siteService);
				simplePageBean.setToolManager(toolManager);
				simplePageBean.setSessionManager(sessionManager);
				simplePageBean.setSimplePageToolDao(simplePageToolDao);
				
				long currentPageId = item.getPageId();
				SimplePage currentPage = simplePageToolDao.getPage(currentPageId);
				simplePageBean.setCurrentPageId(currentPageId);
				simplePageBean.setCurrentPage(currentPage);
				isVisible = simplePageBean.isItemVisible(item);
			}
		}
		return isVisible;
	}

	public Map getCustomProperties(String ref)
	{
		return null;
	}

	public String getCustomRDF(String ref)
	{
		return null;
	}

	private Reference getReference(String reference)
	{
		try
		{
			Reference r = entityManager.newReference(reference);
			if (log.isDebugEnabled())
			{
				log.debug("Lessons.getReference:" + reference + ":" + r);
			}
			return r;
		}
		catch (Exception ex)
		{
		}
		return null;
	}

	private EntityProducer getProducer(Reference ref)
	{
		try
		{
			return ref.getEntityProducer();
		}
		catch (Exception ex)
		{
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getId(java.lang.String)
	 */
	public String getId(String reference)
	{
		try
		{
			String r = getReference(reference).getId();
			if (log.isDebugEnabled())
			{
				log.debug("Lessons.getId:" + reference + ":" + r);
			}
			return r;
		}
		catch (Exception ex)
		{
			return "";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getSubType(java.lang.String)
	 */
	public String getSubType(String reference)
	{
		try
		{
			String r = getReference(reference).getSubType();
			if (log.isDebugEnabled())
			{
				log.debug("Lessons.getSubType:" + reference + ":" + r);
			}
			return r;
		}
		catch (Exception ex)
		{
			return "";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getType(java.lang.String)
	 */
	public String getType(String reference)
	{
		try
		{
			String r = getReference(reference).getType();
			if (log.isDebugEnabled())
			{
				log.debug("Lessons.getType:" + reference + ":" + r);
			}
			return r;
		}
		catch (Exception ex)
		{
			return "";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getType(java.lang.String)
	 */
	public String getContainer(String reference)
	{
		try
		{
			String r = getReference(reference).getContainer();
			if (log.isDebugEnabled())
			{
				log.debug("Lessons.getContainer:" + reference + ":" + r);
			}
			return r;
		}
		catch (Exception ex)
		{
			return "";
		}
	}
	
	private long idFromRef (String reference, int length) {
		long id=-1;
		String[] refParts = reference.split(Entity.SEPARATOR);
		if (refParts.length == length) {
			id = Integer.parseInt(refParts[length-1]);
		}		
		return id;	
	}
	
	//Seems like there should be a method for this, but is what most of the code does, lessons length is 4
	private long idFromRef (String reference) {
		return idFromRef(reference,4);
	}

}
