package com.rsmart.customer.integration.job;

import org.sakaiproject.api.app.scheduler.ConfigurableJobPropertyValidationException;
import org.sakaiproject.api.app.scheduler.ConfigurableJobPropertyValidator;



/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 9/12/12
 * Time: 2:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class QuartzEventPurgeValidator implements ConfigurableJobPropertyValidator
{
    public void assertValid(String propertyLabel, String value)
        throws ConfigurableJobPropertyValidationException
    {
        if (QuartzEventsPurgeJob.NUMBER_HOURS.equals(propertyLabel))
        {
            if (value == null || value.trim().length() < 1)
            {
                throw new ConfigurableJobPropertyValidationException ("hours.empty");
            }

            int num = 0;

            try
            {
                num = Integer.parseInt(value);
            }
            catch (NumberFormatException nfe)
            {
                throw new ConfigurableJobPropertyValidationException ("days.numberformat");
            }

            if (num < 1)
                throw new ConfigurableJobPropertyValidationException ("days.lessthanone");

        }

        if (QuartzEventsPurgeJob.JOBNAME_FILTER.equals(propertyLabel))
        {
            if (value == null || value.trim().length() < 1)
            {
                throw new ConfigurableJobPropertyValidationException ("jobName.filter.empty");
            }


        }

    }
}
