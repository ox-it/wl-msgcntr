package org.sakaiproject.component.app.messageforums;

import static org.junit.Assert.*;
import junit.framework.TestCase;

import org.junit.Test;
import org.sakaiproject.api.app.messageforums.MessageParsingService;

public class TestMessageParsingServiceImpl extends TestCase {

	private String message1="The quick brown fox jumped over the lazy dog\nwww.bbc.co.uk";
	private String result1="The quick brown fox jumped over the lazy dog\nwww.bbc.co.uk";
	
	private String message2="The quick brown fox jumped over the lazy dog<br />www.bbc.co.uk";
	private String result2="The quick brown fox jumped over the lazy dog\nwww.bbc.co.uk";
	
	private String message3="The quick brown fox jumped over the lazy dog\n<a href='www.bbc.co.uk'>www.bbc.co.uk</a>";
	private String result3="The quick brown fox jumped over the lazy dog\nwww.bbc.co.uk";
	
	private String storage="The quick brown fox jumped over the lazy dog<br /><a href='www.bbc.co.uk'>www.bbc.co.uk</a>";
	
	public TestMessageParsingServiceImpl(String name) {
		super(name);
	}
	
	private MessageParsingService messageParsingService = new MessageParsingServiceImpl();
	
	@Test
	public void test1() {
		assertEquals(storage, 
				messageParsingService.parse(message1));
		assertEquals(result1, 
				messageParsingService.format(messageParsingService.parse(message1)));
	}
	
	@Test
	public void test2() {
		assertEquals(storage, 
				messageParsingService.parse(message2));
		assertEquals(result2, 
				messageParsingService.format(messageParsingService.parse(message2)));
	}
	
	@Test
	public void test3() {
		assertEquals(storage, 
				messageParsingService.parse(message3));
		assertEquals(result3, 
				messageParsingService.format(messageParsingService.parse(message3)));
	}
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(TestMessageParsingServiceImpl.class);
	}

}
