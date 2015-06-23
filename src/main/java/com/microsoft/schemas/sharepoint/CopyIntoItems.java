
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
 *         &lt;element name="Fields" type="{http://schemas.microsoft.com/sharepoint/soap/}FieldInformationCollection" minOccurs="0"/>
 *         &lt;element name="Stream" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
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
    "destinationUrls",
    "fields",
    "stream"
})
@XmlRootElement(name = "CopyIntoItems")
public class CopyIntoItems {

    @XmlElement(name = "SourceUrl")
    protected String sourceUrl;
    @XmlElement(name = "DestinationUrls")
    protected DestinationUrlCollection destinationUrls;
    @XmlElement(name = "Fields")
    protected FieldInformationCollection fields;
    @XmlElement(name = "Stream")
    protected byte[] stream;

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

    /**
     * Gets the value of the fields property.
     * 
     * @return
     *     possible object is
     *     {@link FieldInformationCollection }
     *     
     */
    public FieldInformationCollection getFields() {
        return fields;
    }

    /**
     * Sets the value of the fields property.
     * 
     * @param value
     *     allowed object is
     *     {@link FieldInformationCollection }
     *     
     */
    public void setFields(FieldInformationCollection value) {
        this.fields = value;
    }

    /**
     * Gets the value of the stream property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getStream() {
        return stream;
    }

    /**
     * Sets the value of the stream property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setStream(byte[] value) {
        this.stream = ((byte[]) value);
    }

}
