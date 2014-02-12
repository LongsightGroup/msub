// under construction

var groups = null;

function Group(paramRoleid, pRoleplural, pRolesingular, pUsers)
{
	this.roleid = paramRoleid;
	this.roleplural = pRoleplural;
	this.rolesingular = pRolesingular;
	this.users = pUsers;
}

function User(pUserid, pDisplayname, pEmail)
{
	this.userid = pUserid;
	this.displayname = pDisplayname;
	this.email = pEmail;
}

function testMail()
{
	alert("Hello from inside JSF");
}

function populate(groupsparam)
{
	 groups = groupsparam;
        var selectdiv = document.getElementById("selectdiv");
        var groupslist = document.createElement("select");
        groupslist.setAttribute("id", "selectemails");
        groupslist.setAttribute("multiple", "multiple");
        groupslist.setAttribute("size", "15");

        for (var i = 0; i < groups.length; i++)
        {
                var optgroup = document.createElement("optgroup");
                optgroup.label = groups[i].roleplural;

                var users = groups[i].users;
                for (var j = 0; j < users.length; j++)
                {
                        var option = document.createElement("option");
                        var text = document.createTextNode(users[j].displayname);
                        option.appendChild(text);
                        optgroup.appendChild(option);
                }
                groupslist.appendChild(optgroup);
        }
        selectdiv.appendChild(groupslist);

}

