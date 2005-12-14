/**********************************************************************************
* $URL: $
* $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.messageforums;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.api.kernel.session.ToolSession;
import org.sakaiproject.api.kernel.session.cover.SessionManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.framework.portal.cover.PortalService;
import org.sakaiproject.service.legacy.authzGroup.AuthzGroup;
import org.sakaiproject.service.legacy.authzGroup.Member;
import org.sakaiproject.service.legacy.authzGroup.Role;
import org.sakaiproject.service.legacy.authzGroup.cover.AuthzGroupService;
import org.sakaiproject.service.legacy.content.ContentResource;
import org.sakaiproject.service.legacy.content.cover.ContentHostingService;
import org.sakaiproject.service.legacy.coursemanagement.CourseMember;
import org.sakaiproject.service.legacy.entity.Reference;
import org.sakaiproject.service.legacy.filepicker.FilePickerHelper;
import org.sakaiproject.service.legacy.security.cover.SecurityService;
import org.sakaiproject.service.legacy.site.cover.SiteService;
import org.sakaiproject.service.legacy.user.User;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;
import org.sakaiproject.tool.messageforums.ui.PrivateForumDecoratedBean;
import org.sakaiproject.tool.messageforums.ui.PrivateMessageDecoratedBean;
import org.sakaiproject.tool.messageforums.ui.PrivateTopicDecoratedBean;

public class PrivateMessagesTool
{
  private static final Log LOG = LogFactory.getLog(PrivateMessagesTool.class);

  /**
   *Dependency Injected 
   */
  private PrivateMessageManager prtMsgManager;
  private MessageForumsMessageManager messageManager;
  private MessageForumsForumManager forumManager;
  private ErrorMessages errorMessages;
  
  /** Dependency Injected   */
  private MessageForumsTypeManager typeManager;
  
  /** Naigation for JSP   */
  public static final String MAIN_PG="main";
  public static final String DISPLAY_MESSAGES_PG="pvtMsg";
  public static final String SELECTED_MESSAGE_PG="pvtMsgDetail";
  public static final String COMPOSE_MSG_PG="compose";
  public static final String MESSAGE_SETTING_PG="pvtMsgSettings";
  public static final String SEARCH_RESULT_MESSAGES_PG="pvtMsgEx";
  public static final String DELETE_MESSAGES_PG="pvtMsgDelete";
  public static final String DELETE_FOLDER_PG="pvtMsgFolderDelete";
  public static final String MESSAGE_STATISTICS_PG="pvtMsgStatistics";
  
  /** portlet configuration parameter values**/
  public static final String PVTMSG_MODE_RECEIVED = "Received";
  public static final String PVTMSG_MODE_SENT = "Sent";
  public static final String PVTMSG_MODE_DELETE = "Deleted";
  public static final String PVTMSG_MODE_DRAFT = "Drafts";
  public static final String PVTMSG_MODE_CASE = "Personal Folders";
  
  public static final String RECIPIANTS_ENTIRE_CLASS= "Entire Class";
  public static final String RECIPIANTS_ALL_INSTRUCTORS= "All Instructors";
  
  private boolean pvtAreaEnabled= false;
  
  PrivateForumDecoratedBean decoratedForum;
  
  private PrivateForum forum; 
  private Area pvtArea;
  private List pvtTopics=new ArrayList();
  private List decoratedPvtMsgs;
  private String msgNavMode="" ;
  private PrivateMessageDecoratedBean detailMsg ;
  
  private String currentMsgUuid; //this is the message which is being currently edited/displayed/deleted
  private boolean navModeIsDelete=false ; // Delete mode to show up extra buttons in pvtMsg.jsp page
  private List selectedItems;
  
  private String userName;    //current user
  private Date time ;       //current time
  
  //delete confirmation screen - single delete 
  private boolean deleteConfirm=false ; //used for displaying delete confirmation message in same jsp
  
  //Compose Screen
  private List selectedComposeToList;
  private String composeSendAs="pvtmsg"; // currently set as Default as change by user is allowed
  private String composeSubject ;
  private String composeBody ;
  private String composeLabel ;   
  private List totalComposeToList=new ArrayList();
  
  //Delete items - Checkbox display and selection - Multiple delete
  private List selectedDeleteItems;
  private List totalDisplayItems=new ArrayList() ;
  
  //reply to 
  private String replyToBody ;
  
  //Setting Screen
  private String activatePvtMsg="yes";
  private String forwardPvtMsg="no";
  private String forwardPvtMsgEmail;
  private boolean superUser; 
  
  //message header screen
  private String searchText="Pl. enter search text";
  private String selectView;
  //////////////////////
  /** The configuration mode, received, sent,delete, case etc ... */
  public static final String STATE_PVTMSG_MODE = "pvtmsg.mode";
  
  public PrivateMessagesTool()
  {    
  }

  
  /**
   * @return
   */
  public MessageForumsTypeManager getTypeManager()
  {
    return typeManager;
  }


  /**
   * @param prtMsgManager
   */
  public void setPrtMsgManager(PrivateMessageManager prtMsgManager)
  {
    this.prtMsgManager = prtMsgManager;
  }
  
  /**
   * @param messageManager
   */
  public void setMessageManager(MessageForumsMessageManager messageManager)
  {
    this.messageManager = messageManager;
  }

  
  /**
   * @param typeManager
   */
  public void setTypeManager(MessageForumsTypeManager typeManager)
  {
    this.typeManager = typeManager;
  }

  public Area getArea()
  {
    Area privateArea=prtMsgManager.getPrivateMessageArea();
        
    PrivateForum pf = prtMsgManager.initializePrivateMessageArea(privateArea);    
    
    pvtTopics = pf.getTopics();
    forum=pf;           
        
    return privateArea;
  }
  
  public boolean getPvtAreaEnabled()
  {
    return true ;
    //TODO - as below
    //return prtMsgManager.isPrivateAreaUnabled();
  }
  
  //Return decorated Forum
  public PrivateForumDecoratedBean getDecoratedForum()
  {

      PrivateForumDecoratedBean decoratedForum = new PrivateForumDecoratedBean(forum) ;
      for (Iterator iterator = pvtTopics.iterator(); iterator.hasNext();)
      {
        Topic topic = (Topic) iterator.next();
        if (topic != null)
        {
          PrivateTopicDecoratedBean decoTopic= new PrivateTopicDecoratedBean(topic) ;
          //decoTopic.setTotalNoMessages(prtMsgManager.getTotalNoMessages(topic)) ;
          //decoTopic.setUnreadNoMessages(prtMsgManager.getUnreadNoMessages(SessionManager.getCurrentSessionUserId(), topic)) ;
          
          String typeUuid = null;
          if ("Received".equalsIgnoreCase(topic.getTitle())){
            typeUuid = typeManager.getReceivedPrivateMessageType();
          }
          else if ("Sent".equalsIgnoreCase(topic.getTitle())){
            typeUuid = typeManager.getSentPrivateMessageType();
          }
          else if ("Deleted".equalsIgnoreCase(topic.getTitle())){
            typeUuid = typeManager.getDeletedPrivateMessageType();
          }
          else if ("Drafts".equalsIgnoreCase(topic.getTitle())){
            typeUuid = typeManager.getDraftPrivateMessageType();
          }
          else {
            // 
          }
          
          decoTopic.setTotalNoMessages(prtMsgManager.findMessageCount(topic.getId(),
              typeUuid));
          decoTopic.setUnreadNoMessages(prtMsgManager.findUnreadMessageCount(topic.getId(),
              typeUuid));
          
          decoratedForum.addTopic(decoTopic);
        }          
      }

    return decoratedForum ;
  }

  public List getDecoratedPvtMsgs()
  {
    decoratedPvtMsgs=new ArrayList() ;
    //Shouldn't we be doing like this only 
    
    String typeUuid = null;
    
    if (PVTMSG_MODE_RECEIVED.equals(getSelectedTopicTitle())){
      typeUuid = typeManager.getReceivedPrivateMessageType();  
    }
    else if (PVTMSG_MODE_SENT.equals(getSelectedTopicTitle())){
      typeUuid = typeManager.getSentPrivateMessageType();  
    }
    else if (PVTMSG_MODE_DELETE.equals(getSelectedTopicTitle())){
      typeUuid = typeManager.getDeletedPrivateMessageType();  
    }
    else if (PVTMSG_MODE_DRAFT.equals(getSelectedTopicTitle())){
      typeUuid = typeManager.getDraftPrivateMessageType();  
    }
    else{
      //
    }
    
    decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_DATE,
        PrivateMessageManager.SORT_ASC);
    
//    Area privateArea=prtMsgManager.getPrivateMessageArea();
//    if(privateArea != null ) {
//     List forums=privateArea.getPrivateForums();
//      //Private message return ONLY ONE ELEMENT
//      for (Iterator iter = forums.iterator(); iter.hasNext();)
//      {
//        forum = (PrivateForum) iter.next();
//        pvtTopics=forum.getTopics();
//        
//        //now get messages for each topics
//        for (Iterator iterator = pvtTopics.iterator(); iterator.hasNext();)
//        {
//          Topic topic = (Topic) iterator.next();          
//          if(topic.getTitle().equals(PVTMSG_MODE_RECEIVED))
//          {            
//            //TODO -- getMessages() should be changed to getReceivedMessages() ;
//            //decoratedPvtMsgs=prtMsgManager.getReceivedMessages(getUserId()) ;
//            decoratedPvtMsgs=topic.getMessages() ;
//            break;
//          } 
//          if(topic.getTitle().equals(PVTMSG_MODE_SENT))
//          {
//            //decoratedPvtMsgs=prtMsgManager.getSentMessages(getUserId()) ;
//            decoratedPvtMsgs=topic.getMessages() ;
//            break;
//          }  
//          if(topic.getTitle().equals(PVTMSG_MODE_DELETE))
//          {
//            //decoratedPvtMsgs=prtMsgManager.getDeletedMessages(getUserId()) ;
//            decoratedPvtMsgs=topic.getMessages() ;
//            break;
//          }  
//          if(topic.getTitle().equals(PVTMSG_MODE_DRAFT))
//          {
//            //decoratedPvtMsgs=prtMsgManager.getDraftedMessages(getUserId()) ;
//            decoratedPvtMsgs=topic.getMessages() ;
//            break;
//          }  
//          if(topic.getTitle().equals(PVTMSG_MODE_CASE))
//          {
//            //decoratedPvtMsgs=prtMsgManager.getMessagesByTopic(getUserId(), getSelectedTopicId()) ;
//            decoratedPvtMsgs=topic.getMessages() ;
//            break;
//          }    
//        }
//        //create decorated List
        decoratedPvtMsgs = createDecoratedDisplay(decoratedPvtMsgs);
//      }
//    }
    return decoratedPvtMsgs ;
  }
  
  //decorated display - from List of Message
  public List createDecoratedDisplay(List msg)
  {
    List decLs= new ArrayList() ;
    for (Iterator iter = msg.iterator(); iter.hasNext();)
    {
      PrivateMessage element = (PrivateMessage) iter.next();
      decLs.add(new PrivateMessageDecoratedBean(element)) ;
    }
    return decLs;
  }
  
  public void setDecoratedPvtMsgs(List displayPvtMsgs)
  {
    this.decoratedPvtMsgs=displayPvtMsgs;
  }
  
  public String getMsgNavMode() 
  {
    return msgNavMode ;
  }
 
  public PrivateMessageDecoratedBean getDetailMsg()
  {
    return detailMsg ;
  }

  public void setDetailMsg(PrivateMessageDecoratedBean detailMsg)
  {
    this.detailMsg = detailMsg;
  }

  public String getCurrentMsgUuid()
  {
    return currentMsgUuid;
  }
  
  public void setCurrentMsgUuid(String currentMsgUuid)
  {
    this.currentMsgUuid = currentMsgUuid;
  }

  public List getSelectedItems()
  {
    return selectedItems;
  }
  
  public void setSelectedItems(List selectedItems)
  {
    this.selectedItems=selectedItems ;
  }
  
  public boolean isDeleteConfirm()
  {
    return deleteConfirm;
  }

  public void setDeleteConfirm(boolean deleteConfirm)
  {
    this.deleteConfirm = deleteConfirm;
  }

  public boolean getNavModeIsDelete()
  {
    return navModeIsDelete;
  }
  
  public void setNavModeIsDelete(boolean navModeIsDelete)
  {
    this.navModeIsDelete=navModeIsDelete ;
  }
  
  //Deleted page - checkbox display and selection
  public List getSelectedDeleteItems()
  {
    return selectedDeleteItems;
  }
  public List getTotalDisplayItems()
  {
    return totalDisplayItems;
  }
  public void setTotalDisplayItems(List totalDisplayItems)
  {
    this.totalDisplayItems = totalDisplayItems;
  }
  public void setSelectedDeleteItems(List selectedDeleteItems)
  {
    this.selectedDeleteItems = selectedDeleteItems;
  }

  //Compose Getter and Setter
  public String getComposeBody()
  {
    return composeBody;
  }
  
  public void setComposeBody(String composeBody)
  {
    this.composeBody = composeBody;
  }

  public String getComposeLabel()
  {
    return composeLabel;
  }
  
  public void setComposeLabel(String composeLabel)
  {
    this.composeLabel = composeLabel;
  }

  public String getComposeSendAs()
  {
    return composeSendAs;
  }

  public void setComposeSendAs(String composeSendAs)
  {
    this.composeSendAs = composeSendAs;
  }

  public String getComposeSubject()
  {
    return composeSubject;
  }

  public void setComposeSubject(String composeSubject)
  {
    this.composeSubject = composeSubject;
  }

  public void setSelectedComposeToList(List selectedComposeToList)
  {
    this.selectedComposeToList = selectedComposeToList;
  }
  
  public void setTotalComposeToList(List totalComposeToList)
  {
    this.totalComposeToList = totalComposeToList;
  }
  
  public List getSelectedComposeToList()
  {
    return selectedComposeToList;
  }
  
  //return a list of participants of 'participant' type object
  
  public List getTotalComposeToList()
  {
    List members = new Vector();
    
    //Add Entire class and all participants item for display in JSP
    List ps = new Vector();
    Participant ec= new Participant();
    ec.name=RECIPIANTS_ENTIRE_CLASS;
    ps.add(ec) ;

    Participant ep= new Participant();
    ep.name=RECIPIANTS_ALL_INSTRUCTORS;
    ps.add(ep) ;
    
    List totalComposeToList=new ArrayList() ;
    for (int i = 0; i < ps.size(); i++)
    {
      totalComposeToList.add(new SelectItem(((Participant) ps.get(i)).getName(),((Participant) ps.get(i)).getName()));
    }
    
    List participants = getAllParticipants();  
    for (int i = 0; i < participants.size(); i++)
    {
      totalComposeToList.add(new SelectItem(((Participant) participants.get(i)).getName(),((Participant) participants.get(i)).getName()));
    }
    return totalComposeToList;
  }

  public String getUserName() {
   String userId=SessionManager.getCurrentSessionUserId();
   try
   {
     User user=UserDirectoryService.getUser(userId) ;
     userName= user.getDisplayName();
   }
   catch (IdUnusedException e)
   {
    //e.printStackTrace();
   }
   return userName;
  }
  
  public String getUserId()
  {
    return SessionManager.getCurrentSessionUserId();
  }
  //Reply time
  public Date getTime()
  {
    return new Date();
  }
  //Reply to page
  public String getReplyToBody() {
    return replyToBody;
  }
  public void setReplyToBody(String replyToBody) {
    this.replyToBody=replyToBody;
  }


  //message header Getter 
  public String getSearchText()
  {
    return searchText ;
  }
  public void setSearchText(String searchText)
  {
    this.searchText=searchText;
  }
  public String getSelectView() 
  {
    return selectView ;
  }
  public void setSelectView(String selectView)
  {
    this.selectView=selectView ;
  }
  //////////////////////////////////////////////////////////////////////////////////  
  /**
   * called when any topic like Received/Sent/Deleted clicked
   * @return - pvtMsg
   */
  private String selectedTopicTitle="";
  private String selectedTopicId="";
  public String getSelectedTopicTitle()
  {
    return selectedTopicTitle ;
  }
  public void setSelectedTopicTitle(String selectedTopicTitle) 
  {
    this.selectedTopicTitle=selectedTopicTitle;
  }
  public String getSelectedTopicId()
  {
    return selectedTopicId;
  }
  private void setSelectedTopicId(String selectedTopicId)
  {
    this.selectedTopicId=selectedTopicId;    
  }
  
  public String processActionHome()
  {
    LOG.debug("processActionHome()");
    return  "main";
  }
  public String processDisplayForum()
  {
    LOG.debug("processDisplayForum()");
    return "pvtMsg" ;
  }
  public String processPvtMsgTopic()
  {
    LOG.debug("processPvtMsgTopic()");
    //get external parameter
    selectedTopicTitle = getExternalParameterByKey("pvtMsgTopicTitle") ;
    setSelectedTopicId(getExternalParameterByKey("pvtMsgTopicId")) ;
    msgNavMode=getSelectedTopicTitle();
    //TODO - getPvtMessages based on topicId
    return "pvtMsg";
  }
  
  /**
   * process Cancel from all JSP's
   * @return - pvtMsg
   */  
  public String processPvtMsgCancel() {
    LOG.debug("processPvtMsgCancel()");

    if(getMsgNavMode().equals(""))
    {
      return "main" ; // if navigation is from main page
    }
    else
    {
      return "pvtMsg";
      //return processPvtMsgTopic();   
    }  
  }
  
  /**
   * called when subject of List of messages to Topic clicked for detail
   * @return - pvtMsgDetail
   */ 
  public String processPvtMsgDetail() {
    LOG.debug("processPvtMsgDetail()");
    
    String msgId=getExternalParameterByKey("current_msg_detail");
    setCurrentMsgUuid(msgId) ; 
    //retrive the detail for this message with currentMessageId
    for (Iterator iter = this.getDecoratedPvtMsgs().iterator(); iter.hasNext();)
    {
      PrivateMessageDecoratedBean dMsg= (PrivateMessageDecoratedBean) iter.next();
      if (dMsg.getMsg().getId().equals(new Long(msgId)))
      {
        this.setDetailMsg(dMsg);
      }
    }
    this.deleteConfirm=false; //reset this as used for multiple action in same JSP
    
    return "pvtMsgDetail";
  }

  /**
   * called from Single delete Page
   * @return - pvtMsgReply
   */ 
  public String processPvtMsgReply() {
    LOG.debug("processPvtMsgReply()");
    
    //from message detail screen
    this.setDetailMsg(getDetailMsg()) ;
//    
//    //from compose screen
//    this.setComposeSendAs(getComposeSendAs()) ;
//    this.setTotalComposeToList(getTotalComposeToList()) ;
//    this.setSelectedComposeToList(getSelectedComposeToList()) ;
    
    return "pvtMsgReply";
  }
  
  /**
   * called from Single delete Page
   * @return - pvtMsgMove
   */ 
  public String processPvtMsgMove() {
    LOG.debug("processPvtMsgMove()");
    return "pvtMsgMove";
  }
  
  /**
   * called from Single delete Page
   * @return - pvtMsgDetail
   */ 
  public String processPvtMsgDeleteConfirm() {
    LOG.debug("processPvtMsgDeleteConfirm()");
    
    this.setDeleteConfirm(true);
    /*
     * same action is used for delete..however if user presses some other action after first
     * delete then 'deleteConfirm' boolean is reset
     */
    return "pvtMsgDetail" ;
  }
  
  /**
   * called from Single delete Page -
   * called when 'delete' button pressed second time
   * @return - pvtMsg
   */ 
  public String processPvtMsgDeleteConfirmYes() {
    LOG.debug("processPvtMsgDeleteConfirmYes()");
    if(getDetailMsg() != null)
    {
      //TODO - remove getMessageById()- not required if we remove dummy data
      PrivateMessage msg = (PrivateMessage) prtMsgManager.getMessageById(getDetailMsg().getMsg().getId()) ;
      if(msg != null)
      {
        prtMsgManager.deletePrivateMessage(msg) ;
      }
    }
    return "main" ;
  }
  
  //RESET form variable - required as the bean is in session and some attributes are used as helper for navigation
  public void resetFormVariable() {
    
    this.setNavModeIsDelete(false); 
    this.msgNavMode="" ;
    this.deleteConfirm=false;
    
    attachments.clear();
    oldAttachments.clear();
  }
  
  /**
   * process Compose action from different JSP'S
   * @return - pvtMsgCompose
   */ 
  public String processPvtMsgCompose() {
    this.setDetailMsg(new PrivateMessageDecoratedBean(messageManager.createPrivateMessage()));
    LOG.debug("processPvtMsgCompose()");
    return "pvtMsgCompose" ;
  }
  
  /**
   * process from Compose screen
   * @return - pvtMsg
   */ 
  public String processPvtMsgSend() {
    LOG.debug("processPvtMsgSend()");
    
    PrivateMessage pMsg= constructMessage() ;
    
    prtMsgManager.sendPrivateMessage(pMsg, getRecipiants());            

    if(getMsgNavMode().equals(""))
    {
      return "main" ; // if navigation is from main page
    }
    else
    {
      return "pvtMsg";
      //return processPvtMsgTopic();   
    }
  }
 
  /**
   * process from Compose screen
   * @return - pvtMsg
   */
  public String processPvtMsgSaveDraft() {
    LOG.debug("processPvtMsgSaveDraft()");
    
    PrivateMessage dMsg=constructMessage() ;
    dMsg.setDraft(Boolean.TRUE);

    prtMsgManager.sendPrivateMessage(dMsg, getRecipiants());    

    if(getMsgNavMode().equals(""))
    {
      return "main" ; // if navigation is from main page
    }
    else
    {
      return "pvtMsg";
      //return processPvtMsgTopic();   
    } 
  }
  // created separate method as to be used with processPvtMsgSend() and processPvtMsgSaveDraft()
  public PrivateMessage constructMessage()
  {
    PrivateMessage aMsg;
    // in case of compose this is a new message 
    if (this.getDetailMsg() == null )
    {
      aMsg = messageManager.createPrivateMessage() ;
    }
    //if reply to a message then message is existing
    else {
      aMsg = (PrivateMessage)this.getDetailMsg().getMsg();       
    }
    if (aMsg != null)
    {
      //TODO - check where recipiants are stored
      //aMsg.setRecipients(getSelectedComposeToList());
      aMsg.setTitle(getComposeSubject());
      aMsg.setBody(getComposeBody());
      // these are set by the create method above -- you can remove them or keep them if you really want :)
      //aMsg.setCreatedBy(getUserId());
      //aMsg.setCreated(getTime()) ;
      aMsg.setAuthor(getUserId());
      aMsg.setDraft(Boolean.FALSE);      
      aMsg.setApproved(Boolean.TRUE);      
    }
    //Add attachments
    for(int i=0; i<attachments.size(); i++)
    {
      prtMsgManager.addAttachToPvtMsg(aMsg, (Attachment)attachments.get(i));         
    }    
    //clear
    attachments.clear();
    oldAttachments.clear();
    
    return aMsg;    
  }
  ///////////////////// Previous/Next topic and message on Detail message page
  public String processDisplayNextMsg()
  {
    LOG.debug("processDisplayNextMsg()");
    return processDisplayMsgById("nextMsgId");
  }
  
  /**
   * @return
   */
  public String processDisplayPreviousMsg()
  {
    LOG.debug("processDisplayPreviousMsg()");
    return processDisplayMsgById("previousMsgId");
  }
  /**
   * @param externalTopicId
   * @return
   */
  private String processDisplayMsgById(String externalMsgId)
  {
    LOG.debug("processDisplayMsgById()");
    String msgId=getExternalParameterByKey(externalMsgId);
    if(msgId!=null)
    {
      PrivateMessageDecoratedBean dbean=null;
      PrivateMessage msg = (PrivateMessage) prtMsgManager.getMessageById(new Long(msgId)) ;
      if(msg != null)
      {
        dbean.addPvtMessage(new PrivateMessageDecoratedBean(msg)) ;
        detailMsg = dbean;
      }
    }
    else
    {
      //TODO :  appropriate error page
      return "pvtMsg";
    }
    return "pvtMsgDetail";
  }
  
  //////////////////////REPLY SEND  /////////////////
  public String processPvtMsgReplySend() {
    LOG.debug("processPvtMsgReplySend()");
    
    PrivateMessage rMsg=getDetailMsg().getMsg() ;
    //add replyTo message
    PrivateMessage rrepMsg = messageManager.createPrivateMessage() ;
    //TODO settings from jsp    
    rrepMsg.setTitle(rMsg.getTitle()) ;
    rrepMsg.setDraft(Boolean.TRUE);
    rrepMsg.setAuthor(getUserId());
    rrepMsg.setApproved(Boolean.TRUE);
    rrepMsg.setBody(getReplyToBody()) ;
    
    rrepMsg.setInReplyTo(rMsg) ;
    
    prtMsgManager.sendPrivateMessage(rrepMsg, getRecipiants());    
    return "pvtMsg" ;
  }
 
  /**
   * process from Compose screen
   * @return - pvtMsg
   */
  public String processPvtMsgReplySaveDraft() {
    LOG.debug("processPvtMsgReplySaveDraft()");
    
    PrivateMessage drMsg=getDetailMsg().getMsg() ;
    drMsg.setDraft(Boolean.TRUE);
    PrivateMessage drrepMsg = messageManager.createPrivateMessage() ;
    drrepMsg.setTitle(drMsg.getTitle()) ;
    drrepMsg.setDraft(Boolean.TRUE);
    drrepMsg.setAuthor(getUserId());
    drrepMsg.setInReplyTo(drMsg) ;
    drrepMsg.setApproved(Boolean.TRUE);
    drrepMsg.setBody(getReplyToBody()) ;
    
    prtMsgManager.sendPrivateMessage(drMsg, getRecipiants());    
    return "pvtMsg" ;    
  }
  
  ////////////////////////////////////////////////////////////////  
  public String processPvtMsgEmptyDelete() {
    LOG.debug("processPvtMsgEmptyDelete()");
    
    List delSelLs=new ArrayList() ;
    //this.setDisplayPvtMsgs(getDisplayPvtMsgs());    
    for (Iterator iter = this.decoratedPvtMsgs.iterator(); iter.hasNext();)
    {
      PrivateMessageDecoratedBean element = (PrivateMessageDecoratedBean) iter.next();
      if(element.getIsSelected())
      {
        delSelLs.add(element);
      }      
    }
    this.setSelectedDeleteItems(delSelLs);
    if(delSelLs.size()<1)
    {
      return null;  //stay in the same page if nothing is selected for delete
    }else {
      return "pvtMsgDelete";
    }
  }
  
  public String processPvtMsgMultiDelete()
  { 
    LOG.debug("processPvtMsgMultiDelete()");
  
    for (Iterator iter = getSelectedDeleteItems().iterator(); iter.hasNext();)
    {
      //We don't need decorated at this point as we will be deleting PrivateMessage object
      PrivateMessage element = ((PrivateMessageDecoratedBean) iter.next()).getMsg();
      if (element != null) 
      {
        //TODO - remove getMessageById()- not required if we remove dummy data
        PrivateMessage msg = (PrivateMessage) prtMsgManager.getMessageById(element.getId()) ;
        if(msg != null)
        {
          prtMsgManager.deletePrivateMessage(element) ;
        }        
      }      
    }
    return "main" ;
  }

  
  public String processPvtMsgDispOtions() 
  {
    LOG.debug("processPvtMsgDispOptions()");
    
    return "pvtMsgOrganize" ;
  }
  
  

  //select all
  private boolean isSelectAllJobsSelected = false;  
  public boolean isSelectAllJobsSelected()
  {
    return isSelectAllJobsSelected;
  }
  public void setSelectAllJobsSelected(boolean isSelectAllJobsSelected)
  {
    this.isSelectAllJobsSelected = isSelectAllJobsSelected;
  }
  
  public String processSelectAllJobs()
  {
    List newLs=new ArrayList() ;
//    isSelectAllJobsSelected = !isSelectAllJobsSelected;
//    processRefreshJobs();
    for (Iterator iter = this.getDecoratedPvtMsgs().iterator(); iter.hasNext();)
    {
      PrivateMessageDecoratedBean element = (PrivateMessageDecoratedBean) iter.next();
      element.setIsSelected(true);
      newLs.add(element) ;
      //TODO
    }
    this.setDecoratedPvtMsgs(newLs) ;
    return "pvtMsg";
  }
  
  //////////////////////////////   ATTACHMENT PROCESSING        //////////////////////////
  private ArrayList attachments = new ArrayList();
  
  private String removeAttachId = null;
  private ArrayList prepareRemoveAttach = new ArrayList();
  private boolean attachCaneled = false;
  private ArrayList oldAttachments = new ArrayList();
  private List allAttachments = new ArrayList();

  
  public ArrayList getAttachments()
  {
    ToolSession session = SessionManager.getCurrentToolSession();
    if (session.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null &&
        session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) 
    {
      List refs = (List)session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
      Reference ref = (Reference)refs.get(0);
      
      for(int i=0; i<refs.size(); i++)
      {
        ref = (Reference) refs.get(i);
        Attachment thisAttach = prtMsgManager.createPvtMsgAttachment(
            ref.getId(), ref.getProperties().getProperty(ref.getProperties().getNamePropDisplayName()));
        
        //TODO - remove this as being set for test only  
        thisAttach.setPvtMsgAttachId(new Long(1));
        
        attachments.add(thisAttach);
        
      }
    }
    session.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
    session.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
    
    return attachments;
  }
  
  public List getAllAttachments()
  {
    ToolSession session = SessionManager.getCurrentToolSession();
    if (session.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null &&
        session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) 
    {
      List refs = (List)session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
      Reference ref = (Reference)refs.get(0);
      
      for(int i=0; i<refs.size(); i++)
      {
        ref = (Reference) refs.get(i);
        Attachment thisAttach = prtMsgManager.createPvtMsgAttachment(
            ref.getId(), ref.getProperties().getProperty(ref.getProperties().getNamePropDisplayName()));
        
        //TODO - remove this as being set for test only
        thisAttach.setPvtMsgAttachId(new Long(1));
        allAttachments.add(thisAttach);
      }
    }
    session.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
    session.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
    
    if( allAttachments == null || (allAttachments.size()<1))
    {
      allAttachments.addAll(this.getDetailMsg().getMsg().getAttachments()) ;
    }
    return allAttachments;
  }
  
  public void setAttachments(ArrayList attachments)
  {
    this.attachments = attachments;
  }
  
  public String getRemoveAttachId()
  {
    return removeAttachId;
  }

  public final void setRemoveAttachId(String removeAttachId)
  {
    this.removeAttachId = removeAttachId;
  }
  
  public ArrayList getPrepareRemoveAttach()
  {
    if((removeAttachId != null) && (!removeAttachId.equals("")))
    {
      prepareRemoveAttach.add(prtMsgManager.getPvtMsgAttachment(new Long(removeAttachId)));
    }
    
    return prepareRemoveAttach;
  }

  public final void setPrepareRemoveAttach(ArrayList prepareRemoveAttach)
  {
    this.prepareRemoveAttach = prepareRemoveAttach;
  }
  
  //Redirect to File picker
  public String processAddAttachmentRedirect()
  {
    LOG.debug("processAddAttachmentRedirect()");
    try
    {
      ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
      context.redirect("sakai.filepicker.helper/tool");
      return null;
    }
    catch(Exception e)
    {
      //logger.error(this + ".processAddAttachRedirect - " + e);
      //e.printStackTrace();
      return null;
    }
  }
  //Process remove attachment 
  public String processDeleteAttach()
  {
    LOG.debug("processDeleteAttach()");
    
    ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
    String attachId = null;
    
    Map paramMap = context.getRequestParameterMap();
    Iterator itr = paramMap.keySet().iterator();
    while(itr.hasNext())
    {
      Object key = itr.next();
      if( key instanceof String)
      {
        String name =  (String)key;
        int pos = name.lastIndexOf("pvtmsg_current_attach");
        
        if(pos>=0 && name.length()==pos+"pvtmsg_current_attach".length())
        {
          attachId = (String)paramMap.get(key);
          break;
        }
      }
    }
    
    removeAttachId = attachId;
    
    //separate screen
//    if((removeAttachId != null) && (!removeAttachId.equals("")))
//      return "removeAttachConfirm";
//    else
//      return null;
    List newLs= new ArrayList();
    for (Iterator iter = getAttachments().iterator(); iter.hasNext();)
    {
      Attachment element = (Attachment) iter.next();
      if(!((element.getPvtMsgAttachId().toString()).equals(attachId)))
      {
        newLs.add(element);
      }
    }
    this.setAttachments((ArrayList) newLs) ;
    
    return null ;
  }
  
  //process deleting confirm from separate screen
  public String processRemoveAttach()
  {
    LOG.debug("processRemoveAttach()");
    
    try
    {
      Attachment sa = prtMsgManager.getPvtMsgAttachment(new Long(removeAttachId));
      String id = sa.getAttachmentId();
      
      for(int i=0; i<attachments.size(); i++)
      {
        Attachment thisAttach = (Attachment)attachments.get(i);
        if(((Long)thisAttach.getPvtMsgAttachId()).toString().equals(removeAttachId))
        {
          attachments.remove(i);
          break;
        }
      }
      
      ContentResource cr = ContentHostingService.getResource(id);
      prtMsgManager.removePvtMsgAttachment(sa);
      if(id.toLowerCase().startsWith("/attachment"))
        ContentHostingService.removeResource(id);
    }
    catch(Exception e)
    {
//      logger.error(this + ".processRemoveAttach() - " + e);
//      e.printStackTrace();
    }
    
    removeAttachId = null;
    prepareRemoveAttach.clear();
    return "compose";
    
  }
  
  public String processRemoveAttachCancel()
  {
    LOG.debug("processRemoveAttachCancel()");
    
    removeAttachId = null;
    prepareRemoveAttach.clear();
    return "compose" ;
  }
  

  ////////////  SETTINGS        //////////////////////////////
  //Setting Getter and Setter
  public String getActivatePvtMsg()
  {
    return activatePvtMsg;
  }
  public void setActivatePvtMsg(String activatePvtMsg)
  {
    this.activatePvtMsg = activatePvtMsg;
  }
  public String getForwardPvtMsg()
  {
    return forwardPvtMsg;
  }
  public void setForwardPvtMsg(String forwardPvtMsg)
  {
    this.forwardPvtMsg = forwardPvtMsg;
  }
  public String getForwardPvtMsgEmail()
  {
    return forwardPvtMsgEmail;
  }
  public void setForwardPvtMsgEmail(String forwardPvtMsgEmail)
  {
    this.forwardPvtMsgEmail = forwardPvtMsgEmail;
  }
  public boolean getSuperUser()
  {
    superUser=SecurityService.isSuperUser();
    return superUser;
  }
  public void setSuperUser(boolean superUser)
  {
    this.superUser = superUser;
  }
  
  public String processPvtMsgOrganize()
  {
    LOG.debug("processPvtMsgOrganize()");
    return null ;
    //return "pvtMsgOrganize";
  }

  public String processPvtMsgStatistics()
  {
    LOG.debug("processPvtMsgStatistics()");
    
    return null ;
    //return "pvtMsgStatistics";
  }

  public String processPvtMsgSettings()
  {
    LOG.debug("processPvtMsgSettings()");
    
    return "pvtMsgSettings";
  }

  public String processPvtMsgSettingRevise() {
    LOG.debug("processPvtMsgSettingRevise()");
    
    String email= getForwardPvtMsgEmail();
    String act=getActivatePvtMsg() ;
    String frd=getForwardPvtMsg() ;
    //prtMsgManager.saveAreaSetting();
    return "main" ;
  }
  

  ///////////////////   FOLDER SETTINGS         ///////////////////////
  //TODO - may add total number of messages with this folder.. 
  //--getDecoratedForum() iteratae and when title eqauls selectedTopicTitle - then get total number of messages
  private String addFolder;
  private boolean ismutable;
  
  public String getAddFolder()
  {
    return addFolder ;    
  }
  public void setAddFolder(String addFolder)
  {
    this.addFolder=addFolder;
  }
  
  public boolean getIsmutable()
  {
    return prtMsgManager.isMutableTopicFolder(getSelectedTopicId());
  }
  
  //navigated from header pagecome from Header page 
  public String processPvtMsgFolderSettings() {
    LOG.debug("processPvtMsgFolderSettings()");
    String topicTitle= getExternalParameterByKey("pvtMsgTopicTitle");
    setSelectedTopicTitle(topicTitle) ;
    String topicId=getExternalParameterByKey("pvtMsgTopicId") ;
    setSelectedTopicId(topicId);
    
    return "pvtMsgFolderSettings" ;
  }

  public String processPvtMsgFolderSettingRevise() {
    LOG.debug("processPvtMsgFolderSettingRevise()");
    
    if(this.ismutable)
    {
      return null;
    }else 
    {
      return "pvtMsgFolderRevise" ;
    }    
  }
  
  public String processPvtMsgFolderSettingAdd() {
    LOG.debug("processPvtMsgFolderSettingAdd()");    
    return "pvtMsgFolderAdd" ;
  }
  public String processPvtMsgFolderSettingDelete() {
    LOG.debug("processPvtMsgFolderSettingDelete()");
    
    if(this.ismutable)
    {
      return null;
    }else {
      return "pvtMsgFolderDelete" ;
    }    
  }
  
  public String processPvtMsgFolderSettingCancel() 
  {
    LOG.debug("processPvtMsgFolderSettingCancel()");
    
    return "main" ;
  }
  
  //Create a folder within a forum
  public String processPvtMsgFldCreate() 
  {
    LOG.debug("processPvtMsgFldCreate()");
    
    String createFolder=getAddFolder() ;
    if(createFolder == null)
    {
      return null ;
    } else {
      prtMsgManager.createTopicFolderInForum(this.getDecoratedForum().getForum().getId().toString(), this.getUserId(), createFolder);
      return "main" ;
    }
  }
  
  //revise
  public String processPvtMsgFldRevise() 
  {
    LOG.debug("processPvtMsgFldRevise()");
    
    String newTopicTitle = this.getSelectedTopicTitle();    
    prtMsgManager.renameTopicFolder(getSelectedTopicId(), getUserId(), newTopicTitle);
    
    return "main" ;
  }
  
  //Delete
  public String processPvtMsgFldDelete() 
  {
    LOG.debug("processPvtMsgFldDelete()");
    
    prtMsgManager.deleteTopicFolder(getSelectedTopicId()) ;
    
    return "main";
  }
  public String processPvtMsgFldAddCancel() 
  {
    LOG.debug("processPvtMsgFldAddCancel()");
    
    return "main";
  }
  
  ///////////////   SEARCH      ///////////////////////
  private List searchPvtMsgs;
  public List getSearchPvtMsgs()
  {
    return searchPvtMsgs;
  }
  public void setSearchPvtMsgs(List searchPvtMsgs)
  {
    this.searchPvtMsgs=searchPvtMsgs ;
  }
  public String processSearch() 
  {
    LOG.debug("processSearch()");
    
    List newls = new ArrayList() ;
    for (Iterator iter = getDecoratedPvtMsgs().iterator(); iter.hasNext();)
    {
      PrivateMessageDecoratedBean element = (PrivateMessageDecoratedBean) iter.next();
      
      String message=element.getMsg().getTitle();
      StringTokenizer st = new StringTokenizer(message);
      while (st.hasMoreTokens())
      {
        if(st.nextToken().equalsIgnoreCase(getSearchText()))
        {
          newls.add(element) ;
          break;
        }
      }
    }
      this.setSearchPvtMsgs(newls) ;

    return "pvtMsgEx" ;
  }
  
 
  //////////////        HELPER      //////////////////////////////////
  /**
   * @return
   */
  private String getExternalParameterByKey(String parameterId)
  {
    String parameterValue = null;
    ExternalContext context = FacesContext.getCurrentInstance()
        .getExternalContext();
    Map paramMap = context.getRequestParameterMap();
    Iterator itr = paramMap.keySet().iterator();
    while (itr.hasNext())
    {
      String key = (String) itr.next();
      if (key != null && key.equals(parameterId))
      {
        parameterValue = (String) paramMap.get(key);
        break;
      }
    }
    return parameterValue;
  }

  
  private List getRecipiants()
  {
    List retLs = new Vector() ;
    List selLs = getSelectedComposeToList();
    for (int i = 0; i < selLs.size(); i++)
    {
      if (selLs.get(i).equals(RECIPIANTS_ENTIRE_CLASS))
      {
        for (Iterator iterator = getAllParticipants().iterator(); iterator.hasNext();)
        {
          Participant p = (Participant) iterator.next();
          retLs.add(p.uniqname); // uniqname is the userId
        }
        break;
      }
      // instructors
      if (selLs.get(i).equals(RECIPIANTS_ALL_INSTRUCTORS))
      {
        for (Iterator iterator = getAllInstructorParticipants().iterator(); iterator.hasNext();)
        {
          Participant p = (Participant) iterator.next();
          retLs.add(p.uniqname); // uniqname is the userId
        }
        break;
      }
      else 
      {
        retLs.add(selLs.get(i));
      }
    }
    return retLs ;
  }
  /*
   * return all participants
   */
  private List getAllParticipants()
  {
    List members = new Vector();
    
    List participants = new Vector();    
    String realmId = SiteService.siteReference(PortalService.getCurrentSiteId());//SiteService.siteReference((String) state.getAttribute(STATE_SITE_INSTANCE_ID));
 
    try
    {
      AuthzGroup realm = AuthzGroupService.getAuthzGroup(realmId);
      Set grants = realm.getMembers();
      //Collections.sort(users);
      for (Iterator i = grants.iterator(); i.hasNext();)
      {
        Member g = (Member) i.next();
        String userString = g.getUserId();
        Role r = g.getRole();
        
        boolean alreadyInList = false;
        for (Iterator p = members.iterator(); p.hasNext() && !alreadyInList;)
        {
          CourseMember member = (CourseMember) p.next();
          String memberUniqname = member.getUniqname();
          if (userString.equalsIgnoreCase(memberUniqname))
          {
              alreadyInList = true;
              if (r != null)
              {
                  member.setRole(r.getId());
              }
              participants.add(member);
          }
        }
        if (!alreadyInList)
        {
          try
          {
            User user = UserDirectoryService.getUser(userString);
            Participant participant = new Participant();
            participant.name = user.getSortName();
            participant.uniqname = userString;
            if (r != null)
            {
                participant.role = r.getId();
            }
            //Don't add admin/admin 
            if(!(participant.uniqname).equals("admin"))
            {
              participants.add(participant);
            }                
          }
          catch (IdUnusedException e)
          {
            // deal with missing user quietly without throwing a warning message
          }
        }
      }
    }
    catch (IdUnusedException e)
    {
      //Log.warn("chef", this + "  IdUnusedException " + realmId);
    } 
    return participants ;
  }  

  
  /*
   * return all participants
   */
  private List getAllInstructorParticipants()
  {
    List members = new Vector();
    
    List participants = new Vector();    
    String realmId = SiteService.siteReference(PortalService.getCurrentSiteId());//SiteService.siteReference((String) state.getAttribute(STATE_SITE_INSTANCE_ID));
 
    try
    {
      AuthzGroup realm = AuthzGroupService.getAuthzGroup(realmId);
      Set grants = realm.getMembers();
      //Collections.sort(users);
      for (Iterator i = grants.iterator(); i.hasNext();)
      {
        Member g = (Member) i.next();
        String userString = g.getUserId();
        Role r = g.getRole();
        
        boolean alreadyInList = false;
        for (Iterator p = members.iterator(); p.hasNext() && !alreadyInList;)
        {
          CourseMember member = (CourseMember) p.next();
          String memberUniqname = member.getUniqname();
          if (userString.equalsIgnoreCase(memberUniqname))
          {
              alreadyInList = true;
              if (r != null)
              {
                  member.setRole(r.getId());
              }
              participants.add(member);
          }
        }
        if (!alreadyInList)
        {
          try
          {
            User user = UserDirectoryService.getUser(userString);
            Participant participant = new Participant();
            participant.name = user.getSortName();
            participant.uniqname = userString;
            if (r != null)
            {
                participant.role = r.getId();
            }
            //Don't add admin/admin 
            if(!(participant.uniqname).equals("admin") && prtMsgManager.isInstructor(participant.uniqname))
            {
              participants.add(participant);
            }                
          }
          catch (IdUnusedException e)
          {
            // deal with missing user quietly without throwing a warning message
          }
        }
      }
    }
    catch (IdUnusedException e)
    {
      //Log.warn("chef", this + "  IdUnusedException " + realmId);
    } 
    return participants ;
  }
  
  /**
   * Participant in site access roles
   *
   */
 public class Participant
 {
   public String name = "";
   public String uniqname = "";
   public String role = ""; 
       
   public String getName() {return name; }
   public String getUniqname() {return uniqname; }
   public String getRole() { return role; } // cast to Role
   public boolean isRemoveable(){return true;}
   
 } // Participant
 
 
  //////// GETTER AND SETTER  ///////////////////  
  public String processUpload(ValueChangeEvent event)
  {
    return "pvtMsg" ; 
  }
  
  public String processUploadConfirm()
  {
    return "pvtMsg";
  }
  
  public String processUploadCancel()
  {
    return "pvtMsg" ;
  }


  public void setForumManager(MessageForumsForumManager forumManager)
  {
    this.forumManager = forumManager;
  } 

}