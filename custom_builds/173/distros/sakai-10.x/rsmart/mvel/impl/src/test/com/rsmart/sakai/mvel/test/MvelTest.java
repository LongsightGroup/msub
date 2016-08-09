package com.rsmart.sakai.mvel.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rsmart.sakai.mvel.api.MvelService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-beans.xml" })
public class MvelTest {

	@Autowired
	private MvelService mvel;

	@Test
	public void testEvaluate() {
		int result = (Integer) mvel.evaluate("3 * 3");
		Assert.assertEquals(9, result);
		
		result = (Boolean) mvel.evaluate("a = 3 * 3; a == 9") ? 1 : 0;
		Assert.assertEquals(1, result);
		
		List<String> list = (List) mvel.evaluate("[\"Kevin\", \"John\", \"Earle\"]");
		Assert.assertEquals(3, list.size());
	}

	@Test
	public void testBase64Evaluate() {
		int result = (Integer) mvel.evaluate("MyAqIDMK");
		Assert.assertEquals(9, result);
		
		result = (Boolean) mvel.evaluate("YSA9IDMgKiAzOyBhID09IDkK") ? 1 : 0;
		Assert.assertEquals(1, result);
		
		List<String> list = (List) mvel.evaluate("WyJLZXZpbiIsICJKb2huIiwgIkVhcmxlIl0K");
		Assert.assertEquals(3, list.size());
	}

	@Test
	public void testEvaluateParamsAsMap() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("two", new Integer(2));
		
		int result = (Integer) mvel.evaluate("two * two", params);
		Assert.assertEquals(4, result);
	}
	
	@Test
	public void testEvaluateParamsAsJson() {
		String params = "{\"two\": 2, \"three\": 3}";
		
		int result = (Integer) mvel.evaluate("two * three", params);
		Assert.assertEquals(6, result);
	}
	
	@Test
	public void testBase64EvaluateParamsAsJson() {
		String params = "{\"two\": 2, \"three\": 3}";
		
		int result = (Integer) mvel.evaluate("dHdvICogdGhyZWUK", params);
		Assert.assertEquals(6, result);
	}

	@Test
	public void testEvaluateAsString() {
		String result = mvel.evaluateAsString("3 * 3");
		Assert.assertEquals("9", result);
		
		result = mvel.evaluateAsString("a = 3 * 3; a == 9");
		Assert.assertEquals("true", result);
		
		result = mvel.evaluateAsString("[\"Kevin\", \"John\", \"Earle\"]");
		String shouldBe = "Kevin" + "\n" + "John" + "\n" + "Earle" + "\n";
		Assert.assertEquals(shouldBe, result);
	}
	
	@Test
	public void testBase64EvaluateAsString() {
		String result = mvel.evaluateAsString("MyAqIDMK");
		Assert.assertEquals("9", result);
		
		result = mvel.evaluateAsString("YSA9IDMgKiAzOyBhID09IDkK");
		Assert.assertEquals("true", result);
		
		result = mvel.evaluateAsString("WyJLZXZpbiIsICJKb2huIiwgIkVhcmxlIl0K");
		String shouldBe = "Kevin" + "\n" + "John" + "\n" + "Earle" + "\n";
		Assert.assertEquals(shouldBe, result);
	}

	@Test
	public void testEvaluateAsStringParamsAsMap() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("two", new Integer(2));
		
		String result = mvel.evaluateAsString("two * two", params);
		Assert.assertEquals("4", result);
	}
	
	@Test
	public void testEvaluateAsStringParamsAsJson() {
		String params = "{\"two\": 2, \"three\": 3}";
		
		String result = mvel.evaluateAsString("two * three", params);
		Assert.assertEquals("6", result);
	}
}
