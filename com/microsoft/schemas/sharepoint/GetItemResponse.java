
package com.microsoft.schemas.sharepoint;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
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
 *         &lt;element name="GetItemResult" type="{http://www.w3.org/2001/XMLSchema}unsignedInt"/>
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
    "getItemResult",
    "fields",
    "stream"
})
@XmlRootElement(name = "GetItemResponse")
public class GetItemResponse {

    @XmlElement(name = "GetItemResult")
    @XmlSchemaType(name = "unsignedInt")
    protected long getItemResult;
    @XmlElement(name = "Fields")
    protected FieldInformationCollection fields;
    @XmlElement(name = "Stream")
    protected byte[] stream;

    /**
     * Gets the value of the getItemResult property.
     * 
     */
    public long getGetItemResult() {
        return getItemResult;
    }

    /**
     * Sets the value of the getItemResult property.
     * 
     */
    public void setGetItemResult(long value) {
        this.getItemResult = value;
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
