/**********************************************************************************
 * $URL: https://svn.rsmart.com/svn/vendor/branches/sakai/rsmart-cle/msgcntr/messageforums-hbm/src/java/org/sakaiproject/component/app/messageforums/dao/hibernate/PrivateForumImpl.java $
 * $Id: PrivateForumImpl.java 13692 2009-04-24 16:14:32Z jbush $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.app.messageforums.dao.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.PrivateForum;

public class PrivateForumImpl extends BaseForumImpl implements PrivateForum {

    private static final Log LOG = LogFactory.getLog(PrivateForumImpl.class);
    
    private String owner;
    private Boolean autoForward;
    private String autoForwardEmail;
    private Boolean previewPaneEnabled;

    // indecies for hibernate
    //private int areaindex;

    public Boolean getAutoForward() {
        return autoForward;
    }

    public void setAutoForward(Boolean autoForward) {
        this.autoForward = autoForward;
    }

    public String getAutoForwardEmail() {
        return autoForwardEmail;
    }

    public void setAutoForwardEmail(String autoForwardEmail) {
        this.autoForwardEmail = autoForwardEmail;
    }

    public Boolean getPreviewPaneEnabled() {
        return previewPaneEnabled;
    }

    public void setPreviewPaneEnabled(Boolean previewPaneEnabled) {
        this.previewPaneEnabled = previewPaneEnabled;
    }

//    public int getAreaindex() {
//        try {
//            return getArea().getPrivateForums().indexOf(this);
//        } catch (Exception e) {
//            return areaindex;
//        }
//    }
//
//    public void setAreaindex(int areaindex) {
//        this.areaindex = areaindex;
//    }

    public String getOwner()
    {
      return owner;
    }

    public void setOwner(String owner)
    {
      this.owner = owner;
    }

}
