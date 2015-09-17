<?xml version="1.0" ?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:rsn="com.rsmart.util.xml.XmlXpathFuncs"
                xmlns:o="urn:schemas-microsoft-com:office:office"
                xmlns:x="urn:schemas-microsoft-com:office:excel"
                xmlns:dt="uuid:C2F41010-65B3-11d1-A29F-00AA00C14882"
                xmlns:html="http://www.w3.org/TR/REC-html40"
                xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">

   <xsl:variable name="roleRow" select="Worksheet/Table/Row[@order=7]"/>
   <xsl:variable name="siteTypeRow" select="Worksheet/Table/Row[@order=6]"/>

   <xsl:variable name="tools" select="document('output/tools-scrubbed.xml')"/>
   <xsl:variable name="siteRoles" select="document('output/site-role-scrubbed.xml')"/>
   <xsl:variable name="myws" select="document('output/myws-scrubbed.xml')"/>

   <xsl:template match="Worksheet">
      <perms>
         <xsl:apply-templates select="$tools/tools"/>
      </perms>
   </xsl:template>

   <xsl:template match="tool">
      <tool>
         <xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
         <xsl:attribute name="type"><xsl:value-of select="functions/@type" /></xsl:attribute>
         <xsl:apply-templates select="$siteRoles/siteTypeRoles/sites">
            <xsl:with-param name="currentTool" select="."/>
         </xsl:apply-templates>
         <xsl:apply-templates select="$siteRoles/siteTypeRoles/siteTypes">
            <xsl:with-param name="currentTool" select="."/>
         </xsl:apply-templates>
         <xsl:call-template name="mywsRoles">
            <xsl:with-param name="currentTool" select="."/>
         </xsl:call-template>
      </tool>
   </xsl:template>

   <xsl:template name="mywsRoles">
      <xsl:param name="currentTool"/>
      <xsl:variable name="userSiteStart" select="rsn:findOrder($siteTypeRow, 'User Worksites')/@order"/>

      <xsl:for-each select="$roleRow/Cell[@order>=$userSiteStart]">
         <xsl:if test="Data != ''">
            <xsl:variable name="siteName" select="Data"/>
            <xsl:variable name="siteId" select="$myws/myws/toolCategories[@siteName=$siteName]/@siteId"/>
            <xsl:variable name="column" select="@order"/>
            <xsl:if test="$siteId != '~admin'">
               <realm>
                  <xsl:attribute name="id"><xsl:value-of
                     select="concat('!user.template.', substring($siteId, 7))"/></xsl:attribute>
                  <role id=".auth">
                     <xsl:for-each select="$currentTool/functions/function">
                        <xsl:variable name="currentFunction" select="."/>
                        <xsl:variable name="functionCellData"
                           select="/Worksheet/Table/Row[@order>=8]/Cell[@order=4]/Data[. = $currentFunction/@id]"/>
                        <xsl:variable name="functionRow" select="$functionCellData/../.."/>
                        <xsl:if test="$functionRow/Cell[@order=$column]/Data = 'x'
                                 or $functionRow/Cell[@order=$column]/Data = 'X'">
                           <function>
                              <xsl:attribute name="id"><xsl:value-of select="$currentFunction/@id"/></xsl:attribute>
                           </function>
                        </xsl:if>
                     </xsl:for-each>
                  </role>
               </realm>
            </xsl:if>
         </xsl:if>
      </xsl:for-each>

   </xsl:template>

   <xsl:template match="site">
      <xsl:param name="currentTool"/>
      <realm>
         <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
         <xsl:apply-templates select="roles">
            <xsl:with-param name="currentTool" select="$currentTool"/>
         </xsl:apply-templates>
      </realm>
   </xsl:template>

   <xsl:template match="siteType">
      <xsl:param name="currentTool"/>
      <realm>
         <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
         <xsl:attribute name="siteType"><xsl:value-of select="@shortTypeId" /></xsl:attribute>
         <xsl:apply-templates select="roles">
            <xsl:with-param name="currentTool" select="$currentTool"/>
         </xsl:apply-templates>
      </realm>
   </xsl:template>

   <xsl:template match="role">
      <xsl:param name="currentTool"/>
      <role>
         <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
         <xsl:apply-templates select="$currentTool/functions">
            <xsl:with-param name="currentRole" select="."/>
         </xsl:apply-templates>
      </role>
   </xsl:template>

   <xsl:template match="function">
      <xsl:param name="currentRole"/>
      <xsl:variable name="currentSiteType" select="$currentRole/../../name"/>
      <xsl:variable name="currentFunction" select="."/>

      <xsl:variable name="currentFunctionRow"
                    select="/Worksheet/Table/Row[@order>=8]/Cell[@order=4]/Data[. = $currentFunction/@id]/../.."/>
      <xsl:for-each select="$siteTypeRow/Cell[@order>=5]">
         <xsl:if test="$currentSiteType = Data">
            <xsl:variable name="roleColumn" select="@order + $currentRole/@order"/>
            <xsl:choose>
               <xsl:when test="$roleRow/Cell[@order=$roleColumn]/Data = $currentRole/@id">
                  <xsl:if test="$currentFunctionRow/Cell[@order=$roleColumn]/Data = 'x'
                        or $currentFunctionRow/Cell[@order=$roleColumn]/Data = 'X'">
                     <function>
                        <xsl:attribute name="id"><xsl:value-of select="$currentFunction/@id"/></xsl:attribute>
                     </function>
                  </xsl:if>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:message>WARNING:Didn't find role: <xsl:value-of select="$currentRole/@id"/>
                     found <xsl:value-of select="$roleRow/Cell[@order=$roleColumn]/Data"/>
                  </xsl:message>
               </xsl:otherwise>
            </xsl:choose>
         </xsl:if>
      </xsl:for-each>
   </xsl:template>

</xsl:stylesheet>
