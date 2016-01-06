<?xml version="1.0" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

   <xsl:output method="xml" version="1.0" encoding="UTF-8" omit-xml-declaration="no"
               doctype-public="-//SPRING//DTD BEAN//EN"
               doctype-system="http://www.springframework.org/dtd/spring-beans.dtd"
               indent="yes" />

   <xsl:template match="bean[@id='org.theospi.portfolio.security.model.SakaiDefaultPermsManager.reports']">
      <xsl:comment>
         commenting out report perm bean, this is handled elsewhere.
         <xsl:copy>
            <xsl:apply-templates select="@*|node()" >
            </xsl:apply-templates>
         </xsl:copy>
      </xsl:comment>
   </xsl:template>

   <!-- Identity transformation -->
   <xsl:template match="@*|*">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()" >
         </xsl:apply-templates>
      </xsl:copy>
   </xsl:template>

</xsl:stylesheet>