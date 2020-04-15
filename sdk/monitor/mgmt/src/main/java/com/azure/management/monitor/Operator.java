// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.management.monitor;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for Operator. */
public final class Operator extends ExpandableStringEnum<Operator> {
    /** Static value Equals for Operator. */
    public static final Operator EQUALS = fromString("Equals");

    /** Static value NotEquals for Operator. */
    public static final Operator NOT_EQUALS = fromString("NotEquals");

    /** Static value GreaterThan for Operator. */
    public static final Operator GREATER_THAN = fromString("GreaterThan");

    /** Static value GreaterThanOrEqual for Operator. */
    public static final Operator GREATER_THAN_OR_EQUAL = fromString("GreaterThanOrEqual");

    /** Static value LessThan for Operator. */
    public static final Operator LESS_THAN = fromString("LessThan");

    /** Static value LessThanOrEqual for Operator. */
    public static final Operator LESS_THAN_OR_EQUAL = fromString("LessThanOrEqual");

    /** Static value Include for Operator. */
    public static final Operator INCLUDE = fromString("Include");

    /**
     * Creates or finds a Operator from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding Operator.
     */
    @JsonCreator
    public static Operator fromString(String name) {
        return fromString(name, Operator.class);
    }

    /** @return known Operator values. */
    public static Collection<Operator> values() {
        return values(Operator.class);
    }
}
