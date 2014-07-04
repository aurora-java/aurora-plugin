
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
 *         &lt;element name="UndoCheckOutResult" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
    "undoCheckOutResult"
})
@XmlRootElement(name = "UndoCheckOutResponse")
public class UndoCheckOutResponse {

    @XmlElement(name = "UndoCheckOutResult")
    protected boolean undoCheckOutResult;

    /**
     * Gets the value of the undoCheckOutResult property.
     * 
     */
    public boolean isUndoCheckOutResult() {
        return undoCheckOutResult;
    }

    /**
     * Sets the value of the undoCheckOutResult property.
     * 
     */
    public void setUndoCheckOutResult(boolean value) {
        this.undoCheckOutResult = value;
    }

}
