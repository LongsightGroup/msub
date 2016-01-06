<?xml version="1.0" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

   <xsl:output method="xml" version="1.0" encoding="UTF-8" omit-xml-declaration="no"
               indent="yes" cdata-section-elements="name"/>

   <xsl:variable name="siteTypes" select="document('output/site-role-scrubbed.xml')"/>
   <xsl:variable name="tools" select="document('output/tools-scrubbed.xml')"/>
   <xsl:variable name="myws" select="document('output/myws-scrubbed.xml')"/>

   <xsl:variable name="siteTypeBeanPrefix" select="'org.theospi.portfolio.portal.model.SiteType.'"/>
   <xsl:variable name="toolCategoryBeanPrefix" select="'org.theospi.portfolio.portal.model.ToolCategory.'"/>

   <xsl:template match="toolOrder">
      <toolOrder>
         <xsl:for-each select="$siteTypes/siteTypeRoles/siteTypes/siteType">
            <xsl:call-template name="siteTypeBean">
               <xsl:with-param name="siteType" select="."/>
            </xsl:call-template>
         </xsl:for-each>
         <xsl:for-each select="$siteTypes/siteTypeRoles/sites/site">
            <xsl:call-template name="siteBean">
               <xsl:with-param name="site" select="."/>
            </xsl:call-template>
         </xsl:for-each>
         <xsl:for-each select="$myws/myws/toolCategories">
            <xsl:call-template name="mywsBean">
               <xsl:with-param name="myws" select="."/>
            </xsl:call-template>
         </xsl:for-each>
      </toolOrder>
   </xsl:template>

   <xsl:template name="globalSiteTypeBean">
      <xsl:param name="shortId"/>
      <xsl:param name="order"/>
      <xsl:param name="toolCategories"/>
      <xsl:param name="siteTypeName"/>
      <xsl:param name="siteTypeDescription"/>

      <category>
         <xsl:attribute name="name"><xsl:value-of select="$shortId"/></xsl:attribute>
         <xsl:for-each select="$toolCategories/category">
            <xsl:choose>
               <xsl:when test="tools/tool[@id='rsmart.home.page']">
                  <tool id = "sakai.iframe.myworkspace" />
                  <tool id = "sakai.iframe.site" />
                  <tool id = "sakai.synoptic.chat" />
                  <tool id = "sakai.synoptic.discussion" />
                  <tool id = "sakai.synoptic.announcement" />
                  <tool id = "home" selected = "true" />
               </xsl:when>
               <xsl:otherwise>
                  <xsl:call-template name="categoryBean">
                     <xsl:with-param name="shortId" select="$shortId"/>
                     <xsl:with-param name="category" select="."/>
                  </xsl:call-template>
               </xsl:otherwise>
            </xsl:choose>
         </xsl:for-each>
      </category>

   </xsl:template>

   <xsl:template name="categoryBean">
      <xsl:param name="shortId"/>
      <xsl:param name="category"/>

      <xsl:choose>
      <xsl:when test="$category/@type='uncategorized'">
         <xsl:for-each select="$category/tools/tool">
            <xsl:call-template name="toolBean">
               <xsl:with-param name="tool" select="."/>
            </xsl:call-template>
         </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
         <toolCategory>
            <xsl:attribute name="id">
               <xsl:call-template name="categoryBeanId">
                  <xsl:with-param name="shortId" select="$shortId"/>
                  <xsl:with-param name="category" select="$category"/>
               </xsl:call-template>
            </xsl:attribute>
            <name><xsl:value-of select="$category/name"/></name>
            <xsl:for-each select="$category/tools/tool">
               <xsl:call-template name="toolBean">
                  <xsl:with-param name="tool" select="."/>
               </xsl:call-template>
            </xsl:for-each>
         </toolCategory>
      </xsl:otherwise>
      </xsl:choose>

   </xsl:template>

   <xsl:template name="toolBean">
      <xsl:param name="tool"/>
      <xsl:variable name="toolLookup" select="$tools/tools/tool[@id=$tool/@id]"/>
      <xsl:variable 
         name="toolId"><xsl:choose><xsl:when test="$toolLookup/@realId"><xsl:value-of 
         select="$toolLookup/@realId"/></xsl:when><xsl:otherwise><xsl:value-of 
         select="$toolLookup/@id"/></xsl:otherwise></xsl:choose></xsl:variable>
      <tool id="{$toolId}">
         <xsl:if test="$toolId = 'sakai.siteinfo'">
            <xsl:attribute name="required">true</xsl:attribute>
         </xsl:if>
      </tool>
   </xsl:template>

   <xsl:template name="siteTypeBean">
      <xsl:param name="siteType"/>
      <xsl:variable name="shortId" select="$siteType/@shortTypeId"/>
      <xsl:variable name="order" select="$siteType/@order"/>
      <xsl:variable name="toolCatFile" select="concat('output/', $shortId, '-scrubbed.xml')"/>
      <xsl:variable name="toolCategories" select="document($toolCatFile)/toolCategories"/>

      <xsl:call-template name="globalSiteTypeBean">
         <xsl:with-param name="shortId" select="$shortId"/>
         <xsl:with-param name="order" select="$order"/>
         <xsl:with-param name="toolCategories" select="$toolCategories"/>
         <xsl:with-param name="siteTypeName" select="$siteType/groupName"/>
         <xsl:with-param name="siteTypeDescription" select="$siteType/groupDescription"/>
      </xsl:call-template>
   </xsl:template>

   <xsl:template name="siteBean">
      <xsl:param name="site"/>
      <xsl:variable name="shortId" select="$site/@typeId"/>
      <xsl:variable name="order" select="0"/>
      <xsl:variable name="toolCatFile" select="concat('output/', $shortId, '-scrubbed.xml')"/>
      <xsl:variable name="toolCategories" select="document($toolCatFile)/toolCategories"/>

      <xsl:call-template name="globalSiteTypeBean">
         <xsl:with-param name="shortId" select="$shortId"/>
         <xsl:with-param name="order" select="$order"/>
         <xsl:with-param name="toolCategories" select="$toolCategories"/>
         <xsl:with-param name="siteTypeName" select="$site/name"/>
         <xsl:with-param name="siteTypeDescription" select=""/>
      </xsl:call-template>
   </xsl:template>

   <xsl:template name="mywsBean">
      <xsl:param name="myws"/>
      <xsl:variable name="shortId" select="$myws/@typeId"/>
      <xsl:variable name="order" select="0"/>

      <xsl:call-template name="globalSiteTypeBean">
         <xsl:with-param name="shortId" select="$shortId"/>
         <xsl:with-param name="order" select="$order"/>
         <xsl:with-param name="toolCategories" select="$myws"/>
         <xsl:with-param name="siteTypeName" select=""/>
         <xsl:with-param name="siteTypeDescription" select=""/>
      </xsl:call-template>
   </xsl:template>

   <xsl:template name="categoryBeanId">
      <xsl:param name="shortId"/>
      <xsl:param name="category"/>
      <xsl:choose>
         <xsl:when test="$category/@type = 'uncategorized'">
            <xsl:value-of select="$toolCategoryBeanPrefix"/><xsl:value-of select="$shortId"/>.root<xsl:value-of select="$category/@order"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="$toolCategoryBeanPrefix"/><xsl:value-of select="$shortId"/>.<xsl:value-of select="$category/@shortName"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

</xsl:stylesheet>