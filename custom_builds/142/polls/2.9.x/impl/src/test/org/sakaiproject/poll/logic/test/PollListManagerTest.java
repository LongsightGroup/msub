/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.poll.logic.test;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.poll.dao.PollDao;
import org.sakaiproject.poll.logic.test.stubs.ExternalLogicStubb;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Vote;
import org.sakaiproject.poll.service.impl.PollListManagerImpl;
import org.sakaiproject.poll.service.impl.PollVoteManagerImpl;
import org.springframework.test.AbstractTransactionalSpringContextTests;

public class PollListManagerTest extends AbstractTransactionalSpringContextTests {

	private static Log log = LogFactory.getLog(PollListManagerTest.class);	
	
	private TestDataPreload tdp = new TestDataPreload();

	private PollListManagerImpl pollListManager;
	private PollVoteManagerImpl pollVoteManager;
	private ExternalLogicStubb externalLogicStubb;
	
	protected String[] getConfigLocations() {
		// point to the needed spring config files, must be on the classpath
		// (add component/src/webapp/WEB-INF to the build path in Eclipse),
		// they also need to be referenced in the project.xml file
		return new String[] { "hibernate-test.xml", "classpath:org/sakaiproject/poll/spring-hibernate.xml" };
	}

	// run this before each test starts
	protected void onSetUpBeforeTransaction() throws Exception {
	}
	
	// run this before each test starts and as part of the transaction
	protected void onSetUpInTransaction() {
		PollDao dao = (PollDao) applicationContext.getBean("org.sakaiproject.poll.dao.impl.PollDaoTarget");
		if (dao == null) {
			log.error("onSetUpInTransaction: DAO could not be retrieved from spring context");
			return;
		}
		
		pollListManager = new PollListManagerImpl();
		pollListManager.setDao(dao);
		
		pollVoteManager = new PollVoteManagerImpl();
		pollVoteManager.setDao(dao);
		
		
		externalLogicStubb = new ExternalLogicStubb();
		pollListManager.setExternalLogic(externalLogicStubb);
		pollVoteManager.setExternalLogic(externalLogicStubb);
		pollListManager.setPollVoteManager(pollVoteManager);
		
		// preload testData
		tdp.preloadTestData(dao);
	}
	
    public void testGetPollById() {
    	externalLogicStubb.currentUserId = TestDataPreload.USER_UPDATE;
    	
    	//we shouldNot find this poll
    	Poll pollFail = pollListManager.getPollById(Long.valueOf(99));
    	assertNull(pollFail);
    	
    	//this one should exist
    	externalLogicStubb.currentUserId = TestDataPreload.USER_UPDATE;
    	Poll poll1 = pollListManager.getPollById(Long.valueOf(1));
    	assertNotNull(poll1);
    	
    	//it should have options
    	assertNotNull(poll1.getPollOptions());
    	assertTrue(poll1.getPollOptions().size() > 0);
    	
    	//we expect this one to fails
		externalLogicStubb.currentUserId = TestDataPreload.USER_NO_ACCEESS;
		try {
			Poll poll2 = pollListManager.getPollById(Long.valueOf(1));
			fail("should not be allowed to read this poll");
		} 
		catch (SecurityException e) {
			e.printStackTrace();
		}
    }
	
    
    public void testSavePoll() {
    	externalLogicStubb.currentUserId = TestDataPreload.USER_UPDATE;
		
    	Poll poll1 = new Poll();
		poll1.setCreationDate(new Date());
		poll1.setVoteOpen(new Date());
		poll1.setVoteClose(new Date());
		poll1.setDescription("this is some text");
		poll1.setText("something");
		poll1.setOwner(TestDataPreload.USER_UPDATE);
		
		poll1.setSiteId(TestDataPreload.LOCATION1_ID);
		
		
		//If this has a value something is wrong without POJO
		assertNull(poll1.getPollId());
		
		pollListManager.savePoll(poll1);
		
		//if this is null we have a problem
		assertNotNull(poll1.getPollId());
		
		Poll poll2 = pollListManager.getPollById(poll1.getPollId());
		assertNotNull(poll2);
		assertEquals(poll1.getPollText(), poll2.getPollText());
		
		//TODO add failure cases - null parameters
		
		//we should not be able to save empty polls
		
		//a user needs privileges to save the poll
		try {
			pollListManager.savePoll(null);
			fail();
		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		
		
		//a user needs privileges to save the poll
		try {
			Poll poll = new Poll();
			poll.setText("sdfgsdf");
			pollListManager.savePoll(poll);
			fail();
		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		
		externalLogicStubb.currentUserId = TestDataPreload.USER_NO_ACCEESS;
		try {
			pollListManager.savePoll(poll1);
			fail();
		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		catch (SecurityException se) {
			se.printStackTrace();
		}
		
    }
	
    
    public void testDeletePoll() {
    	
    	externalLogicStubb.currentUserId = TestDataPreload.USER_UPDATE;
    	Poll poll1 = new Poll();
		poll1.setCreationDate(new Date());
		poll1.setVoteOpen(new Date());
		poll1.setVoteClose(new Date());
		poll1.setDescription("this is some text");
		poll1.setText("something");
		poll1.setOwner(TestDataPreload.USER_UPDATE);
		poll1.setSiteId(TestDataPreload.LOCATION1_ID);
		
		
		
		//we should not be able to delete a poll that hasn't been saved
		try {
			pollListManager.deletePoll(poll1);
			fail();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		
		pollListManager.savePoll(poll1);
		
	    Option option1 = new Option();
	    option1.setPollId(poll1.getPollId());
	    option1.setOptionText("asdgasd");
	    
	    Option option2 = new Option();
	    option2.setPollId(poll1.getPollId());
	    option2.setOptionText("zsdbsdfb");
	    
	    pollListManager.saveOption(option2);
	    pollListManager.saveOption(option1);
	    
	    Vote vote = new Vote();
	    vote.setIp("Localhost");
	    vote.setPollId(poll1.getPollId());
	    vote.setPollOption(option1.getOptionId());
	    
	    
	    pollVoteManager.saveVote(vote);
	    
	    Long option1Id = option1.getOptionId();
	    Long option2Id = option2.getOptionId();
	    Long voteId = vote.getId();
	    
	    
		externalLogicStubb.currentUserId = TestDataPreload.USER_NO_ACCEESS;
		
    	try {
			pollListManager.deletePoll(poll1);
			fail();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		externalLogicStubb.currentUserId = TestDataPreload.USER_UPDATE;
    	try {
			pollListManager.deletePoll(poll1);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		
		
		//check that child options are deteled
		Vote v1 = pollVoteManager.getVoteById(voteId);
		assertNull(v1);
		
		Option o1 = pollListManager.getOptionById(option1Id);
		Option o2 = pollListManager.getOptionById(option2Id);
		assertNull(o1);
		assertNull(o2);
		
		
    }
}
