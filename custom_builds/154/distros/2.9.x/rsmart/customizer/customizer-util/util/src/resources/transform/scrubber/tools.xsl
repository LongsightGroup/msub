<?xml version="1.0" ?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:o="urn:schemas-microsoft-com:office:office"
                xmlns:x="urn:schemas-microsoft-com:office:excel"
                xmlns:dt="uuid:C2F41010-65B3-11d1-A29F-00AA00C14882"
                xmlns:html="http://www.w3.org/TR/REC-html40"
                xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">

   <xsl:variable name="perms" select="document('output/s_permissions.xml')"/>

	<xsl:template match="Worksheet">
      <tools>
         <xsl:apply-templates select="/Worksheet/Table"/>
      </tools>
   </xsl:template>

   <xsl:template match="Row[@order>6]">
      <xsl:if test="Cell[@order=4]/Data">
         <xsl:variable name="regFile" select="concat('source/', Cell[@order=4]/Data)" />
         <xsl:variable name="reg" select="document($regFile)"/>
         <xsl:choose>
            <xsl:when test="$reg/registration/tool/@id">
               <xsl:call-template name="toolEntry">
                  <xsl:with-param name="row" select="."/>
                  <xsl:with-param name="reg" select="$reg"/>
               </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
               <xsl:message>WARNING: invalid or missing reg file: <xsl:value-of select="$regFile"/></xsl:message>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:if>
   </xsl:template>

   <xsl:template name="toolEntry">
      <xsl:param name="row"/>
      <xsl:param name="reg"/>
      <tool>
         <xsl:variable name="toolId" select="$reg/registration/tool/@id"/>
         <xsl:attribute name="id"><xsl:value-of select="$toolId"/></xsl:attribute>
         <xsl:if test="$reg/registration/tool/@realId">
            <xsl:attribute name="realId"><xsl:value-of select="$reg/registration/tool/@realId"/></xsl:attribute>
         </xsl:if>
         <xsl:attribute name="name"><xsl:value-of select="$row/Cell[@order=2]/Data"/></xsl:attribute>
         <xsl:attribute name="regFile">
            <xsl:value-of select="$row/Cell[@order=4]/Data"/>
         </xsl:attribute>
         <xsl:if test="$reg/registration/tool/@fake = 'true'">
            <xsl:attribute name="fake">true</xsl:attribute>
         </xsl:if>
         <icon>
            <xsl:value-of select="$row/Cell[@order=5]/Data"/>
         </icon>
         <description>
            <xsl:value-of select="$row/Cell[@order=3]/Data"/>
         </description>
         <functions type="sakai">
            <xsl:call-template name="listFunctions">
               <xsl:with-param name="toolName" select="$row/Cell[@order=2]/Data"/>
            </xsl:call-template>
         </functions>
         <defaultConfig>
            <xsl:for-each select="$reg/registration/tool/configuration">
               <xsl:copy-of select="."/>
            </xsl:for-each>
         </defaultConfig>
      </tool>
   </xsl:template>

   <xsl:template name="listFunctions">
      <xsl:param name="toolName"/>
      <xsl:for-each select="$perms/Worksheet/Table/Row[@order>6]/Cell[@order=2]/Data">
         <xsl:if test=". = $toolName">
            <xsl:variable name="funcStart" select="../../@order"/>
            <xsl:variable name="group2" select="../../@group2"/>

            <xsl:for-each select="$perms/Worksheet/Table/Row[@order>$funcStart and @group2 = $group2]">
               <function>
                  <xsl:attribute name="id">
                     <xsl:value-of select="Cell[@order=4]/Data"/>
                  </xsl:attribute>
                  <xsl:attribute name="order">
                     <xsl:value-of select="@order - $funcStart"/>
                  </xsl:attribute>
                  <description>
                     <xsl:value-of select="Cell[@order=3]/Data"/>
                  </description>
               </function>
            </xsl:for-each>
         </xsl:if>
      </xsl:for-each>
   </xsl:template>

   <xsl:template match="Row">
   </xsl:template>

</xsl:stylesheet>
