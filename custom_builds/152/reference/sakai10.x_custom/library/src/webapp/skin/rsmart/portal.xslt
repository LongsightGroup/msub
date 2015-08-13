<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE xsl:stylesheet [
      <!ENTITY copy "&#169;">
      <!ENTITY nbsp "&#160;">
      <!ENTITY frasl "&#47;">
      ]>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xhtml="http://www.w3.org/1999/xhtml"
                xmlns:osp="http://www.osportfolio.org/OspML"
                xmlns:xs="http://www.w3.org/2001/XMLSchema">

   <xsl:output method="html" version="4.01"
               encoding="utf-8" indent="yes" doctype-public="http://www.w3.org/TR/html4/loose.dtd"/>

   <xsl:variable name="config" select="/portal/config"/>
   <xsl:variable name="externalized" select="/portal/externalized"/>

   <xsl:variable name="roles" select="/portal/roles"/>
   
   
   <xsl:variable name="dynamicGateway">
	   <xsl:choose>
				<xsl:when test="/portal/config/portal.dynamicGateway=''"><xsl:value-of select="'false'"/></xsl:when>
				<xsl:otherwise><xsl:value-of select="/portal/config/portal.dynamicGateway"/></xsl:otherwise>
	   </xsl:choose>	   
   </xsl:variable>
    
    <xsl:variable name="adminRole">
	   <xsl:choose>
				<xsl:when test="//site/siteId='!admin'"><xsl:value-of select="'true'"/></xsl:when>
				<xsl:otherwise><xsl:value-of select="'false'"/></xsl:otherwise>
	   </xsl:choose>	   
   </xsl:variable>
   

   <!--
   ============match /portal===============
   main template processing
   ========================================
   -->
   <xsl:template match="portal">
      <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
         <head>
            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
            <xsl:for-each select="skins/skin">
               <link type="text/css" rel="stylesheet" media="all">
                  <xsl:attribute name="href">
                     <xsl:value-of select="."/>
                  </xsl:attribute>
               </link>
            </xsl:for-each>
            <xsl:if test="$externalized/entry[@key='brand_portal_stylesheet']!=''">
               <link type="text/css" rel="stylesheet" media="all">
                  <xsl:attribute name="href">
                     <xsl:value-of select="$externalized/entry[@key='brand_portal_stylesheet']"/>
                  </xsl:attribute>
               </link>
            </xsl:if>
            <link href="/portal/styles/portalstyles.css" type="text/css" rel="stylesheet" media="all"/>
            
            
             <xsl:comment><xsl:text><![CDATA[[if IE 7]><link rel="stylesheet" type="text/css" href="]]></xsl:text>
            <xsl:value-of select="substring-before(//skins/skin,'portal.css')"/>
            <xsl:text><![CDATA[ie7hacks.css"><![endif]]]></xsl:text></xsl:comment>
            
            

            <meta http-equiv="Content-Style-Type" content="text/css"/>
            <title>
               <xsl:value-of disable-output-escaping="yes" select="pageTitle"/>
            </title>
            <script type="text/javascript" language="JavaScript" src="/library/js/headscripts.js">
               <xsl:value-of select="' '"/>
            </script>
            <script type="text/javascript" language="JavaScript" src="/library/js/jquery.js">
               <xsl:value-of select="' '"/>
            </script>
            <script type="text/javascript" language="JavaScript" src="/kaltura/javascript/kaltura-display.js"></script>
            <script type="text/javascript" language="JavaScript" src="/library/js/animatedcollapse.js">
               <xsl:value-of select="' '"/>
            </script>
            <script type="text/javascript" language="JavaScript" src="/portal/scripts/portalscripts.js">
               <xsl:value-of select="' '"/>
            </script>
            <script type="text/javascript" language="JavaScript"> 
               /* 
                  SAK-20578
                  Sets some state information in the portalscripts.js javascript module... note that some of the 
                  information added to portalscripts.js is not used by the present OSP Portal. Those can be 
                  added, but will need to be included in the rendering context within XsltRenderContext.java 
               */
 
               setLoginStatus (<xsl:choose><xsl:when test="currentUser">true</xsl:when><xsl:otherwise>false</xsl:otherwise></xsl:choose>, 
                               "/", 
                               "<xsl:value-of select="loginInfo/logInOutUrl"/>", 
                               "<xsl:value-of select="currentUser/id"/>", 
                               ""); 
	 	  
               //timeout information not presently used by OSP Portal 
               setTimeoutInfo (<xsl:value-of select="portalState/timeoutDialogEnabled"/>,
                               <xsl:value-of select="portalState/timeoutDialogWarningSeconds"/>);
               
               //UI state parameters not presently used by OSP Portal 
               setUIToggleState (<xsl:value-of select="portalState/portal_allow_auto_minimize"/>,
                                 <xsl:value-of select="portalState/portal_allow_minimize_tools"/>,
                                 <xsl:value-of select="portalState/portal_allow_minimize_navigation"/>);
            </script> 
         </head>
          <body class="portalBody" onload="resizeFrame(document.getElementById('ifrm'))" onresize="resizeFrame(document.getElementById('ifrm'))">

			<script type="text/javascript">
			// Firefox worked fine. Internet Explorer shows scrollbar because of frameborder
			function resizeFrame(f) {
                if (f != null) {
                    f.style.height = f.contentWindow.document.body.scrollHeight + "px";
                }
            }
			</script>
    
    
		   <script type="text/javascript" language="JavaScript">
				   var sakaiPortalWindow = "";
		   </script>
            <!--     <xsl:if test="loginInfo/topLogin = 'true' and not(currentUser)">
          <xsl:attribute name="onload">document.forms[0].eid.focus();</xsl:attribute>
       </xsl:if> -->
			<xsl:if test="currentUser">
            <a href="#tocontent" class="skip" accesskey="c">
               <xsl:attribute name="title">
                  <xsl:value-of select="$externalized/entry[@key='sit_jumpcontent']"/>
               </xsl:attribute>
               <xsl:value-of select="$externalized/entry[@key='sit_jumpcontent']"/>
            </a>
            <a href="#toolmenu" class="skip" accesskey="l">
               <xsl:attribute name="title">
                  <xsl:value-of select="$externalized/entry[@key='sit_jumptools']"/>
               </xsl:attribute>
               <xsl:value-of select="$externalized/entry[@key='sit_jumptools']"/>
            </a>
            <a href="#sitetabs" class="skip" title="jump to worksite list" accesskey="w">
               <xsl:attribute name="title">
                  <xsl:value-of select="$externalized/entry[@key='sit_jumpworksite']"/>
               </xsl:attribute>
               <xsl:value-of select="$externalized/entry[@key='sit_jumpworksite']"/>
            </a>
			</xsl:if>
            <table width="100%" border="0" cellspacing="0" cellpadding="0">
            
             <tr>
                     <td id="siteTabsHolder">
                        <xsl:call-template name="site_tabs"/>
                     </td>
                  </tr>
				   
				<xsl:if test="$adminRole='false'">   
                <xsl:if test="/portal/sites/siteTypes/siteType/sites/site/viewAsStudentLink">   
                  <tr>
                     <td id="roleView">
						 <xsl:call-template name="role_view"/>
                     </td>
                  </tr>
				</xsl:if>
				</xsl:if>
				
               <tr>
                  <td>
                     <div id="portalOuterContainer">
                        <div id="portalContainer">
                           <div id="container">
                              <xsl:attribute name="class">
                                 <xsl:value-of select="siteTypes/siteType[@selected='true']/name"/>
                              </xsl:attribute>
                              <xsl:if test="currentUser">
                                 <xsl:call-template name="site_tools"/>
                              </xsl:if>
                              <xsl:choose>
                                 <xsl:when test="currentUser">
                                    <div id="selectNav">
                                       <div class="dhtml_more_tabs">
                                          <!-- NAVIGATION SLIDER -->
                                          <span class="skip">
                                             <xsl:value-of select="$externalized/entry[@key='sit_selectmessage']"/>
                                          </span>

                                          <!-- LIST ALL COURSES FIRST, BY TERM -->
                                          <div class="termContainer">
                                             <h4>COURSES</h4>
                                             <xsl:for-each select="sites/siteTypes/siteType[@type='course']">

                                                <div class="courseContainer">
                                                   <h4>
                                                      <xsl:value-of disable-output-escaping="yes" select="title"/>
                                                   </h4>
                                                   <ul id="siteLinkList2">
                                                      <xsl:for-each select="sites/site">
                                                         <xsl:sort select="@order" data-type="number"/>
                                                         <xsl:apply-templates select=".">
                                                            <xsl:with-param name="extra" select="'false'"/>
                                                         </xsl:apply-templates>
                                                      </xsl:for-each>
                                                   </ul>
                                                </div>
                                                <!-- /courseContainer -->
                                             </xsl:for-each>
                                          </div>

                                          <!-- PORTFOLIOS NEXT -->
                                          <xsl:for-each select="sites/siteTypes/siteType[@type='portfolio']">
                                             <xsl:sort select="@order" data-type="number"/>
                                             <div class="termContainer">
                                                <h4>
                                                   <xsl:value-of disable-output-escaping="yes" select="title"/>
                                                </h4>
                                                <ul id="siteLinkList2">
                                                   <xsl:for-each select="sites/site">
                                                      <xsl:sort select="@order" data-type="number"/>
                                                      <xsl:apply-templates select=".">
                                                         <xsl:with-param name="extra" select="'false'"/>
                                                      </xsl:apply-templates>
                                                   </xsl:for-each>
                                                </ul>
                                             </div>
                                             <!-- /termContainer -->
                                          </xsl:for-each>

                                          <!-- PROJECTS NEXT -->
                                          <xsl:for-each select="sites/siteTypes/siteType[@type='project']">
                                             <xsl:sort select="@order" data-type="number"/>
                                             <div class="termContainer">
                                                <h4>
                                                   <xsl:value-of disable-output-escaping="yes" select="title"/>
                                                </h4>
                                                <ul id="siteLinkList2">
                                                   <xsl:for-each select="sites/site">
                                                      <xsl:sort select="@order" data-type="number"/>
                                                      <xsl:apply-templates select=".">
                                                         <xsl:with-param name="extra" select="'false'"/>
                                                      </xsl:apply-templates>
                                                   </xsl:for-each>
                                                </ul>
                                             </div>
                                             <!-- /termContainer -->
                                          </xsl:for-each>
                                          
                                           <xsl:for-each select="sites/siteTypes/siteType[@type='aw']">
                                             <xsl:sort select="@order" data-type="number"/>
                                             <div class="termContainer">
                                                 <h4>
                                                   <xsl:value-of disable-output-escaping="yes" select="title"/>
                                                </h4>
                                                <ul id="siteLinkList2">
                                                   <xsl:for-each select="sites/site">
                                                      <xsl:sort select="@order" data-type="number"/>
                                                      <xsl:apply-templates select=".">
                                                         <xsl:with-param name="extra" select="'false'"/>
                                                      </xsl:apply-templates>
                                                   </xsl:for-each>
                                                </ul>
                                             </div>
                                             <!-- /termContainer -->
                                          </xsl:for-each>

                                          <!-- LASTLY, ANY CUSTOM SITE TYPES -->
                                          <!--
                                                           <xsl:for-each select="sites/siteTypes/siteType[@type='']">
                                                              <xsl:sort select="@order" data-type="number"/>
                                                              <div class="termContainer">
                                                                 <h4><xsl:value-of disable-output-escaping="yes" select="title" /></h4>
                                                                 <ul id="siteLinkList2">
                                                                    <xsl:for-each select="sites/site">
                                                                       <xsl:sort select="@order" data-type="number"/>
                                                                       <xsl:apply-templates select=".">
                                                                          <xsl:with-param name="extra" select="'false'" />
                                                                       </xsl:apply-templates>
                                                                    </xsl:for-each>
                                                                 </ul>
                                                              </div>
                                                           </xsl:for-each>
                                          -->

                                          <div id="more_tabs_instr">
                                             <!-- <xsl:value-of disable-output-escaping="yes" select="$externalized/entry[@key='sit_moretab_inst']"/> -->
                                             <xsl:if test="//tabsToolUrl">
                                                <a title="View/edit the list of hidden sites"
                                                   alt="View/edit the list of hidden sites">
                                                   <xsl:attribute name="href">
                                                      <xsl:value-of select="//tabsToolUrl"/>
                                                   </xsl:attribute>
									<xsl:text>
										<xsl:value-of select="$externalized/entry[@key='sit_hiddensites']"/>View hidden sites
									</xsl:text>
                                                </a>
                                             </xsl:if>
                                          </div>
                                       </div>
                                       <!-- /  -->
                                    </div>
                                    <!-- /selectNav -->
                                 
                               
                                    <div id="siteTitle">
                                       <xsl:value-of disable-output-escaping="yes" select="//tabsSites/site[@selected='true']/fullTitle"/>
                                    </div>
                                    
<script type="text/javascript">
<xsl:text> var collapse=new animatedcollapse("selectNav", 400, false, '', </xsl:text>
<xsl:choose>
	<!-- more cource sites -->
	<xsl:when test="count(sites/siteTypes/siteType[@type='course']/sites/site)+count(sites/siteTypes/siteType[@type='course']/title)*2 >= count(sites/siteTypes/siteType[@type='project']/sites/site) and count(sites/siteTypes/siteType[@type='course']/sites/site)+count(sites/siteTypes/siteType[@type='course']/title)*2 >= count(sites/siteTypes/siteType[@type='portfolio']/sites/site)">
	<xsl:value-of select="(count(sites/siteTypes/siteType[@type='course']/sites/site)+(count(sites/siteTypes/siteType[@type='course']/title)*2))*18+100"/>
	</xsl:when>
	<!-- more project sites -->
	<xsl:when test="count(sites/siteTypes/siteType[@type='project']/sites/site) >= count(sites/siteTypes/siteType[@type='portfolio']/sites/site) and count(sites/siteTypes/siteType[@type='project']/sites/site) >= count(sites/siteTypes/siteType[@type='course']/sites/site)+count(sites/siteTypes/siteType[@type='course']/title)*2">
	<xsl:value-of select="count(sites/siteTypes/siteType[@type='project']/sites/site)*18+100"/>
	</xsl:when>
	<!-- more project sites -->
	<xsl:when test="count(sites/siteTypes/siteType[@type='portfolio']/sites/site) >= count(sites/siteTypes/siteType[@type='project']/sites/site) and count(sites/siteTypes/siteType[@type='portfolio']/sites/site) >= count(sites/siteTypes/siteType[@type='course']/sites/site)+count(sites/siteTypes/siteType[@type='course']/title)*2">
	<xsl:value-of select="count(sites/siteTypes/siteType[@type='portfolio']/sites/site)*18+100"/>
	</xsl:when>
	<xsl:otherwise>
	</xsl:otherwise>
</xsl:choose>

<xsl:text>);</xsl:text>
</script>

                                    
                                       <!--script type="text/javascript">
                                       var collapse=new animatedcollapse("selectNav", 400, false, '', 80)
                                    </script-->

                                    <xsl:for-each select="categories/category">
                                       <xsl:sort select="@order" data-type="number"/>
                                       <xsl:apply-templates select=".">
                                          <xsl:with-param name="content" select="'true'"/>
                                       </xsl:apply-templates>
                                    </xsl:for-each>
                                    <div id="supplementaryNavGraphic"></div>
                                 </xsl:when>
                                 <xsl:otherwise>  
                                          
								 <!-- #########                Selecting html or dynamic gateway              ###########       --> 
                                
								<xsl:if test="$dynamicGateway='true'">
										  <xsl:call-template name="site_tools" />
										  <xsl:for-each select="categories/category" >
											  <xsl:sort select="@order" data-type="number" />
											  <xsl:apply-templates select=".">
												  <xsl:with-param name="content" select="'true'"/>
											  </xsl:apply-templates>
										   </xsl:for-each>
								</xsl:if> 
								
								<xsl:if test="$dynamicGateway='false' and currentSite!='!gateway'">
										  <xsl:call-template name="site_tools" />
										  <xsl:for-each select="categories/category" >
											  <xsl:sort select="@order" data-type="number" />
											  <xsl:apply-templates select=".">
												  <xsl:with-param name="content" select="'true'"/>
											  </xsl:apply-templates>
										   </xsl:for-each>
								</xsl:if> 
       
							    <xsl:if test="$dynamicGateway='false' and currentSite='!gateway'">
										   <iframe name="ifrm" id="ifrm" width="100%" frameborder="0" border="0" height="755" title="Home">
											   <xsl:attribute name="src">
												  <xsl:value-of select="substring-before(//skins/skin,'portal.css')"/><xsl:text>home.html</xsl:text>
											   </xsl:attribute>
											   Sorry, your browser doesn't support iframes.
											</iframe>
                                </xsl:if>  
                                
                                 
                                  
								<!-- ####################################################################     -->                                  
                                 </xsl:otherwise>
                              </xsl:choose>
                              <div>
                                 <xsl:call-template name="footer"/>
                              </div>                     
                           </div>
                        </div>
                     </div>
                  </td>
               </tr>
            </table>
         </body>
      </html>
   </xsl:template>

   <!--
   ===============name portal_tool========================
   setup an iframe with the currently selected helper tool
   param: base - the node to get key and helperUrl from
   =======================================================
   -->
   <xsl:template name="portal_tool">
      <xsl:param name="base"/>
      <xsl:variable name="key" select="$base/key"/>
      <h1 class="skip">
         <xsl:value-of select="$externalized/entry[@key='sit_contentshead']"/>
      </h1>
      <a id="tocontent" class="skip" name="tocontent"></a>
      <div id="content">
         <div id="col1">
            <div class="portlet">
               <div class="portletMainWrap">
                  <iframe
                        class="portletMainIframe"
                        height="560"
                        width="100%"
                        frameborder="0"
                        marginwidth="0"
                        marginheight="0"
                        scrolling="auto">
                     <xsl:attribute name="title">
                        <xsl:value-of select="$externalized/entry[@key=$key]"/>
                     </xsl:attribute>
                     <xsl:attribute name="name">
                        <xsl:value-of select="$base/escapedKey"/>
                     </xsl:attribute>
                     <xsl:attribute name="id">
                        <xsl:value-of select="$base/escapedKey"/>
                     </xsl:attribute>
                     <xsl:attribute name="src">
                        <xsl:value-of select="$base/helperUrl"/>
                     </xsl:attribute>
                     your browser doesn't support iframes
                  </iframe>
               </div>
            </div>
         </div>
      </div>
   </xsl:template>


   <!--
   =========match category that isn't categorized=====================
   process a tool category
   param:content - "true" or "false" if rendering tool content or tool list
   =================================
   -->
   <xsl:template match="category[key='org.theospi.portfolio.portal.model.ToolCategory.uncategorized']">
      <xsl:param name="content"/>
      <xsl:for-each select="pages/page">
         <xsl:sort select="@order" data-type="number"/>
         <xsl:apply-templates select=".">
            <xsl:with-param name="content" select="$content"/>
         </xsl:apply-templates>
      </xsl:for-each>
   </xsl:template>

   <!--
   =========match category================
   process a tool category
   param:content - "true" or "false" if rendering tool content or tool list
   ================================================
   -->
   <xsl:template match="category[key!='org.theospi.portfolio.portal.model.ToolCategory.uncategorized']">
      <xsl:param name="content"/>
      <xsl:if test="$content != 'true'">
         <xsl:variable name="key" select="key"/>
         <li>
            <div class="toolSubMenuHolderRS">
               <div class="toolSubMenuHolderRS_top">
                  <div></div>
               </div>
               <div class="toolSubMenuHolderRS_content">
                  <div class="toolSubMenuHeadingRS">
                     <xsl:value-of select="$key"/>
                  </div>
                  <!-- <div class="toolSubMenuHolder_tools"> -->
                  <ul id="toolSubMenuRS" class="toolSubMenuRS">
                     <xsl:for-each select="pages/page">
                        <xsl:sort select="@order" data-type="number"/>
                        <xsl:apply-templates select=".">
                           <xsl:with-param name="content" select="$content"/>
                        </xsl:apply-templates>
                     </xsl:for-each>
                  </ul>
                  <!-- </div> -->
               </div>
               <div class="toolSubMenuHolderRS_bottom">
                  <div></div>
               </div>
            </div>
            <div class="toolSubMenuRSSpacer">
               <br/>
            </div>
         </li>
      </xsl:if>
      <xsl:if test="$content = 'true'">
         <xsl:for-each select="pages/page">
            <xsl:sort select="@order" data-type="number"/>
            <xsl:apply-templates select=".">
               <xsl:with-param name="content" select="$content"/>
            </xsl:apply-templates>
         </xsl:for-each>
      </xsl:if>
   </xsl:template>



 <!--
   ========name site_tabs============
   Handle putting up the site tabs
   ===============================
   -->
   <xsl:template name="site_tabs">
  
      <!-- site tabs here -->
      <div id="siteNavWrapper">
         <xsl:attribute name="class">
            <xsl:value-of select="siteTypes/siteType[@selected='true']/name"/>
         </xsl:attribute>
         
         
      
        <!-- ############### Top for dynamic gateway page ############  -->         
         
         <xsl:if test="$dynamicGateway='true' or ($dynamicGateway='false' and currentSite!='!gateway')">
             <xsl:comment>dynamic gateway</xsl:comment>
                 <div id="mastHead">
        
               <div id="mastLogo">
                  <a href="/xsl-portal" alt="">
                     <img title="Return to My Workspace" alt="Return to My Workspace">
                        <xsl:attribute name="src">
                           <xsl:value-of select="config/logo"/>
                        </xsl:attribute>
                     </img>
                  </a>
               </div>
               <div id="mastBanner">
                  <img title="Banner" alt="Banner">
                     <xsl:attribute name="src">
                        <xsl:value-of select="config/banner"/>
                     </xsl:attribute>
                  </img>
               </div>
               <div id="mastLogin">
               
               <xsl:choose>
        <xsl:when test="currentUser">
        
                  <div id="loginLinks">
                     <xsl:variable name="helloMessage">
                        <xsl:text>Hello,&nbsp;</xsl:text>
                     </xsl:variable>
                     <span class="welcome">
                        <xsl:choose>
                           <xsl:when test="currentUser/first != ''">
                              <xsl:value-of select="$helloMessage"/>
                              <xsl:value-of select="currentUser/first"/>
                           </xsl:when>
                           <xsl:otherwise>
                              <xsl:value-of select="$helloMessage"/>
                              <xsl:value-of select="currentUser/userid"/>
                           </xsl:otherwise>
                        </xsl:choose>
                        !&nbsp;|
                     </span>
                     &nbsp;
                     <xsl:text>(</xsl:text>
                     <a target="_parent">
                        <xsl:attribute name="href">
                           <xsl:value-of select="config/logout"/>
                        </xsl:attribute>
                        <xsl:attribute name="title">
                           <xsl:value-of select="loginInfo/logoutText"/>
                        </xsl:attribute>
                        <xsl:value-of select="loginInfo/logoutText"/>
                     </a>
                     <xsl:text>)</xsl:text>
                  </div>
                  
        </xsl:when>
        
        <xsl:otherwise>
           <xsl:choose>
	          <xsl:when test="loginInfo/loginTopLogin = 'true'">

		<div>
			<form method="post" action="/xsl-portal/xlogin" enctype="application/x-www-form-urlencoded" target="_parent">
			<table id="logiTable">
				
					<tr>
						<td><span id="logiText">Username:</span></td>
						<td><input name="eid" id="eid" type="text" style ="width: 8em" /></td>
						<td><span id="logiText">Password:</span></td>
						<td><input name="pw" id="pw" type="password" style ="width: 8em" /></td>
						<td><input id="logiButton" type="image" value="submit" src="/library/skin/rsmart/images/loginButton.gif" /></td>
					</tr>
				
			</table>
			 
			</form>
		</div>

	          </xsl:when>
              <xsl:otherwise>
                  <xsl:choose>
                      <xsl:when test="string(loginInfo/loginText)">
                         <xsl:variable name="loginLabel"><xsl:value-of select="loginInfo/loginText"/></xsl:variable>
                      </xsl:when>
                      <xsl:otherwise>
                          <xsl:variable name="loginLabel">Login</xsl:variable>
                      </xsl:otherwise>
                  </xsl:choose>

                  <table id="logiTable">

					<tr>
						<td>
                          <a>
                             <xsl:attribute name="href">
                                 <xsl:value-of select="loginInfo/logInOutUrl"/>
                             </xsl:attribute>
                             <xsl:attribute name="alt">
                                 <xsl:choose>
                                    <xsl:when test="string(loginInfo/loginText)">
                                        <xsl:value-of select="loginInfo/loginText"/>
                                    </xsl:when>
                                    <xsl:otherwise>Login</xsl:otherwise>
                                 </xsl:choose>
                             </xsl:attribute>
                             <xsl:choose>
                                 <xsl:when test="string(loginInfo/image1)">
                                     <img>
                                         <xsl:attribute name="src"><xsl:value-of select="loginInfo/image1"/></xsl:attribute>
                                     </img>
                                 </xsl:when>
                                 <xsl:otherwise>
                                     <xsl:choose>
                                        <xsl:when test="string(loginInfo/loginText)">
                                            <xsl:value-of select="loginInfo/loginText"/>
                                        </xsl:when>
                                        <xsl:otherwise>Login</xsl:otherwise>
                                     </xsl:choose>
                                 </xsl:otherwise>
                             </xsl:choose>
                          </a>
                        </td>
                  </tr>
                </table>

              </xsl:otherwise>
           </xsl:choose>
        </xsl:otherwise>
        </xsl:choose>
               </div>
        
         </div>
                </xsl:if>  
            
<!-- ##################################################### -->



<!-- ############### Top for HTML gateway page ##############  --> 
               
             <xsl:if test="$dynamicGateway='false' and currentSite='!gateway'">
                 
            <xsl:if test="currentUser">
             <div id="mastHead">
            <table width="100%" border="0" cellspacing="0" cellpadding="0">
				<tr>
					<td>

               <div id="mastLogo">
                  <a href="/xsl-portal" alt="">
                     <img title="Return to My Workspace" alt="Return to My Workspace">
                        <xsl:attribute name="src">
                           <xsl:value-of select="config/logo"/>
                        </xsl:attribute>
                     </img>
                  </a>
               </div>
               </td>
               <td>
               <div id="mastBanner">
                  <img title="Banner" alt="Banner">
                     <xsl:attribute name="src">
                        <xsl:value-of select="config/banner"/>
                     </xsl:attribute>
                  </img>
               </div>
               </td>
               <td>
               <div id="mastLogin">
                  <div id="loginLinks">
                     <xsl:variable name="helloMessage">
                        <xsl:text>Hello,&nbsp;</xsl:text>
                     </xsl:variable>
                     <span class="welcome">
                        <xsl:choose>
                           <xsl:when test="currentUser/first != ''">
                              <xsl:value-of select="$helloMessage"/>
                              <xsl:value-of select="currentUser/first"/>
                           </xsl:when>
                           <xsl:otherwise>
                              <xsl:value-of select="$helloMessage"/>
                              <xsl:value-of select="currentUser/userid"/>
                           </xsl:otherwise>
                        </xsl:choose>
                        !&nbsp;|
                     </span>
                     &nbsp;
                     <xsl:text>(</xsl:text>
                     <a target="_parent">
                        <xsl:attribute name="href">
                           <xsl:value-of select="config/logout"/>
                        </xsl:attribute>
                        <xsl:attribute name="title">
                           <xsl:value-of select="loginInfo/logoutText"/>
                        </xsl:attribute>
                        <xsl:value-of select="loginInfo/logoutText"/>
                     </a>
                     <xsl:text>)</xsl:text>
                  </div>
               </div>
               </td>
              	</tr>
				</table> 
            
         </div>
         </xsl:if>
	</xsl:if>  
         
  <!-- ##################################################### -->       
     
         
         <xsl:if test="currentUser">
         <div class="siteNavWrap">
          <a id="sitetabs" class="skip" name="sitetabs"></a>
         <h1 class="skip">
            <!--xsl:value-of select="$externalized/entry[@key='sit.toolshead']"/-->
            <xsl:value-of select="$externalized/entry[@key='sit_worksiteshead']"/>
         </h1> 

            <xsl:choose>
               <xsl:when test="currentUser">
                  <div id="siteNav">
                     <div id="quickLinks">
                        <li class="label">Quicklinks:</li>
                        <xsl:if test="/portal/currentUser/id = 'admin'">
                           <xsl:for-each select="/portal/sites/tabsSites/site[@myWorkspace='false']">
                              <xsl:sort select="title" data-type="text"/>
                              <xsl:apply-templates select=".">
                                 <xsl:with-param name="extra" select="'false'"/>
								 <xsl:with-param name="quick" select="'true'"/>
                              </xsl:apply-templates>
                              <xsl:if test="not(position()=last())">
                                 <li>|</li>
                              </xsl:if>
                           </xsl:for-each>
                        </xsl:if>
                        <xsl:if test="/portal/currentUser/id != 'admin'">
                           <xsl:for-each select="/portal/sites/tabsSites/site[@myWorkspace='false']">
                              <xsl:sort select="@order" data-type="number"/>
                              <xsl:apply-templates select=".">
                                 <xsl:with-param name="extra" select="'false'"/>								 
								 <xsl:with-param name="quick" select="'true'"/>
                              </xsl:apply-templates>
                              <xsl:if test="not(position()=last())">
                                 <li>|</li>
                              </xsl:if>
                           </xsl:for-each>
                        </xsl:if>

                        <xsl:if test="//tabsToolUrl">
                           <li>
                              <a title="Edit Quicklinks" alt="Edit Quicklinks">
                                 <xsl:attribute name="href">
                                    <xsl:value-of select="//tabsToolUrl"/>
                                 </xsl:attribute>
                                <img alt="Edit Quicklinks" name="editQuickLinks" id="editQuickLinks" width="27" height="16" border="0">
                                   <xsl:attribute name="src">
                                     <xsl:value-of select="substring-before(//skins/skin,'portal.css')"/><xsl:text>images/editQuickLinks.gif</xsl:text>
                                 </xsl:attribute>
                                 </img>
                              </a>
                           </li>
                        </xsl:if>
                     </div>
                     <div id="linkNav">
                        <a id="sitetabs" class="skip" name="sitetabs"></a>
                        <h1 class="skip">
                           <xsl:value-of select="$externalized/entry[@key='sit_worksiteshead']"/>
                        </h1>
                        <ul id="siteLinkList">
                           <xsl:for-each select="/portal/sites/tabsSites/site[@myWorkspace='true']">
                              <xsl:sort select="@order" data-type="number"/>
                              <xsl:apply-templates select=".">
                                 <xsl:with-param name="extra" select="'false'"/>
                              </xsl:apply-templates>
                           </xsl:for-each>

                           <!--	<xsl:if test="/portal/sites/tabsMoreSites/site"> -->
                           <li class="more-tab" id="mySites">
                              <a href="javascript:collapse.slideit()" onclick="javascript:classSwitch('mySites', 'more-tab', 'more-active');">
                                 <xsl:attribute name="title">
                                    <xsl:value-of select="$externalized/entry[@key='sit_more']"/>
                                 </xsl:attribute>
                                 <span class="outer">
                                    <span class="inner">
                                       <xsl:value-of select="$externalized/entry[@key='sit_more_tab']"/>
                                    </span>
                                 </span>
                              </a>
                           </li>
                           <!--				</xsl:if> -->
                           <li style="display:none;border-width:0" class="fixTabsIE">
                              <a href="javascript:void(0);">#x20;</a>
                           </li>
                        </ul>
                     </div>
                  </div>
               </xsl:when>
               <xsl:otherwise>
                  <div id="siteNav"></div>
               </xsl:otherwise>
            </xsl:choose>
            <div class="divColor" id="tabBottom"></div>
         </div>
         </xsl:if>
      </div>
   </xsl:template>   
   

   <!--
   =========match selected 1 column layous============
   process a selected page with one column layouts
   param:content - "true" or "false" if rendering tool content or tool list
   ===================================================
   -->
   <xsl:template match="page[@layout='0' and @selected='true']">
      <xsl:param name="content"/>
      <xsl:if test="$content='true'">
         <xsl:call-template name="page-content">
            <xsl:with-param name="page" select="."/>
         </xsl:call-template>
      </xsl:if>
      <xsl:if test="$content='false'">
         <li class="selectedTool">
            <span>
               <xsl:attribute name="class">
                  <xsl:value-of select="menuClass"/>
               </xsl:attribute>
               <xsl:value-of disable-output-escaping="yes" select="title"/>
            </span>
         </li>
      </xsl:if>
   </xsl:template>

   <!--
   ===============match selected 2 column layous============
   process a selected page with two column layouts
   param:content - "true" or "false" if rendering tool content or tool list
   =========================================================
   -->
   <xsl:template match="page[@layout='1' and @selected='true']">
      <xsl:param name="content"/>
      <xsl:if test="$content='true'">
         <xsl:call-template name="page-content-columns">
            <xsl:with-param name="page" select="."/>
         </xsl:call-template>
      </xsl:if>
      <xsl:if test="$content='false'">
         <li class="selectedTool">
            <xsl:attribute name="accesskey">
               <xsl:value-of select="../../@order"/>
            </xsl:attribute>
            <span>
               <xsl:attribute name="class">
                  <xsl:value-of select="menuClass"/>
               </xsl:attribute>
               <xsl:value-of disable-output-escaping="yes" select="title"/>
            </span>
         </li>
      </xsl:if>
   </xsl:template>

   <!--
   ===============match page (default case)=================
   process a page
   param:content - "true" or "false" if rendering tool content or tool list
   =========================================================
   -->
   <xsl:template match="page">
      <xsl:param name="content"/>
      <xsl:if test="$content='true'">
         <!-- do nothing -->
      </xsl:if>
      <xsl:if test="$content='false'">
         <li>
            <a>
               <xsl:if test="@popUp='false'">
                  <xsl:attribute name="href">
                     <xsl:value-of select="url"/>
                  </xsl:attribute>
               </xsl:if>
               <xsl:if test="@popUp='true'">
                  <xsl:attribute name="href">#</xsl:attribute>
                  <xsl:attribute name="onclick">window.open('<xsl:value-of select="popUrl"/>','<xsl:value-of select="string('title')"/>','resizable=yes,toolbar=no,scrollbars=yes,width=800,height=600')</xsl:attribute>
               </xsl:if>
               <xsl:attribute name="accesskey">
                  <xsl:value-of select="../../@order"/>
               </xsl:attribute>
               <xsl:attribute name="class">
                  <xsl:value-of select="menuClass"/>
               </xsl:attribute>
               <span>
                  <xsl:value-of disable-output-escaping="yes" select="title"/>
               </span>
            </a>
         </li>
      </xsl:if>
   </xsl:template>

   <!--
   ======================name page-content============
   process a page's content
   param:page - node for the current page
   ===================================================
   -->
   <xsl:template name="page-content">
      <xsl:param name="page"/>
      <h1 class="skip">
         <xsl:value-of select="$externalized/entry[@key='sit_contentshead']"/>
      </h1>
      <a id="tocontent" class="skip" name="tocontent"></a>
      <div id="content">
         <div id="col1">
            <div class="portlet">

               <xsl:for-each select="$page/columns/column[@index='0']/tools/tool">
                  <xsl:call-template name="tool">
                     <xsl:with-param name="tool" select="."/>
                  </xsl:call-template>
               </xsl:for-each>

            </div>
         </div>
      </div>
   </xsl:template>

   <!--
   ================name page-content-columns================
   process a page's content
   param:page - node for the current page
   =========================================================
   -->
   <xsl:template name="page-content-columns">
      <xsl:param name="page"/>
      <h1 class="skip">
         <xsl:value-of select="$externalized/entry[@key='sit_contentshead']"/>
      </h1>
      <a id="tocontent" class="skip" name="tocontent"></a>
      <div id="content">
         <div id="col1of2">
            <div class="portlet">
               <xsl:for-each select="$page/columns/column[@index='0']/tools/tool">
                  <xsl:call-template name="tool">
                     <xsl:with-param name="tool" select="."/>
                  </xsl:call-template>
               </xsl:for-each>
            </div>
         </div>
         <div id="col2of2">
            <div class="portlet">
               <xsl:for-each select="$page/columns/column[@index='1']/tools/tool">
                  <xsl:call-template name="tool">
                     <xsl:with-param name="tool" select="."/>
                  </xsl:call-template>
               </xsl:for-each>
            </div>
         </div>
      </div>
   </xsl:template>

   <!--
   ================name tool===============================
   process a tool for displaying content
   param:tool - node for the current tool
   ========================================================
   -->
   <xsl:template name="tool">
      <xsl:param name="tool"/>

      <xsl:if test="$tool/@isPortletPlacement='false'">
          <div class="portletTitleWrap">
             <div class="portletTitle">
                <div class="title">
                   <xsl:if test="$tool/@hasReset='true'">
                      <a>
                         <xsl:attribute name="href">
                            <xsl:value-of select="$tool/toolReset"/>
                         </xsl:attribute>
                         <xsl:attribute name="title">
                            <xsl:value-of select="$externalized/entry[@key='sit_reset']"/>
                         </xsl:attribute>
                         <xsl:attribute name="target">
                            <xsl:value-of select="$tool/escapedId"/>
                         </xsl:attribute>
                         <img src="/library/image/transparent.gif" border="1">
                            <xsl:attribute name="alt">
                               <xsl:value-of select="$externalized/entry[@key='sit_reset']"/>
                            </xsl:attribute>
                         </img>
                      </a>
                   </xsl:if>
                   <h2>
                      <xsl:value-of disable-output-escaping="yes" select="$tool/title"/>
                   </h2>
                </div>
                <div class="action">
                   <xsl:if test="$tool/@hasHelp='true'">
                      <a accesskey="h" target="_blank">
                         <xsl:attribute name="href">
                            <xsl:value-of select="$tool/toolHelp"/>
                         </xsl:attribute>
                         <xsl:attribute name="onClick">openWindow('<xsl:value-of select="$tool/toolHelp"/>','<xsl:value-of select="$externalized/entry[@key='sit_help']"/>','resizable=yes,toolbar=no,scrollbars=yes,menubar=yes,width=800,height=600'); return false</xsl:attribute>
                         <img src="/library/image/transparent.gif" border="0">
                            <xsl:attribute name="alt">
                               <xsl:value-of select="$externalized/entry[@key='sit_help']"/>
                            </xsl:attribute>
                         </img>
                      </a>
                   </xsl:if>
                </div>
             </div>
          </div>
      </xsl:if>

      <div class="portletMainWrap">

         <xsl:choose>
            <xsl:when test="$tool/@renderResult = 'true'">
               <xsl:copy-of select="$tool/content/node()"/>
            </xsl:when>
            <xsl:otherwise>
               <iframe
                     class="portletMainIframe"
                     height="560"
                     width="100%"
                     frameborder="0"
                     marginwidth="0"
                     marginheight="0"
                     scrolling="auto">
                  <xsl:attribute name="title">
                     <xsl:value-of select="$tool/title" disable-output-escaping="yes"/>
                  </xsl:attribute>
                  <xsl:attribute name="name">
                     <xsl:value-of select="$tool/escapedId"/>
                  </xsl:attribute>
                  <xsl:attribute name="id">
                     <xsl:value-of select="$tool/escapedId"/>
                  </xsl:attribute>
                  <xsl:attribute name="src">
                     <xsl:value-of select="$tool/url"/>
                  </xsl:attribute>
                  your browser doesn't support iframes
               </iframe>
            </xsl:otherwise>
         </xsl:choose>

      </div>
   </xsl:template>

   <!--
   ======================name site_tools====================
   process the site tools list
   =============================================================
   -->
   <xsl:template name="site_tools">
      <div class="divColor" id="toolMenuWrap">
         <div id="worksiteLogo">
            <xsl:if test="/portal/config/@pageNavPublished = 'false'">
               <p id="siteStatus">unpublished site</p>
            </xsl:if>
         </div>
         <a id="toolmenu" class="skip" name="toolmenu"></a>
         <h1 class="skip">
            <xsl:value-of select="$externalized/entry[@key='sit_toolshead']"/>
         </h1>

         <div id="toolMenu">
            <ul>

               <xsl:for-each select="categories/category">
                  <xsl:sort select="@order" data-type="number"/>
                  <xsl:apply-templates select=".">
                     <xsl:with-param name="content" select="'false'"/>
                  </xsl:apply-templates>
               </xsl:for-each>

               <xsl:if test="//tool/@hasHelp='true'">
               <li>
                  <a accesskey="h" href="javascript:;" class="icon-sakai-help">
                     <xsl:attribute name="onclick">window.open('<xsl:value-of select="config/helpUrl"/>','Help','resizable=yes,toolbar=no,scrollbars=yes, width=800,height=600')</xsl:attribute>
                     <xsl:attribute name="onkeypress">window.open('<xsl:value-of select="config/helpUrl"/>','Help','resizable=yes,toolbar=no,scrollbars=yes, width=800,height=600')</xsl:attribute>Help</a>
               </li>
			</xsl:if>
            </ul>
         </div>

         <xsl:if test="$config/presence[@include='true']">
            <xsl:call-template name="presence"/>
         </xsl:if>

      </div>
   </xsl:template>

   <!--
   ===============name footer==========================
   process the main portal footer
   ========================================================
   -->
   <xsl:template name="footer">
      <div align="center" id="footer">
         <div class="footerExtNav" align="center">
            <xsl:for-each select="config/bottomNavs/bottomNav">
               <xsl:value-of select="." disable-output-escaping="yes"/>
               <xsl:if test="last() != position()">
                  <xsl:value-of select="' | '"/>
               </xsl:if>
            </xsl:for-each>
         </div>

         <div id="footerInfo">
            <span class="skip">
               <xsl:value-of select="$externalized/entry[@key='site_newwindow']"/>
            </span>

            <!-- <xsl:for-each select="config/poweredBy">
              <a href="http://sakaiproject.org" target="_blank">
                 <img border="0" src="/library/image/sakai_powered.gif" alt="Powered by Sakai" />
              </a>
           </xsl:for-each> -->

            <br/>
            <!-- <span class="sakaiCopyrightInfo"><xsl:value-of select="config/copyright"/><br />
              <xsl:value-of select="config/service"/> - <xsl:value-of select="config/serviceVersion"/> - Sakai <xsl:value-of
                 select="config/sakaiVersion"/> - Server "<xsl:value-of select="config/server"/>"
           </span> -->
            <div class="sakaiCopyrightInfo">
               Copyright &copy; 2005-2008 <a href="http://www.rsmart.com" target="_blank">The rSmart Group</a>. All
               rights reserved.<br/>Portions of The rSmart Sakai CLE are copyrighted by other parties as described in
               the
               <a onclick="window.open('/library/content/gateway/acknowledgments.html','Acknowledgments','scrollbars=yes,resizable=yes,width=800,height=500')" title="View the Acknowledgments screen" href="#">Acknowledgments</a>
               screen.
               <br/>
               <xsl:value-of select="config/service"/>
               -
               <xsl:value-of select="config/serviceVersion"/>
               - Sakai
               <xsl:value-of select="config/sakaiVersion"/>
               - Server "<xsl:value-of select="config/server"/>"
            </div>

         </div>
      </div>
   </xsl:template>

   <!--
   ===============match site current my workspace===============
   =====================================================================
   -->
   <xsl:template match="site[@selected='true' and @myWorkspace='true']">
      <li class="selectedTab">
         <a href="#">
            <span>
               <xsl:value-of select="$externalized/entry[@key='sit_mywor']"/>
            </span>
         </a>
      </li>
   </xsl:template>

   <!--
   ===============match site current===============
   =====================================================================
   -->
   <xsl:template match="site[@selected='true' and @myWorkspace!='true']">
      <li class="selectedTab">
         <a href="#">
            <span>
               <xsl:value-of disable-output-escaping="yes" select="title"/>
            </span>
         </a>
      </li>
   </xsl:template>

   <!--
   ===============match site my workspace===============
   =====================================================================
   -->
   <xsl:template match="site[@myWorkspace='true' and @selected!='true']">
      <li>
         <a>
            <xsl:attribute name="href">
               <xsl:value-of select="url"/>
            </xsl:attribute>
            <xsl:attribute name="title">
               <xsl:value-of select="$externalized/entry[@key='sit_mywor']"/>
            </xsl:attribute>
            <span>
               <xsl:value-of select="$externalized/entry[@key='sit_mywor']"/>
            </span>
         </a>
      </li>
   </xsl:template>

   <!--
   ===============match site===============
   =====================================================================
   -->
   <xsl:template match="site">
      <li>
         <a>
            <xsl:attribute name="href">
               <xsl:value-of select="url"/>
            </xsl:attribute>
            <xsl:attribute name="title">
               <xsl:value-of disable-output-escaping="yes" select="title"/>
            </xsl:attribute>
            <span>
               <xsl:value-of disable-output-escaping="yes" select="title"/>
            </span>
			 <xsl:param name="quick"/>
			<xsl:if test="$quick !='true'">
				<xsl:variable name="description"><xsl:value-of select="shortDescription"/></xsl:variable>
				<xsl:if test="$description != '' and type = 'course' and /portal/config/displaySiteDescription = 'true' "> 
					(<xsl:value-of select="substring($description, 1, 35)"/>) 
				</xsl:if>
			</xsl:if>	
         </a> 
      </li>
   </xsl:template>

   <!--
   ===============match site that has been selected===============
   process a selected site for navigation
   param:extra - if this is running during the "more" list
   ===================================================================
   -->
   <!--xsl:template match="site[@selected='true']">
      <xsl:param name="extra"/>
      <xsl:if test="$extra='false'">
         <td>
            <a href="#">
               <xsl:value-of select="title"/>
			</a>
			
         </td>
      </xsl:if>
   </xsl:template-->

   <!--
   ===============match site (default case)===============
   process a selected site for navigation
   param:extra - if this is running during the "more" list
   ============================================================
   -->
   <!--xsl:template match="site">
      <xsl:param name="extra"/>
      <xsl:if test="$extra='false'">
         <td>
            <a target="_parent">
               <xsl:attribute name="href">
                  <xsl:value-of select="url"/>
               </xsl:attribute>
               <xsl:attribute name="title">
                  <xsl:value-of select="title"/>
               </xsl:attribute>
               <xsl:value-of select="title"/>
            </a>
         </td>
      </xsl:if>
   </xsl:template-->

   <!--
   ======-name presence===========================
   process the presence area
   ====================================================
   -->
   <xsl:template name="presence">
      <div class="presenceWrapper">
         <div id="presenceTitle">
            <xsl:value-of select="$externalized/entry[@key='sit_presencetitle']"/>
         </div>
         <iframe
               name="presence"
               id="presenceIframe"
               title="Users Present in Site"
               height="200"
               frameborder="0"
               marginwidth="0"
               marginheight="0"
               scrolling="auto">
            <xsl:attribute name="src">
               <xsl:value-of select="$config/presence"/>
            </xsl:attribute>
            Your browser doesn't support frames
         </iframe>
      </div>
   </xsl:template>

   <!--
   =================name breadcrumbs==============
   breadcrumb processing
   ===============================================
   -->

   <!--
     <xsl:template name="breadcrumbs">
        <xsl:variable name="siteTypeKey" select="siteTypes/siteType[@selected='true']/key"/>
        <xsl:variable name="toolCategoryKey" select="categories/category[@selected='true']/key"/>

  <div class="breadcrumb breadcrumbHolder workspace">
   -->
   <!--Active link/breadcrumb li gets the class selectedCrumb-->
   <!--
       <xsl:if test="siteTypes/siteType[@selected='true']">
          <xsl:if test="siteTypes/siteType[@selected='true' and key!='org.theospi.portfolio.portal.myWorkspace'
                        and not(//portal/siteTabs/div[@id='blank']/div/div[@id='siteNav'])]">
             <xsl:call-template name="breadcrumb_entry">
                <xsl:with-param name="node" select="siteTypes/siteType[@selected='true']"/>
                <xsl:with-param name="title" select="$externalized/entry[@key=$siteTypeKey]"/>
                <xsl:with-param name="last" select="count(siteTypes/siteType/sites/site[@selected='true']) = 0"/>
             </xsl:call-template>
          </xsl:if>

          <xsl:if test="siteTypes/siteType/sites/site[@selected='true']">
             <xsl:call-template name="breadcrumb_entry">
                <xsl:with-param name="node" select="siteTypes/siteType/sites/site[@selected='true']"/>
                <xsl:with-param name="title" select="siteTypes/siteType/sites/site[@selected='true']/title"/>
             </xsl:call-template>
             <xsl:if test="categories/category[@selected='true']">
                <xsl:if test="categories/category[key!='org.theospi.portfolio.portal.model.ToolCategory.uncategorized']">
                   <xsl:call-template name="breadcrumb_entry">
                      <xsl:with-param name="node" select="categories/category[@selected='true']"/>
                      <xsl:with-param name="title" select="$externalized/entry[@key=$toolCategoryKey]"/>
                      <xsl:with-param name="last" select="count(categories/category/pages/page[@selected='true']) = 0"/>
                   </xsl:call-template>
                </xsl:if>
             </xsl:if>

             <xsl:if test="categories/category/pages/page[@selected='true']">
                <xsl:call-template name="breadcrumb_entry">
                   <xsl:with-param name="node" select="categories/category/pages/page[@selected='true']"/>
                   <xsl:with-param name="title" select="categories/category/pages/page[@selected='true']/title"/>
                   <xsl:with-param name="last" select="'true'"/>
                </xsl:call-template>
             </xsl:if>

          </xsl:if>
       </xsl:if>
  </div>
     </xsl:template>
   -->
   <!--
   =================name breadcrumb_entry==============
   breadcumb processing
   ===============================================
   -->
   <!--
     <xsl:template name="breadcrumb_entry">
        <xsl:param name="node"/>
        <xsl:param name="title"/>
        <xsl:param name="last"/>
              <a>
                 <xsl:if test="$last = 'true'">
                    <xsl:attribute name="class">selectedCrumb</xsl:attribute>
                 </xsl:if>
                 <xsl:attribute name="href">
                    <xsl:value-of select="$node/url"/>
                 </xsl:attribute>
                 <xsl:attribute name="title">
                    <xsl:value-of select="$title"/>
                 </xsl:attribute>
                 <xsl:value-of select="$title"/>
              </a>
              <xsl:if test="$last != 'true'">
                 &gt;
              </xsl:if>
     </xsl:template>
   -->
   <!--
   ====================================================
   -->
   <xsl:template name="tool_category">
      <xsl:param name="category"/>
      <xsl:variable name="layoutFile" select="$category/layoutFile"/>
      <xsl:variable name="layout" select="document($layoutFile)"/>

      <h1 class="skip">
         <xsl:value-of select="$externalized/entry[@key='sit_contentshead']"/>
      </h1>
      <a id="tocontent" class="skip" name="tocontent"></a>
      <div id="content">
         <div id="col1">
            <div class="portlet">
               <div class="portletMainWrap">
                  <div class="portletBody">
                     <xsl:apply-templates select="$layout/*">
                        <xsl:with-param name="category" select="$category"/>
                     </xsl:apply-templates>
                  </div>
               </div>
            </div>
         </div>
      </div>
   </xsl:template>

   <xsl:template match="osp:tool">
      <xsl:param name="category"/>
      <xsl:variable name="currentToolId" select="@id"/>
      <xsl:if test="$category/pages/page[@toolId=$currentToolId]">
         <xsl:apply-templates select="@*|node()">
            <xsl:with-param name="currentTool" select="$category/pages/page[@toolId=$currentToolId]"/>
            <xsl:with-param name="category" select="$category"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   <xsl:template match="osp:toolIterator">
      <xsl:param name="category"/>
      <xsl:param name="currentTool"/>
      <xsl:variable name="currentToolId" select="$currentTool/@toolId"/>
      <xsl:variable name="iteratorNode" select="."/>
      <xsl:for-each select="$category/pages/page[@toolId=$currentToolId]">
         <xsl:apply-templates select="$iteratorNode/*">
            <xsl:with-param name="currentTool" select="."/>
            <xsl:with-param name="category" select="$category"/>
         </xsl:apply-templates>
      </xsl:for-each>
   </xsl:template>

   <xsl:template match="osp:toolTitle">
      <xsl:param name="category"/>
      <xsl:param name="currentTool"/>
      <xsl:value-of disable-output-escaping="yes" select="$currentTool/title"/>
   </xsl:template>

   <xsl:template match="osp:toolLink">
      <xsl:param name="category"/>
      <xsl:param name="currentTool"/>
      <a>
         <xsl:if test="$currentTool/@popUp='false'">
            <xsl:attribute name="href">
               <xsl:value-of select="$currentTool/url"/>
            </xsl:attribute>
            <xsl:attribute name="target">_parent</xsl:attribute>
         </xsl:if>
         <xsl:if test="$currentTool/@popUp='true'">
            <xsl:attribute name="href">#</xsl:attribute>
            <xsl:attribute name="onclick">window.open('<xsl:value-of select="$currentTool/popUrl"/>','<xsl:value-of select="string('title')"/>','resizable=yes,toolbar=no,scrollbars=yes, width=800,height=600')</xsl:attribute>
         </xsl:if>
         <xsl:apply-templates select="@*|node()">
            <xsl:with-param name="currentTool" select="$currentTool"/>
            <xsl:with-param name="category" select="$category"/>
         </xsl:apply-templates>
      </a>
   </xsl:template>

   <xsl:template match="osp:site_role">
      <xsl:param name="category"/>
      <xsl:param name="currentTool"/>
      <xsl:variable name="roleId" select="@role"/>
      <xsl:comment>
         got a role section:
         <xsl:value-of select="$roleId"/>
      </xsl:comment>
      <xsl:if test="$roles/role[@id=$roleId]">
         <xsl:comment>
            matched a role:
            <xsl:value-of select="$roleId"/>
         </xsl:comment>
         <xsl:apply-templates select="@*|node()">
            <xsl:with-param name="currentTool" select="$currentTool"/>
            <xsl:with-param name="category" select="$category"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   <!-- Identity transformation -->
   <xsl:template match="@*|*">
      <xsl:param name="currentTool"/>
      <xsl:param name="category"/>
      <xsl:if test="count($category) > 0">
         <xsl:copy>
            <xsl:apply-templates select="@*|node()">
               <xsl:with-param name="currentTool" select="$currentTool"/>
               <xsl:with-param name="category" select="$category"/>
            </xsl:apply-templates>
         </xsl:copy>
      </xsl:if>
   </xsl:template>
   
 <xsl:template name="role_view">                                         
    <!--xsl:if test="/portal/sites/siteTypes/siteType/sites/site/viewAsStudentLink"-->				 
			<div id="roleSwitch">			
			<xsl:choose>
				<xsl:when test="/portal/sites/siteTypes/siteType/sites/site/viewAsStudentLink/roleSwitchState='true'">
						   <a id="roleSwitchAnchor">
							 <xsl:attribute name="href">
								<xsl:value-of select="/portal/sites/siteTypes/siteType/sites/site/viewAsStudentLink/switchRoleUrl"/>
							 </xsl:attribute>
							 <xsl:value-of select="$externalized/entry[@key='rs_exitRoleSwap']"/><xsl:text> </xsl:text>
							 <xsl:value-of select="/portal/sites/siteTypes/siteType/sites/site/viewAsStudentLink/roleUrlValue"/><xsl:text> </xsl:text>
							 <xsl:value-of select="$externalized/entry[@key='rs_view']"/>
						  </a>
				</xsl:when>
								
				<xsl:when test="/portal/sites/siteTypes/siteType/sites/site/viewAsStudentLink/roleswapdropdown='true'">
				<xsl:text><xsl:value-of select="$externalized/entry[@key='rs_viewSiteAs']"/></xsl:text><xsl:text>  </xsl:text>
							<a>
							 <xsl:attribute name="href">
								<xsl:value-of select="/portal/sites/siteTypes/siteType/sites/site/viewAsStudentLink/switchRoleUrl"/>
							 </xsl:attribute>
							 </a>
							 
					    <select id="roleSwitchSelect" onchange="javascript:parent.location = this.options[this.selectedIndex].value;">	
					    <!--select id="roleSwitchSelect" onchange="javascript:if (this.options[this.selectedIndex].value != '') { parent.location = this.options[this.selectedIndex].value; } else { this.selectedIndex = 0; }"--> 
		                        	<option value="" selected="selected"><xsl:value-of select="$externalized/entry[@key='rs_selectRole']"/></option> 	 
		                       <xsl:for-each select="/portal/sites/siteTypes/siteType/sites/site/viewAsStudentLink/siteRoles/role">
		                            <option>
		                             <xsl:attribute name="value">
		<xsl:value-of select="/portal/sites/siteTypes/siteType/sites/site/viewAsStudentLink/switchRoleUrl"/><xsl:value-of select="."/><xsl:value-of select="/portal/sites/siteTypes/siteType/sites/site/viewAsStudentLink/panelString"/>
									 </xsl:attribute>
		                            <xsl:value-of select="."/>
		                            </option> 	 
		                       </xsl:for-each>	 
		                </select>
				</xsl:when>
							
				<xsl:otherwise>
						<a id="roleSwitchAnchor">
							 <xsl:attribute name="href">
								<xsl:value-of select="/portal/sites/siteTypes/siteType/sites/site/viewAsStudentLink/switchRoleUrl"/>
							 </xsl:attribute>
							  <xsl:value-of select="$externalized/entry[@key='rs_enterRoleSwap']"/><xsl:text> </xsl:text>
							 <xsl:value-of select="/portal/sites/siteTypes/siteType/sites/site/viewAsStudentLink/roleUrlValue"/><xsl:text>  </xsl:text>
							 <xsl:value-of select="$externalized/entry[@key='rs_view']"/>
						 </a>
				</xsl:otherwise>
			</xsl:choose>		 				
			</div>
       <!--/xsl:if-->                                        
</xsl:template>

</xsl:stylesheet>
