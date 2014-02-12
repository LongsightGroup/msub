<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<%
response.setContentType("text/html; charset=UTF-8");
%>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<f:view>
        <sakai:view_container title="Stanford iTuneU">
                <sakai:view_content>
<h:form>
<f:verbatim>
<TABLE WIDTH=100% CELLPADDING=4 CELLSPACING=2 BORDER=0>
<TR><TD><FONT FACE="arial,helvetica" SIZE="-1">
<P>
Stanford iTunes for Courses is a new Stanford-only space for sharing audio files 
via the Apple iTunes store interface.  Access privileges from CourseWork are passed 
along to the iTunes environment automatically, so once students, instructors or admins 
are registered in CourseWork they can go directly to the Stanford iTunes site for 
their course.
</P>
<P>
Users browse, play, and download files in the Stanford iTunes space using the iTunes program, 
which is available for both PC and Mac platforms as a free download. Once in Stanford iTunes, 
admins can upload and organize files in a series of tabs designated for download, playback only, 
podcasting, and as dropboxes.
</P>
<P>
For more information on the Stanford iTunes Project, please visit <a href="http://itunes.stanford.edu">http://itunes.stanford.edu</a>.
</P>
<P>
The free iTunes software, version 4.7.1 or newer, is required to access Stanford iTunes content. 
You can download the latest version of iTunes at 
<a href="http://www.apple.com/itunes/download">http://www.apple.com/itunes/download</a>.
This tool is currently under development. While we are beta testing we ask that you 
provide feedback on how it is working, as well as any future feature requests, 
to stanford-itunes@lists.stanford.edu. For basic instructions on how to use the tool, 
please consult the documentation found at 
<a href="http://itunes.stanford.edu/documentation">http://itunes.stanford.edu/documentation</a>.
</P>
<P><B>
Accessing the Stanford iTunes for Courses from this admininstration tool will allow you to edit the
iTunes course information and content.
</B></P>
<P>
</f:verbatim>
<h:commandLink accesskey="c" title="itunes" action="itunes" immediate="true">
      <h:outputText value="Click here" />
</h:commandLink>
<f:verbatim>
to open the page you requested in iTunes.
</P>
</FONT>
</TD></TR>
</TABLE>
</f:verbatim>
</h:form>

                </sakai:view_content>
        </sakai:view_container>
</f:view>

