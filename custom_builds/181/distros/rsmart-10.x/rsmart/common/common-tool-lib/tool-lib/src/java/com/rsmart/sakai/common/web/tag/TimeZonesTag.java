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
package com.rsmart.sakai.common.web.tag;

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

/**
 * JSP Tag <b>timeZones</b>, used to loop through all the TimeZone's
 * so that ID's and Display Names can be accessed by using the standard
 * JSP &lt;jsp:getProperty&gt; tag.
 * <p>
 * The script variable of name <b>id</b> is availble only within the
 * body of the <b>timeZones</b> tag.
 * <p>
 * Loops through all the timeZones.
 * <p>
 * If the optional attribute <b>locale</b> is <b>true</b>, the Displaynames
 * are formatted for the clients locale if known.
 * <p>
 * The optional attribute <b>localeRef</b> can be used to specify
 * the name of a page, session, application, or request scope attribute
 * of type java.util.Locale to use.
 * <p>
 * The optional attribute <b>style</b> can be set to <i>SHORT</i> or
 * <i>LONG</i>.
 * <p>
 * JSP Tag Lib Descriptor
 * <p><pre>
 * &lt;name&gt;timeZones&lt;/name&gt;
 * &lt;tagclass&gt;org.apache.taglibs.datetime.TimeZonesTag&lt;/tagclass&gt;
 * &lt;teiclass&gt;org.apache.taglibs.datetime.TimeZonesTEI&lt;/teiclass&gt;
 * &lt;bodycontent&gt;JSP&lt;/bodycontent&gt;
 * &lt;info&gt;Loop through all the timeZone display names.&lt;/info&gt;
 *   &lt;attribute&gt;
 *     &lt;name&gt;id&lt;/name&gt;
 *     &lt;required&gt;true&lt;/required&gt;
 *     &lt;rtexprvalue&gt;false&lt;/rtexprvalue&gt;
 *   &lt;/attribute&gt;
 *   &lt;attribute&gt;
 *     &lt;name&gt;locale&lt;/name&gt;
 *     &lt;required&gt;false&lt;/required&gt;
 *     &lt;rtexprvalue&gt;false&lt;/rtexprvalue&gt;
 *   &lt;/attribute&gt;
 *   &lt;attribute&gt;
 *     &lt;name&gt;localeRef&lt;/name&gt;
 *     &lt;required&gt;false&lt;/required&gt;
 *     &lt;rtexprvalue&gt;false&lt;/rtexprvalue&gt;
 *   &lt;/attribute&gt;
 *   &lt;attribute&gt;
 *     &lt;name&gt;style&lt;/name&gt;
 *     &lt;required&gt;false&lt;/required&gt;
 *     &lt;rtexprvalue&gt;false&lt;/rtexprvalue&gt;
 *   &lt;/attribute&gt;
 * </pre>
 *
 * @author Glenn Nielsen
 */

public class TimeZonesTag extends BodyTagSupport
{
    // timeZones tag attributes
    private boolean locale_flag = false;
    private String localeRef = null;
    private String style_string = "SHORT";

    // timeZones tag invocation variables
    private int style = TimeZone.SHORT;
    private String [] timeZones = null;
    private TimeZone timeZone = null;
    private int zone_num = 0;

    /**
     * Initializes tag so it can loop through the time zones.
     *
     * @return EVAL_BODY_TAG, or SKIP_BODY if no time zones are found
     */
    public final int doStartTag() throws JspException
    {
        // Initialize variables
        zone_num = 0;

        if( style_string == null || style_string.equals("SHORT") )
            style = TimeZone.SHORT;
        else if( style_string.equals("LONG") )
            style = TimeZone.LONG;
        else
           throw new JspTagException(
               "Datetime tag timeZones style attribute must be set to" +
               " either SHORT or LONG");

   timeZones = TimeZone.getAvailableIDs();
   Arrays.sort( timeZones );

	timeZone = TimeZone.getTimeZone(timeZones[zone_num]);
	if( timeZone == null )
	    return SKIP_BODY;

	pageContext.setAttribute(id,this,PageContext.PAGE_SCOPE);
	return EVAL_BODY_TAG;
    }

    /**
     * Method called at end of each timeZones tag.
     *
     * @return EVAL_BODY_TAG if there is another timeZone, or SKIP_BODY if there are no more timeZones
     */
    public final int doAfterBody() throws JspException
    {
	// See if we are done looping through timeZones
	zone_num++;
	if( zone_num >= timeZones.length )
	    return SKIP_BODY;

	// There is another timeZone, so loop again
	timeZone = TimeZone.getTimeZone(timeZones[zone_num]);
	if( timeZone == null )
	    return SKIP_BODY;

	return EVAL_BODY_TAG;
    }

    /**
     * Method called at end of Tag
     *
     * @return EVAL_PAGE
     */
    public final int doEndTag() throws JspException
    {
        pageContext.removeAttribute(id,PageContext.PAGE_SCOPE);
	try
	{
	    if(bodyContent != null)
	    bodyContent.writeOut(bodyContent.getEnclosingWriter());
	} catch(java.io.IOException e)
	{
	    throw new JspException("IO Error: " + e.getMessage());
	}
	return EVAL_PAGE;
    }

    /**
     * Locale flag, if set to true, format timeZone Displayname
     * for client's preferred locale if known.
     *
     * @param boolean either <b>true</b> or <b>false</b>
     */
    public final void setLocale(boolean flag)
    {
        locale_flag = flag;
    }

    /**
     * Provides a key to search the page context for in order to get the
     * java.util.Locale to use.
     *
     * @param String name of locale attribute to use
     */
    public void setLocaleRef(String value)
    {
        localeRef = value;
    }

    /**
     * Set they style of Displaynames to either <b>SHORT</b> or <b>LONG</b>.
     *
     * @param String style, either <b>SHORT</b> or <b>LONG</b>
     */
    public final void setStyle(String str)
    {
        style_string = str;
    }

    /**
     * Returns the display name of the timeZone.
     * <p>
     * &lt;jsp:getProperty name=<i>"id"</i> property="displayName"/&gt;
     *
     * @return String - display name
     */
    public final String getDisplayName() throws JspException
    {
	String dn = null;
	Date now = new Date();
	boolean daylight = false;

	if( timeZone.useDaylightTime() )
	    daylight = timeZone.inDaylightTime(now);

        if( localeRef != null ) {
            Locale locale = (Locale)pageContext.findAttribute(localeRef);
            if( locale == null ) {
                throw new JspException(
                    "datetime amPms tag could not find locale for localeRef \"" +
                    localeRef + "\".");
            }
            dn = timeZone.getDisplayName(daylight,style,locale);
	} else if( locale_flag) {
	    dn = timeZone.getDisplayName(daylight,style,
                     (Locale)pageContext.getRequest().getLocale());
	} else {
	    dn = timeZone.getDisplayName(daylight, style);
	}

	if( dn == null )
	    dn = "";

	return dn;
    }

    /**
     * Returns the value of the time zone ID.
     * <p>
     * &lt;jsp:getProperty name=<i>"id"</i> property="zoneId"/&gt;
     *
     * @return String - time zone ID
     */
    public final String getZoneId()
    {
	return timeZone.getID();
    }

}
