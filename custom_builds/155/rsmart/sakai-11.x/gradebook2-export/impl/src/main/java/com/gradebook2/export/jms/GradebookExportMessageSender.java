package com.gradebook2.export.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.gradebook2.export.institutional.advisor.util.GradebookExportData;

public class GradebookExportMessageSender
{
	private JmsTemplate jmsTemplate;
	
	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public void sendGradebookExportMessage(final GradebookExportData data)
	{
		jmsTemplate.send(
		new MessageCreator(){
			public Message createMessage(Session session) throws JMSException {
				TextMessage message = session.createTextMessage();
				String jsonMessage = MarshallToJson(data);
				
				message.setText(jsonMessage);
				return message;
			}
		});
	}
	
	private String MarshallToJson(GradebookExportData data)
	{
		return null;
	}
}