
package com.microsoft.schemas.sharepoint;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CopyResultCollection complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CopyResultCollection">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CopyResult" type="{http://schemas.microsoft.com/sharepoint/soap/}CopyResult" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CopyResultCollection", propOrder = {
    "copyResult"
})
public class CopyResultCollection {

    @XmlElement(name = "CopyResult", nillable = true)
    protected List<CopyResult> copyResult;

    /**
     * Gets the value of the copyResult property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the copyResult property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCopyResult().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CopyResult }
     * 
     * 
     */
    public List<CopyResult> getCopyResult() {
        if (copyResult == null) {
            copyResult = new ArrayList<CopyResult>();
        }
        return this.copyResult;
    }

}
