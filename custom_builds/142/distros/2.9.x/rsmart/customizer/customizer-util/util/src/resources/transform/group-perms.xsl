<?xml version="1.0" ?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:rsn="com.rsmart.util.xml.XmlXpathFuncs">

   <xsl:output method="xml" version="1.0" encoding="UTF-8" omit-xml-declaration="no"
               doctype-public="-//SPRING//DTD BEAN//EN"
               doctype-system="http://www.springframework.org/dtd/spring-beans.dtd"
               indent="yes" />

   <xsl:param name="rsmartRuntime"/>
   <xsl:variable name="siteRoles" select="document('output/site-role-scrubbed.xml')"/>
   <xsl:variable name="sectionTool" select="/perms/tool[@id = 'sakai.sections']"/>

   <xsl:template match="tool">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()" >
         </xsl:apply-templates>
         <xsl:variable name="tool" select="." />
         <xsl:for-each select="$siteRoles/siteTypeRoles/siteTypes/siteType[@groupId]">
            <xsl:variable name="id" select="@id"/>
            <realm id="{@groupId}" siteType="{@shortTypeId}" copyOf="{$id}">
               <xsl:comment>added this realm</xsl:comment>
               <xsl:variable name="instructorList"
                             select="$sectionTool/realm[@id = $id]/role[function/@id='section.role.instructor']"/>
               <xsl:variable name="studentList"
                             select="$sectionTool/realm[@id = $id]/role[function/@id='section.role.student']"/>
               <xsl:variable name="taList"
                             select="$sectionTool/realm[@id = $id]/role[function/@id='section.role.ta']"/>
               <xsl:variable name="allRolesList"
                             select="$sectionTool/realm[@id = $id]/role"/>

               <xsl:choose>
                  <xsl:when test="$tool/@id = 'sakai.sections'">
                     <xsl:for-each select="rsn:subsequence(($instructorList), 1, 1)">
                        <xsl:variable name="roleId" select="@id"/>
                        <role id="{$roleId}">
                           <xsl:copy-of select="$tool/realm[@id = $id]/role[@id=$roleId]/node()"/>
                        </role>
                     </xsl:for-each>
                     <xsl:for-each select="rsn:subsequence(($studentList), 1, 1)">
                        <xsl:variable name="roleId" select="@id"/>
                        <role id="{$roleId}">
                           <xsl:copy-of select="$tool/realm[@id = $id]/role[@id=$roleId]/node()"/>
                        </role>
                     </xsl:for-each>
                     <xsl:for-each select="rsn:subsequence(($taList), 1, 1)">
                        <xsl:variable name="roleId" select="@id"/>
                        <role id="{$roleId}">
                           <xsl:copy-of select="$tool/realm[@id = $id]/role[@id=$roleId]/node()"/>
                        </role>
                     </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>
                     <xsl:for-each select="$allRolesList">
                        <xsl:variable name="roleId" select="@id"/>
                        <role id="{$roleId}">
                           <xsl:copy-of select="$tool/realm[@id = $id]/role[@id=$roleId]/node()"/>
                        </role>
                     </xsl:for-each>
                  </xsl:otherwise>
               </xsl:choose>
            </realm>
         </xsl:for-each>
      </xsl:copy>
   </xsl:template>

   <!-- Identity transformation -->
   <xsl:template match="@*|*">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()" >
         </xsl:apply-templates>
      </xsl:copy>
   </xsl:template>

</xsl:stylesheet>