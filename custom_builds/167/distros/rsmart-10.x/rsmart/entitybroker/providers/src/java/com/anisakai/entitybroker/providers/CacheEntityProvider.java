/**
 * 
 */
package com.anisakai.entitybroker.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;

import com.anisakai.entitybroker.providers.model.EntityCache;

/**
 * MemoryEntityProvider
 *
 * @author Earle Nietzel
 * Created on Sep 11, 2013
 * 
 */
public class CacheEntityProvider extends AbstractAsahiEntityProvider implements CoreEntityProvider,
	ActionsExecutable, Outputable, Resolvable, Describeable, CollectionResolvable {

	public final static String ENTITY_PREFIX = "cache";
	
	private CacheManager cacheManager;

	/* (non-Javadoc)
	 * @see com.anisakai.entitybroker.providers.AbstractAsahiEntityProvider#getLocalEntityPrefix()
	 */
	@Override
	public String getLocalEntityPrefix() {
		return ENTITY_PREFIX;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider#entityExists(java.lang.String)
	 */
	@Override
	public boolean entityExists(String id) {
		Cache cache = cacheManager.getCache(id);
		
		if (cache != null) {
			return true;
		}
		
		return false;
	}
	
	@EntityCustomAction(action="memory",viewKey=EntityView.VIEW_LIST)
	public Object getJvmMemoryStatus(EntityReference ref) {
		TreeMap<String, String> status = new TreeMap<String, String>();

		status.put("free", String.valueOf(Runtime.getRuntime().freeMemory()));
		status.put("total", String.valueOf(Runtime.getRuntime().totalMemory()));
		status.put("maximum", String.valueOf(Runtime.getRuntime().maxMemory()));

		return new ActionReturn(status);
	}

	@EntityCustomAction(action="cacheNames",viewKey=EntityView.VIEW_LIST)
	public Object getCacheNames(EntityReference ref) {
		List<String> cacheNames = Arrays.asList(cacheManager.getCacheNames());
		Collections.sort(cacheNames);
		return new ActionReturn(cacheNames);
	}

	@EntityCustomAction(action="cacheSummary",viewKey=EntityView.VIEW_LIST)
	public Object getCacheSummary(EntityReference ref) {
		TreeMap<String, String> summary = new TreeMap<String, String>();
		
		for (Ehcache cache : getAllCaches()) {
			EntityCache eCache = new EntityCache(cache);
			summary.put(eCache.getId(), "Size " + eCache.getCacheSize() + ":Hits " + eCache.getCacheHits() + ":Ratio " + eCache.getCacheHitRatio());
		}
		
		return new ActionReturn(summary);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable#getHandledOutputFormats()
	 */
	@Override
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.XML, Formats.HTML, Formats.JSON };	
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable#getEntity(org.sakaiproject.entitybroker.EntityReference)
	 */
	@Override
	public Object getEntity(EntityReference ref) {
		if (ref == null || ref.getId() == null) {
			return new EntityCache();
		}
		
		return new EntityCache(cacheManager.getCache(ref.getId()));
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable#getEntities(org.sakaiproject.entitybroker.EntityReference, org.sakaiproject.entitybroker.entityprovider.search.Search)
	 */
	@Override
	public List<?> getEntities(EntityReference ref, Search search) {
		List<EntityCache> caches = new ArrayList<EntityCache>();
		
		if (search.getRestrictionByProperty("id") != null) {
        	String id = search.getRestrictionByProperty("id").getStringValue();
        	
        	if (entityExists(id)) {
        		caches.add((EntityCache) getEntity(ref));
        	}
		} else {
			for (Ehcache cache : getAllCaches()) {
				caches.add(new EntityCache(cache));
			}
		}
		
		return caches;
	}
	
	private List<Ehcache> getAllCaches()
	{
		final String[] cacheNames = cacheManager.getCacheNames();
		Arrays.sort(cacheNames);
		final List<Ehcache> caches = new ArrayList<Ehcache>(cacheNames.length);
		for (String cacheName : cacheNames) {
			caches.add(cacheManager.getEhcache(cacheName));
		}
		return caches;
	}
	
	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
}
