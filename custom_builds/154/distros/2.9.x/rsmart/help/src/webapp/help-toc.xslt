<?xml version="1.0" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

   <xsl:output method="xml" version="1.0" encoding="UTF-8" omit-xml-declaration="no"
               indent="yes" />


   <xsl:template match="//div[@id='c-_root']">
         <help-reg>
            <xsl:for-each select="p">
              <xsl:sort data-type="text" select="normalize-space(.)"/>       
               <xsl:call-template name="processCategory">
                  <xsl:with-param name="category" select="."/>
               </xsl:call-template>
            </xsl:for-each>      
         </help-reg>
   </xsl:template>
   
   <xsl:template name="processCategory">
      <xsl:param name="category"/>
      
      <category>
         <xsl:attribute name="name"><xsl:value-of select="normalize-space($category)"/></xsl:attribute>
         <resource defaultForTool="">
            <xsl:attribute name="name"><xsl:value-of select="normalize-space($category)"/></xsl:attribute>
            <xsl:attribute name="location">/<xsl:value-of select="normalize-space($category/a/@href)"/></xsl:attribute>
         </resource>
         <xsl:for-each select="following-sibling::div[1]/p">
            <xsl:call-template name="processResource">
               <xsl:with-param name="resource" select="."/>
            </xsl:call-template>
         </xsl:for-each>      
      </category>
   </xsl:template>
   
   <xsl:template name="processResource">
      <xsl:param name="resource"/>
         <resource defaultForTool="">
            <xsl:attribute name="name"><xsl:value-of select="normalize-space($resource/a)"/></xsl:attribute>
            <xsl:attribute name="location">/<xsl:value-of select="normalize-space($resource/a/@href)"/></xsl:attribute>
         </resource>
   </xsl:template>


</xsl:stylesheet>