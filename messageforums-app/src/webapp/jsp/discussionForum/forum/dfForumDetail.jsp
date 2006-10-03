<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<f:view>
  <sakai:view>

    <h:form id="msgForum">
<!--jsp/discussionForum/forum/dfForumDetail.jsp-->
      <h:panelGrid columns="2" summary="layout" width="100%" styleClass="navPanel  specialLink">
        <h:panelGroup>
          	<f:verbatim><div class="breadCrumb"><h3></f:verbatim>
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"/>
			      <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			      <h:outputText value="#{ForumTool.selectedForum.forum.title}" />
			    <f:verbatim></h3></div></f:verbatim>
        </h:panelGroup>
        <h:panelGroup styleClass="itemNav">
        		<h:commandLink action="#{ForumTool.processActionNewTopic}"  value="#{msgs.cdfm_new_topic}" rendered="#{ForumTool.selectedForum.newTopic}" 
        		               title=" #{msgs.cdfm_new_topic}">
					  <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
				  </h:commandLink>
				  <f:verbatim><h:outputText value=" | " rendered="#{ForumTool.selectedForum.changeSettings}"/></f:verbatim>
				  <h:commandLink action="#{ForumTool.processActionForumSettings}" value="#{msgs.cdfm_forum_settings}" rendered="#{ForumTool.selectedForum.changeSettings}"
				                 title=" #{msgs.cdfm_forum_settings}">
					  <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
				  </h:commandLink>
        </h:panelGroup>
      </h:panelGrid>
		  <p class="textPanel">
		  <h:outputText value="#{ForumTool.selectedForum.forum.shortDescription}" />
		  </p>
		  <p class="textPanelFooter specialLink">
			<h:commandLink immediate="true" action="#{ForumTool.processActionToggleDisplayForumExtendedDescription}" rendered="#{ForumTool.selectedForum.hasExtendedDesciption}"
				 	id="forum_extended_show" value="#{msgs.cdfm_read_full_description}" title="#{msgs.cdfm_read_full_description}">
				<f:param value="#{forum.forum.id}" name="forumId"/>
			  <f:param value="processActionDisplayForum" name="redirectToProcessAction"/>
			</h:commandLink>
			<h:commandLink immediate="true" action="#{ForumTool.processActionToggleDisplayForumExtendedDescription}" id="forum_extended_hide"
						 value="#{msgs.cdfm_hide_full_description}" rendered="#{ForumTool.selectedForum.readFullDesciption}" title="#{msgs.cdfm_hide_full_description}">
				<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
				<f:param value="processActionDisplayForum" name="redirectToProcessAction"/>
			</h:commandLink>
			</p>
				<%-- gsilver: show the text, not the editor - don't know how in this case--%>

				<mf:htmlShowArea value="#{ForumTool.selectedForum.forum.extendedDescription}" 
		                     rendered="#{ForumTool.selectedForum.readFullDesciption}" 
		                     hideBorder="false" />

		<h:dataTable id="topics" styleClass="topicBloc" value="#{ForumTool.selectedForum.topics}" var="topic" width="100%"  cellspacing="0" cellpadding="0">
			<h:column>
			<f:verbatim><div class="hierItemBlockChild"></f:verbatim>	
        <h:panelGrid columns="2" summary="layout" width="100%"  styleClass="topicHeadings specialLink" columnClasses="bogus,itemAction" cellspacing="0" cellpadding="0">
          <h:panelGroup>
			 	<f:verbatim><h4></f:verbatim>
				    <h:commandLink action="#{ForumTool.processActionDisplayTopic}" id="topic_title" value="#{topic.topic.title}" title=" #{topic.topic.title}">
						  <f:param value="#{topic.topic.id}" name="topicId"/>
						  <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					  </h:commandLink>
					 <f:verbatim></h4></f:verbatim> 
						<h:outputText styleClass="textPanelFooter" id="topic_msg_count" value=" #{msgs.cdfm_openb} #{topic.totalNoMessages} #{msgs.cdfm_lowercase_msg} - #{topic.unreadNoMessages} #{msgs.cdfm_unread} #{msgs.cdfm_closeb}" rendered="#{topic.isRead && topic.totalNoMessages < 2}"/>
						<h:outputText styleClass="textPanelFooter" id="topic_msgs_count" value=" #{msgs.cdfm_openb} #{topic.totalNoMessages} #{msgs.cdfm_lowercase_msgs} - #{topic.unreadNoMessages} #{msgs.cdfm_unread} #{msgs.cdfm_closeb}" rendered="#{topic.isRead && topic.totalNoMessages > 1}"/>
				  </h:panelGroup>
				  <h:panelGroup styleClass="msgNav">
						<h:commandLink action="#{ForumTool.processActionTopicSettings}" id="topic_setting" value="#{msgs.cdfm_topic_settings}"
						rendered="#{topic.changeSettings}" title=" #{msgs.cdfm_topic_settings}">
							<f:param value="#{topic.topic.id}" name="topicId"/>
							<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
						</h:commandLink>
				  </h:panelGroup>
			  </h:panelGrid>
			<f:verbatim><div class="textPanel"></f:verbatim>  
			<h:panelGroup><h:outputText id="topic_desc" value="#{topic.topic.shortDescription}" /></h:panelGroup>
			<f:verbatim></div></f:verbatim>
			<%-- gsilver:need a rendered attribute on this next block - or is this just a forgotten bit of cruft? a placeholder to display responses in context?--%>
			<h:dataTable id="messages" value="#{topics.messages}" var="message">
			<h:column>
				<h:outputText id="message_title" value="#{message.message.title}"/>
				<f:verbatim><br /></f:verbatim>
				<h:outputText id="message_desc" value="#{message.message.shortDescription}" />
			</h:column>
		</h:dataTable>
				<f:verbatim></div></f:verbatim>
				</h:column>
		</h:dataTable>
	 </h:form>
    </sakai:view>
</f:view>

