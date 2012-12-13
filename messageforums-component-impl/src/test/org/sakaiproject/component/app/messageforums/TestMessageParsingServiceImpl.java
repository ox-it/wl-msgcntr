package org.sakaiproject.component.app.messageforums;

import static org.junit.Assert.*;
import junit.framework.TestCase;

import org.junit.Test;
import org.sakaiproject.api.app.messageforums.MessageParsingService;

public class TestMessageParsingServiceImpl extends TestCase {

	private String message1="The quick brown fox jumped over the lazy dog\nwww.bbc.co.uk";
	private String result1="The quick brown fox jumped over the lazy dog\nwww.bbc.co.uk";
	private String storage1="The quick brown fox jumped over the lazy dog<br /><a href='http://www.bbc.co.uk'>www.bbc.co.uk</a>";
	
	private String message2="The quick brown fox jumped over the lazy dog<br />www.bbc.co.uk";
	private String result2="The quick brown fox jumped over the lazy dog<br />www.bbc.co.uk";
	private String storage2="The quick brown fox jumped over the lazy dog&lt;br /&gt;<a href='http://www.bbc.co.uk'>www.bbc.co.uk</a>";
	
	private String message3="just sending http://news.bbc.co.uk and expecting WebLearn to mark it\nup?";
	private String result3="just sending http://news.bbc.co.uk and expecting WebLearn to mark it\nup?";
	private String storage3="just sending <a href='http://news.bbc.co.uk'>http://news.bbc.co.uk</a> and expecting WebLearn to mark it<br />up?";
				
	private String message4="<b>Fred bloggs</b> visits the bbc website: http://bbc.co.uk.\n\n<i>Alice</i> on the other hand doesnt.\n\"";
	private String result4="<b>Fred bloggs</b> visits the bbc website: http://bbc.co.uk.\n\n<i>Alice</i> on the other hand doesnt.\n\"";
	private String storage4="&lt;b&gt;Fred bloggs&lt;/b&gt; visits the bbc website: <a href='http://bbc.co.uk'>http://bbc.co.uk</a>.<br /><br />&lt;i&gt;Alice&lt;/i&gt; on the other hand doesnt.<br />\"";
			
	public TestMessageParsingServiceImpl(String name) {
		super(name);
	}
	
	private MessageParsingService messageParsingService = new MessageParsingServiceImpl();
	
	@Test
	public void test1() {
		assertEquals(storage1, 
				messageParsingService.parse(message1));
		assertEquals(result1, 
				messageParsingService.format(messageParsingService.parse(message1)));
	}
	
	@Test
	public void test2() {
		assertEquals(storage2, 
				messageParsingService.parse(message2));
		assertEquals(result2, 
				messageParsingService.format(messageParsingService.parse(message2)));
	}
	
	@Test
	public void test3() {
		assertEquals(storage3, 
				messageParsingService.parse(message3));
		assertEquals(result3, 
				messageParsingService.format(messageParsingService.parse(message3)));
	}
	
	@Test
	public void test4() {
		assertEquals(storage4, 
				messageParsingService.parse(message4));
		assertEquals(result4, 
				messageParsingService.format(messageParsingService.parse(message4)));
	}
	
	@Test
	public void testURLParsing() {
		// These are tests where the whole string should make it through without any changes.
		testURL("http://www.google.com");
		testURL("http://www.google.com/");
		testURL("http://www.facebook.com/login?go=true");
		
		// Brackets, escaped and not.
		testURL("http://en.wikipedia.org/wiki/PC_Tools_(Central_Point_Software)");
		testURL("http://msdn.microsoft.com/en-us/library/aa752574(VS.85).aspx");
		testURL("http://en.wikipedia.org/wiki/PC_Tools_%28Central_Point_Software%29");
		testURL("http://msdn.microsoft.com/en-us/library/aa752574%28VS.85%29.aspx");
		
		// Test post

	}
	
	@Test
	public void testNonURL() {
		assertEquals("2.99", messageParsingService.parse("2.99"));
		assertEquals("112,122,000", messageParsingService.parse("112,122,000"));
		assertEquals("End of sentance.", messageParsingService.parse("End of sentance."));
	}
	
	@Test
	public void testMissingProtocol() {
		assertEquals("<a href='http://news.bbc.co.uk'>news.bbc.co.uk</a>", messageParsingService.parse("news.bbc.co.uk"));
	}
	
	@Test
	public void testBadSentance() {
		assertEquals("No space after stop.My other", messageParsingService.parse("No space after stop.My other"));
	}
	
	@Test
	public void testPort() {
		testURL("http://www.someserver.com:8080/more/");
		testURL("http://www.someserver.com:8080");
	}
	
	@Test
	public void testEmail() {
		testUnchanged("matthew.buckett@it.ox.ac.uk ");
	}
	public void testURL(String url) {
		assertEquals("<a href='"+ url+ "'>"+ url+ "</a>", messageParsingService.parse(url));
	}
	
	public void testUnchanged(String test) {
		assertEquals(test, messageParsingService.parse(test));
	}
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(TestMessageParsingServiceImpl.class);
	}

}
