<?xml version="1.0" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

   <xsl:output method="xml" version="1.0" encoding="UTF-8" omit-xml-declaration="no"
               doctype-public="-//SPRING//DTD BEAN//EN"
               doctype-system="http://www.springframework.org/dtd/spring-beans.dtd"
               indent="yes" />

   <xsl:param name="rsmartRuntime"/>
   <xsl:variable name="perms" select="document('output/perms-scrubbed.xml')"/>
   <xsl:variable name="siteRoles" select="document('output/site-role-scrubbed.xml')"/>
   <xsl:variable name="realmPrefix" select="'org.theospi.portfolio.security.DefaultRealmManager.'"/>

   <xsl:template match="beans">
      <beans>
         <xsl:apply-templates select="@*|node()" >
         </xsl:apply-templates>
         <xsl:for-each select="$perms/perms/tool[@id='rsmart.home.page']/realm">
            <xsl:call-template name="createDefaultRealm">
               <xsl:with-param name="realm" select="."/>
            </xsl:call-template>
         </xsl:for-each>
      </beans>
   </xsl:template>

   <xsl:template match="bean[@name='org.theospi.portfolio.security.model.SakaiDefaultPermsManager.base']">
      <bean>
         <xsl:apply-templates select="@*" />
         <xsl:if test="$rsmartRuntime = 'true'">
            <xsl:attribute name="class">com.rsmart.admin.customizer.impl.CustomizerSakaiDefaultPermsManager</xsl:attribute>
            <xsl:attribute name="name">com.rsmart.admin.customizer.impl.CustomizerSakaiDefaultPermsManager.base</xsl:attribute>
         </xsl:if>
         <xsl:apply-templates select="property[@name!='realmManagers']" />
         <property name="realmManagers">
            <list>
               <xsl:for-each select="$perms/perms/tool[@id='rsmart.home.page']/realm">
                  <xsl:variable name="escapedId" select="translate(@id, '!~', 'bt')"/>
                  <ref>
                     <xsl:attribute name="bean">
                        <xsl:value-of select="concat($realmPrefix, $escapedId)"/><xsl:if 
                           test="$rsmartRuntime = 'true'">.runtime</xsl:if></xsl:attribute>
                  </ref>
               </xsl:for-each>
            </list>
         </property>
      </bean>
   </xsl:template>

   <!-- delete existing Default Realm Managers -->
   <xsl:template match="bean[@class='org.theospi.portfolio.security.model.DefaultRealmManagerImpl']">
   </xsl:template>

   <xsl:template match="bean[@id='org.theospi.portfolio.security.model.SakaiDefaultPermsManager.sakaiTools']">
      <bean>
         <xsl:apply-templates select="@*" />
         <xsl:if test="$rsmartRuntime = 'true'">
            <xsl:attribute name="parent">com.rsmart.admin.customizer.impl.CustomizerSakaiDefaultPermsManager.base</xsl:attribute>
            <xsl:attribute name="id">org.theospi.portfolio.security.model.SakaiDefaultPermsManager.sakaiTools.runtime</xsl:attribute>
         </xsl:if>
         <xsl:apply-templates select="property[@name!='defaultPermissions']" />
         <property name="defaultPermissions">
            <map>
               <xsl:for-each select="$perms/perms/tool[@id='rsmart.home.page']/realm">
                  <entry>
                     <xsl:attribute name="key"><xsl:value-of select="@id"/></xsl:attribute>
                     <map>
                        <xsl:for-each select="role">
                           <xsl:call-template name="rolePermEntry">
                              <xsl:with-param name="role" select="."/>
                           </xsl:call-template>
                        </xsl:for-each>
                     </map>
                  </entry>
               </xsl:for-each>
            </map>
         </property>
      </bean>
   </xsl:template>

   <!--
   template to remove refs to
   <ref bean="org.theospi.portfolio.security.DefaultRealmManager.portfolio"/>
   -->
   <xsl:template match="ref[@bean='org.theospi.portfolio.security.DefaultRealmManager.portfolio']">
      <xsl:comment>removed ref to org.theospi.portfolio.security.DefaultRealmManager.portfolio</xsl:comment>
   </xsl:template>

   <xsl:template name="rolePermEntry">
      <xsl:param name="role"/>
      <xsl:variable name="realmId" select="$role/../@id"/>
      <xsl:variable name="roleId" select="$role/@id"/>

      <entry>
         <xsl:attribute name="key"><xsl:value-of select="$roleId"/></xsl:attribute>
         <list>
            <xsl:for-each select="$perms/perms/tool[@type='sakai']/realm[@id=$realmId]/role[@id=$roleId]/function">
               <value><xsl:value-of select="@id"/></value>
            </xsl:for-each>
         </list>
      </entry>
   </xsl:template>

   <xsl:template name="createDefaultRealm">
      <xsl:param name="realm"/>
      <xsl:variable name="realmId" select="$realm/@id"/>
      <xsl:variable name="copiedFromId" select="$realm/@copyOf"/>
      <xsl:variable name="escapedId" select="translate($realmId, '!~', 'bt')"/>
      <bean class="org.theospi.portfolio.security.model.DefaultRealmManagerImpl"
         singleton="true" init-method="init">
         <xsl:choose>
            <xsl:when test="$rsmartRuntime = 'true'">
               <xsl:attribute name="class">com.rsmart.admin.customizer.impl.CustomizerRealmManagerImpl</xsl:attribute>
               <xsl:attribute name="id"><xsl:value-of select="concat($realmPrefix, $escapedId)"/>.runtime</xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
               <xsl:attribute name="id">
                  <xsl:value-of select="concat($realmPrefix, $escapedId)"/>
               </xsl:attribute>
            </xsl:otherwise>            
         </xsl:choose>
         <property name="authzGroupService">
            <ref bean="org.sakaiproject.authz.api.AuthzGroupService"/>
         </property>
         <property name="startupResetManager">
            <ref bean="org.theospi.portfolio.admin.intf.StartupResetManager"/>
         </property>
         <property name="newRealmName">
            <xsl:attribute name="value"><xsl:value-of select="$realmId"/></xsl:attribute>
         </property>
         <property name="recreate">
            <xsl:attribute name="value">${realm.reset}</xsl:attribute>
         </property>
         <property name="autoDdl">
            <xsl:attribute name="value">${auto.ddl}</xsl:attribute>
         </property>
         <property name="roles">
            <list>
               <xsl:for-each select="$realm/role">
                  <xsl:variable name="roleId" select="@id"/>
                  <xsl:choose>
                     <xsl:when
                        test="$siteRoles/siteTypeRoles/siteTypes/siteType[@id=$realmId]/roles/role[@id=$roleId and @maintainer='true']
                           or $siteRoles/siteTypeRoles/siteTypes/siteType[@id=$copiedFromId]/roles/role[@id=$roleId and @maintainer='true']
                           or $siteRoles/siteTypeRoles/sites/site[@id=$realmId]/roles/role[@id=$roleId and @maintainer='true']">
                        <bean class="org.theospi.portfolio.security.model.RealmRole">
                           <property name="role">
                              <xsl:attribute name="value"><xsl:value-of select="@id"/></xsl:attribute>
                           </property>
                           <property name="maintain" value="true"/>
                        </bean>
                     </xsl:when>
                     <xsl:otherwise>
                        <value><xsl:value-of select="@id"/></value>
                     </xsl:otherwise>
                  </xsl:choose>
               </xsl:for-each>
            </list>
         </property>
      </bean>
   </xsl:template>

   <xsl:template match="bean">
      <xsl:if test="$rsmartRuntime != 'true'">
         <xsl:copy>
            <xsl:apply-templates select="@*|node()" >
            </xsl:apply-templates>
         </xsl:copy>
      </xsl:if>
   </xsl:template>
   
   <!-- Identity transformation -->
   <xsl:template match="@*|*">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()" >
         </xsl:apply-templates>
      </xsl:copy>
   </xsl:template>

</xsl:stylesheet>