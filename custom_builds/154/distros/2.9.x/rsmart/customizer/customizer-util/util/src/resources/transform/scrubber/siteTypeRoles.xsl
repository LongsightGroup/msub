<?xml version="1.0" ?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:o="urn:schemas-microsoft-com:office:office"
                xmlns:x="urn:schemas-microsoft-com:office:excel"
                xmlns:dt="uuid:C2F41010-65B3-11d1-A29F-00AA00C14882"
                xmlns:html="http://www.w3.org/TR/REC-html40"
                xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">

   <xsl:variable name="siteTabs" select="document('output/tabs.xml')"/>

   <xsl:template match="Worksheet">
      <siteTypeRoles>
         <sites>
            <xsl:for-each select="/Worksheet/Table/Row[@order>5]/Cell[@order=2]/Data">
               <xsl:call-template name="siteRow">
                  <xsl:with-param name="sitesFlag" select="'true'"/>
                  <xsl:with-param name="siteRow" select="../.."/>
               </xsl:call-template>
            </xsl:for-each>
         </sites>
         <siteTypes>
            <xsl:for-each select="/Worksheet/Table/Row[@order>5]/Cell[@order=2]/Data">
               <xsl:call-template name="siteRow">
                  <xsl:with-param name="sitesFlag" select="'false'"/>
                  <xsl:with-param name="siteRow" select="../.."/>
               </xsl:call-template>
            </xsl:for-each>
         </siteTypes>
      </siteTypeRoles>
   </xsl:template>

   <xsl:template name="siteRow">
      <xsl:param name="sitesFlag"/>
      <xsl:param name="siteRow"/>
      <xsl:variable name="siteId" select="normalize-space($siteRow/Cell[@order=5]/Data)"/>
      <xsl:choose>
         <xsl:when test="$sitesFlag = 'true' and ($siteId = '!admin')">
            <xsl:call-template name="adminSite">
               <xsl:with-param name="siteRow" select="$siteRow" />
            </xsl:call-template>
         </xsl:when>
         <xsl:when test="$sitesFlag = 'false' and $siteId != '!admin'">
            <xsl:call-template name="siteType">
               <xsl:with-param name="siteTypeRow" select="$siteRow" />
            </xsl:call-template>
         </xsl:when>
      </xsl:choose>
   </xsl:template>

   <xsl:template name="adminSite">
      <xsl:param name="siteRow"/>
      <xsl:variable name="siteId" select="normalize-space($siteRow/Cell[@order=5]/Data)"/>
      <xsl:variable name="siteName" select="normalize-space($siteRow/Cell[@order=2]/Data)"/>
      <site>
         <xsl:attribute name="id">
            <xsl:value-of select="$siteId"/>
         </xsl:attribute>
         <xsl:attribute name="typeId">
            <xsl:value-of select="'aw'"/>
         </xsl:attribute>
         <xsl:attribute name="order">
            <xsl:value-of select="$siteRow/@order"/>
         </xsl:attribute>
         <name><xsl:value-of select="$siteName"/></name>
         <xsl:variable name="rolesStart" select="$siteRow/@order + 2"/>
         <xsl:variable name="group" select="$siteRow/@group2"/>

         <roles>
            <xsl:for-each select="$siteRow/../Row[@order>=$rolesStart and @group2=$group]">
               <xsl:if test="Cell[@order=3]/Data">
                  <xsl:call-template name="role">
                     <xsl:with-param name="roleRow" select="."/>
                     <xsl:with-param name="order" select="@order - $rolesStart"/>
                  </xsl:call-template>
               </xsl:if>
            </xsl:for-each>
         </roles>
      </site>
   </xsl:template>

   <xsl:template name="siteType">
      <xsl:param name="siteTypeRow"/>
      <xsl:variable name="siteTypeId" select="normalize-space($siteTypeRow/Cell[@order=5]/Data)"/>
      <xsl:variable name="siteTypeName" select="normalize-space($siteTypeRow/Cell[@order=2]/Data)"/>
      <xsl:variable name="tabRow" select="$siteTabs/Worksheet/Table/Row/Cell[@order=4]/Data[. = $siteTypeName]/../.."/>

      <xsl:if test="not($tabRow)">
         <xsl:message>WARNING:  Couldn't find site type in tab worksheet:  <xsl:value-of select="$siteTypeName" /></xsl:message>
      </xsl:if>

      <siteType>
         <xsl:attribute name="id">
            <xsl:value-of select="$siteTypeId"/>
         </xsl:attribute>
         <xsl:if test="starts-with($siteTypeId, '!site.template.')">
            <xsl:attribute name="shortTypeId">
               <xsl:value-of select="substring($siteTypeId, 16)" />
            </xsl:attribute>
            <xsl:attribute name="groupId">
               <xsl:value-of select="concat('!group.template.', substring($siteTypeId, 16))" />
            </xsl:attribute>
         </xsl:if>
         <xsl:attribute name="order">
            <xsl:value-of select="$tabRow/@order"/>
         </xsl:attribute>
         <name><xsl:value-of select="$siteTypeName"/></name>
         <groupName>
            <xsl:call-template name="groupName"><xsl:with-param name="tabRow" select="$tabRow"/></xsl:call-template>
         </groupName>
         <groupDescription><xsl:value-of select="$tabRow/Cell[@order=5]/Data" /></groupDescription>
         <xsl:variable name="rolesStart" select="$siteTypeRow/@order + 2"/>
         <xsl:variable name="group" select="$siteTypeRow/@group2"/>
         <roles>
            <xsl:for-each select="$siteTypeRow/../Row[@order>=$rolesStart and @group2=$group]">
               <xsl:if test="Cell[@order=3]/Data">
                  <xsl:call-template name="role">
                     <xsl:with-param name="roleRow" select="."/>
                     <xsl:with-param name="order" select="@order - $rolesStart"/>
                  </xsl:call-template>
               </xsl:if>
            </xsl:for-each>
         </roles>
      </siteType>
   </xsl:template>

   <xsl:template name="groupName">
      <xsl:param name="tabRow"/>
      <xsl:variable name="tabRowOrder" select="$tabRow/@order"/>
      <xsl:choose>
         <xsl:when test="$tabRow/Cell[@order=3]/Data">
            <xsl:value-of select="$tabRow/Cell[@order=3]/Data" />
         </xsl:when>
         <xsl:otherwise>
            <xsl:call-template name="groupName">
               <xsl:with-param name="tabRow" select="$tabRow/../Row[@order=$tabRowOrder - 1]"/>
            </xsl:call-template>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <xsl:template name="role">
      <xsl:param name="roleRow"/>
      <xsl:param name="order"/>
      <role>
         <xsl:attribute name="id"><xsl:value-of select="Cell[@order=3]/Data"/></xsl:attribute>
         <xsl:attribute name="order"><xsl:value-of select="$order"/></xsl:attribute>
         <xsl:if test="Cell[@order=4]/Data = 'x'">
            <xsl:attribute name="maintainer">true</xsl:attribute>
         </xsl:if>
         <description>
            <xsl:value-of select="Cell[@order=5]/Data"/>
         </description>
      </role>
   </xsl:template>

</xsl:stylesheet>