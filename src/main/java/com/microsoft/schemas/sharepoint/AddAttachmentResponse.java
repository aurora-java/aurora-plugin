
package com.microsoft.schemas.sharepoint;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AddAttachmentResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "addAttachmentResult"
})
@XmlRootElement(name = "AddAttachmentResponse")
public class AddAttachmentResponse {

    @XmlElement(name = "AddAttachmentResult")
    protected String addAttachmentResult;

    /**
     * Gets the value of the addAttachmentResult property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddAttachmentResult() {
        return addAttachmentResult;
    }

    /**
     * Sets the value of the addAttachmentResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddAttachmentResult(String value) {
        this.addAttachmentResult = value;
    }

}
