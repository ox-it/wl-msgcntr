package org.sakaiproject.component.app.messageforums;

import org.sakaiproject.api.app.messageforums.MessageParsingService;
import org.sakaiproject.util.FormattedText;

public class MessageParsingServiceImpl implements MessageParsingService {

	public String parse(String message) {
		String withMarkup = message.replaceAll("\\r", "");
		
		withMarkup = withMarkup.replaceAll("&", "&amp;");
		withMarkup = withMarkup.replaceAll("<", "&lt;");
		withMarkup = withMarkup.replaceAll(">", "&gt;");
		withMarkup = withMarkup.replaceAll("\"", "&quot;");
		
		withMarkup = withMarkup.replaceAll("\\n", "<br />");
		//withMarkup = withMarkup.replaceAll( "</?a[^>]*>", "" );
		withMarkup = withMarkup.replaceAll("(@)?((https?|ftps?)://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?", "<a href='$0'>$0</a>");
		return withMarkup;
	}
	
	public String format(String message) {
		String noMarkup = message.replaceAll("</p>", "<br /><br />");
		noMarkup = FormattedText.convertFormattedTextToPlaintext(noMarkup);
		return noMarkup;
	}

}
