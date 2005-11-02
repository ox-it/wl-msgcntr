/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.messageforums.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.OpenTopic;
import org.sakaiproject.api.app.messageforums.model.ControlPermissionsModel;
import org.sakaiproject.api.app.messageforums.model.MessagePermissionsModel;
import org.sakaiproject.api.app.messageforums.model.OpenTopicModel;

public class OpenTopicModelImpl extends TopicModelImpl implements OpenTopicModel {

    private static final Log LOG = LogFactory.getLog(OpenTopicModelImpl.class);
    
    private ControlPermissionsModel controlPermissions;
    private MessagePermissionsModel messagePermissions;
    private Boolean locked;
    
    // package level constructor only used for Testing
    OpenTopicModelImpl() {}
    
    public OpenTopicModelImpl(OpenTopic openTopic) {
        // TODO: set up this model based on hibernate object passes
        
    }
    
    public ControlPermissionsModel getControlPermissions() {
        return controlPermissions;
    }

    public void setControlPermissions(ControlPermissionsModel controlPermissions) {
        this.controlPermissions = controlPermissions;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public MessagePermissionsModel getMessagePermissions() {
        return messagePermissions;
    }

    public void setMessagePermissions(MessagePermissionsModel messagePermissions) {
        this.messagePermissions = messagePermissions;
    }
        
}
