<?xml version="1.0" encoding="UTF-8" ?>
<!--
 * <p>Copyright: Copyright (c) 2005 Sakai</p>
 * <p>Description: QTI Persistence XML to XML Transform for Import</p>
 * @author <a href="mailto:esmiley@stanford.edu">Ed Smiley</a>
 * @version $Id: extractSection.xsl,v 1.3 2005/04/27 02:38:35 esmiley.stanford.edu Exp $
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" doctype-public="-//W3C//DTD HTML 4.01//EN"
 doctype-system="http://www.w3.org/TR/html4/strict.dtd"/>

<xsl:template match="/">
  <sectionData>
   <ident><xsl:value-of select="//section/@ident" /></ident>
   <title><xsl:value-of select="//section/@title" /></title>
    <!-- our metadata -->
    <description>
    <xsl:value-of select="//section/qtimetadata/qtimetadatafield[1]/fieldentry"/>
    </description>
     <author_type>
    <xsl:value-of select="//section/qtimetadata/qtimetadatafield[2]/fieldentry"/>
    </author_type>
     <point_value>
    <xsl:value-of select="//section/qtimetadata/qtimetadatafield[3]/fieldentry"/>
    </point_value>
     <num_questions>
    <xsl:value-of select="//section/qtimetadata/qtimetadatafield[4]/fieldentry"/>
    </num_questions>
     <poolid_for_random_draw>
    <xsl:value-of select="//section/qtimetadata/qtimetadatafield[5]/fieldentry"/>
    </poolid_for_random_draw>
    <poolname_for_random_draw>
    <xsl:value-of select="//section/qtimetadata/qtimetadatafield[6]/fieldentry"/>
    </poolname_for_random_draw>
    <question_order>
    <xsl:value-of select="//section/qtimetadata/qtimetadatafield[7]/fieldentry"/>
    </question_order>
    <objective>
    <xsl:value-of select="//section/qtimetadata/qtimetadatafield[8]/fieldentry"/>
    </objective>
    <keyword>
    <xsl:value-of select="//section/qtimetadata/qtimetadatafield[9]/fieldentry"/>
    </keyword>
    <rubric>
    <xsl:value-of select="//section/qtimetadata/qtimetadatafield[10]/fieldentry"/>
    </rubric>
    <attachment>
    <xsl:value-of select="//section/qtimetadata/qtimetadatafield[11]/fieldentry"/>
    </attachment>
  </sectionData>
</xsl:template>

</xsl:stylesheet>
