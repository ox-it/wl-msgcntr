<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />

<f:view>
  <sakai:view_container title="#{msgs.cdfm_container_title}">
    <sakai:view_content>
      <h:form id="pvtMsgDetail">
      	
      	<div class="left-header-section">
					<h:commandLink action="#{PrivateMessagesTool.processActionHome}" value="Message Forums" /> / 
					<h:commandLink action="#{PrivateMessagesTool.processDisplayForum}" value="Private Messages" >
					</h:commandLink> /
					<h:outputText value="#{PrivateMessagesTool.msgNavMode}" />
			 	<sakai:instruction_message value="Private Message Details" />
				</div>
				<div class="right-header-section">
					<h:outputText value="Previous Folder" />
					<h:outputText value="Next Folder" />	
					<sakai:instruction_message />
				</div>
      	
      	
      	
      	<br>
      	
      	<div class="base-div">
        
          <table width="100%" align="left" style="background-color:#DDDFE4;">
            <tr>
              <td align="left">
                <h:outputText style="font-weight:bold"  value="Subject "/>
              </td>
              <td align="left">
              	<h:outputText value="#{PrivateMessagesTool.detailMsg.message.title}" />  
              </td>  
              <td align="left"><h:outputText style="font-weight:bold"  value="Previous Message" />
		<h:outputText style="font-weight:bold"  value="Next Message" />	
	      </td>
        
            </tr>
            <tr>
              <td align="left">
                <h:outputText style="font-weight:bold"  value="Authored By "/>
              </td>
              <td align="left">
              	<h:outputText value="#{PrivateMessagesTool.detailMsg.message.createdBy}" />  
              	<h:outputText value="-" />  
              	<h:outputText value="#{PrivateMessagesTool.detailMsg.message.created}" />  
              </td>
              <td></td>
            </tr>
            <tr>
              <td align="left">
                <h:outputText style="font-weight:bold"  value="Attachments "/>
              </td>
              <td align="left">
              </td>
              <td></td>
            </tr>
            <tr>
              <td align="left">
                <h:outputText style="font-weight:bold"  value="Label "/>
              </td>
              <td align="left">
              	<h:outputText value="#{PrivateMessagesTool.detailMsg.message.label}" />  
              </td>
              <td></td>
            </tr>                                    
          </table>    
        
        <br/><br/>
        
        

				<h:panelGroup rendered="#{PrivateMessagesTool.deleteConfirm}">
					<h:outputText style="background-color:#FFF8DF;border:1px solid #B8B88A;color:#663300;font-size:x-small;margin:5px 0px 5px 0px;padding:5px 5px 5px 25px;" 
					value="! Are you sure you want to delete this message? If yes, click Delete to delete the message." />
				</h:panelGroup>        
        
        <sakai:button_bar rendered="#{!PrivateMessagesTool.deleteConfirm}" >
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgReply}" value="Reply to Message" />
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgMove}" value="Move" />
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgDeleteConfirm}" value="Delete" />
        </sakai:button_bar>
        
        <sakai:button_bar rendered="#{PrivateMessagesTool.deleteConfirm}" >  
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgDeleteConfirmYes}" value="Delete" />
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgCancel}" value="Cancel" />
        </sakai:button_bar>

        <sakai:group_box>
          <sakai:panel_edit>
            <sakai:doc_section>            
              <h:inputTextarea value="#{PrivateMessagesTool.detailMsg.message.body}" style="width: 100%; align:left; height: 10%;" />
            </sakai:doc_section>    
          </sakai:panel_edit>
        </sakai:group_box>
         </div>               

      </h:form>
     
    </sakai:view_content>
  </sakai:view_container>
</f:view> 

