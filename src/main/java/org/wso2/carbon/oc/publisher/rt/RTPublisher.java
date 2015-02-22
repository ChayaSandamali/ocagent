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

package org.wso2.carbon.oc.publisher.rt;

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
import org.wso2.carbon.oc.internal.OCAgentDataExtractor;
import org.wso2.carbon.oc.internal.OCAgentDataHolder;
import org.wso2.carbon.oc.internal.OCAgentUtils;
import org.wso2.carbon.oc.internal.messages.RegistrationResponse;
import org.wso2.carbon.oc.internal.messages.SynchronizationResponse;
import org.wso2.carbon.oc.publisher.OCDataPublisher;
import org.wso2.carbon.oc.publisher.OCPublisherConstants;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Allows to publish real time data to oc web app
 * This is the default publisher
 */
public class RTPublisher implements OCDataPublisher {



    private static Logger logger = LoggerFactory.getLogger(RTPublisher.class);
    ObjectMapper objectMapper = new ObjectMapper();

    /**
     * The http client used to connect Operations Center.
     */
    private HttpClient httpClient;

	/**
	 * check oc registration message
	 */
    private boolean isRegistered = false;

    private String ocUrl;
    private long interval;

    private static final String REGISTRATION_PATH = "/api/register";
    private static final String SYNCHRONIZATION_PATH = "/api/update";
    private static final String CONTENT_TYPE = "application/json";
    private static final String CHARACTER_SET = "UTF-8";

	@Override public void init(Map<String, String> configMap) {
	    // get set config
        String username = configMap.get(OCPublisherConstants.USERNAME);
        String password = configMap.get(OCPublisherConstants.PASSWORD);
        this.ocUrl = configMap.get(OCPublisherConstants.REPORT_URL);

        this.interval = Long.parseLong(configMap.get(OCPublisherConstants.INTERVAL));


        if (StringUtils.isBlank(this.ocUrl)) {
            throw new IllegalArgumentException("Operations Center URL is unspecified.");
        }
        this.httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        this.httpClient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        this.httpClient.getParams().setAuthenticationPreemptive(true);
	    logger.info("RTPublisher init done");
    }



	@Override
    public void publish(OCAgentDataExtractor dataExtractor) {
        logger.info("======real-time===========reporting");

        if (!isRegistered) {
            register(dataExtractor);
        } else {
            sync(dataExtractor);
        }
    }

	/**
	 * send the real time registration message
	 */
    private void register(OCAgentDataExtractor dataExtractor) {

        String jsonString = RTMessageUtil.getRegistrationRequestMessage(dataExtractor);

        String responseBody = sendPostRequest(ocUrl + REGISTRATION_PATH, jsonString, HttpStatus.SC_CREATED);
        if (responseBody != null && responseBody.length() > 0) {
            RegistrationResponse registrationResponse = null;
            try {
	            registrationResponse = objectMapper.readValue(responseBody, RegistrationResponse.class);
            } catch (IOException e) {
                logger.error("Failed to read values from RegistrationResponse", e);
            }

            if (registrationResponse != null) {
                isRegistered = true;
                OCAgentDataHolder.getInstance().
                        setServerId(Integer.parseInt(registrationResponse.getServerId()));
                logger.info("Registered in Operations Center successfully.");
            } else {
                logger.error("Unable receive JSON registration response.");
            }
        }
    }

	/**
	 * send the real time synchronization message
	 */
    private void sync(OCAgentDataExtractor dataExtractor) {

        String jsonString = RTMessageUtil.getSynchronizationRequestMessage(dataExtractor);

        String responseBody = sendPostRequest(ocUrl + SYNCHRONIZATION_PATH, jsonString, HttpStatus.SC_OK);
        if (responseBody != null && responseBody.length() > 0) {
            SynchronizationResponse synchronizationResponse = null;
            try {
	            synchronizationResponse = objectMapper.readValue(responseBody, SynchronizationResponse.class);
            } catch (IOException e) {
                logger.error("Failed to read values from SynchronizationResponse", e);
                return;
            }

            if (synchronizationResponse != null) {
                if ("updated".equals(synchronizationResponse.getStatus())) {
                    String command = synchronizationResponse.getCommand();
                    logger.info("Executing command. [Command:" + command + "]");
                    OCAgentUtils.performAction(command);
                } else if ("error".equals(synchronizationResponse.getStatus())) {
                    logger.error("Unable to synchronize properly.");
                    isRegistered = false;
                }

            } else {
                logger.error("Unable receive JSON synchronization response.");
            }
        }
    }

	/**
	 * Send basic post request
	 * @param url       - operations center url
	 * @param request   - json string request message
	 * @param expected  - expected http status code
	 * @return
	 */
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
                logger.error("RTPublisher connection down: ", e);
                isRegistered = false;
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("Failed to register with Operations Center", e);
        } finally {
            postMethod.releaseConnection();
        }
        return null;
    }




    public long getInterval() {
        return interval;
    }
}
