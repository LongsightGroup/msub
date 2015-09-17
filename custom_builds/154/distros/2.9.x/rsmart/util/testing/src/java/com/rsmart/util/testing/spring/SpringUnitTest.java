package com.rsmart.util.testing.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.junit.Before;

public class SpringUnitTest
{
    private ApplicationContext
        context = null;

    @Before
    public void setUp() throws Exception
    {
        context = new ClassPathXmlApplicationContext(this.getClass().getName() + ".xml");
    }
    
    public boolean isBeanInContext (String name)
    {
        return context.containsBean(name);
    }
    
    public Object getBeanFromContext (String name)
    {
        return context.getBean(name);
    }

}
