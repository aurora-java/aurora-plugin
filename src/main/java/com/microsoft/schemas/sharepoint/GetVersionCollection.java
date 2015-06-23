
package com.microsoft.schemas.sharepoint;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element name="strlistID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="strlistItemID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="strFieldName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "strlistID",
    "strlistItemID",
    "strFieldName"
})
@XmlRootElement(name = "GetVersionCollection")
public class GetVersionCollection {

    protected String strlistID;
    protected String strlistItemID;
    protected String strFieldName;

    /**
     * Gets the value of the strlistID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrlistID() {
        return strlistID;
    }

    /**
     * Sets the value of the strlistID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrlistID(String value) {
        this.strlistID = value;
    }

    /**
     * Gets the value of the strlistItemID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrlistItemID() {
        return strlistItemID;
    }

    /**
     * Sets the value of the strlistItemID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrlistItemID(String value) {
        this.strlistItemID = value;
    }

    /**
     * Gets the value of the strFieldName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrFieldName() {
        return strFieldName;
    }

    /**
     * Sets the value of the strFieldName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrFieldName(String value) {
        this.strFieldName = value;
    }

}
