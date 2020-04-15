// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.management.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.JsonFlatten;
import com.azure.management.appservice.ProxyOnlyResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/** The FunctionEnvelope model. */
@JsonFlatten
@Fluent
public class FunctionEnvelopeInner extends ProxyOnlyResource {
    /*
     * Function App ID.
     */
    @JsonProperty(value = "properties.function_app_id")
    private String functionAppId;

    /*
     * Script root path URI.
     */
    @JsonProperty(value = "properties.script_root_path_href")
    private String scriptRootPathHref;

    /*
     * Script URI.
     */
    @JsonProperty(value = "properties.script_href")
    private String scriptHref;

    /*
     * Config URI.
     */
    @JsonProperty(value = "properties.config_href")
    private String configHref;

    /*
     * Test data URI.
     */
    @JsonProperty(value = "properties.test_data_href")
    private String testDataHref;

    /*
     * Secrets file URI.
     */
    @JsonProperty(value = "properties.secrets_file_href")
    private String secretsFileHref;

    /*
     * Function URI.
     */
    @JsonProperty(value = "properties.href")
    private String href;

    /*
     * Config information.
     */
    @JsonProperty(value = "properties.config")
    private Object config;

    /*
     * File list.
     */
    @JsonProperty(value = "properties.files")
    private Map<String, String> files;

    /*
     * Test data used when testing via the Azure Portal.
     */
    @JsonProperty(value = "properties.test_data")
    private String testData;

    /*
     * The invocation URL
     */
    @JsonProperty(value = "properties.invoke_url_template")
    private String invokeUrlTemplate;

    /*
     * The function language
     */
    @JsonProperty(value = "properties.language")
    private String language;

    /*
     * Gets or sets a value indicating whether the function is disabled
     */
    @JsonProperty(value = "properties.isDisabled")
    private Boolean isDisabled;

    /**
     * Get the functionAppId property: Function App ID.
     *
     * @return the functionAppId value.
     */
    public String functionAppId() {
        return this.functionAppId;
    }

    /**
     * Set the functionAppId property: Function App ID.
     *
     * @param functionAppId the functionAppId value to set.
     * @return the FunctionEnvelopeInner object itself.
     */
    public FunctionEnvelopeInner withFunctionAppId(String functionAppId) {
        this.functionAppId = functionAppId;
        return this;
    }

    /**
     * Get the scriptRootPathHref property: Script root path URI.
     *
     * @return the scriptRootPathHref value.
     */
    public String scriptRootPathHref() {
        return this.scriptRootPathHref;
    }

    /**
     * Set the scriptRootPathHref property: Script root path URI.
     *
     * @param scriptRootPathHref the scriptRootPathHref value to set.
     * @return the FunctionEnvelopeInner object itself.
     */
    public FunctionEnvelopeInner withScriptRootPathHref(String scriptRootPathHref) {
        this.scriptRootPathHref = scriptRootPathHref;
        return this;
    }

    /**
     * Get the scriptHref property: Script URI.
     *
     * @return the scriptHref value.
     */
    public String scriptHref() {
        return this.scriptHref;
    }

    /**
     * Set the scriptHref property: Script URI.
     *
     * @param scriptHref the scriptHref value to set.
     * @return the FunctionEnvelopeInner object itself.
     */
    public FunctionEnvelopeInner withScriptHref(String scriptHref) {
        this.scriptHref = scriptHref;
        return this;
    }

    /**
     * Get the configHref property: Config URI.
     *
     * @return the configHref value.
     */
    public String configHref() {
        return this.configHref;
    }

    /**
     * Set the configHref property: Config URI.
     *
     * @param configHref the configHref value to set.
     * @return the FunctionEnvelopeInner object itself.
     */
    public FunctionEnvelopeInner withConfigHref(String configHref) {
        this.configHref = configHref;
        return this;
    }

    /**
     * Get the testDataHref property: Test data URI.
     *
     * @return the testDataHref value.
     */
    public String testDataHref() {
        return this.testDataHref;
    }

    /**
     * Set the testDataHref property: Test data URI.
     *
     * @param testDataHref the testDataHref value to set.
     * @return the FunctionEnvelopeInner object itself.
     */
    public FunctionEnvelopeInner withTestDataHref(String testDataHref) {
        this.testDataHref = testDataHref;
        return this;
    }

    /**
     * Get the secretsFileHref property: Secrets file URI.
     *
     * @return the secretsFileHref value.
     */
    public String secretsFileHref() {
        return this.secretsFileHref;
    }

    /**
     * Set the secretsFileHref property: Secrets file URI.
     *
     * @param secretsFileHref the secretsFileHref value to set.
     * @return the FunctionEnvelopeInner object itself.
     */
    public FunctionEnvelopeInner withSecretsFileHref(String secretsFileHref) {
        this.secretsFileHref = secretsFileHref;
        return this;
    }

    /**
     * Get the href property: Function URI.
     *
     * @return the href value.
     */
    public String href() {
        return this.href;
    }

    /**
     * Set the href property: Function URI.
     *
     * @param href the href value to set.
     * @return the FunctionEnvelopeInner object itself.
     */
    public FunctionEnvelopeInner withHref(String href) {
        this.href = href;
        return this;
    }

    /**
     * Get the config property: Config information.
     *
     * @return the config value.
     */
    public Object config() {
        return this.config;
    }

    /**
     * Set the config property: Config information.
     *
     * @param config the config value to set.
     * @return the FunctionEnvelopeInner object itself.
     */
    public FunctionEnvelopeInner withConfig(Object config) {
        this.config = config;
        return this;
    }

    /**
     * Get the files property: File list.
     *
     * @return the files value.
     */
    public Map<String, String> files() {
        return this.files;
    }

    /**
     * Set the files property: File list.
     *
     * @param files the files value to set.
     * @return the FunctionEnvelopeInner object itself.
     */
    public FunctionEnvelopeInner withFiles(Map<String, String> files) {
        this.files = files;
        return this;
    }

    /**
     * Get the testData property: Test data used when testing via the Azure Portal.
     *
     * @return the testData value.
     */
    public String testData() {
        return this.testData;
    }

    /**
     * Set the testData property: Test data used when testing via the Azure Portal.
     *
     * @param testData the testData value to set.
     * @return the FunctionEnvelopeInner object itself.
     */
    public FunctionEnvelopeInner withTestData(String testData) {
        this.testData = testData;
        return this;
    }

    /**
     * Get the invokeUrlTemplate property: The invocation URL.
     *
     * @return the invokeUrlTemplate value.
     */
    public String invokeUrlTemplate() {
        return this.invokeUrlTemplate;
    }

    /**
     * Set the invokeUrlTemplate property: The invocation URL.
     *
     * @param invokeUrlTemplate the invokeUrlTemplate value to set.
     * @return the FunctionEnvelopeInner object itself.
     */
    public FunctionEnvelopeInner withInvokeUrlTemplate(String invokeUrlTemplate) {
        this.invokeUrlTemplate = invokeUrlTemplate;
        return this;
    }

    /**
     * Get the language property: The function language.
     *
     * @return the language value.
     */
    public String language() {
        return this.language;
    }

    /**
     * Set the language property: The function language.
     *
     * @param language the language value to set.
     * @return the FunctionEnvelopeInner object itself.
     */
    public FunctionEnvelopeInner withLanguage(String language) {
        this.language = language;
        return this;
    }

    /**
     * Get the isDisabled property: Gets or sets a value indicating whether the function is disabled.
     *
     * @return the isDisabled value.
     */
    public Boolean isDisabled() {
        return this.isDisabled;
    }

    /**
     * Set the isDisabled property: Gets or sets a value indicating whether the function is disabled.
     *
     * @param isDisabled the isDisabled value to set.
     * @return the FunctionEnvelopeInner object itself.
     */
    public FunctionEnvelopeInner withIsDisabled(Boolean isDisabled) {
        this.isDisabled = isDisabled;
        return this;
    }
}
