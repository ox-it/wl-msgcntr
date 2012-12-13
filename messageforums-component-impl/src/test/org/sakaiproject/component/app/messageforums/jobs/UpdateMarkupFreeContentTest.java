package org.sakaiproject.component.app.messageforums.jobs;

import static org.junit.Assert.*;

import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.api.app.messageforums.MessageParsingService;
import org.sakaiproject.component.app.messageforums.MessageParsingServiceImpl;

public class UpdateMarkupFreeContentTest {

	private UpdateMarkupFreeContent updateContent;

	@Before
	public void setUp() throws Exception {
		MessageParsingService messageParsingService = new MessageParsingServiceImpl();
		updateContent = new UpdateMarkupFreeContent();
		updateContent.setMessageParsingService(messageParsingService);
	}

	@Test
	public void testSimple() {
		assertEquals("",updateContent.updateBody(""));
		assertEquals("hello", updateContent.updateBody("hello"));
		assertEquals("hello", updateContent.updateBody("<b>hello</b>"));
	}
	
	@Test
	public void testComplex() {
		assertEquals("line 1<br />line 2", updateContent.updateBody("line 1<br />line 2"));
		assertEquals("line 1<br />line 2", updateContent.updateBody("line 1<br/>line 2"));
		assertEquals("line 1<br />line 2", updateContent.updateBody("line 1<br>line 2"));
		assertEquals("line 1<br />line 2", updateContent.updateBody("line 1<BR>line 2"));
		assertEquals("line 1<br />line 2", updateContent.updateBody("line 1<br clear='all'>line 2"));
	}
	
	@Test
	public void testHTMLEntities() {
		assertEquals("\"", updateContent.updateBody("\""));
		assertEquals("&amp;", updateContent.updateBody("&amp;"));
		assertEquals("&lt;&gt;", updateContent.updateBody("&lt;&gt;"));
	}
	
	@Test 
	public void testLinks() {
		assertEquals("<a href='http://news.bbc.co.uk'>http://news.bbc.co.uk</a>", updateContent.updateBody("<a href='http://news.bbc.co.uk'>http://news.bbc.co.uk</a>"));
		assertEquals("<a href='http://www.google.com/news/'>http://www.google.com/news/</a>", updateContent.updateBody("http://www.google.com/news/"));
	}
	
	@Test
	public void testNewlines() {
		assertEquals("Hello All,<br />I would like to start", updateContent.updateBody("Hello All,\n<div><br/>\nI would like to start"));
	}
	
	@Test
	public void testMoreContent() {
		String original = loadResource("post-original.txt");
		String filtered = loadResource("post-filtered.txt");
		assertEquals(filtered, updateContent.updateBody(original));
	}
	
	@Test
	public void testPostWithBr() {
		String original = loadResource("post-with-br-original.txt");
		String filtered = loadResource("post-with-br-filtered.txt");
		assertEquals(filtered, updateContent.updateBody(original));
	}

	private String loadResource(String resource) {
		return new Scanner(getClass().getResourceAsStream(resource)).useDelimiter("\\A").next();
	}

}
