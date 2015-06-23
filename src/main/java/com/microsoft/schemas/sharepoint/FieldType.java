
package com.microsoft.schemas.sharepoint;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FieldType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="FieldType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Invalid"/>
 *     &lt;enumeration value="Integer"/>
 *     &lt;enumeration value="Text"/>
 *     &lt;enumeration value="Note"/>
 *     &lt;enumeration value="DateTime"/>
 *     &lt;enumeration value="Counter"/>
 *     &lt;enumeration value="Choice"/>
 *     &lt;enumeration value="Lookup"/>
 *     &lt;enumeration value="Boolean"/>
 *     &lt;enumeration value="Number"/>
 *     &lt;enumeration value="Currency"/>
 *     &lt;enumeration value="URL"/>
 *     &lt;enumeration value="Computed"/>
 *     &lt;enumeration value="Threading"/>
 *     &lt;enumeration value="Guid"/>
 *     &lt;enumeration value="MultiChoice"/>
 *     &lt;enumeration value="GridChoice"/>
 *     &lt;enumeration value="Calculated"/>
 *     &lt;enumeration value="File"/>
 *     &lt;enumeration value="Attachments"/>
 *     &lt;enumeration value="User"/>
 *     &lt;enumeration value="Recurrence"/>
 *     &lt;enumeration value="CrossProjectLink"/>
 *     &lt;enumeration value="ModStat"/>
 *     &lt;enumeration value="AllDayEvent"/>
 *     &lt;enumeration value="Error"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "FieldType")
@XmlEnum
public enum FieldType {

    @XmlEnumValue("Invalid")
    INVALID("Invalid"),
    @XmlEnumValue("Integer")
    INTEGER("Integer"),
    @XmlEnumValue("Text")
    TEXT("Text"),
    @XmlEnumValue("Note")
    NOTE("Note"),
    @XmlEnumValue("DateTime")
    DATE_TIME("DateTime"),
    @XmlEnumValue("Counter")
    COUNTER("Counter"),
    @XmlEnumValue("Choice")
    CHOICE("Choice"),
    @XmlEnumValue("Lookup")
    LOOKUP("Lookup"),
    @XmlEnumValue("Boolean")
    BOOLEAN("Boolean"),
    @XmlEnumValue("Number")
    NUMBER("Number"),
    @XmlEnumValue("Currency")
    CURRENCY("Currency"),
    URL("URL"),
    @XmlEnumValue("Computed")
    COMPUTED("Computed"),
    @XmlEnumValue("Threading")
    THREADING("Threading"),
    @XmlEnumValue("Guid")
    GUID("Guid"),
    @XmlEnumValue("MultiChoice")
    MULTI_CHOICE("MultiChoice"),
    @XmlEnumValue("GridChoice")
    GRID_CHOICE("GridChoice"),
    @XmlEnumValue("Calculated")
    CALCULATED("Calculated"),
    @XmlEnumValue("File")
    FILE("File"),
    @XmlEnumValue("Attachments")
    ATTACHMENTS("Attachments"),
    @XmlEnumValue("User")
    USER("User"),
    @XmlEnumValue("Recurrence")
    RECURRENCE("Recurrence"),
    @XmlEnumValue("CrossProjectLink")
    CROSS_PROJECT_LINK("CrossProjectLink"),
    @XmlEnumValue("ModStat")
    MOD_STAT("ModStat"),
    @XmlEnumValue("AllDayEvent")
    ALL_DAY_EVENT("AllDayEvent"),
    @XmlEnumValue("Error")
    ERROR("Error");
    private final String value;

    FieldType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FieldType fromValue(String v) {
        for (FieldType c: FieldType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
