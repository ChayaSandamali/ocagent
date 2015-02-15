/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.oc.internal;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.oc.internal.exceptions.ParameterUnavailableException;
import org.wso2.carbon.oc.internal.messages.OCRegistrationRequest;
import org.wso2.carbon.oc.internal.messages.OCRegistrationResponse;
import org.wso2.carbon.oc.internal.messages.OCSynchronizationRequest;
import org.wso2.carbon.oc.internal.messages.OCSynchronizationResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class OperationsCenterConnector {
    private static Logger logger = LoggerFactory.getLogger(OperationsCenterConnector.class);
    ObjectMapper objectMapper = new ObjectMapper();

    /**
     * The http client used to connect Operations Center.
     */
    private HttpClient httpClient;

    boolean isRegistered = false;

    private String ocUrl;
    private static final String REGISTRATION_PATH = "/api/register";
    private static final String SYNCHRONIZATION_PATH = "/api/update";
    private static final String CONTENT_TYPE = "application/json";
    private static final String CHARACTER_SET = "UTF-8";

    public OperationsCenterConnector(String ocUrl, String username, String password) {
        this.ocUrl = ocUrl;
        if (StringUtils.isBlank(this.ocUrl)) {
            throw new IllegalArgumentException("Operations Center URL is unspecified.");
        }
        this.httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        this.httpClient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        this.httpClient.getParams().setAuthenticationPreemptive(true);
    }

    /**
     * @return
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void execute() {
        logger.info("=================reporting");

        if (isRegistered == false) {
            register();
        } else {
            sync();
        }
    }

    private boolean register() {
        OCRegistrationRequest ocRegistrationRequest = new OCRegistrationRequest();
        try {
            ocRegistrationRequest.getRegistrationRequest().setIp(OperationsCenterAgentDataExtractor.getInstance().getLocalIp());
            ocRegistrationRequest.getRegistrationRequest().setServerName(OperationsCenterAgentDataExtractor.getInstance().getServerName());
            ocRegistrationRequest.getRegistrationRequest().setServerVersion(OperationsCenterAgentDataExtractor.getInstance().getServerVersion());
            ocRegistrationRequest.getRegistrationRequest().setDomain(OperationsCenterAgentDataExtractor.getInstance().getDomain());
            ocRegistrationRequest.getRegistrationRequest().setSubDomain(OperationsCenterAgentDataExtractor.getInstance().getSubDomain());
            ocRegistrationRequest.getRegistrationRequest().setAdminServiceUrl(OperationsCenterAgentDataExtractor.getInstance().getAdminServiceUrl());
            ocRegistrationRequest.getRegistrationRequest().setStartTime(OperationsCenterAgentDataExtractor.getInstance().getServerStartTime());
            ocRegistrationRequest.getRegistrationRequest().setOs(OperationsCenterAgentDataExtractor.getInstance().getOs());
            ocRegistrationRequest.getRegistrationRequest().setTotalMemory(OperationsCenterAgentDataExtractor.getInstance().getTotalMemory());
            ocRegistrationRequest.getRegistrationRequest().setCpuCount(OperationsCenterAgentDataExtractor.getInstance().getCpuCount());
            ocRegistrationRequest.getRegistrationRequest().setCpuSpeed(OperationsCenterAgentDataExtractor.getInstance().getCpuSpeed());
            List<String> patches = OperationsCenterAgentDataExtractor.getInstance().getPatches();
            if (patches.size() > 0) {
                ocRegistrationRequest.getRegistrationRequest().setPatches(patches);
            }
        } catch (ParameterUnavailableException e) {
            logger.error("Failed to read registration parameter. ", e);
            return false;
        }

        String jsonString = null;
        try {
            jsonString = objectMapper.writeValueAsString(ocRegistrationRequest);
        } catch (IOException e) {
            logger.error("Failed to get JSON String from OCRegistrationRequest", e);
            return false;
        }

        String responseBody = sendPostRequest(ocUrl + REGISTRATION_PATH, jsonString, HttpStatus.SC_CREATED);
        if (responseBody != null && responseBody.length() > 0) {
            OCRegistrationResponse ocRegistrationResponse = null;
            try {
                ocRegistrationResponse = objectMapper.readValue(responseBody, OCRegistrationResponse.class);
            } catch (IOException e) {
                logger.error("Failed to read values from OCRegistrationResponse", e);
                return false;
            }

            if (ocRegistrationResponse != null) {
                isRegistered = true;
                OperationsCenterAgentDataHolder.getInstance().
                        setServerId(Integer.parseInt(ocRegistrationResponse.getRegistrationResponse().getServerId()));
                logger.info("Registered in Operations Center successfully.");
            } else {
                logger.error("Unable receive JSON registration response.");
            }
        }
        return false;
    }

    private void sync() {
        OCSynchronizationRequest ocSynchronizationRequest = new OCSynchronizationRequest();

        try {
            ocSynchronizationRequest.getSynchronizationRequest().
                    setAdminServiceUrl(OperationsCenterAgentDataExtractor.getInstance().getAdminServiceUrl());
            ocSynchronizationRequest.getSynchronizationRequest().
                    setServerUpTime(OperationsCenterAgentDataExtractor.getInstance().getServerUpTime());
            ocSynchronizationRequest.getSynchronizationRequest().
                    setThreadCount(OperationsCenterAgentDataExtractor.getInstance().getThreadCount());
            ocSynchronizationRequest.getSynchronizationRequest().
                    setFreeMemory(OperationsCenterAgentDataExtractor.getInstance().getFreeMemory());
            ocSynchronizationRequest.getSynchronizationRequest().
                    setIdleCpuUsage(OperationsCenterAgentDataExtractor.getInstance().getIdelCpuUsage());
            ocSynchronizationRequest.getSynchronizationRequest().
                    setSystemCpuUsage(OperationsCenterAgentDataExtractor.getInstance().getSystemCpuUsage());
            ocSynchronizationRequest.getSynchronizationRequest().
                    setUserCpuUsage(OperationsCenterAgentDataExtractor.getInstance().getUserCpuUsage());
            ocSynchronizationRequest.getSynchronizationRequest().
                    setSystemLoadAverage(OperationsCenterAgentDataExtractor.getInstance().getSystemLoadAverage());




        } catch (ParameterUnavailableException e) {
            logger.error("Failed to read synchronization parameter. ", e);
            return;
        }

        String jsonString = null;
        try {
            jsonString = objectMapper.writeValueAsString(ocSynchronizationRequest);
        } catch (IOException e) {
            logger.error("Failed to get JSON String from ocSynchronizationRequest", e);
            return;
        }

        String responseBody = sendPostRequest(ocUrl + SYNCHRONIZATION_PATH, jsonString, HttpStatus.SC_OK);
        if (responseBody != null && responseBody.length() > 0) {
            OCSynchronizationResponse ocSynchronizationResponse = null;
            try {
                ocSynchronizationResponse = objectMapper.readValue(responseBody, OCSynchronizationResponse.class);
            } catch (IOException e) {
                logger.error("Failed to read values from OCSynchronizationResponse", e);
                return;
            }

            if (ocSynchronizationResponse != null) {
                if ("updated".equals(ocSynchronizationResponse.getSynchronizationResponse().getStatus())) {
                    String command = ocSynchronizationResponse.getSynchronizationResponse().getCommand();
                    logger.info("Executing command. [Command:" + command + "]");
                    OperationsCenterAgentUtils.performAction(command);
                } else if ("error".equals(ocSynchronizationResponse.getSynchronizationResponse().getStatus())) {
                    logger.error("Unable to synchronize properly.");
                    isRegistered = false;
                }

            } else {
                logger.error("Unable receive JSON synchronization response.");
            }
        }
    }

    public String sendPostRequest(String url, String request, int expected) {
        PostMethod postMethod = new PostMethod(url);
        try {
            RequestEntity entity = new StringRequestEntity(request, CONTENT_TYPE, CHARACTER_SET);
            postMethod.setRequestEntity(entity);
            if (logger.isTraceEnabled()) {
                logger.trace("Sending POST request. " + request);
            }
            try {
                int statusCode = httpClient.executeMethod(postMethod);
                if (statusCode == expected) {
                    String responseBody = postMethod.getResponseBodyAsString();
                    if (logger.isTraceEnabled()) {
                        logger.trace("Response received. " + responseBody);
                    }
                    return responseBody;
                } else {
                    logger.error("Request failed with status Code : " + statusCode);
                }
            }catch (IOException e) {
                logger.error("Failed to Execute post method", e);
                isRegistered = false;
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("Failed to register with Operations Center", e);
        } finally {
            postMethod.releaseConnection();
        }
        return null;
    }


}
