

    <h:panelGroup styleClass="specialLink">
    	<h:outputText value="#{msgs.df_view}" />
    	<h:selectOneMenu id="select_label" onchange="this.form.submit();"  valueChangeListener="#{ForumTool.processValueChangedForMessageOrganize}" value="#{ForumTool.selectedMessageView}">
			<f:selectItem itemValue="thread" itemLabel="#{msgs.msg_organize_thread}" />
   			<f:selectItem itemValue="date" itemLabel="#{msgs.msg_organize_date_asc}" />
   			<f:selectItem itemValue="date_desc" itemLabel="#{msgs.msg_organize_date_desc}" />
			<f:selectItem itemValue="unread" itemLabel="#{msgs.msg_organize_unread}" />
	  </h:selectOneMenu>
	</h:panelGroup>
  

<h:messages styleClass="alertMessage" id="errorMessages"  />
