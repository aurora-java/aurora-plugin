
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
 *         &lt;element name="SourceUrl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DestinationUrls" type="{http://schemas.microsoft.com/sharepoint/soap/}DestinationUrlCollection" minOccurs="0"/>
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
    "sourceUrl",
    "destinationUrls"
})
@XmlRootElement(name = "CopyIntoItemsLocal")
public class CopyIntoItemsLocal {

    @XmlElement(name = "SourceUrl")
    protected String sourceUrl;
    @XmlElement(name = "DestinationUrls")
    protected DestinationUrlCollection destinationUrls;

    /**
     * Gets the value of the sourceUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * Sets the value of the sourceUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceUrl(String value) {
        this.sourceUrl = value;
    }

    /**
     * Gets the value of the destinationUrls property.
     * 
     * @return
     *     possible object is
     *     {@link DestinationUrlCollection }
     *     
     */
    public DestinationUrlCollection getDestinationUrls() {
        return destinationUrls;
    }

    /**
     * Sets the value of the destinationUrls property.
     * 
     * @param value
     *     allowed object is
     *     {@link DestinationUrlCollection }
     *     
     */
    public void setDestinationUrls(DestinationUrlCollection value) {
        this.destinationUrls = value;
    }

}
