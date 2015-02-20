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

package org.wso2.carbon.oc.internal.messages;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.oc.internal.OCAgentDataExtractor;
import org.wso2.carbon.oc.internal.exceptions.ParameterUnavailableException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This helps to build appropriate messages according to different
 * publishers
 */
public class MessageUtil {
	private static Logger logger = LoggerFactory.getLogger(MessageUtil.class);
	private static ObjectMapper objectMapper = new ObjectMapper();  // for json conversion
	private static String bamRegisterPayloadDef;                    // BAM registration message payload definition
	private static String bamSyncPayloadDef;                        // BAM synchronization message payload definition

	/**
	 *
	 * @return String - real time registration request message
	 */
	public static String getRTRegistrationRequest() {
		String message = null;

		try {
			message = objectMapper.writeValueAsString(MessageUtil.getOCRegistrationRequest());

		} catch (IOException e) {
			logger.error("Failed to get JSON String from ocSynchronizationRequest", e);
		}
		return message;
	}

	/**
	 *
	 * @return String - real time synchronization message
	 */
	public static String getRTSynchronizationRequest() {
		String message = null;

		try {
			message = objectMapper.writeValueAsString(MessageUtil.getOCSynchronizationRequest());

		} catch (IOException e) {
			logger.error("Failed to get JSON String from ocSynchronizationRequest", e);
		}
		return message;
	}

	/**
	 *
	 * @return String - message broker registration message
	 */
	public static String getRegistrationPayload() {
		String message = null;

		OCEvent event = new OCEvent();
		event.setPayload(MessageUtil.getOCRegistrationRequest().getRegistrationRequest());

		try {
			message = objectMapper.writeValueAsString(event);

		} catch (IOException e) {
			logger.error("Failed to get JSON String from ocSynchronizationRequest", e);
		}
		return message;
	}

	/**
	 *
	 * @return String - message broker synchronization message
	 */
	public static String getSynchronizationPayload() {
		String message = null;

		OCEvent event = new OCEvent();
		event.setPayload(MessageUtil.getOCSynchronizationRequest().getSynchronizationRequest());

		try {
			message = objectMapper.writeValueAsString(event);

		} catch (IOException e) {
			logger.error("Failed to get JSON String from ocSynchronizationRequest", e);
		}

		return message;
	}

	/**
	 *
	 * @return Object[] - BAM registration message
	 */
	public static Object[] getBAMRegistrationRequest() {
		RegistrationRequest r = MessageUtil.getOCRegistrationRequest().getRegistrationRequest();
		return new Object[] { r.getIp(), r.getServerName(), r.getServerVersion(), r.getDomain(),
		                      r.getSubDomain(), r.getAdminServiceUrl(), r.getStartTime(), r.getOs(),
		                      r.getTotalMemory(), Double.parseDouble("" + r.getCpuCount()),
		                      r.getCpuSpeed(), 12342143.53, "" };
	}

	/**
	 *
	 * @return Object[] - BAM synchronization message
	 */
	public static Object[] getBAMSynchronizationRequest() {
		SynchronizationRequest s = MessageUtil.getOCSynchronizationRequest().getSynchronizationRequest();
		return new Object[] { s.getFreeMemory(), s.getIdleCpuUsage(), s.getSystemCpuUsage(),
		                      s.getUserCpuUsage(),
		                      s.getAdminServiceUrl(), s.getServerUpTime(),
		                      Double.parseDouble("" + s.getThreadCount()),
		                      s.getSystemLoadAverage(), Double.parseDouble("" + s.getTimestamp()),
		                      ""
		};
	}

	/**
	 * This helps to build the payload definition
	 * @return String - BAM registration payload definition
	 */
	public static String getBAMRegisterPayloadDef() {
		JsonNode root = null;
		if (bamRegisterPayloadDef == null) {
			try {
				root = objectMapper
						.readTree(objectMapper.writeValueAsString(
								MessageUtil.getOCRegistrationRequest()));
				Map<String, String> flat = new HashMap<String, String>();
				StringBuilder streamIdBuilder = new StringBuilder();
				bamRegisterPayloadDef = traverse(root, flat, streamIdBuilder);
				bamRegisterPayloadDef =
						bamRegisterPayloadDef.substring(0, bamRegisterPayloadDef.length() - 1);
			} catch (IOException e) {
				logger.info(e.getMessage(), e);
			}
		}
		return bamRegisterPayloadDef;
	}

	/**
	 * This helps to build the payload definition
	 * @return String - BAM synchronization payload definition
	 */
	public static String getBAMSyncPayloadDef() {
		JsonNode root = null;
		if (bamSyncPayloadDef == null) {
			try {
				root = objectMapper.readTree(
						objectMapper.writeValueAsString(MessageUtil.getOCSynchronizationRequest()));
				Map<String, String> flat = new HashMap<String, String>();
				StringBuilder streamIdBuilder = new StringBuilder();
				bamSyncPayloadDef = traverse(root, flat, streamIdBuilder);
				bamSyncPayloadDef = bamSyncPayloadDef.substring(0, bamSyncPayloadDef.length() - 1);
			} catch (IOException e) {
				logger.info(e.getMessage(), e);
			}
		}
		return bamSyncPayloadDef;
	}

	/**
	 *  This method is a recursive method to iterate through
	 *  json key value pair
	 * @param node - jackson jsoon tree node
	 * @param result - ref of json key, val
	 * @param sb - StringBuilder ref to store payload json format
	 * @return payload json format
	 */
	private static String traverse(JsonNode node, Map<String, String> result, StringBuilder sb) {

		Iterator<Map.Entry<String, JsonNode>> it = node.getFields();
		while (it.hasNext()) {
			Map.Entry<String, JsonNode> entry = it.next();
			JsonNode n = entry.getValue();
			if (n.isObject()) { // if JSON object, traverse recursively
				traverse(n, result, sb);
			} else { // if not, just add as String
				result.put(entry.getKey(), n.asText());
				String dType = "STRING";
				if (isNumber(entry.getValue().toString())) {
					dType = "DOUBLE";
				}

				sb.append("{");
				sb.append("'name':");
				sb.append("'" + entry.getKey() + "'");
				sb.append(",");
				sb.append("'type':");
				sb.append("'" + dType + "'");
				sb.append("}");
				sb.append(",");
			}
		}

		return sb.toString();
	}

	/**
	 * This is a helper method to identify the
	 * basic data types [STRING, DOUBLE]
	 * @param s - String expected number
	 * @return boolean - num / !num
	 */
	private static boolean isNumber(String s) {
		boolean result = false;
		try {
			double d = Double.parseDouble(s);
			result = true;
		} catch (NumberFormatException e) {
			return result;
		}
		return result;
	}

	/**
	 * This method init the registration request message
	 * and fill with appropriate data
	 * @return OCRegistrationRequest - static data / meta data from server
	 */
	private static OCRegistrationRequest getOCRegistrationRequest() {
		OCRegistrationRequest ocRegistrationRequest = new OCRegistrationRequest();

		try {
			ocRegistrationRequest.getRegistrationRequest().
					setIp(OCAgentDataExtractor.getInstance().getLocalIp());
			ocRegistrationRequest.getRegistrationRequest().
					setServerName(OCAgentDataExtractor.getInstance().getServerName());
			ocRegistrationRequest.getRegistrationRequest().
					setServerVersion(
							OCAgentDataExtractor.getInstance().getServerVersion());
			ocRegistrationRequest.getRegistrationRequest().
					setDomain(OCAgentDataExtractor.getInstance().getDomain());
			ocRegistrationRequest.getRegistrationRequest().
					setSubDomain(OCAgentDataExtractor.getInstance().getSubDomain());
			ocRegistrationRequest.getRegistrationRequest().
					setAdminServiceUrl(
							OCAgentDataExtractor.getInstance().getAdminServiceUrl());
			ocRegistrationRequest.getRegistrationRequest().
					setStartTime(
							OCAgentDataExtractor.getInstance().getServerStartTime());
			ocRegistrationRequest.getRegistrationRequest().
					setOs(OCAgentDataExtractor.getInstance().getOs());
			ocRegistrationRequest.getRegistrationRequest().
					setTotalMemory(
							OCAgentDataExtractor.getInstance().getTotalMemory());
			ocRegistrationRequest.getRegistrationRequest().
					setCpuCount(OCAgentDataExtractor.getInstance().getCpuCount());
			ocRegistrationRequest.getRegistrationRequest().
					setCpuSpeed(OCAgentDataExtractor.getInstance().getCpuSpeed());

			List<String> patches = OCAgentDataExtractor.getInstance().getPatches();
			if (patches.size() > 0) {
				ocRegistrationRequest.getRegistrationRequest().setPatches(patches);
			}

		} catch (ParameterUnavailableException e) {
			logger.error("Failed to read registration parameter. ", e);
		}

		return ocRegistrationRequest;
	}

	/**
	 * This method init the synchronization request message
	 * and fill with appropriate data
	 * @return OCRegistrationRequest - dynamic data from server
	 */
	private static OCSynchronizationRequest getOCSynchronizationRequest() {
		OCSynchronizationRequest ocSynchronizationRequest = new OCSynchronizationRequest();

		try {
			ocSynchronizationRequest.getSynchronizationRequest().
					setAdminServiceUrl(
							OCAgentDataExtractor.getInstance().getAdminServiceUrl());
			ocSynchronizationRequest.getSynchronizationRequest().
					setServerUpTime(
							OCAgentDataExtractor.getInstance().getServerUpTime());
			ocSynchronizationRequest.getSynchronizationRequest().
					setThreadCount(
							OCAgentDataExtractor.getInstance().getThreadCount());
			ocSynchronizationRequest.getSynchronizationRequest().
					setFreeMemory(OCAgentDataExtractor.getInstance().getFreeMemory());
			ocSynchronizationRequest.getSynchronizationRequest().
					setIdleCpuUsage(
							OCAgentDataExtractor.getInstance().getIdelCpuUsage());
			ocSynchronizationRequest.getSynchronizationRequest().
					setSystemCpuUsage(
							OCAgentDataExtractor.getInstance().getSystemCpuUsage());
			ocSynchronizationRequest.getSynchronizationRequest().
					setUserCpuUsage(
							OCAgentDataExtractor.getInstance().getUserCpuUsage());
			ocSynchronizationRequest.getSynchronizationRequest().
					setSystemLoadAverage(OCAgentDataExtractor.getInstance()
					                                                       .getSystemLoadAverage());
			ocSynchronizationRequest.getSynchronizationRequest().
					setTenants(OCAgentDataExtractor.getInstance().getAllTenants());

		} catch (ParameterUnavailableException e) {
			logger.error("Failed to read synchronization parameter. ", e);
		}
		return ocSynchronizationRequest;
	}
}
