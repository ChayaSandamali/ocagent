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

package org.wso2.carbon.oc.agent.publisher.rt;

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
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.oc.agent.internal.OCAgentUtils;
import org.wso2.carbon.oc.agent.publisher.OCDataPublisher;

import javax.xml.ws.http.HTTPException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows to publish real time data to oc web app
 * This is the default publisher
 */
public class RTPublisher implements OCDataPublisher {

	private static final String REGISTRATION_PATH = "/api/register";
	private static final String SYNCHRONIZATION_PATH = "/api/update";
	private static final String CONTENT_TYPE = "application/json";
	private static final String CHARACTER_SET = "UTF-8";
	private static Logger logger = LoggerFactory.getLogger(RTPublisher.class);
	private ObjectMapper objectMapper = new ObjectMapper();
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

	@Override public void init(Map<String, String> configMap) {
		// get set config
		String username = configMap.get(RTConstants.USERNAME);
		String password = configMap.get(RTConstants.PASSWORD);
		this.ocUrl = configMap.get(RTConstants.REPORT_URL);

		this.interval = Long.parseLong(configMap.get(RTConstants.INTERVAL));

		if (StringUtils.isBlank(this.ocUrl)) {
			throw new IllegalArgumentException("Operations Center URL is unspecified.");
		}
		this.httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
		this.httpClient.getState().setCredentials(AuthScope.ANY,
		                                          new UsernamePasswordCredentials(username,
		                                                                          password));
		this.httpClient.getParams().setAuthenticationPreemptive(true);
		logger.info("RTPublisher init done");
	}

	@Override
	public void publish(Map<String, Object> dataMap) {
		logger.info("======real-time===========reporting");

		if (!isRegistered) {
			register(dataMap);
		} else {
			sync(dataMap);
		}
	}

	/**
	 * send the real time registration message
	 */
	private void register(Map<String, Object> dataMap) {

		String jsonString = RTMessageUtil.getRegistrationRequestMessage(dataMap);

		String responseBody =
				null;
		try {
			responseBody =
					sendPostRequest(ocUrl + REGISTRATION_PATH, jsonString, HttpStatus.SC_CREATED);
		} catch (IOException e) {
			logger.error("RTPublisher connection down while registering: ", e);
			isRegistered = false;
		}
		if (responseBody != null && responseBody.length() > 0) {
			Map<String, String> regResMap;
			try {

				regResMap = objectMapper
						.readValue(responseBody, new TypeReference<HashMap<String, String>>() {
						});

				isRegistered = true;
			} catch (IOException e) {
				logger.error("Failed to read values from RegistrationResponse", e);
				isRegistered = false;
			}

//			if (regResMap != null) {
//				isRegistered = true;
//
//				logger.info("Registered in Operations Center successfully.");
//			} else {
//				logger.error("Unable receive JSON registration response.");
//			}
		}
	}

	/**
	 * send the real time synchronization message
	 */
	private void sync(Map<String, Object> dataMap) {

		String jsonString = RTMessageUtil.getSynchronizationRequestMessage(dataMap);

		String responseBody =
				null;
		try {
			responseBody =
					sendPostRequest(ocUrl + SYNCHRONIZATION_PATH, jsonString, HttpStatus.SC_OK);
		} catch (IOException e) {
			logger.error("RTPublisher connection down while sync messaging: ", e);
			isRegistered = false;
		}
		if (responseBody != null && responseBody.length() > 0) {
			Map<String, String> synResMap;
			try {
				synResMap = objectMapper
						.readValue(responseBody, new TypeReference<HashMap<String, String>>() {
						});
				isRegistered = true;
			} catch (IOException e) {
				logger.error("Failed to read values from SynchronizationResponse", e);
				isRegistered = false;
				return;
			}

			if (synResMap != null) {
				if ("updated".equals(synResMap.get("status"))) {
					String command = synResMap.get("command");
					logger.info("Executing command. [Command:" + command + "]");
					OCAgentUtils.performAction(command);
				} else if ("error".equals(synResMap.get(""))) {
					logger.error("Unable to synchronize properly.");
				}

			} else {
				logger.error("Unable receive JSON synchronization response.");
			}
		}
	}

	/**
	 * Send basic post request
	 *
	 * @param url      - operations center url
	 * @param request  - json string request message
	 * @param expected - expected http status code
	 * @return
	 */
	public String sendPostRequest(String url, String request, int expected) throws IOException {
		PostMethod postMethod = new PostMethod(url);
		try {
			RequestEntity entity = new StringRequestEntity(request, CONTENT_TYPE, CHARACTER_SET);
			postMethod.setRequestEntity(entity);
			if (logger.isTraceEnabled()) {
				logger.trace("Sending POST request. " + request);
			}
			//try {
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
			//} catch (IOException e) {
				//logger.error("RTPublisher connection down: ", e);
			//}
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