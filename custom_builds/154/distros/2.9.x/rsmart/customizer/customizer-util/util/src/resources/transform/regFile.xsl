<?xml version="1.0" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

   <xsl:variable name="tools" select="document('output/tools-scrubbed.xml')"/>
   <xsl:variable name="siteRoles" select="document('output/site-role-scrubbed.xml')"/>

   <xsl:template match="tool">
      <xsl:variable name="toolId" select="@id"/>
      <xsl:variable name="toolElement" select="$tools/tools/tool[@id=$toolId]"/>
      <tool>
         <xsl:attribute name="title"><xsl:value-of select="$toolElement/@name"/></xsl:attribute>
         <xsl:attribute name="description"><xsl:value-of select="$toolElement/description"/></xsl:attribute>
         <xsl:apply-templates select="@*|node()" >
         </xsl:apply-templates>
         <xsl:call-template name="processCategories">
            <xsl:with-param name="toolId" select="$toolId"/>
         </xsl:call-template>
      </tool>
   </xsl:template>

   <xsl:template name="processCategories">
      <xsl:param name="toolId"/>
      <xsl:for-each select="$siteRoles/siteTypeRoles/siteTypes/siteType[@shortTypeId]">
         <xsl:variable name="categoryFile" select="concat('output/', @shortTypeId, '-scrubbed.xml')"/>
         <xsl:variable name="category" select="document($categoryFile)"/>
         <xsl:call-template name="processCategory">
            <xsl:with-param name="toolId" select="$toolId"/>
            <xsl:with-param name="category" select="$category/toolCategories"/>
            <xsl:with-param name="typeId" select="@shortTypeId"/>
         </xsl:call-template>
      </xsl:for-each>
      <xsl:variable name="category" select="document('output/aw-scrubbed.xml')"/>
      <xsl:call-template name="processCategory">
         <xsl:with-param name="toolId" select="$toolId"/>
         <xsl:with-param name="category" select="$category/toolCategories"/>
         <xsl:with-param name="typeId" select="'aw'"/>
      </xsl:call-template>
      <xsl:variable name="myws" select="document('output/myws-scrubbed.xml')"/>
      <xsl:for-each select="$myws/myws/toolCategories">
         <xsl:call-template name="processCategory">
            <xsl:with-param name="toolId" select="$toolId"/>
            <xsl:with-param name="category" select="."/>
            <xsl:with-param name="typeId" select="@typeId"/>
         </xsl:call-template>
      </xsl:for-each>
   </xsl:template>

   <xsl:template name="processCategory">
      <xsl:param name="toolId"/>
      <xsl:param name="category"/>
      <xsl:param name="typeId"/>
      <xsl:if test="$category/category/tools/tool[@id=$toolId]">
         <category>
            <xsl:attribute name="name"><xsl:value-of select="$typeId"/></xsl:attribute>
         </category>
      </xsl:if>
   </xsl:template>
   
   <xsl:template match="@description|@title">
      <xsl:if test="name(..) != 'tool'">
         <xsl:copy/>
      </xsl:if>
   </xsl:template>

   <xsl:template match="category">
      <!-- leave out orig categories -->
   </xsl:template>

   <!-- Identity transformation -->
   <xsl:template match="@*|*">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()" >
         </xsl:apply-templates>
      </xsl:copy>
   </xsl:template>

</xsl:stylesheet>