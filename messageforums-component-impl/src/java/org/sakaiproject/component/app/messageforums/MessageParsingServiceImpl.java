package org.sakaiproject.component.app.messageforums;

import org.sakaiproject.api.app.messageforums.MessageParsingService;

public class MessageParsingServiceImpl implements MessageParsingService {

	public String parse(String message) {
		System.out.println("MessageParsingServiceImpl.parse");
		return message;
	}

}
