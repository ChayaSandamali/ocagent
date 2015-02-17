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
import org.wso2.carbon.oc.internal.OperationsCenterAgentDataExtractor;
import org.wso2.carbon.oc.internal.OperationsCenterAgentDataHolder;
import org.wso2.carbon.oc.internal.exceptions.ParameterUnavailableException;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This helps to build appropriate messages according to different
 * publishers
 */
public class MessageHelper {
	private static Logger logger = LoggerFactory.getLogger(MessageHelper.class);
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
			message = objectMapper.writeValueAsString(ocRegistrationRequestBuilder());

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
			message = objectMapper.writeValueAsString(ocSynchronizationRequestBuilder());

		} catch (IOException e) {
			logger.error("Failed to get JSON String from ocSynchronizationRequest", e);
		}
		return message;
	}

	/**
	 *
	 * @return String - message broker registration message
	 */
	public static String getMBRegistrationRequest() {
		String message = null;

		OCEvent event = new OCEvent();
		event.setPayload(ocRegistrationRequestBuilder().getRegistrationRequest());

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
	public static String getMBSynchronizationRequest() {
		String message = null;

		OCEvent event = new OCEvent();
		event.setPayload(ocSynchronizationRequestBuilder().getSynchronizationRequest());

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
		RegistrationRequest r = ocRegistrationRequestBuilder().getRegistrationRequest();
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
		SynchronizationRequest s = ocSynchronizationRequestBuilder().getSynchronizationRequest();
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
						.readTree(objectMapper.writeValueAsString(ocRegistrationRequestBuilder()));
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
						objectMapper.writeValueAsString(ocSynchronizationRequestBuilder()));
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
	private static OCRegistrationRequest ocRegistrationRequestBuilder() {
		OCRegistrationRequest ocRegistrationRequest = new OCRegistrationRequest();

		try {
			ocRegistrationRequest.getRegistrationRequest().
					setIp(OperationsCenterAgentDataExtractor.getInstance().getLocalIp());
			ocRegistrationRequest.getRegistrationRequest().
					setServerName(OperationsCenterAgentDataExtractor.getInstance().getServerName());
			ocRegistrationRequest.getRegistrationRequest().
					setServerVersion(
							OperationsCenterAgentDataExtractor.getInstance().getServerVersion());
			ocRegistrationRequest.getRegistrationRequest().
					setDomain(OperationsCenterAgentDataExtractor.getInstance().getDomain());
			ocRegistrationRequest.getRegistrationRequest().
					setSubDomain(OperationsCenterAgentDataExtractor.getInstance().getSubDomain());
			ocRegistrationRequest.getRegistrationRequest().
					setAdminServiceUrl(
							OperationsCenterAgentDataExtractor.getInstance().getAdminServiceUrl());
			ocRegistrationRequest.getRegistrationRequest().
					setStartTime(
							OperationsCenterAgentDataExtractor.getInstance().getServerStartTime());
			ocRegistrationRequest.getRegistrationRequest().
					setOs(OperationsCenterAgentDataExtractor.getInstance().getOs());
			ocRegistrationRequest.getRegistrationRequest().
					setTotalMemory(
							OperationsCenterAgentDataExtractor.getInstance().getTotalMemory());
			ocRegistrationRequest.getRegistrationRequest().
					setCpuCount(OperationsCenterAgentDataExtractor.getInstance().getCpuCount());
			ocRegistrationRequest.getRegistrationRequest().
					setCpuSpeed(OperationsCenterAgentDataExtractor.getInstance().getCpuSpeed());

			List<String> patches = OperationsCenterAgentDataExtractor.getInstance().getPatches();
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
	private static OCSynchronizationRequest ocSynchronizationRequestBuilder() {
		OCSynchronizationRequest ocSynchronizationRequest = new OCSynchronizationRequest();

		try {
			ocSynchronizationRequest.getSynchronizationRequest().
					setAdminServiceUrl(
							OperationsCenterAgentDataExtractor.getInstance().getAdminServiceUrl());
			ocSynchronizationRequest.getSynchronizationRequest().
					setServerUpTime(
							OperationsCenterAgentDataExtractor.getInstance().getServerUpTime());
			ocSynchronizationRequest.getSynchronizationRequest().
					setThreadCount(
							OperationsCenterAgentDataExtractor.getInstance().getThreadCount());
			ocSynchronizationRequest.getSynchronizationRequest().
					setFreeMemory(OperationsCenterAgentDataExtractor.getInstance().getFreeMemory());
			ocSynchronizationRequest.getSynchronizationRequest().
					setIdleCpuUsage(
							OperationsCenterAgentDataExtractor.getInstance().getIdelCpuUsage());
			ocSynchronizationRequest.getSynchronizationRequest().
					setSystemCpuUsage(
							OperationsCenterAgentDataExtractor.getInstance().getSystemCpuUsage());
			ocSynchronizationRequest.getSynchronizationRequest().
					setUserCpuUsage(
							OperationsCenterAgentDataExtractor.getInstance().getUserCpuUsage());
			ocSynchronizationRequest.getSynchronizationRequest().
					setSystemLoadAverage(OperationsCenterAgentDataExtractor.getInstance()
					                                                       .getSystemLoadAverage());
			ocSynchronizationRequest.getSynchronizationRequest().
					setTenants(OperationsCenterAgentDataHolder.getInstance().getRealmService()
					                                          .getTenantManager().getAllTenants());

		} catch (ParameterUnavailableException e) {
			logger.error("Failed to read synchronization parameter. ", e);
		} catch (UserStoreException e) {
			logger.info(e.getMessage(), e);
		}
		return ocSynchronizationRequest;
	}
}
