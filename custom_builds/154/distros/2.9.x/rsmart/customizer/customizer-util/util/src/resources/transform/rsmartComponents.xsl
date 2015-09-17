<?xml version="1.0" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

   <xsl:output method="xml" version="1.0" encoding="UTF-8" omit-xml-declaration="no"
               doctype-public="-//SPRING//DTD BEAN//EN"
               doctype-system="http://www.springframework.org/dtd/spring-beans.dtd"
               indent="yes" />

   <xsl:param name="rsmartRuntime"/>
   <xsl:variable name="portfolioAdmin" select="document('output/portfolioAdmin-scrubbed.xml')"/>
   <xsl:variable name="aw" select="document('output/aw-scrubbed.xml')"/>
   <xsl:variable name="myws" select="document('output/myws-scrubbed.xml')"/>
   <xsl:variable name="tools" select="document('output/tools-scrubbed.xml')"/>
   <xsl:variable name="sitesRoles" select="document('output/site-role-scrubbed.xml')"/>

   <xsl:template match="beans">
      <beans>
         <xsl:apply-templates select="@*|node()" >
         </xsl:apply-templates>
      </beans>
   </xsl:template>

   <xsl:template match="bean[@id='org.theospi.portfolio.admin.intf.SakaiIntegrationPlugin.cleanupTools']">
      <bean>
         <xsl:apply-templates select="@*" />
         <xsl:apply-templates select="property[@name!='potentialIntegrations']" />
         <property name="potentialIntegrations">
            <list>
               <ref bean="org.theospi.portfolio.admin.model.IntegrationOption.cleanupAdminWorkspace"/>
               <ref bean="org.theospi.portfolio.admin.model.IntegrationOption.cleanupPortfolioAdmin"/>
               <xsl:for-each select="$myws/myws/toolCategories">
                  <ref>
                     <xsl:attribute name="bean">
                        <xsl:value-of select="concat(
                           'org.theospi.portfolio.admin.model.IntegrationOption.cleanup.',
                           translate(@siteId, '!~', 'bt'))"/>
                     </xsl:attribute>
                  </ref>
               </xsl:for-each>
            </list>
         </property>
      </bean>

      <xsl:call-template name="cleanupOptionBean">
         <xsl:with-param name="beanId"
                         select="'org.theospi.portfolio.admin.model.IntegrationOption.cleanupAdminWorkspace'"/>
         <xsl:with-param name="toolCategories" select="$aw/toolCategories"/>
         <xsl:with-param name="siteId" select="'!admin'"/>
      </xsl:call-template>

      <xsl:call-template name="cleanupOptionBean">
         <xsl:with-param name="beanId"
                         select="'org.theospi.portfolio.admin.model.IntegrationOption.cleanupPortfolioAdmin'"/>
         <xsl:with-param name="toolCategories" select="$portfolioAdmin/toolCategories"/>
         <xsl:with-param name="siteId" select="'PortfolioAdmin'"/>
      </xsl:call-template>

      <xsl:call-template name="mywsCleanupOptions"/>

   </xsl:template>

   <xsl:template match="bean[@id='org.theospi.portfolio.admin.intf.SakaiIntegrationPlugin.rsmartSites']">
      <bean>
         <xsl:apply-templates select="@*" />
         <xsl:apply-templates select="property[@name!='potentialIntegrations']" />
         <property name="potentialIntegrations">
            <list>
               <ref bean="org.theospi.portfolio.admin.model.IntegrationOption.rsmartSite.aw"></ref>
               <ref bean="org.theospi.portfolio.admin.model.IntegrationOption.rsmartSite.portfolioAdmin"></ref>
               <xsl:for-each select="$myws/myws/toolCategories">
                  <ref>
                     <xsl:attribute name="bean">
                        <xsl:value-of select="concat(
                           'org.theospi.portfolio.admin.model.IntegrationOption.rsmartSite.',
                           translate(@siteId, '!~', 'bt'))"/>
                     </xsl:attribute>
                  </ref>
               </xsl:for-each>
            </list>
         </property>
      </bean>

      <xsl:call-template name="siteOptionBean">
         <xsl:with-param name="beanId" select="'org.theospi.portfolio.admin.model.IntegrationOption.rsmartSite.aw'"/>
         <xsl:with-param name="siteId" select="'!admin'"/>
         <xsl:with-param name="typeId" select="'aw'"/>
         <xsl:with-param name="title" select="$sitesRoles/siteTypeRoles/sites/site[@id='!admin']/name"/>
      </xsl:call-template>

      <xsl:call-template name="siteOptionBean">
         <xsl:with-param name="beanId"
                         select="'org.theospi.portfolio.admin.model.IntegrationOption.rsmartSite.portfolioAdmin'"/>
         <xsl:with-param name="siteId" select="'PortfolioAdmin'"/>
         <xsl:with-param name="typeId" select="'portfolioAdmin'"/>
         <xsl:with-param name="title"
                         select="$sitesRoles/siteTypeRoles/siteTypes/siteType[@id='portfolioAdmin']/name"/>
      </xsl:call-template>

      <xsl:call-template name="mywsSiteOptions"/>

   </xsl:template>

   <xsl:template match="bean[@id='org.theospi.portfolio.admin.intf.SakaiIntegrationPlugin.rsmartPages']">
      <bean>
         <xsl:apply-templates select="@*" />
         <xsl:apply-templates select="property[@name!='potentialIntegrations']" />
         <property name="potentialIntegrations">
            <list>
               <xsl:for-each select="$myws/myws/toolCategories/category/tools/tool[@id != '']">
                  <xsl:sort select="@order" data-type="number" />
                  <xsl:if test="../../../@siteId != '~admin' or @id != 'rsmart.home.page'">
                     <ref>
                        <xsl:attribute name="bean">
                           <xsl:call-template name="pageBeanId">
                              <xsl:with-param name="tool" select="."/>
                           </xsl:call-template>
                        </xsl:attribute>
                     </ref>
                  </xsl:if>
               </xsl:for-each>
               <xsl:for-each select="$aw/toolCategories/category/tools/tool[@id != '' and @id != 'rsmart.home.page']">
                  <xsl:sort select="@order" data-type="number" />
                  <ref>
                     <xsl:attribute name="bean">
                        <xsl:call-template name="pageBeanId">
                           <xsl:with-param name="tool" select="."/>
                        </xsl:call-template>
                     </xsl:attribute>
                  </ref>
               </xsl:for-each>
               <xsl:for-each select="$portfolioAdmin/toolCategories/category/tools/tool[@id != '']">
                  <xsl:sort select="@order" data-type="number" />
                  <ref>
                     <xsl:attribute name="bean">
                        <xsl:call-template name="pageBeanId">
                           <xsl:with-param name="tool" select="."/>
                        </xsl:call-template>
                     </xsl:attribute>
                  </ref>
               </xsl:for-each>
            </list>
         </property>
      </bean>

      <xsl:for-each select="$myws/myws/toolCategories/category/tools/tool[@id = 'rsmart.home.page']">
         <xsl:if test="../../../@siteId != '~admin'">
            <xsl:call-template name="homePageBean">
               <xsl:with-param name="tool" select="."/>
            </xsl:call-template>
         </xsl:if>
      </xsl:for-each>

      <xsl:for-each select="$myws/myws/toolCategories/category/tools/tool[@id != '' and @id != 'rsmart.home.page']">
         <xsl:call-template name="pageBean">
            <xsl:with-param name="tool" select="."/>
         </xsl:call-template>
      </xsl:for-each>

      <xsl:for-each select="$aw/toolCategories/category/tools/tool[@id != '' and @id != 'rsmart.home.page']">
         <xsl:call-template name="pageBean">
            <xsl:with-param name="tool" select="."/>
         </xsl:call-template>
      </xsl:for-each>

      <xsl:for-each select="$portfolioAdmin/toolCategories/category/tools/tool[@id != '' and @id != 'rsmart.home.page']">
         <xsl:call-template name="pageBean">
            <xsl:with-param name="tool" select="."/>
         </xsl:call-template>
      </xsl:for-each>

      <xsl:call-template name="homePageBean">
         <xsl:with-param name="tool" select="$portfolioAdmin/toolCategories/category/tools/tool[@id = 'rsmart.home.page']"/>
      </xsl:call-template>

      <xsl:for-each select="$tools/tools/tool">
         <xsl:call-template name="toolBean">
            <xsl:with-param name="tool" select="."/>
         </xsl:call-template>
      </xsl:for-each>

   </xsl:template>

   <xsl:template name="toolBean">
      <xsl:param name="tool"/>
      <bean abstract="false" autowire="default"
            class="org.theospi.portfolio.admin.service.ToolOption"
            dependency-check="default" lazy-init="default" singleton="true">
         <xsl:attribute name="id">
            <xsl:value-of select="concat('org.theospi.portfolio.admin.service.ToolOption.rsmart.', $tool/@id)" />
         </xsl:attribute>
         <property name="toolId">
            <xsl:choose>
               <xsl:when test="$tool/@realId">
                  <value><xsl:value-of select="$tool/@realId"/></value>
               </xsl:when>
               <xsl:otherwise>
                  <value><xsl:value-of select="$tool/@id"/></value>
               </xsl:otherwise>
            </xsl:choose>
         </property>
         <property name="title">
            <value><xsl:value-of select="$tool/@name" /></value>
         </property>
         <property name="layoutHints">
            <value>0,0</value>
         </property>
         <property name="initProperties">
            <props>
               <xsl:for-each select="$tool/defaultConfig/configuration">
                  <prop>
                     <xsl:attribute name="key"><xsl:value-of select="@name" /></xsl:attribute>
                     <xsl:value-of select="@value" />
                  </prop>
               </xsl:for-each>
            </props>
         </property>
      </bean>
   </xsl:template>

   <xsl:template name="homePageBean">
      <xsl:param name="tool"/>

      <xsl:variable name="toolId" select="$tool/@id"/>
      <xsl:variable name="toolLookup" select="$tools/tools/tool[@id=$toolId]"/>
      <xsl:variable name="siteId" select="$tool/../../../@siteId"/>
      <bean abstract="false" autowire="default"
            class="org.theospi.portfolio.admin.service.PageOption"
            dependency-check="default" lazy-init="default" singleton="true">
         <xsl:attribute name="id">
            <xsl:call-template name="pageBeanId">
               <xsl:with-param name="tool" select="$tool"/>
            </xsl:call-template>
         </xsl:attribute>
         <property name="label">
            <value>user site home page</value>
         </property>
         <property name="worksiteId">
            <xsl:choose>
               <xsl:when test="$siteId = '!site.template.portfolioAdmin'">
                  <value>PortfolioAdmin</value>
               </xsl:when>
               <xsl:otherwise>
                  <value><xsl:value-of select="$siteId"/></value>                  
               </xsl:otherwise>
            </xsl:choose>
         </property>
         <property name="pageName">
            <value><xsl:value-of select="$toolLookup/@name" /></value>
         </property>
         <property name="layout">
            <value>1</value>
         </property>
         <property name="positionFromEnd">
            <value>50</value>
         </property>
         <property name="tools">
            <list>
               <xsl:choose>
                  <xsl:when test="$siteId = '!site.template.portfolioAdmin'">
                     <ref bean="org.theospi.portfolio.admin.service.ToolOption.rsmart.portfolioAdminTemplateSiteInfo"/>
                     <ref bean="org.theospi.portfolio.admin.service.ToolOption.rsmart.portfolioAdminTemplateAnnouncement"/>
                  </xsl:when>
                  <xsl:otherwise>
                     <ref bean="org.theospi.portfolio.admin.service.ToolOption.rsmart.sharedTemplateMOTD"/>
                     <ref bean="org.theospi.portfolio.admin.service.ToolOption.rsmart.sharedTemplateMyWSInfo"/>
                     <ref bean="org.theospi.portfolio.admin.service.ToolOption.rsmart.sharedTemplateMyWSCal"/>
                  </xsl:otherwise>
               </xsl:choose>
            </list>
         </property>
      </bean>
   </xsl:template>

   <xsl:template name="pageBean">
      <xsl:param name="tool"/>

      <xsl:variable name="toolId" select="$tool/@id"/>
      <xsl:variable name="toolLookup" select="$tools/tools/tool[@id=$toolId]"/>
      <xsl:variable name="siteId" select="$tool/../../../@siteId"/>
      <bean abstract="false" autowire="default"
            class="org.theospi.portfolio.admin.service.PageOption"
            dependency-check="default" lazy-init="default" singleton="true">
         <xsl:attribute name="id">
            <xsl:call-template name="pageBeanId">
               <xsl:with-param name="tool" select="$tool"/>
            </xsl:call-template>
         </xsl:attribute>
         <property name="label">
            <value>site page</value>
         </property>
         <property name="worksiteId">
            <xsl:choose>
               <xsl:when test="$siteId = '!site.template.portfolioAdmin'">
                  <value>PortfolioAdmin</value>
               </xsl:when>
               <xsl:otherwise>
                  <value><xsl:value-of select="$siteId" /></value>
               </xsl:otherwise>
            </xsl:choose>
         </property>
         <property name="pageName">
            <value><xsl:value-of select="$toolLookup/@name" /></value>
         </property>
         <property name="positionFromEnd">
            <value>0</value>
         </property>
         <property name="layout">
            <value>0</value>
         </property>
         <property name="tools">
            <list>
               <ref>
                  <xsl:attribute name="bean">
                     <xsl:choose>
                        <xsl:when test="($siteId = '!admin' or $siteId = '~admin') and $toolId = 'sakai.resources'">org.theospi.portfolio.admin.service.ToolOption.rsmart.adminResources</xsl:when>
                        <xsl:otherwise><xsl:value-of select="concat('org.theospi.portfolio.admin.service.ToolOption.rsmart.', $toolId)" /></xsl:otherwise>
                     </xsl:choose>   
                  </xsl:attribute>
               </ref>
            </list>
         </property>
      </bean>
   </xsl:template>

   <xsl:template name="pageBeanId">
      <xsl:param name="tool"/>
      <xsl:variable name="siteId" select="$tool/../../../@siteId"/>
      <xsl:value-of select="concat('org.theospi.portfolio.admin.model.IntegrationOption.rsmartPage.',
         translate($siteId, '!~', 'bt'), '.', $tool/@id, '.', $tool/@order)" />
   </xsl:template>

   <xsl:template name="mywsSiteOptions">
      <xsl:for-each select="$myws/myws/toolCategories">
         <xsl:call-template name="siteOptionBean">
            <xsl:with-param name="beanId"
                            select="concat(
                              'org.theospi.portfolio.admin.model.IntegrationOption.rsmartSite.',
                              translate(@siteId, '!~', 'bt'))"/>
            <xsl:with-param name="siteId" select="@siteId"/>
            <xsl:with-param name="typeId" select="@typeId"/>
            <xsl:with-param name="title" select="'My Workspace'"/>
         </xsl:call-template>
      </xsl:for-each>
   </xsl:template>

   <xsl:template name="siteOptionBean">
      <xsl:param name="beanId"/>
      <xsl:param name="siteId"/>
      <xsl:param name="typeId"/>
      <xsl:param name="title"/>
      <bean class="org.theospi.portfolio.admin.service.SiteOption">
         <xsl:attribute name="id"><xsl:value-of select="$beanId"/></xsl:attribute>
         <property name="siteId">
            <xsl:attribute name="value"><xsl:value-of select="$siteId"/></xsl:attribute>
         </property>
         <property name="siteType">
            <xsl:attribute name="value"><xsl:value-of select="$typeId"/></xsl:attribute>
         </property>
         <property name="siteTitle">
            <xsl:attribute name="value"><xsl:value-of select="$title"/></xsl:attribute>
         </property>
         <property name="siteDescription" value="Template site for users"/>
         <property name="label" value="User Site Template Creation"/>
         <property name="include" value="true"/>
      </bean>
   </xsl:template>

   <xsl:template name="mywsCleanupOptions">
      <xsl:for-each select="$myws/myws/toolCategories">
         <xsl:call-template name="cleanupOptionBean">
            <xsl:with-param name="beanId"
                            select="concat(
                              'org.theospi.portfolio.admin.model.IntegrationOption.cleanup.',
                              translate(@siteId, '!~', 'bt'))"/>
            <xsl:with-param name="toolCategories" select="."/>
            <xsl:with-param name="siteId" select="@siteId"/>
         </xsl:call-template>
      </xsl:for-each>
   </xsl:template>

   <xsl:template name="cleanupOptionBean">
      <xsl:param name="beanId"/>
      <xsl:param name="siteId"/>
      <xsl:param name="toolCategories"/>

      <bean class="com.rsmart.sakai.osp.integration.service.CleanupSiteToolsOption">
         <xsl:attribute name="id"><xsl:value-of select="$beanId"/></xsl:attribute>
         <property name="include" value="true"/>
         <property name="label">
            <xsl:attribute name="value">
               <xsl:value-of select="concat('cleanup ', $siteId)"/>
            </xsl:attribute>
         </property>
         <property name="siteId">
            <xsl:attribute name="value"><xsl:value-of select="$siteId"/></xsl:attribute>
         </property>
         <property name="ignorePages">
            <list>
               <value>Home</value>
            </list>
         </property>
         <property name="tools">
            <list>
            </list>
         </property>
      </bean>

   </xsl:template>

   <xsl:template match="bean[@class='com.rsmart.sakai.osp.integration.service.CleanupSiteToolsOption']"></xsl:template>
   <xsl:template match="bean[@class='org.theospi.portfolio.admin.service.SiteOption']"></xsl:template>
   <xsl:template match="bean[@class='org.theospi.portfolio.admin.service.PageOption']"></xsl:template>
   <xsl:template match="bean[@class='org.theospi.portfolio.admin.service.ToolOption'
      and @id != 'org.theospi.portfolio.admin.service.ToolOption.rsmart.sharedTemplateMOTD'
      and @id != 'org.theospi.portfolio.admin.service.ToolOption.rsmart.sharedTemplateMyWSInfo'
      and @id != 'org.theospi.portfolio.admin.service.ToolOption.rsmart.portfolioAdminTemplateAnnouncement'
      and @id != 'org.theospi.portfolio.admin.service.ToolOption.rsmart.portfolioAdminTemplateSiteInfo'
      and @id != 'org.theospi.portfolio.admin.service.ToolOption.rsmart.adminResources'
      and @id != 'org.theospi.portfolio.admin.service.ToolOption.rsmart.sharedTemplateMyWSCal']
      "></xsl:template>

   <xsl:template match="bean[@id='org.theospi.portfolio.admin.intf.SakaiIntegrationService.rsmart']">
      <xsl:choose>
         <xsl:when test="$rsmartRuntime = 'true'">
            <bean class="com.rsmart.admin.customizer.impl.PluginList" 
                  id="com.rsmart.admin.customizer.impl.PluginList">
               <property name="integrationPlugins">
                  <list>
                     <value>org.theospi.portfolio.admin.intf.SakaiIntegrationPlugin.rsmartSites</value>
                     <value>org.theospi.portfolio.admin.intf.SakaiIntegrationPlugin.cleanupTools</value>
                     <value>org.theospi.portfolio.admin.intf.SakaiIntegrationPlugin.rsmartPages</value>
                  </list>
               </property>
            </bean>
                        
         </xsl:when>
         <xsl:otherwise>
            <xsl:copy>
               <xsl:apply-templates select="@*|node()" >
               </xsl:apply-templates>
            </xsl:copy>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!-- Identity transformation -->
   <xsl:template match="@*|*">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()" >
         </xsl:apply-templates>
      </xsl:copy>
   </xsl:template>

</xsl:stylesheet>