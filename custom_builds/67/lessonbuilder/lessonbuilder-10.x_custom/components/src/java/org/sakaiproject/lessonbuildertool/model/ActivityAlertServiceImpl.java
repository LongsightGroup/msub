package org.sakaiproject.lessonbuildertool.model;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.scheduler.DelayedInvocation;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.lessonbuildertool.ActivityAlert;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;

public class ActivityAlertServiceImpl implements ActivityAlertService{
	private static Log log = LogFactory.getLog(ActivityAlertServiceImpl.class);
	private ScheduledInvocationManager scheduledInvocationManager;
	private TimeService timeService;
	private SimplePageToolDao simplePageToolDao;
	private static final String ID_DELIMITER = ";";

	@Override
	public void scheduleActivityAlert(ActivityAlert alert){
		log.info("ActivityAlertServiceImpl.scheduleActivityAlert");
		if(alert != null && alert.getSiteId() != null && !"".equals(alert.getSiteId().trim())
				 && alert.getTool() != null && !"".equals(alert.getTool().trim())
				 && alert.getReference() != null && !"".equals(alert.getReference().trim())){
			//first clear out any existing scheduled alerts:
			clearActivityAlert(alert);
			if(alert.getBeginDate() != null && alert.getRecurrence() != null && 
					((alert.getStudentRecipients() != null && !"".equals(alert.getStudentRecipients().trim()))
					|| (alert.getNonStudentRecipients() != null && !"".equals(alert.getNonStudentRecipients().trim())))){
				Date scheduleDate = alert.getBeginDate();
				Calendar c = Calendar.getInstance();
				Date now = c.getTime();
				if(ActivityAlert.RECURRENCCE_NONE != alert.getRecurrence().intValue()){
					while(scheduleDate.before(now)){
						c.setTime(scheduleDate);
						//find the next DATE 
						if(ActivityAlert.RECURRENCCE_DAILY == alert.getRecurrence()){
							c.add(Calendar.DAY_OF_YEAR, 1);
						}else if(ActivityAlert.RECURRENCCE_WEEKLY == alert.getRecurrence()){
							c.add(Calendar.DAY_OF_YEAR, 7);
						}else{
							break;
						}
						scheduleDate = c.getTime();
					}
					if(alert.getEndDate() == null){
						alert.setEndDate(alert.getBeginDate());
					}
					if(scheduleDate.after(alert.getEndDate())){
						scheduleDate = null;
					}
				}
				if(scheduleDate != null && scheduleDate.after(now)){
					log.info("Scheduling alert at: " + scheduleDate);
					Time scheduleTime = timeService.newTime(scheduleDate.getTime());
					scheduledInvocationManager.createDelayedInvocation(scheduleTime, "org.sakaiproject.lessonbuildertool.model.ActivityAlertService", getActivitySchedulerId(alert));
				}
			}		
		}	
	}

	@Override
	public void clearActivityAlert(ActivityAlert alert) {
		log.info("ActivityAlertServiceImpl.clearActivityAlert");
		// Remove any existing notifications for this area
		DelayedInvocation[] fdi = scheduledInvocationManager.findDelayedInvocations("org.sakaiproject.lessonbuildertool.model.ActivityAlertService", getActivitySchedulerId(alert));
		if (fdi != null && fdi.length > 0)
		{
			for (DelayedInvocation d : fdi)
			{
				scheduledInvocationManager.deleteDelayedInvocation(d.uuid);
			}
		}
	}
	
	private String getActivitySchedulerId(ActivityAlert alert){
		return alert.getSiteId() + ID_DELIMITER + alert.getTool() + ID_DELIMITER + alert.getReference();
	}
	
	@Override
	public void execute(String id) {
		log.info("ActivityAlertServiceImpl.execute");
		
		String[] idSplit = id.split(ID_DELIMITER);
		if(idSplit.length == 3){
			ActivityAlert alert = simplePageToolDao.findActivityAlert(idSplit[0], idSplit[1], idSplit[2]);
			if(alert != null){
				
				
				
				
				
				
				//Now see if you there needs to be a recurrence or not:
				scheduleActivityAlert(alert);
			}
		}
	}

	public ScheduledInvocationManager getScheduledInvocationManager() {
		return scheduledInvocationManager;
	}

	public void setScheduledInvocationManager(
			ScheduledInvocationManager scheduledInvocationManager) {
		this.scheduledInvocationManager = scheduledInvocationManager;
	}

	public TimeService getTimeService() {
		return timeService;
	}

	public void setTimeService(TimeService timeService) {
		this.timeService = timeService;
	}

	public SimplePageToolDao getSimplePageToolDao() {
		return simplePageToolDao;
	}

	public void setSimplePageToolDao(SimplePageToolDao simplePageToolDao) {
		this.simplePageToolDao = simplePageToolDao;
	}

}
