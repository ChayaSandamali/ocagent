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

package org.wso2.carbon.oc.publisher;

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
import org.wso2.carbon.oc.internal.OCAgentDataHolder;
import org.wso2.carbon.oc.internal.OCAgentUtils;
import org.wso2.carbon.oc.internal.OCConstants;
import org.wso2.carbon.oc.internal.messages.MessageUtil;
import org.wso2.carbon.oc.internal.messages.OCRegistrationResponse;
import org.wso2.carbon.oc.internal.messages.OCSynchronizationResponse;

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

    public RTPublisher(Map<String, String> configMap) {
	    // get set config
//        Map<String, String> configMap = OCAgentUtils
//		        .getPublisher(RTPublisher.class.getCanonicalName());
        String username = configMap.get(OCConstants.USERNAME);
        String password = configMap.get(OCConstants.PASSWORD);
        this.ocUrl = configMap.get(OCConstants.REPORT_URL);

        this.interval = Long.parseLong(configMap.get(OCConstants.INTERVAL));


        if (StringUtils.isBlank(this.ocUrl)) {
            throw new IllegalArgumentException("Operations Center URL is unspecified.");
        }
        this.httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        this.httpClient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        this.httpClient.getParams().setAuthenticationPreemptive(true);
	    logger.info("RTPublisher init done");
    }

	@Override public void init() {

	}

	@Override
    public void publish() {
        logger.info("======real-time===========reporting");

        if (!isRegistered) {
            register();
        } else {
            sync();
        }
    }

	/**
	 * send the real time registration message
	 */
    private void register() {

        String jsonString = MessageUtil.getRTRegistrationRequest();

        String responseBody = sendPostRequest(ocUrl + REGISTRATION_PATH, jsonString, HttpStatus.SC_CREATED);
        if (responseBody != null && responseBody.length() > 0) {
            OCRegistrationResponse ocRegistrationResponse = null;
            try {
                ocRegistrationResponse = objectMapper.readValue(responseBody, OCRegistrationResponse.class);
            } catch (IOException e) {
                logger.error("Failed to read values from OCRegistrationResponse", e);
            }

            if (ocRegistrationResponse != null) {
                isRegistered = true;
                OCAgentDataHolder.getInstance().
                        setServerId(Integer.parseInt(ocRegistrationResponse.getRegistrationResponse().getServerId()));
                logger.info("Registered in Operations Center successfully.");
            } else {
                logger.error("Unable receive JSON registration response.");
            }
        }
    }

	/**
	 * send the real time synchronization message
	 */
    private void sync() {

        String jsonString = MessageUtil.getRTSynchronizationRequest();

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
                    OCAgentUtils.performAction(command);
                } else if ("error".equals(ocSynchronizationResponse.getSynchronizationResponse().getStatus())) {
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
