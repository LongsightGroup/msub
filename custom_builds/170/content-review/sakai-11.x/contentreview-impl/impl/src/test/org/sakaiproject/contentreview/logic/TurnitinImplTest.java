package org.sakaiproject.contentreview.logic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.contentreview.impl.turnitin.TurnitinReviewServiceImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

@ContextConfiguration({"/hibernate-test.xml", "/spring-hibernate.xml"})
public class TurnitinImplTest extends AbstractTransactionalJUnit4SpringContextTests {
	private static final Log log = LogFactory.getLog(TurnitinImplTest.class);
	

	@Test
	public void testFileEscape() {
		TurnitinReviewServiceImpl tiiService = new TurnitinReviewServiceImpl();
		String someEscaping = tiiService.escapeFileName("Practical%203.docx", "contentId");
		Assert.assertEquals("Practical_3.docx", someEscaping);
		
		someEscaping = tiiService.escapeFileName("Practical%203%.docx", "contentId");
		Assert.assertEquals("contentId", someEscaping);
		
		someEscaping = tiiService.escapeFileName("Practical3.docx", "contentId");
		Assert.assertEquals("Practical3.docx", someEscaping);
		
		
	}
	
	
}
