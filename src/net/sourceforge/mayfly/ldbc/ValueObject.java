package net.sourceforge.mayfly.ldbc;

import org.apache.commons.lang.builder.*;

public class ValueObject {
    static {
        ReflectionToStringBuilder.setDefaultStyle(ToStringStyle.MULTI_LINE_STYLE);
    }

    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}