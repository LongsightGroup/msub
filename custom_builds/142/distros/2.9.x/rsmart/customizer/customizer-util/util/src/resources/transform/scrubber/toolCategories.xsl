<?xml version="1.0" ?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:rsn="com.rsmart.util.xml.XmlXpathFuncs"
                xmlns:o="urn:schemas-microsoft-com:office:office"
                xmlns:x="urn:schemas-microsoft-com:office:excel"
                xmlns:dt="uuid:C2F41010-65B3-11d1-A29F-00AA00C14882"
                xmlns:html="http://www.w3.org/TR/REC-html40"
                xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">

   <xsl:variable name="tools" select="document('output/tools-scrubbed.xml')"/>

   <xsl:template match="Worksheet">
      <xsl:choose>
         <xsl:when test="Table/Row[@order=6]/Cell[@order=1]/Data">
            <!-- processing myws -->
            <myws>
               <xsl:for-each select="Table/Row[@order>=6]/Cell[@order=1]/Data">
                  <xsl:sort select="../../@order" data-type="number" />
                  <xsl:variable name="siteRow" select="../.."/>
                  <xsl:variable name="group1" select="$siteRow/@group1"/>
                  <xsl:variable name="dataStart" select="$siteRow/@order + 2"/>
                  <xsl:variable name="siteId" select="$siteRow/Cell[@order=3]/Data"/>
                  <toolCategories>
                     <xsl:attribute name="siteId"><xsl:value-of select="$siteId" /></xsl:attribute>
                     <xsl:attribute name="siteName">
                        <xsl:value-of select="$siteRow/Cell[@order=1]/Data" />
                     </xsl:attribute>
                     <xsl:attribute name="typeId">
                        <xsl:value-of select="concat('myworkspace', translate($siteId, '!~.', 'btd'))" />
                     </xsl:attribute>
                     <xsl:for-each
                        select="/Worksheet/Table/Row[@order>$dataStart and @group1=$group1]/Cell[@order=2]/Data">
                        <xsl:sort select="../../@order" data-type="number" />
                        <xsl:call-template name="processCategory">
                           <xsl:with-param name="categoryRow" select="../.."/>
                           <xsl:with-param name="roleRow" select="/Worksheet/Table/Row[@order=$dataStart]"/>
                           <xsl:with-param name="myws" select="'true'"/>
                        </xsl:call-template>
                     </xsl:for-each>
                  </toolCategories>
               </xsl:for-each>
            </myws>
         </xsl:when>
         <xsl:otherwise>
            <xsl:variable name="roleRow" select="Table/Row[@order=10]"/>
            <xsl:variable name="siteId" select="normalize-space(Table/Row[@order=7]/Cell[@order=3]/Data)"/>
            <toolCategories>
               <xsl:attribute name="siteId"><xsl:value-of select="$siteId"/></xsl:attribute>
               <xsl:for-each select="Table/Row[@order>=11]/Cell[@order=2]/Data">
                  <xsl:sort select="../../@order" data-type="number" />
                  <xsl:call-template name="processCategory">
                     <xsl:with-param name="categoryRow" select="../.."/>
                     <xsl:with-param name="roleRow" select="$roleRow"/>
                     <xsl:with-param name="myws" select="'false'"/>
                  </xsl:call-template>
               </xsl:for-each>
            </toolCategories>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <xsl:template name="processCategory">
      <xsl:param name="myws"/>
      <xsl:param name="categoryRow"/>
      <xsl:param name="roleRow"/>
      <xsl:variable name="categoryName" select="$categoryRow/Cell[@order=2]/Data"/>

      <category>
         <xsl:attribute name="order"><xsl:value-of select="$categoryRow/@order"/></xsl:attribute>
         <xsl:attribute name="shortName">
            <xsl:value-of select="rsn:translateKey($categoryName)"/>
         </xsl:attribute>
         <xsl:choose>
            <xsl:when test="$categoryRow/Cell[@order=2]/Data = '-'">
               <!-- process uncategorized -->
               <xsl:attribute name="type">uncategorized</xsl:attribute>
               <tools>
                  <xsl:call-template name="processToolRow">
                     <xsl:with-param name="categoryRow" select="$categoryRow"/>
                     <xsl:with-param name="toolRow" select="$categoryRow"/>
                     <xsl:with-param name="roleRow" select="$roleRow"/>
                     <xsl:with-param name="myws" select="$myws"/>
                     <xsl:with-param name="uncat" select="'true'"/>
                  </xsl:call-template>
               </tools>
            </xsl:when>
            <xsl:otherwise>
               <!-- process normal category -->
               <xsl:variable name="start" select="$categoryRow/@order"/>
               <xsl:variable name="group2" select="$categoryRow/@group2"/>
               <name><xsl:value-of select="$categoryName" /></name>
               <globalRoleText>
                  <xsl:for-each select="$roleRow/Cell[@order>4]/Data">
                     <xsl:sort select="../@order" data-type="number" />
                     <xsl:variable name="column" select="../@order"/>
                     <xsl:if test="$roleRow/Cell[@order=$column]/Data != 'Notes'">
                        <role>
                           <xsl:attribute name="order">
                              <xsl:value-of select="$column"/>
                           </xsl:attribute>
                           <xsl:choose>
                              <xsl:when test="$myws = 'true'">
                                 <xsl:attribute name="type">all</xsl:attribute>
                              </xsl:when>
                              <xsl:otherwise>
                                 <xsl:attribute name="id">
                                    <xsl:value-of select="$roleRow/Cell[@order=$column]/Data"/>
                                 </xsl:attribute>
                              </xsl:otherwise>
                           </xsl:choose>
                           <xsl:call-template name="roleText">
                              <xsl:with-param name="roleCell" select="$categoryRow/Cell[@order=$column]"/>
                              <xsl:with-param name="emptyMessage"
                                  select="concat('text for role ', ., ' in category ', $categoryName)"/>
                              <xsl:with-param name="myws" select="$myws"/>
                           </xsl:call-template>
                        </role>
                     </xsl:if>
                  </xsl:for-each>
               </globalRoleText>
               <tools>
                  <xsl:for-each select="/Worksheet/Table/Row[@order>$start and @group2=$group2]">
                     <xsl:sort select="@order" data-type="number" />
                     <xsl:call-template name="processToolRow">
                        <xsl:with-param name="categoryRow" select="$categoryRow"/>
                        <xsl:with-param name="toolRow" select="."/>
                        <xsl:with-param name="roleRow" select="$roleRow"/>
                        <xsl:with-param name="myws" select="$myws"/>
                        <xsl:with-param name="uncat" select="'false'"/>
                     </xsl:call-template>
                  </xsl:for-each>
               </tools>
            </xsl:otherwise>
         </xsl:choose>
      </category>
   </xsl:template>

   <xsl:template name="processToolRow">
      <xsl:param name="myws"/>
      <xsl:param name="categoryRow"/>
      <xsl:param name="toolRow"/>
      <xsl:param name="roleRow"/>
      <xsl:param name="uncat"/>
      <xsl:variable name="toolName" select="$toolRow/Cell[@order=3]/Data"/>
      <xsl:if test="$toolName">
         <xsl:variable name="toolLookup" select="$tools/tools/tool[@name = $toolName]"/>
         <xsl:choose>
            <xsl:when test="not($toolLookup/@id)">
               <xsl:message>WARNING: unable to find tool named: <xsl:value-of select="$toolName"/>
               <xsl:choose><xsl:when
                     test="$myws = 'true'"> in myws tab.</xsl:when>
                  <xsl:otherwise> in site <xsl:value-of select="/Worksheet/Table/Row[@order=6]/Cell[@order=3]/Data"/>.</xsl:otherwise>
               </xsl:choose>
               </xsl:message>
            </xsl:when>
            <xsl:when test="$toolLookup/@id = 'sakai.help'">
            </xsl:when>
            <xsl:otherwise>
               <tool>
               <xsl:attribute name="order"><xsl:value-of select="$toolRow/@order - $categoryRow/@order" /></xsl:attribute>
               <xsl:attribute name="id"><xsl:value-of select="$toolLookup/@id" /></xsl:attribute>
               <functions>
                  <xsl:variable name="functionText" select="normalize-space($toolRow/Cell[@order=4]/Data)"/>
                  <xsl:if test="$toolRow/Cell[@order=4]/Data">
                     <xsl:variable name="functionData" select="concat($toolRow/Cell[@order=4]/Data, '')"/>
                     <xsl:for-each select="rsn:tokenize($functionData, ',')">
                        <function>
                           <xsl:attribute name="id"><xsl:value-of select="normalize-space(.)"/></xsl:attribute>
                           <xsl:attribute name="order"><xsl:value-of select="position()"/></xsl:attribute>
                        </function>
                     </xsl:for-each>
                  </xsl:if>
               </functions>
               <xsl:if test="$uncat = 'false'">
                  <roleText>
                     <xsl:for-each select="$roleRow/Cell[@order>4]/Data">
                        <xsl:sort select="../@order" data-type="number" />
                        <xsl:variable name="column" select="../@order"/>
                        <xsl:if test="$roleRow/Cell[@order=$column]/Data != 'Notes'">
                           <role>
                              <xsl:attribute name="order">
                                 <xsl:value-of select="$column"/>
                              </xsl:attribute>
                              <xsl:choose>
                                 <xsl:when test="$myws = 'true'">
                                    <xsl:attribute name="type">all</xsl:attribute>
                                 </xsl:when>
                                 <xsl:otherwise>
                                    <xsl:attribute name="id">
                                       <xsl:value-of select="$roleRow/Cell[@order=$column]/Data"/>
                                    </xsl:attribute>
                                 </xsl:otherwise>
                              </xsl:choose>
                              <xsl:call-template name="roleText">
                                 <xsl:with-param name="roleCell" select="$toolRow/Cell[@order=$column]"/>
                                 <xsl:with-param name="emptyMessage"
                                     select="concat('text for role ', $roleRow/Cell[@order=$column]/Data,
                                     ' for tool ', $toolName, ' in category ',
                                     $categoryRow/Cell[@order=2]/Data)"/>
                                 <xsl:with-param name="myws" select="$myws"/>
                              </xsl:call-template>
                           </role>
                        </xsl:if>
                     </xsl:for-each>
                  </roleText>
               </xsl:if>
            </tool>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:if>
   </xsl:template>

   <xsl:template name="roleText">
      <xsl:param name="roleCell"/>
      <xsl:param name="emptyMessage"/>
      <xsl:param name="myws"/>
      <xsl:variable name="prevCell" select="$roleCell/@order - 1"/>
      <xsl:choose>
         <xsl:when test="$roleCell/Data = '&quot;'">
            <xsl:call-template name="roleText">
               <xsl:with-param name="roleCell" select="$roleCell/../Cell[@order=$prevCell]"/>
               <xsl:with-param name="emptyMessage" select="$emptyMessage"/>
            </xsl:call-template>
         </xsl:when>
         <xsl:when test="not($roleCell/Data) or $roleCell/Data = ''">
            <!--xsl:message>WARNING: <xsl:value-of select="$emptyMessage"
               disable-output-escaping="yes"/><xsl:choose><xsl:when
                     test="$myws = 'true'"> in myws tab</xsl:when>
                  <xsl:otherwise> in site <xsl:value-of select="/Worksheet/Table/Row[@order=6]/Cell[@order=3]/Data" disable-output-escaping="yes"/></xsl:otherwise>
               </xsl:choose> is missing.</xsl:message-->
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="$roleCell/Data"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

</xsl:stylesheet>