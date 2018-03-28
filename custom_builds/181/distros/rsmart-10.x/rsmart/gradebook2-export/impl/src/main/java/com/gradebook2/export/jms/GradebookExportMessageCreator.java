package com.gradebook2.export.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.jms.core.*;

public class GradebookExportMessageCreator implements MessageCreator
{

	@Override
	public Message createMessage(Session arg0) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}
	
}