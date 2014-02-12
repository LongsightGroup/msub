import os, sys
from SOAPpy import WSDL

siteid = "smalmailtool"
username = "admin"
password = "admin"

server_url = "http://localhost:8080"

login_url = server_url + "/sakai-axis/SakaiLogin.jws?wsdl"
script_url = server_url + "/sakai-axis/SakaiScript.jws?wsdl"

login_proxy = WSDL.SOAPProxy(login_url)
script_proxy = WSDL.SOAPProxy(script_url)

loginsoap = WSDL.SOAPProxy(login_url)
sessionid = loginsoap.login(username, password)

scriptsoap = WSDL.SOAPProxy(script_url)

scriptsoap.addNewSite(sessionid, siteid, "Mailtool", "A site containing all the tools tha",  "All the shipped tools", "", "", True, "access", True, True, "", "test" )

print scriptsoap.addNewPageToSite(sessionid,siteid,"Announcements",0)
print scriptsoap.addNewPageToSite(sessionid,siteid,"Role",0)
print scriptsoap.addNewPageToSite(sessionid,siteid,"User",0)
print scriptsoap.addNewPageToSite(sessionid,siteid,"Tree",0)
print scriptsoap.addNewPageToSite(sessionid,siteid,"Side",0)
print scriptsoap.addNewPageToSite(sessionid,siteid,"Foothill",0)
print scriptsoap.addNewPageToSite(sessionid,siteid,"Sections",0)
print scriptsoap.addNewPageToSite(sessionid,siteid,"Info",0)

print scriptsoap.addNewToolToPage(sessionid,siteid,"Announcements","Announcements","sakai.announcements","")
print scriptsoap.addNewToolToPage(sessionid,siteid,"Sections","Sections","sakai.sections","")
print scriptsoap.addNewToolToPage(sessionid,siteid,"Info","Info","sakai.siteinfo","")

#Role
print scriptsoap.addNewToolToPage(sessionid,siteid,"Role","Role","sakai.mailtool","")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Role","Role","subjectprefix","")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Role","Role","recipview","role")

print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Role","Role","role1realmid","/site/" + siteid)
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Role","Role","role1id","maintain")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Role","Role","role1singular","Instructor")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Role","Role","role1plural","Instructors")

print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Role","Role","role2realmid","/site/" + siteid)
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Role","Role","role2id","access")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Role","Role","role2singular","Student")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Role","Role","role2plural","Students")

#User
print scriptsoap.addNewToolToPage(sessionid,siteid,"User","User","sakai.mailtool","")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"User","User","subjectprefix","")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"User","User","recipview","user")

print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"User","User","role1realmid","/site/" + siteid)
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"User","User","role1id","maintain")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"User","User","role1singular","Instructor")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"User","User","role1plural","Instructors")

print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"User","User","role2realmid","/site/" + siteid)
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"User","User","role2id","access")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"User","User","role2singular","Student")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"User","User","role2plural","Students")

#Tree
print scriptsoap.addNewToolToPage(sessionid,siteid,"Tree","Tree","sakai.mailtool","")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Tree","Tree","subjectprefix","")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Tree","Tree","recipview","tree")

print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Tree","Tree","role1realmid","/site/" + siteid)
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Tree","Tree","role1id","maintain")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Tree","Tree","role1singular","Instructor")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Tree","Tree","role1plural","Instructors")

print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Tree","Tree","role2realmid","/site/" + siteid)
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Tree","Tree","role2id","access")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Tree","Tree","role2singular","Student")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Tree","Tree","role2plural","Students")

#Foothill
print scriptsoap.addNewToolToPage(sessionid,siteid,"Foothill","Foothill","sakai.mailtool","")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Foothill","Foothill","subjectprefix","")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Foothill","Foothill","recipview","foothill")

print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Foothill","Foothill","role1realmid","/site/" + siteid)
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Foothill","Foothill","role1id","maintain")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Foothill","Foothill","role1singular","Instructor")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Foothill","Foothill","role1plural","Instructors")

print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Foothill","Foothill","role2realmid","/site/" + siteid)
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Foothill","Foothill","role2id","access")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Foothill","Foothill","role2singular","Student")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Foothill","Foothill","role2plural","Students")

#Side By Side
print scriptsoap.addNewToolToPage(sessionid,siteid,"Side","Side","sakai.mailtool","")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Side","Side","subjectprefix","")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Side","Side","recipview","sidebyside")

print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Side","Side","role1realmid","/site/" + siteid)
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Side","Side","role1id","maintain")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Side","Side","role1singular","Instructor")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Side","Side","role1plural","Instructors")

print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Side","Side","role2realmid","/site/" + siteid)
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Side","Side","role2id","access")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Side","Side","role2singular","Student")
print scriptsoap.addConfigPropertyToTool(sessionid,siteid,"Side","Side","role2plural","Students")

#Add Users to Site
print scriptsoap.addMemberToSiteWithRole(sessionid, siteid, "wma001", "maintain")
print scriptsoap.addMemberToSiteWithRole(sessionid, siteid, "seb001", "maintain")
print scriptsoap.addMemberToSiteWithRole(sessionid, siteid, "jca001", "maintain")

print scriptsoap.addMemberToSiteWithRole(sessionid, siteid, "kan001", "access")
print scriptsoap.addMemberToSiteWithRole(sessionid, siteid, "kda001", "access")
print scriptsoap.addMemberToSiteWithRole(sessionid, siteid, "msa001", "access")
print scriptsoap.addMemberToSiteWithRole(sessionid, siteid, "jhu001", "access")
print scriptsoap.addMemberToSiteWithRole(sessionid, siteid, "dtr001", "access")
print scriptsoap.addMemberToSiteWithRole(sessionid, siteid, "jwi001", "access")
print scriptsoap.addMemberToSiteWithRole(sessionid, siteid, "cxi001", "access")
print scriptsoap.addMemberToSiteWithRole(sessionid, siteid, "jra001", "access")
print scriptsoap.addMemberToSiteWithRole(sessionid, siteid, "jro001", "access")
print scriptsoap.addMemberToSiteWithRole(sessionid, siteid, "yar001", "access")
print scriptsoap.addMemberToSiteWithRole(sessionid, siteid, "spe001", "access")
print scriptsoap.addMemberToSiteWithRole(sessionid, siteid, "yra001", "access")
print scriptsoap.addMemberToSiteWithRole(sessionid, siteid, "nma001", "access")
print scriptsoap.addMemberToSiteWithRole(sessionid, siteid, "fkl001", "access")
print scriptsoap.addMemberToSiteWithRole(sessionid, siteid, "rme001", "access")
