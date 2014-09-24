<?xml version="1.0" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

   <xsl:output method="xml" version="1.0" encoding="UTF-8" omit-xml-declaration="no"
               doctype-public="-//SPRING//DTD BEAN//EN"
               doctype-system="http://www.springframework.org/dtd/spring-beans.dtd"
               indent="yes" />

   <xsl:variable name="siteTypes" select="document('output/site-role-scrubbed.xml')"/>
   <xsl:variable name="tools" select="document('output/tools-scrubbed.xml')"/>
   <xsl:variable name="myws" select="document('output/myws-scrubbed.xml')"/>

   <xsl:variable name="siteTypeBeanPrefix" select="'org.theospi.portfolio.portal.model.SiteType.'"/>
   <xsl:variable name="toolCategoryBeanPrefix" select="'org.theospi.portfolio.portal.model.ToolCategory.'"/>

   <xsl:template match="beans">
      <beans>
         <xsl:apply-templates select="@*|node()" >
         </xsl:apply-templates>
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
      </beans>
   </xsl:template>

   <xsl:template match="bean[@id='org.theospi.portfolio.portal.intf.PortalManager' or
      @id='com.rsmart.admin.customizer.impl.SiteTypeMapHolder']">
      <bean>
         <xsl:apply-templates select="@*"/>
         <xsl:apply-templates select="property[@name!='siteTypes']" />
         <property name="siteTypes">
            <map>
               <xsl:for-each select="$siteTypes/siteTypeRoles/siteTypes/siteType">
                  <xsl:call-template name="siteTypeEntry">
                     <xsl:with-param name="shortTypeId" select="@shortTypeId"/>
                  </xsl:call-template>
               </xsl:for-each>
               <xsl:for-each select="$siteTypes/siteTypeRoles/sites/site">
                  <xsl:call-template name="siteTypeEntry">
                     <xsl:with-param name="shortTypeId" select="@typeId"/>
                  </xsl:call-template>
               </xsl:for-each>
               <xsl:for-each select="$myws/myws/toolCategories">
                  <xsl:call-template name="siteTypeEntry">
                     <xsl:with-param name="shortTypeId" select="@typeId"/>
                  </xsl:call-template>
               </xsl:for-each>
            </map>
         </property>
      </bean>

   </xsl:template>

   <xsl:template name="globalSiteTypeBean">
      <xsl:param name="shortId"/>
      <xsl:param name="order"/>
      <xsl:param name="toolCategories"/>
      <xsl:param name="siteTypeName"/>
      <xsl:param name="siteTypeDescription"/>

      <bean abstract="false" autowire="default" class="org.theospi.portfolio.portal.model.SiteType"
            dependency-check="default" lazy-init="default" singleton="true">
         <xsl:attribute name="id">
            <xsl:value-of select="concat($siteTypeBeanPrefix, $shortId)"/>
         </xsl:attribute>

         <property name="key">
            <value>org.theospi.portfolio.portal.<xsl:value-of select="$shortId" /></value>
         </property>
         <xsl:if test="$siteTypeName != ''">
            <property name="displayName">
               <value><xsl:value-of select="$siteTypeName" /></value>
            </property>
         </xsl:if>
         <xsl:if test="$siteTypeDescription != ''">
            <property name="description">
               <value><xsl:value-of select="$siteTypeDescription" /></value>
            </property>
         </xsl:if>
         <xsl:if test="$shortId = 'aw'">
            <property name="displayTab" value="false"/>
         </xsl:if>
         <property name="name">
            <value><xsl:value-of select="$shortId"/></value>
         </property>
         <property name="order">
            <value><xsl:value-of select="$order"/></value>
         </property>
         <property name="firstCategory">
            <value>0</value>
         </property>
         <property name="lastCategory">
            <value>0</value>
         </property>
         <property name="toolCategories">
            <list>
               <xsl:for-each select="$toolCategories/category">
                  <xsl:choose>
                     <xsl:when test="tools/tool[@id='rsmart.home.page']"/>
                     <xsl:otherwise>
                        <ref>
                           <xsl:attribute name="bean">
                              <xsl:call-template name="categoryBeanId">
                                 <xsl:with-param name="shortId" select="$shortId"/>
                                 <xsl:with-param name="category" select="."/>
                              </xsl:call-template>
                           </xsl:attribute>
                        </ref>
                     </xsl:otherwise>
                  </xsl:choose>
               </xsl:for-each>
            </list>
         </property>
         <xsl:if test="$shortId = 'portfolioAdmin'">
            <property name="specialSites">
               <list>
                  <value>!admin</value>
               </list>
            </property>
         </xsl:if>
      </bean>

      <xsl:for-each select="$toolCategories/category">
         <xsl:choose>
            <xsl:when test="tools/tool[@id='rsmart.home.page']"/>
            <xsl:otherwise>
               <xsl:call-template name="categoryBean">
                  <xsl:with-param name="shortId" select="$shortId"/>
                  <xsl:with-param name="category" select="."/>
               </xsl:call-template>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:for-each>

   </xsl:template>

   <xsl:template name="categoryBean">
      <xsl:param name="shortId"/>
      <xsl:param name="category"/>
      <bean abstract="false" autowire="default" class="org.theospi.portfolio.portal.model.ToolCategory"
            dependency-check="default" lazy-init="default" singleton="true">
         <xsl:attribute name="id">
            <xsl:call-template name="categoryBeanId">
               <xsl:with-param name="shortId" select="$shortId"/>
               <xsl:with-param name="category" select="$category"/>
            </xsl:call-template>
         </xsl:attribute>
         <property name="order">
            <xsl:attribute name="value"><xsl:value-of select="$category/@order"/></xsl:attribute>
         </property>
         <xsl:choose>
            <xsl:when test="$category/@type='uncategorized'">
               <property name="key" value="org.theospi.portfolio.portal.model.ToolCategory.uncategorized"/>
            </xsl:when>
            <xsl:otherwise>
               <property name="key">
                  <xsl:attribute name="value">
                     <xsl:call-template name="categoryBeanId">
                        <xsl:with-param name="shortId" select="$shortId"/>
                        <xsl:with-param name="category" select="$category"/>
                     </xsl:call-template>
                  </xsl:attribute>
               </property>
               <property name="homePagePath">
                  <xsl:attribute name="value"><xsl:value-of
                     select="$shortId"/>.<xsl:value-of
                     select="$category/@shortName"/>.xhtml</xsl:attribute>
               </property>
               <property name="description">
                  <value><xsl:value-of select="$category/name"/></value>
               </property>
            </xsl:otherwise>
         </xsl:choose>
         <property name="tools">
           <map>
               <xsl:for-each select="$category/tools/tool">
                  <xsl:call-template name="toolBean">
                     <xsl:with-param name="tool" select="."/>
                  </xsl:call-template>
               </xsl:for-each>
           </map>
         </property>
      </bean>
   </xsl:template>

   <xsl:template name="toolBean">
      <xsl:param name="tool"/>
      <xsl:variable name="toolId" select="$tool/@id"/>
      <entry>
         <xsl:choose>
            <xsl:when test="$tool/@realId">
               <xsl:attribute name="key"><xsl:value-of select="$tool/@realId"/></xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
               <xsl:attribute name="key"><xsl:value-of select="$toolId"/></xsl:attribute>
            </xsl:otherwise>
         </xsl:choose>
         <bean class="org.theospi.portfolio.portal.model.ToolType">
            <property name="order">
               <xsl:attribute name="value"><xsl:value-of select="$tool/@order"/></xsl:attribute>
            </property>
            <xsl:call-template name="toolFunctions">
               <xsl:with-param name="tool" select="$tool"/>
               <xsl:with-param name="toolLookup" select="$tools/tools/tool[@id = $toolId]"/>
            </xsl:call-template>
         </bean>
      </entry>
   </xsl:template>

   <xsl:template name="toolFunctions">
      <xsl:param name="tool"/>
      <xsl:param name="toolLookup"/>

      <property name="qualifierType">
         <ref bean="org.theospi.portfolio.portal.model.ToolType.SAKAI_QUALIFIER"/>
      </property>
      <property name="functions">
         <list>
            <xsl:choose>
               <xsl:when test="$tool/functions/function">
                  <xsl:for-each select="$tool/functions/function">
                     <xsl:sort select="@order" data-type="number" />
                     <value><xsl:value-of select="@id"/></value>
                  </xsl:for-each>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:for-each select="$toolLookup/functions/function">
                     <xsl:sort select="@order" data-type="number" />
                     <value><xsl:value-of select="@id"/></value>
                  </xsl:for-each>
               </xsl:otherwise>
            </xsl:choose>
         </list>
      </property>
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

   <xsl:template name="siteTypeEntry">
      <xsl:param name="shortTypeId"/>
      <entry>
         <xsl:attribute name="key"><xsl:value-of select="$shortTypeId"/></xsl:attribute>
         <ref>
            <xsl:attribute name="bean"><xsl:value-of select="concat($siteTypeBeanPrefix, $shortTypeId)"/></xsl:attribute>
         </ref>
      </entry>
   </xsl:template>

   <xsl:template match="bean[@class='org.theospi.portfolio.portal.model.SiteType']"></xsl:template>
   <xsl:template match="bean[@class='org.theospi.portfolio.portal.model.ToolCategory']"></xsl:template>
   <xsl:template match="bean[@class='org.theospi.portfolio.portal.model.ToolType']"></xsl:template>

   <!-- Identity transformation -->
   <xsl:template match="@*|*">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()" >
         </xsl:apply-templates>
      </xsl:copy>
   </xsl:template>

</xsl:stylesheet>