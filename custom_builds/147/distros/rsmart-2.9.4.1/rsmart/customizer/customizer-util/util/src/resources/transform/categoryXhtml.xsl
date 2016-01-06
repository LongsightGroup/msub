<?xml version="1.0" ?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY nbsp "&#160;">
]>
<xsl:stylesheet version="2.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xhtml="http://www.w3.org/1999/xhtml"
   xmlns:osp="http://www.osportfolio.org/OspML">

   <xsl:variable name="tools" select="document('output/tools-scrubbed.xml')"/>

   <xsl:template match="category">
      <table width="100%" border="0" cellpadding="0" cellspacing="0" xmlns="http://www.w3.org/1999/xhtml"
         xmlns:osp="http://www.osportfolio.org/OspML"
         xml:lang="en" lang="en">
      <tr><td>
      <div class="categoryTitle"><xsl:value-of select="name"/></div>
         <div class="categoryHeader">
            <xsl:for-each select="globalRoleText/role[@id]">
               <osp:site_role>
                  <xsl:attribute name="role"><xsl:value-of select="@id" /></xsl:attribute>
                  <xsl:value-of select="."/>
               </osp:site_role>
            </xsl:for-each>
            <xsl:for-each select="globalRoleText/role[@type='all']">
               <xsl:value-of select="."/>
            </xsl:for-each>
         </div>

         <xsl:for-each select="globalRoleText/role[@id]">
            <xsl:call-template name="roleArea">
               <xsl:with-param name="roleId" select="@id"/>
            </xsl:call-template>
         </xsl:for-each>

         <xsl:for-each select="tools/tool[position()=1]/roleText/role[@type='all']">
            <xsl:call-template name="toolAreaList">
               <xsl:with-param name="roleId" select="@id"/>
            </xsl:call-template>
         </xsl:for-each>

      </td></tr></table>
   </xsl:template>

   <xsl:template name="roleArea">
      <xsl:param name="roleId"/>

      <osp:site_role>
         <xsl:attribute name="role"><xsl:value-of select="$roleId" /></xsl:attribute>
         <xsl:call-template name="toolAreaList">
            <xsl:with-param name="roleId" select="$roleId"/>
         </xsl:call-template>
      </osp:site_role>
   </xsl:template>

   <xsl:template name="toolAreaList">
      <xsl:param name="roleId"/>
      <xsl:for-each select="/category/tools/tool[@id != '']">
         <xsl:sort select="@order" data-type="number" />
         <xsl:choose>
            <xsl:when test="@id = 'sakai.news' or @id = 'sakai.iframe'">
               <xsl:call-template name="multiToolArea">
                  <xsl:with-param name="tool" select="."/>
                  <xsl:with-param name="roleId" select="$roleId"/>
               </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
               <xsl:call-template name="toolArea">
                  <xsl:with-param name="tool" select="."/>
                  <xsl:with-param name="roleId" select="$roleId"/>
               </xsl:call-template>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:for-each>
   </xsl:template>

   <xsl:template name="toolArea">
      <xsl:param name="tool"/>
      <xsl:param name="roleId"/>

      <xsl:variable name="toolId" select="$tool/@id"/>
      <xsl:variable name="toolRef" select="$tools/tools/tool[@id=$toolId]"/>
      <xsl:variable name="toolName" select="$toolRef/@name"/>

      <osp:tool>
         <xsl:choose>
            <xsl:when test="$tool/@realId">
               <xsl:attribute name="id"><xsl:value-of select="$tool/@realId"/></xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
               <xsl:attribute name="id"><xsl:value-of select="$toolId"/></xsl:attribute>
            </xsl:otherwise>
         </xsl:choose>
         <div class="lpod">
            <div class="pod_top"><div class="pod_top_left">&nbsp;</div></div>
            <div class="pod_mid">
               <div class="pod_head"><osp:toolLink><span class="pod_headline"><osp:toolTitle/></span></osp:toolLink></div>
               <osp:toolLink>
                  <img class="pod_logo">
                     <xsl:attribute name="src">/library/image/toolIcons/<xsl:value-of select="$toolRef/icon" />
                     </xsl:attribute>
                     <xsl:attribute name="alt"><xsl:value-of select="$toolName" /></xsl:attribute>
                     <xsl:attribute name="title"><xsl:value-of select="$toolName" /></xsl:attribute>
                  </img>
               </osp:toolLink>
               <div class="pod_cont">
                  <xsl:value-of select="$tool/roleText/role[@id=$roleId or @type='all']"/>
               </div>
            </div>
            <div class="pod_butt"><osp:toolLink><div class="pod_button">Launch</div></osp:toolLink></div>
            <div class="pod_bot"><div class="pod_bot_left">&nbsp;</div></div>
         </div>
      </osp:tool>



   </xsl:template>

   <xsl:template name="multiToolArea">
      <xsl:param name="tool"/>
      <xsl:param name="roleId"/>

      <xsl:variable name="toolId" select="$tool/@id"/>
      <xsl:variable name="toolRef" select="$tools/tools/tool[@id=$toolId]"/>
      <xsl:variable name="toolName" select="$toolRef/@name"/>

      <osp:tool>
         <xsl:choose>
            <xsl:when test="$tool/@realId">
               <xsl:attribute name="id"><xsl:value-of select="$tool/@realId"/></xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
               <xsl:attribute name="id"><xsl:value-of select="$toolId"/></xsl:attribute>
            </xsl:otherwise>
         </xsl:choose>
         <div class="lpod">
            <div class="pod_top"><div class="pod_top_left">&nbsp;</div></div>
            <div class="pod_mid">
               <div class="pod_head">
                  <span class="pod_headline"><xsl:value-of select="$toolName" /></span>
               </div>
               <div class="pod_cont_links">
                  <osp:toolIterator>
                     <osp:toolLink>
                        <img class="pod_logo_sm" align="middle">
                           <xsl:attribute name="src">/library/image/toolIcons/<xsl:value-of select="$toolRef/icon" />
                           </xsl:attribute>
                           <xsl:attribute name="alt"><xsl:value-of select="$toolName" /></xsl:attribute>
                           <xsl:attribute name="title"><xsl:value-of select="$toolName" /></xsl:attribute>
                        </img>
                     </osp:toolLink>
                     <osp:toolLink><osp:toolTitle/></osp:toolLink>
                     <br />
                  </osp:toolIterator>
               </div>
               <div class="pod_cont_sm">
                  <xsl:value-of select="$tool/roleText/role[@id=$roleId or @type='all']"/>
               </div>
            </div>
            <div class="pod_bot"><div class="pod_bot_left">&nbsp;</div></div>
         </div>
      </osp:tool>



   </xsl:template>

</xsl:stylesheet>