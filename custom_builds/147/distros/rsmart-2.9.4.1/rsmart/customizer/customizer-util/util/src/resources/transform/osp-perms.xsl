<?xml version="1.0" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

   <xsl:output method="xml" version="1.0" encoding="UTF-8" omit-xml-declaration="no"
               doctype-public="-//SPRING//DTD BEAN//EN"
               doctype-system="http://www.springframework.org/dtd/spring-beans.dtd"
               indent="yes" />

   <xsl:param name="rsmartRuntime"/>
   <xsl:variable name="perms" select="document('output/perms-scrubbed.xml')"/>
   <xsl:variable name="tools" select="document('output/tools-scrubbed.xml')"/>
   <xsl:variable name="siteRoles" select="document('output/site-role-scrubbed.xml')"/>
   <xsl:variable name="beanPrefix" select="'com.rsmart.sakai.security.impl.PermissionInjector.'"/>

   <xsl:template match="beans">
      <beans>
         <xsl:apply-templates select="@*|node()" >
         </xsl:apply-templates>
         <xsl:for-each select="$tools/tools/tool/functions[@type='osp']">
            <xsl:call-template name="createPermissionInjector">
               <xsl:with-param name="tool" select=".."/>
            </xsl:call-template>
         </xsl:for-each>
      </beans>
   </xsl:template>

   <xsl:template match="bean[@class='com.rsmart.sakai.security.impl.PermissionInjector']">
   </xsl:template>

   <xsl:template name="createPermissionInjector">
      <xsl:param name="tool"/>
      <xsl:variable name="regFilePath" select="$tool/@regFile"/>
      <xsl:variable name="regFile" select="document(concat('source/', $regFilePath))"/>
      <xsl:variable name="toolId" select="$tool/@id"/>

      <bean class="com.rsmart.sakai.security.impl.PermissionInjector" init-method="init"
         singleton="true">
         <xsl:if test="$rsmartRuntime = 'true'">
            <xsl:attribute name="class">com.rsmart.admin.customizer.impl.CustomizerPermissionInjector</xsl:attribute>
         </xsl:if>
         <xsl:attribute name="id"><xsl:value-of select="concat($beanPrefix, $tool/@id)"/></xsl:attribute>
         <property name="permissionManager">
            <xsl:attribute name="ref">
               <xsl:value-of
                  select="$regFile/registration/tool/configuration[@name='theospi.toolListenerId']/@value" />
            </xsl:attribute>
         </property>
         <property name="permissionInjectorService" 
                   ref="com.rsmart.sakai.security.intf.PermissionInjectorService"/>
         <property name="key">
            <xsl:attribute name="value">
               <xsl:value-of
                  select="$regFile/registration/tool/configuration[@name='theospi.toolListenerId']/@value" />
            </xsl:attribute>
         </property>
         <property name="siteTypePermMap">
            <map>
               <xsl:for-each select="$perms/perms/tool[@id=$toolId]/realm[@siteType]">
                  <entry>
                     <xsl:attribute name="key"><xsl:value-of select="@siteType" /></xsl:attribute>
                     <map>
                        <xsl:for-each select="role">
                           <xsl:call-template name="rolePerms">
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

   <xsl:template name="rolePerms">
      <xsl:param name="role"/>
      <entry>
         <xsl:attribute name="key"><xsl:value-of select="$role/@id" /></xsl:attribute>
         <list>
            <xsl:for-each select="$role/function">
               <value><xsl:value-of select="@id" /></value>
            </xsl:for-each>
         </list>
      </entry>
   </xsl:template>

   <!-- Identity transformation -->
   <xsl:template match="@*|*">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()" >
         </xsl:apply-templates>
      </xsl:copy>
   </xsl:template>

</xsl:stylesheet>