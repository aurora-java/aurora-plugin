
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
 *         &lt;element name="CopyIntoItemsLocalResult" type="{http://www.w3.org/2001/XMLSchema}unsignedInt"/>
 *         &lt;element name="Results" type="{http://schemas.microsoft.com/sharepoint/soap/}CopyResultCollection" minOccurs="0"/>
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
    "copyIntoItemsLocalResult",
    "results"
})
@XmlRootElement(name = "CopyIntoItemsLocalResponse")
public class CopyIntoItemsLocalResponse {

    @XmlElement(name = "CopyIntoItemsLocalResult")
    @XmlSchemaType(name = "unsignedInt")
    protected long copyIntoItemsLocalResult;
    @XmlElement(name = "Results")
    protected CopyResultCollection results;

    /**
     * Gets the value of the copyIntoItemsLocalResult property.
     * 
     */
    public long getCopyIntoItemsLocalResult() {
        return copyIntoItemsLocalResult;
    }

    /**
     * Sets the value of the copyIntoItemsLocalResult property.
     * 
     */
    public void setCopyIntoItemsLocalResult(long value) {
        this.copyIntoItemsLocalResult = value;
    }

    /**
     * Gets the value of the results property.
     * 
     * @return
     *     possible object is
     *     {@link CopyResultCollection }
     *     
     */
    public CopyResultCollection getResults() {
        return results;
    }

    /**
     * Sets the value of the results property.
     * 
     * @param value
     *     allowed object is
     *     {@link CopyResultCollection }
     *     
     */
    public void setResults(CopyResultCollection value) {
        this.results = value;
    }

}
