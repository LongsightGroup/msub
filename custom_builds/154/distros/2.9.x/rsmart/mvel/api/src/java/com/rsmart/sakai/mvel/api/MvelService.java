package com.rsmart.sakai.mvel.api;

import java.util.Map;

/**
 * MvelService
 * 		This service will evaluate expressions using MVEL {@link http://mvel.codehaus.org/}
 *
 * @author Earle Nietzel
 * Created on Nov 29, 2012
 * 
 */
public interface MvelService {
	
	/**
	 * Refer to {@link MvelService#evaluate(String, Map)}
	 * @param expression Expression to be evaluated
	 * @return Object containing the result of the evaluated expression
	 */
	Object evaluate(String expression);
	
	/**
	 * Method will evaluate the expression with the provided parameters. Parameters are 
	 * passed in to the EL as a map. EL uses the keys as variables and the variables
	 * values are the keys value from the map.
	 * 
	 * @param expression Expression to be evaluated
	 * @param parameters Parameters that will be made available to the EL for evaluation
	 * @return Object containing the result of the evaluated expression
	 */
	Object evaluate(String expression, Map<String, Object> parameters);

	/**
	 * Method will evaluate the expression with the provided parameters. Parameters are 
	 * passed in to the EL as a JSON String. The JSON should be in the form of a Map which
	 * will then be deserialized into a Map<String, Object> and passed to evaluate.
	 * 
	 * @param expression Expression to be evaluated
	 * @param json Parameters that will be deserialized and made available to the EL for evaluation
	 * @return Object containing the result of the evaluated expression
	 */
	Object evaluate(String expression, String json);

	/**
	 * Refer to {@link MvelService#evaluateAsString(String, Map)}
	 * @param expression Expression to be evaluated
	 * @return String containing the result of the evaluated expression
	 */
	String evaluateAsString(String expression);
	
	/**
	 * Method will evaluate the expression with the provided parameters. Parameters are 
	 * passed in to the EL as a map. EL uses the keys as variables and the variables
	 * values are the keys value from the map.
	 * 
	 * @param expression Expression to be evaluated
	 * @param parameters Parameters that will be made available to the EL for evaluation
	 * @return String containing the result of the evaluated expression
	 */
	String evaluateAsString(String expression, Map<String, Object> parameters);

	/**
	 * Method will evaluate the expression with the provided parameters. Parameters are 
	 * passed in to the EL as a JSON String. The JSON should be in the form of a Map which
	 * will then be deserialized into a Map<String, Object> and passed to evaluate.
	 * 
	 * @param expression Expression to be evaluated
	 * @param json Parameters that will be deserialized and made available to the EL for evaluation
	 * @return String containing the result of the evaluated expression
	 */
	String evaluateAsString(String expression, String json);

	/**
	 * Refer to {@link MvelService#evaluateAsJson(String, Map)}
	 * @param expression Expression to be evaluated
	 * @return JSON String containing the result of the evaluated expression
	 */
	String evaluateAsJson(String expression);
	
	/**
	 * Method will evaluate the expression with the provided parameters. Parameters are 
	 * passed in to the EL as a map. EL uses the keys as variables and the variables
	 * values are the keys value from the map.
	 * 
	 * @param expression Expression to be evaluated
	 * @param parameters Parameters that will be made available to the EL for evaluation
	 * @return JSON String containing the result of the evaluated expression
	 */
	String evaluateAsJson(String expression, Map<String, Object> parameters);

	/**
	 * Method will evaluate the expression with the provided parameters. Parameters are 
	 * passed in to the EL as a JSON String. The JSON should be in the form of a Map which
	 * will then be deserialized into a Map<String, Object> and passed to evaluate.
	 * 
	 * @param expression Expression to be evaluated
	 * @param json Parameters that will be deserialized and made available to the EL for evaluation
	 * @return JSON String containing the result of the evaluated expression
	 */
	String evaluateAsJson(String expression, String json);

	/**
	 * Refer to {@link MvelService#evaluateAsXml(String, Map)}
	 * @param expression Expression to be evaluated
	 * @return XML String containing the result of the evaluated expression
	 */
	String evaluateAsXml(String expression);
	
	/**
	 * Method will evaluate the expression with the provided parameters. Parameters are 
	 * passed in to the EL as a map. EL uses the keys as variables and the variables
	 * values are the keys value from the map.
	 * 
	 * @param expression Expression to be evaluated
	 * @param parameters Parameters that will be made available to the EL for evaluation
	 * @return XML String containing the result of the evaluated expression
	 */
	String evaluateAsXml(String expression, Map<String, Object> parameters);
	
	/**
	 * Method will evaluate the expression with the provided parameters. Parameters are 
	 * passed in to the EL as a JSON String. The JSON should be in the form of a Map which
	 * will then be deserialized into a Map<String, Object> and passed to evaluate.
	 * 
	 * @param expression Expression to be evaluated
	 * @param json Parameters that will be deserialized and made available to the EL for evaluation
	 * @return XML String containing the result of the evaluated expression
	 */
	String evaluateAsXml(String expression, String json);

}
