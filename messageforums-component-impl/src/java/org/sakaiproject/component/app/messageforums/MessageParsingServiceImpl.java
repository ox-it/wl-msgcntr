package org.sakaiproject.component.app.messageforums;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sakaiproject.api.app.messageforums.MessageParsingService;
import org.sakaiproject.util.FormattedText;

public class MessageParsingServiceImpl implements MessageParsingService {

	private Pattern brCleanup = Pattern.compile("<\\s*br\\s*[^<>]?>", Pattern.CASE_INSENSITIVE);
	// Key is to look for a domain name which ends in 2 or 3 characters.
	private Pattern findURLs = Pattern.compile("\\(?((?:https?|ftps?)://)?(?:[\\w\\-]+\\.)+([a-z]{2,4})(:\\d+)?(/[#&\\-=?\\+\\%/\\.\\w()]*)?");
	
	public String parse(String message) {
		String withMarkup = message.replaceAll("\\r", "");
		
		// We don't replace &quot; as it doesn't get decoded.
		withMarkup = withMarkup.replaceAll("&", "&amp;");
		withMarkup = withMarkup.replaceAll("<", "&lt;");
		withMarkup = withMarkup.replaceAll(">", "&gt;");
		
		
		withMarkup = withMarkup.replaceAll("\\n", "<br />");
		//withMarkup = withMarkup.replaceAll( "</?a[^>]*>", "" );
		withMarkup = markupURLs(withMarkup);
		return withMarkup;
	}
	
	public String markupURLs(String message) {
		Matcher urls = findURLs.matcher(message);
		StringBuffer out = new StringBuffer();
		while (urls.find()) {
			String url = urls.group();
			// Find the right part of the URL
			if (url.startsWith("(")) {
				url = url.substring(1, url.length() - (url.endsWith(")")?2:1)); 
			}
			// Keep the body of the tag.
			String text=url;
			// If there isn't a protocol, assume http://
			if(urls.group(1) == null) {
				url = "http://"+url;
			}
			urls.appendReplacement(out, "<a href='"+ url+ "'>"+text+"</a>");
		}
		urls.appendTail(out);
		return out.toString();
	}
	
	public String format(String message) {
		String noMarkup = message.replaceAll("</p>", "<br /><br />");
		// convertFormattedText doesn't deal with <br> only <br >, using a pattern as it's case insensitive (<BR>)
		noMarkup = brCleanup.matcher(noMarkup).replaceAll("<br />");
		
		noMarkup = FormattedText.convertFormattedTextToPlaintext(noMarkup);
		return noMarkup;
	}

}
