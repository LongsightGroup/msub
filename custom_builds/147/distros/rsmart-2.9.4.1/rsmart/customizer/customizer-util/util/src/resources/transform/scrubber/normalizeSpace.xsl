<?xml version="1.0" ?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:o="urn:schemas-microsoft-com:office:office"
                xmlns:x="urn:schemas-microsoft-com:office:excel"
                xmlns:dt="uuid:C2F41010-65B3-11d1-A29F-00AA00C14882"
                xmlns:html="http://www.w3.org/TR/REC-html40"
                xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">

   <xsl:output method="xml" version="1.0" encoding="UTF-8" omit-xml-declaration="no"
               cdata-section-elements="Data" indent="yes" />

   <xsl:template match="Data">
      <Data><xsl:apply-templates select="@*"/><xsl:value-of select="normalize-space(.)"/></Data>
   </xsl:template>

   <!-- Identity transformation -->
   <xsl:template match="@*|*">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()" >
         </xsl:apply-templates>
      </xsl:copy>
   </xsl:template>

</xsl:stylesheet>