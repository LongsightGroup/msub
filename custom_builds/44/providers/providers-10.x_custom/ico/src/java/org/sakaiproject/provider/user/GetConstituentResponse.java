package org.sakaiproject.provider.user;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="GetConstituentResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "getConstituentResult" }, namespace="http://ico.edu/")
@XmlRootElement(name = "GetConstituentResponse", namespace ="http://ico.edu/")
public class GetConstituentResponse {

	@XmlElement(name = "GetConstituentResult")
	protected String getConstituentResult;

	/**
 * 	 * Gets the value of the getConstituentResult property.
 * 	 	 * 
 * 	 	 	 * @return possible object is {@link String }
 * 	 	 	 	 * 
 * 	 	 	 	 	 */
	public String getGetConstituentResult() {
		return getConstituentResult;
	}

	/**
 * 	 * Sets the value of the getConstituentResult property.
 * 	 	 * 
 * 	 	 	 * @param value
 * 	 	 	 	 *            allowed object is {@link String }
 * 	 	 	 	 	 * 
 * 	 	 	 	 	 	 */
	public void setGetConstituentResult(String value) {
		this.getConstituentResult = value;
	}

}

