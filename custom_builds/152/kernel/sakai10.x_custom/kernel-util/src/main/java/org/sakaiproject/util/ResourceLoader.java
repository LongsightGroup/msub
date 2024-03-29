/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.PreferencesService;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ResourceLoader provides an alternate implementation of org.util.ResourceBundle, dynamically selecting the prefered locale from either the user's session or from the user's sakai preferences
 * 
 * @author Sugiura, Tatsuki (University of Nagoya)
 * @author Aaron Zeckoski (azeckoski @ unicon.net)
 */
@SuppressWarnings("rawtypes")
public class ResourceLoader extends DummyMap implements InternationalizedMessages
{
	protected static final Log M_log = LogFactory.getLog(ResourceLoader.class);

	// name of ResourceBundle
	protected String baseName = null;
    public String getBaseName() {
        return baseName;
    }

    // Optional ClassLoader for ResourceBundle
	protected ClassLoader classLoader = null;
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    // cached set of ResourceBundle objects
	protected ConcurrentHashMap<Locale, ResourceBundle> bundles = new ConcurrentHashMap<Locale, ResourceBundle>();

    // cached set of last time bundle was loaded
    protected ConcurrentHashMap<Locale, Date> bundlesTimestamp = new ConcurrentHashMap<Locale, Date>();

	// current user id
	protected String userId = null;
    public String getUserId() {
        return userId;
    }

    // session key string for determining validity of ResourceBundle cache
	protected final String LOCALE_SESSION_KEY = "sakai.locale.";

	// Debugging variables for displaying ResourceBundle name & property	
	protected final String DEBUG_LOCALE = "en_US_DEBUG";
	private final String DBG_PREFIX = "** ";
	private final String DBG_SUFFIX = " **";

	private final static Object LOCK = new Object();

	private static SessionManager sessionManager;
	protected static SessionManager getSessionManager() {
        if (sessionManager == null) {
            synchronized (LOCK) {
                sessionManager = (SessionManager) ComponentManager.get(SessionManager.class);
            }
        }
        return sessionManager;
	}

	private static PreferencesService preferencesService;
	protected static PreferencesService getPreferencesService() {
	    if (preferencesService == null) {
	        synchronized (LOCK) {
	            preferencesService = (PreferencesService) ComponentManager.get(PreferencesService.class);
            }
	    }
	    return preferencesService;
	}
	
	/**
	 * Default constructor (may be used to find user's default locale 
	 *                      without specifying a bundle)
	 */
	public ResourceLoader()
	{
	}

	/**
	 * Constructor: set baseName
	 * 
	 * @param name
	 *        default ResourceBundle base filename
	 */
	public ResourceLoader(String name)
	{
		this.baseName = name;
	}

	/**
	 * Constructor: set baseName
	 * 
	 * @param name default ResourceBundle base filename
	 * @param classLoader ClassLoader for ResourceBundle 
	 */
	public ResourceLoader(String name, ClassLoader classLoader)
	{
		this.baseName = name;
		this.classLoader = classLoader;
	}

	/**
	 * Constructor: specified userId, specified baseName 
	 *              (either may be null)
	 * 
	 * @param userId user's internal sakai id (e.g. user.getId())
	 * @param name  default ResourceBundle base filename
	 */
	public ResourceLoader(String userId, String name)
	{
		this.userId = userId; 
		this.baseName = name; 
	}

	/**
	 ** Return ResourceBundle properties as if Map.entrySet() 
	 **/
	@Override
	public Set entrySet()
	{
		return getBundleAsMap().entrySet();
	}

	/**
	 * * Return (generic object) value for specified property in current locale specific ResourceBundle
	 * 
	 * @param key
	 *        property key to look up in current ResourceBundle * *
	 * @return value for specified property key
	 */
	public Object get(Object key)
	{
		return getString(key.toString());
	}

   /**
    ** Return formatted message based on locale-specific pattern
    **
    ** @param key maps to locale-specific pattern in properties file
    ** @param args parameters to format and insert according to above pattern
    ** @return  formatted message
    ** 
    ** @author Sugiura, Tatsuki (University of Nagoya)
    ** @author Jean-Francois Leveque (Universite Pierre et Marie Curie - Paris 6)
    **
    **/
	public String getFormattedMessage(String key, Object... args)
	{
		if ( getLocale().toString().equals(DEBUG_LOCALE) )
			return formatDebugPropertiesString( key );
			
		String pattern = (String) get(key);
		if (M_log.isDebugEnabled()) {
			M_log.debug("getFormattedMessage(key,args) bundle name=" +
			this.baseName + ", locale=" + getLocale().toString() +
			", key=" + key + ", pattern=" + pattern);
		}
			
		return (new MessageFormat(pattern, getLocale())).format(args, new StringBuffer(), null).toString();
	}

	/**
	 * Access some named configuration value as an int.
	 * 
	 * @param key
	 *        property key to look up in current ResourceBundle
	 * @param dflt
	 *        The value to return if not found.
	 * @return The property value with this name, or the default value if not found.
	 */
	public int getInt(String key, int dflt)
	{
		String value = getString(key);

		int originalLength = value.length();
		if (originalLength == 0) return dflt;

		try
		{
			value = value.trim();
			if (originalLength != value.length())
			{
				M_log.warn("getInt(key, dflt) bundle name=" + this.baseName +
						", locale=" + getLocale() + ", key=" +
						key + ", dflt=" + dflt+ ", Trailing whitespace trimmed.");
			}
			return Integer.parseInt(value);
		}
		catch (NumberFormatException e)
		{
			if (M_log.isDebugEnabled()) {
				M_log.debug("getInt(key, dflt) bundle name=" + this.baseName +
						", locale=" + getLocale() + ", key=" +
						key + ", dflt=" + dflt+ ", NumberFormatException");
			}
		}
		return dflt;
	}
   
	/**
	** Return a locale's display Name
	**
	** @return String used to display Locale
	**
	** @author Jean-Francois Leveque (Universite Pierre et Marie Curie - Paris 6)
	**/
	public String getLocaleDisplayName(Locale loc) {
		Locale preferedLoc = getLocale();

		StringBuilder displayName = new StringBuilder(loc.getDisplayLanguage(loc));

		if (StringUtils.isNotBlank(loc.getDisplayCountry(loc))) {
			displayName.append(" - ").append(loc.getDisplayCountry(loc));
		}

		if (StringUtils.isNotBlank(loc.getVariant())) {
			displayName.append(" (").append(loc.getDisplayVariant(loc)).append(")");
		}

		displayName.append(" [").append(loc.toString()).append("] ");
		displayName.append(loc.getDisplayLanguage(preferedLoc));

		if (StringUtils.isNotBlank(loc.getDisplayCountry(preferedLoc))) {
 			displayName.append(" - ").append(loc.getDisplayCountry(preferedLoc));
		}

		return displayName.toString();
	}
   
	/**
	 ** Return user's prefered locale
	 **	 First: return locale from Sakai user preferences, if available
	 **	 Second: return locale from session, if available
	 **	 Last: return system default locale
	 **
	 ** @return user's Locale object
	**
	** @author Sugiura, Tatsuki (University of Nagoya)
	** @author Jean-Francois Leveque (Universite Pierre et Marie Curie - Paris 6)
	** @author Steve Swinsburg (steve.swinsburg@gmail.com)
	 **/
	public Locale getLocale()
	{
	    Locale loc;
	    // check if locale is requested for specific user
	    if ( this.userId != null ) {
	        loc = getLocale( this.userId );
	    } else {
	        try {
	        	
	        	//get current sessionId to use as the key.
	        	//this allows the anon user to also have locale settings 
	        	String sessionId = getSessionManager().getCurrentSession().getId();
	        	M_log.debug("Retrieving locale for sessionId: " + sessionId);
	            loc = (Locale) getSessionManager().getCurrentSession().getAttribute(LOCALE_SESSION_KEY+sessionId);
	        
	        } catch (NullPointerException e) {
                loc = null;
	            if (M_log.isWarnEnabled()) {
	                M_log.warn("getLocale() swallowing NPE - caused by a null sessionmanager or null session, OK for tests, problem if production");
	                //e.printStackTrace();
	            }
	        }
	        	
            // The locale is not in the session at all, so set in session
            if (loc == null) {
                loc = setContextLocale(null);
            }
	    }

	    if (loc == null) {
	        M_log.info("getLocale() Locale not found in preferences or session, returning default");
	        loc = Locale.getDefault();
	    } 
	    	
	    M_log.debug("Locale: " + loc.toString());

	    return loc;
	}
	
	/**
	 ** This method formats a debugging string using the properties key.
	 ** This allows easy identification of context for properties keys, and
	 ** also highlights any hard-coded text, when the debug locale is selected
	 **/
	protected String formatDebugPropertiesString( String key )
	{
	    return DBG_PREFIX + this.baseName + " " + key + DBG_SUFFIX;
	}

	/**
	 ** Get user's preferred locale (or null if not set)
	 ***/
	public Locale getLocale(String userId)
	{
		return getPreferencesService().getLocale(userId);
	}
	
	/**
	 ** Sets user's prefered locale in context
	 **	 First: sets  locale from Sakai user preferences, if available
	 **	 Second: sets locale from user session, if available
	 **	 Last: sets system default locale
	 **
	 ** @return user's Locale object
	**
	** @author Sugiura, Tatsuki (University of Nagoya)
	** @author Jean-Francois Leveque (Universite Pierre et Marie Curie - Paris 6)
	** @author Steve Swinsburg (steve.swinsburg@gmail.com)
	 **/
	public Locale setContextLocale(Locale loc)
	{		 
		
		//	 First : find locale from Sakai user preferences, if available
		if (loc == null) 
		{
			try
			{
				String userId = getSessionManager().getCurrentSessionUserId();
				
				if (M_log.isDebugEnabled()) {
					M_log.debug("setContextLocale(Locale), checking user preferences for userId: " + userId);
				}
				loc = getLocale(userId);
			}
			catch (Exception e)
			{
				if (M_log.isWarnEnabled()) {
					M_log.warn("setContextLocale(Locale) swallowing Exception");
					e.printStackTrace();
				}
			} // ignore and continue
		}
			  
		// Second: find locale from user browser session, if available
		if (loc == null)
		{
			try
			{
				if (M_log.isDebugEnabled()) {
					M_log.debug("setContextLocale(Locale), checking browser session.");
				}
				loc = (Locale) getSessionManager().getCurrentSession().getAttribute("locale");
				
			}
			catch (NullPointerException e)
			{
				if (M_log.isWarnEnabled()) {
					M_log.warn("setContextLocale(Locale) swallowing NPE");
					e.printStackTrace();
				}
			} // ignore and continue
		}

		// Last: find system default locale
		if (loc == null)
		{
			// fallback to default.
			loc = Locale.getDefault();
			if (M_log.isDebugEnabled()) {
				M_log.debug("setContextLocale(Locale), using default locale");
			}
		}
		else if (!Locale.getDefault().getLanguage().equals("en") && loc.getLanguage().equals("en") && !loc.toString().equals(DEBUG_LOCALE))
		{
			// Tweak for English: en is default locale. It has no suffix in filename.
			loc = new Locale("");
			if (M_log.isDebugEnabled()) {
				M_log.debug("setContextLocale(Locale), Tweak for English");
			}
		}
		
		if (M_log.isDebugEnabled()) {
			M_log.debug("Locale is: "+ loc.toString());
		}
		

		//Write the sakai locale into the session with sessionId as key.
		//We do it this way so that anon users can also leverage the locale settings of sites, since anon users have a session.
		//see KNL-984
		try 
		{
			String sessionId = getSessionManager().getCurrentSession().getId();

			if (M_log.isDebugEnabled()) {
				M_log.debug("Setting locale into session: " + sessionId);
			}
			
			getSessionManager().getCurrentSession().setAttribute(LOCALE_SESSION_KEY+sessionId,loc);
		}
		catch (Exception e) 
		{
			if (M_log.isWarnEnabled()) {
				M_log.warn("setContextLocale(Locale) swallowing Exception");
				//e.printStackTrace();
			}
		} //Ignore and continue
		
		
		return loc;
	}

	/**
	 ** Returns true if the given key is defined, otherwise false
	 **/
	public boolean getIsValid( String key )
	{
		try
		{
			String value = getBundle().getString(key);
			return value != null;
		}
		catch (MissingResourceException e)
		{
			return false;
		}
	}

    @Override
    public boolean containsKey(Object key) {
        if (key == null || !(key instanceof String)) {
            return false;
        }
        return getIsValid((String)key);
    }

    /**
	 * Return string value for specified property in current locale specific ResourceBundle
	 * 
	 * @param key property key to look up in current ResourceBundle
	 * @return String value for specified property key
	 */
	public String getString(String key)
	{
	    if ( getLocale().toString().equals(DEBUG_LOCALE) ) {
	        return formatDebugPropertiesString( key );
	    }

	    try
	    {
	        String value = getBundle().getString(key);
	        if (M_log.isDebugEnabled()) {
	            M_log.debug("getString(key) bundle name=" + this.baseName +
	                    ", locale=" + getLocale().toString() + ", key=" +
	                    key + ", value=" + value);
	        }
	        return value;

	    }
	    catch (MissingResourceException e)
	    {
	        if (M_log.isWarnEnabled()) {
	            M_log.warn("bundle \'"+baseName +"\'  missing key: \'" + key 
	                    + "\'  from: " + e.getStackTrace()[3] ); // 3-deep gets us out of ResourceLoader
	        }
	        return "[missing key (mre): " + baseName + " " + key + "]";
	    }
	    catch (NullPointerException e)
	    {
	        if (M_log.isWarnEnabled()) {
	            M_log.warn("bundle \'"+baseName +"\'  null pointer exception: \'" + key 
	                    + "\'  from: " + e.getStackTrace()[3] ); // 3-deep gets us out of ResourceLoader
	        }
	        return "[missing key (npe): " + baseName + " " + key + "]";			
	    }
	    catch (ClassCastException e)
	    {
	        if (M_log.isWarnEnabled()) {
	            M_log.warn("bundle \'"+baseName +"\'  class cast exception: \'" + key 
	                    + "\'  from: " + e.getStackTrace()[3] ); // 3-deep gets us out of ResourceLoader
	        }
	        return "[missing key (clc): " + baseName + " " + key + "]";						
	    }
	}

	/**
	 * Return string value for specified property in current locale specific ResourceBundle
	 * 
	 * @param key
	 *        property key to look up in current ResourceBundle
	 * @param dflt
	 *        the default value to be returned in case the property is missing
	 * @return String value for specified property key
	 */
	public String getString(String key, String dflt)
	{
	    if ( getLocale().toString().equals(DEBUG_LOCALE) )
	        return formatDebugPropertiesString( key );

	    try
	    {
	        return getBundle().getString(key);
	    }
	    catch (MissingResourceException e)
	    {
	        return dflt;
	    }
	    catch (NullPointerException e)
	    {
	        return dflt;
	    }
	    catch(ClassCastException e)
	    {
	        return dflt;
	    }
	}

	/**
	 * Access some named property values as an array of strings. The name is the base name. name + ".count" must be defined to be a positive integer - how many are defined. name + "." + i (1..count) must be defined to be the values.
	 * 
	 * @param key
	 *        property key to look up in current ResourceBundle
	 * @return The property value with this name, or the null if not found.
	 *
	 * @author Sugiura, Tatsuki (University of Nagoya)
	 * @author Jean-Francois Leveque (Universite Pierre et Marie Curie - Paris 6)
	 *
	 */
	public String[] getStrings(String key)
	{
		if ( getLocale().toString().equals(DEBUG_LOCALE) )
			return new String[] { formatDebugPropertiesString(key) };
			
		// get the count
		int count = getInt(key + ".count", 0);
		if (count > 0)
		{
			String[] rv = new String[count];
			for (int i = 1; i <= count; i++)
			{
				String value = "";
				try
				{
					value = getBundle().getString(key + "." + i);
				}
				catch (MissingResourceException e)
				{
					if (M_log.isWarnEnabled()) {
						M_log.warn("getStrings(" + key + ") swallowing MissingResourceException for String " + i);
						e.printStackTrace();
					}
					// ignore the exception
				}
				rv[i - 1] = value;
			}
			return rv;
		}

		return null;
	}


	/**
	 ** Return ResourceBundle properties as if Map.keySet() 
	 **/
	public Set keySet()
	{
		return getBundle().keySet();
	}

	/**
	 * * Clear bundles hashmap
	 */
	public void purgeCache()
	{
		this.bundles = new ConcurrentHashMap<Locale, ResourceBundle>();
		M_log.debug("purge bundle cache");
	}

	/**
	 * Set baseName
	 * 
	 * @param name
	 *        default ResourceBundle base filename
	 */
	public void setBaseName(String name)
	{
		if (M_log.isDebugEnabled()) {
			M_log.debug("set baseName=" + name);
		}
		this.baseName = name;
	}

	/**
	 ** Return ResourceBundle properties as if Map.values() 
	 **/
	public Collection values()
	{
		return getBundleAsMap().values();
	}

	@Override
	public int size() {
		return getBundle().keySet().size();
	}

	@Override
	public boolean isEmpty() {
		return getBundle().keySet().isEmpty();
	}

    /**
	 * Return ResourceBundle for user's preferred locale
	 * 
	 * @return user's ResourceBundle object
	 */
	protected ResourceBundle getBundle()
	{
        if (ServerConfigurationService.getBoolean("load.bundles.from.db", false)) {
            return getBundleFromDb();
        }
		Locale loc = getLocale();
		ResourceBundle bundle = this.bundles.get(loc);
		if (bundle == null)
		{
			if (M_log.isDebugEnabled()) {
				M_log.debug("Load bundle name=" + this.baseName + ", locale=" + getLocale().toString());
			}
			bundle = loadBundle(loc);
		}
		return bundle;
	}

    /**
     *
     * @return ResourceBundle from the cache or retrieved from the MessageService data store
     */
	protected ResourceBundle getBundleFromDb()
	{
		Locale loc = getLocale();
        //TODO consider using a better caching method here
		ResourceBundle bundle = this.bundles.get(loc);
        Date timeStamp = this.bundlesTimestamp.get(loc);
		if ((timeStamp == null || timeStamp.getTime() + ServerConfigurationService.getInt("load.bundles.from.db.timeout", 30000) < new Date().getTime() ) )
		{
			M_log.debug("Load bundle name=" + this.baseName + ", locale=" + getLocale().toString());
			bundle = loadBundle(loc);
		}
		return bundle;
	}

	/**
	 ** Return the ResourceBundle properties as a Map object
	 **/
	protected Map<Object, Object> getBundleAsMap()
	{
		Map<Object, Object> bundle = new ConcurrentHashMap<Object, Object>();

		for (Enumeration e = getBundle().getKeys(); e.hasMoreElements();)
		{
			Object key = e.nextElement();
			bundle.put(key, getBundle().getObject((String) key));
		}

		return bundle;
	}

    /**
     * Return ResourceBundle for specified locale
     *
     * @param bundle
     *        properties bundle * *
     * @return locale specific ResourceBundle
     */
    protected ResourceBundle loadBundle(Locale loc)
	{
        if (ServerConfigurationService.getBoolean("load.bundles.from.db", false)) {
            return loadBundleFromDb(loc);
        }
        ResourceBundle newBundle = null;
        try
        {
            if ( this.classLoader == null )
                newBundle = ResourceBundle.getBundle(this.baseName, loc);
            else
                newBundle = ResourceBundle.getBundle(this.baseName, loc, this.classLoader);
		} catch (NullPointerException e) {
			// IGNORE FAILURE
        }

        setBundle(loc, newBundle);
		return newBundle;
	}

	/**
	 * Return ResourceBundle for specified locale.  Returns values from the classpath
     * overridden with any values found in the database via the MessageService.  Also,
     * responsible for indexing any new or changes values.
	 *
	 * @param loc
	 *        properties bundle * *
	 * @return locale specific ResourceBundle
	 *         (or empty ListResourceBundle in case of error)
	 */
	protected ResourceBundle loadBundleFromDb(Locale loc)
	{
		ResourceBundle newBundle;
		try
		{
            newBundle = DbResourceBundle.addResourceBundle(this.baseName, loc, this.classLoader);
		}
		catch (Exception e)
		{
			if (M_log.isWarnEnabled()) {
				M_log.warn("loadBundle "+baseName+" "+loc.toString(), e );
			}
			throw new MissingResourceException("ResourceLoader.loadBundle failed",
														  "", "" );
		}

        DbResourceBundle.indexResourceBundle(this.baseName, newBundle, loc, this.classLoader);

		setBundle(loc, newBundle);
		return newBundle;
	}

	/**
	 * Add loc (key) and bundle (value) to this.bundles hash
	 * 
	 * @param loc
	 *        Language/Region Locale *
	 * @param bundle
	 *        properties bundle
	 */
	protected void setBundle(Locale loc, ResourceBundle bundle)
	{
		if (loc == null || bundle == null) 
			return;
		this.bundles.put(loc, bundle);
        this.bundlesTimestamp.put(loc, new Date());
	}

    @Override
    public String toString() {
        return "ResourceLoader{" +
                       "base='" + baseName + '\'' +
                       ", user='" + userId + '\'' +
                       '}';
    }

}

@SuppressWarnings({ "rawtypes" })
abstract class DummyMap implements Map
{
	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	public boolean containsKey(Object key)
	{
		return true;
	}

	public boolean containsValue(Object value)
	{
		throw new UnsupportedOperationException();
	}

	public Set entrySet()
	{
		throw new UnsupportedOperationException();
	}

	public abstract Object get(Object key);

	public boolean isEmpty()
	{
		throw new UnsupportedOperationException();
	}

	public Set keySet()
	{
		throw new UnsupportedOperationException();
	}

	public Object put(Object arg0, Object arg1)
	{
		throw new UnsupportedOperationException();
	}

	public void putAll(Map arg0)
	{
		throw new UnsupportedOperationException();
	}

	public Object remove(Object key)
	{
		throw new UnsupportedOperationException();
	}

	public int size()
	{
		throw new UnsupportedOperationException();
	}

	public Collection values()
	{
		throw new UnsupportedOperationException();
	}
}
