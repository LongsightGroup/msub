<?xml version="1.0" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

   <xsl:output method="xml" version="1.0" encoding="UTF-8" omit-xml-declaration="no"
               doctype-public="-//SPRING//DTD BEAN//EN"
               doctype-system="http://www.springframework.org/dtd/spring-beans.dtd"
               indent="yes" />

   <xsl:variable name="aw" select="document('output/aw-scrubbed.xml')"/>
   <xsl:variable name="myws" select="document('output/myws-scrubbed.xml')"/>

   <xsl:template match="beans">
      <beans>
         <xsl:apply-templates select="@*|node()" >
         </xsl:apply-templates>
      </beans>
   </xsl:template>

   <xsl:template match="bean[@id='org.theospi.portfolio.admin.intf.SakaiIntegrationService.common']">
      <bean>
         <xsl:apply-templates select="@*" />
         <xsl:apply-templates select="property[@name!='integrationPlugins']" />
         <property name="integrationPlugins">
            <list>
               <xsl:comment>remove these and replace them in the rsmart version of this file.</xsl:comment>
            </list>
         </property>
      </bean>

   </xsl:template>

   <!-- Identity transformation -->
   <xsl:template match="@*|*">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()" >
         </xsl:apply-templates>
      </xsl:copy>
   </xsl:template>

</xsl:stylesheet>