
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
 *         &lt;element name="pageUrl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="checkoutToLocal" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="lastmodified" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "pageUrl",
    "checkoutToLocal",
    "lastmodified"
})
@XmlRootElement(name = "CheckOutFile")
public class CheckOutFile {

    protected String pageUrl;
    protected String checkoutToLocal;
    protected String lastmodified;

    /**
     * Gets the value of the pageUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPageUrl() {
        return pageUrl;
    }

    /**
     * Sets the value of the pageUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPageUrl(String value) {
        this.pageUrl = value;
    }

    /**
     * Gets the value of the checkoutToLocal property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCheckoutToLocal() {
        return checkoutToLocal;
    }

    /**
     * Sets the value of the checkoutToLocal property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCheckoutToLocal(String value) {
        this.checkoutToLocal = value;
    }

    /**
     * Gets the value of the lastmodified property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastmodified() {
        return lastmodified;
    }

    /**
     * Sets the value of the lastmodified property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastmodified(String value) {
        this.lastmodified = value;
    }

}
