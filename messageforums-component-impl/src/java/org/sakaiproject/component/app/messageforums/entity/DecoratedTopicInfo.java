package org.sakaiproject.component.app.messageforums.entity;

public class DecoratedTopicInfo {
	
	private String topicTitle;
	private Long topicId;
	private int unreadMessagesCount = 0;
	private int messagesCount = 0;
	private String typeUuid;
		
	private Long forumId;
	private String topicShortDescription;
	private String topicExtendedDescription;
		
	public DecoratedTopicInfo() {
	}
		
	public String getTypeUuid() {
		return typeUuid;
	}

	public void setTypeUuid(String typeUuid) {
		this.typeUuid = typeUuid;
	}

	public DecoratedTopicInfo(
			Long topicId, String topicTitle, int unreadMessagesCount, int messagesCount, 
			String typeUuid, Long forumId, String topicShortDescription, 
			String topicExtendedDescription) {
		
		this.topicId = topicId;
		this.topicTitle = topicTitle;
		this.unreadMessagesCount = unreadMessagesCount;
		this.messagesCount = messagesCount;
		this.typeUuid = typeUuid;
			
		this.forumId = forumId;
		this.topicShortDescription = topicShortDescription;
		this.topicExtendedDescription = topicExtendedDescription;
	}
		
	public Long getTopicId() {
		return topicId;
	}
	public void setTopicId(Long topicId) {
		this.topicId = topicId;
	}
	public String getTopicTitle() {
		return topicTitle;
	}
	public void setTopicTitle(String topicTitle) {
		this.topicTitle = topicTitle;
	}
		
	public int getUnreadMessagesCount() {
		return unreadMessagesCount;
	}
	public void setUnreadMessagesCount(int unreadMessagesCount) {
		this.unreadMessagesCount = unreadMessagesCount;
	}

	public int getMessagesCount() {
		return messagesCount;
	}

	public void setMessagesCount(int messagesCount) {
		this.messagesCount = messagesCount;
	}
		
	public Long getForumId() {
		return forumId;
	}
	public void setForumId(Long forumId) {
		this.forumId = forumId;
	}
		
	public String getTopicShortDescription() {
		return topicShortDescription;
	}
	public void setTopicShortDescription(String topicShortDescription) {
		this.topicShortDescription = topicShortDescription;
	}
		
	public String getTopicExtendedDescription() {
		return topicExtendedDescription;
	}
	public void setTopicDescription(String topicExtendedDescription) {
		this.topicExtendedDescription = topicExtendedDescription;
	}	
}
