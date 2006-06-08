/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-hbm/src/java/org/sakaiproject/component/app/messageforums/dao/hibernate/OpenForumImpl.java $
 * $Id: OpenForumImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
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
import org.sakaiproject.api.app.messageforums.OpenForum;

public class OpenForumImpl extends BaseForumImpl implements OpenForum {

    private static final Log LOG = LogFactory.getLog(OpenForumImpl.class);
    
    private Boolean draft;
    private Boolean locked;
    private String defaultAssignName;
    
    // indecies for hibernate
    //protected int areaindex;

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

//    public int getAreaindex() {
//        try {
//            return getArea().getDiscussionForums().indexOf(this);
//        } catch (Exception e) {
//            return areaindex;
//        }
//    }
//
//    public void setAreaindex(int areaindex) {
//        this.areaindex = areaindex;
//    }

    public Boolean getDraft() {
        return draft;
    }

    public void setDraft(Boolean draft) {
        this.draft = draft;
    }

    public String getDefaultAssignName()
    {
      return defaultAssignName;
    }

    public void setDefaultAssignName(String defaultAssignName)
    {
      this.defaultAssignName = defaultAssignName;
    }

}