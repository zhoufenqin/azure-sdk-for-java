/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.network.v2020_03_01;

import com.microsoft.azure.arm.model.HasInner;
import com.microsoft.azure.arm.resources.models.HasManager;
import com.microsoft.azure.management.network.v2020_03_01.implementation.NetworkManager;
import com.microsoft.azure.management.network.v2020_03_01.implementation.FlowLogInformationInner;

/**
 * Type representing FlowLogInformation.
 */
public interface FlowLogInformation extends HasInner<FlowLogInformationInner>, HasManager<NetworkManager> {
    /**
     * @return the enabled value.
     */
    boolean enabled();

    /**
     * @return the flowAnalyticsConfiguration value.
     */
    TrafficAnalyticsProperties flowAnalyticsConfiguration();

    /**
     * @return the format value.
     */
    FlowLogFormatParameters format();

    /**
     * @return the retentionPolicy value.
     */
    RetentionPolicyParameters retentionPolicy();

    /**
     * @return the storageId value.
     */
    String storageId();

    /**
     * @return the targetResourceId value.
     */
    String targetResourceId();

}
