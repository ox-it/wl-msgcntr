package org.sakaiproject.component.app.messageforums.entity;

import java.util.List;

public class DecoratedMessage {
	
	private Long messageId;
	private Long topicId;
	private String title;
	private String body;
	private String lastModified;
	private List<String> attachments;
	private List<DecoratedMessage> replies;
	private String authoredBy;
	private int indentIndex = 0;
	private Long replyTo;
	private String createdOn;
	private boolean read;
	private String recipients;
	private String label;
			 
	public DecoratedMessage() {
	}
			 
	public DecoratedMessage(Long messageId, Long topicId, String title, 
			String body, String lastModified, 
			List<String> attachments, List<DecoratedMessage> replies, 
			String authoredBy, Long replyTo, String createdOn, boolean read, 
			String recipients, String label) {
		
		this.messageId = messageId;
		this.topicId = topicId;
		this.title = title;
		this.body = body;
		this.attachments = attachments;
		this.replies = replies;
		this.lastModified = lastModified;
		this.authoredBy = authoredBy;
		this.replyTo = replyTo;
		this.createdOn = createdOn;
		this.read = read;
		this.recipients = recipients;
		this.label = label;
	}
	
	public Long getMessageId() {
		return messageId;
	}

	public void setMessageId(Long messageId) {
		this.messageId = messageId;
	}

	public Long getTopicId() {
		return topicId;
	}

	public void setTopicId(Long topicId) {
		this.topicId = topicId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public List<String> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<String> attachments) {
		this.attachments = attachments;
	}

	public List<DecoratedMessage> getReplies() {
		return replies;
	}
	public void setReplies(List<DecoratedMessage> replies) {
		this.replies = replies;
	}
	public String getLastModified() {
		return lastModified;
	}
	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}
	public String getAuthoredBy() {
		return authoredBy;
	}
	public void setAuthoredBy(String authoredBy) {
		this.authoredBy = authoredBy;
	}
	public int getIndentIndex() {
		return indentIndex;
	}
	public void setIndentIndex(int indentIndex) {
		this.indentIndex = indentIndex;
	}
	public Long getReplyTo() {
		return replyTo;
	}
	public void setReplyTo(Long replyTo) {
		this.replyTo = replyTo;
	}
	public String getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}
	public boolean isRead() {
		return read;
	}
	public void setRead(boolean read) {
		this.read = read;
	}
	public String getRecipients() {
		return recipients;
	}
	public void setRecipients(String recipients) {
		this.recipients = recipients;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
}
