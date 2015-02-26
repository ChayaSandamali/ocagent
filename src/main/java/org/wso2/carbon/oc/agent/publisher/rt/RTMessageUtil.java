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

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.oc.agent.internal.OCAgentConstants;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Real time message utility.
 */
public class RTMessageUtil {
	private static Logger logger = LoggerFactory.getLogger(RTMessageUtil.class);
	private static ObjectMapper objectMapper = new ObjectMapper();  // for json conversion

	private RTMessageUtil() {
	}

	/**
	 * This method builds dynamic message
	 *
	 * @param dataMap Map<String, Object>  - all server data keys can found @ OCAgentConstants
	 * @return String - json string
	 */
	static String getSynchronizationRequestMessage(Map<String, Object> dataMap) {

		Map<String, Object> syncDataMap = new HashMap<String, Object>();

		syncDataMap
				.put(OCAgentConstants.SERVER_TENANTS, dataMap.get(OCAgentConstants.SERVER_TENANTS));
		syncDataMap.put(OCAgentConstants.SERVER_TIMESTAMP,
		                dataMap.get(OCAgentConstants.SERVER_TIMESTAMP));
		syncDataMap.put(OCAgentConstants.SYSTEM_LOAD_AVERAGE,
		                dataMap.get(OCAgentConstants.SYSTEM_LOAD_AVERAGE));
		syncDataMap.put(OCAgentConstants.SERVER_THREAD_COUNT,
		                dataMap.get(OCAgentConstants.SERVER_THREAD_COUNT));
		syncDataMap
				.put(OCAgentConstants.SERVER_UPTIME, dataMap.get(OCAgentConstants.SERVER_UPTIME));
		syncDataMap.put(OCAgentConstants.SERVER_ADMIN_SERVICE_URL,
		                dataMap.get(OCAgentConstants.SERVER_ADMIN_SERVICE_URL));
		syncDataMap.put(OCAgentConstants.SYSTEM_USER_CPU_USAGE,
		                dataMap.get(OCAgentConstants.SYSTEM_USER_CPU_USAGE));
		syncDataMap.put(OCAgentConstants.SYSTEM_SYSTEM_CPU_USAGE,
		                dataMap.get(OCAgentConstants.SYSTEM_SYSTEM_CPU_USAGE));
		syncDataMap.put(OCAgentConstants.SYSTEM_IDLE_CPU_USAGE,
		                dataMap.get(OCAgentConstants.SYSTEM_IDLE_CPU_USAGE));
		syncDataMap.put(OCAgentConstants.SYSTEM_FREE_MEMORY,
		                dataMap.get(OCAgentConstants.SYSTEM_FREE_MEMORY));

		String message = null;

		try {
			message = objectMapper
					.writeValueAsString(syncDataMap);

		} catch (IOException e) {
			logger.error("Failed to get JSON String from ocSynchronizationRequest", e);
		}
		return message;
	}

	/**
	 * This method builds static message for registration
	 *
	 * @param dataMap Map<String, Object>  - all server data keys can found @ OCAgentConstants
	 * @return String - json string
	 */
	static String getRegistrationRequestMessage(Map<String, Object> dataMap) {
		Map<String, Object> regDataMap = new HashMap<String, Object>();

//		put full var name
		regDataMap.put(OCAgentConstants.SYSTEM_LOCAL_IP,
		               dataMap.get(OCAgentConstants.SYSTEM_LOCAL_IP));
		regDataMap.put(OCAgentConstants.SERVER_NAME, dataMap.get(OCAgentConstants.SERVER_NAME));
		regDataMap
				.put(OCAgentConstants.SERVER_VERSION, dataMap.get(OCAgentConstants.SERVER_VERSION));
		regDataMap.put(OCAgentConstants.SERVER_DOMAIN, dataMap.get(OCAgentConstants.SERVER_DOMAIN));
		regDataMap.put(OCAgentConstants.SERVER_SUBDOMAIN,
		               dataMap.get(OCAgentConstants.SERVER_SUBDOMAIN));
		regDataMap.put(OCAgentConstants.SERVER_ADMIN_SERVICE_URL,
		               dataMap.get(OCAgentConstants.SERVER_ADMIN_SERVICE_URL));
		regDataMap.put(OCAgentConstants.SERVER_START_TIME,
		               dataMap.get(OCAgentConstants.SERVER_START_TIME));
		regDataMap.put(OCAgentConstants.SYSTEM_OS, dataMap.get(OCAgentConstants.SYSTEM_OS));
		regDataMap.put(OCAgentConstants.SYSTEM_TOTAL_MEMORY,
		               dataMap.get(OCAgentConstants.SYSTEM_TOTAL_MEMORY));
		regDataMap.put(OCAgentConstants.SYSTEM_CPU_COUNT,
		               dataMap.get(OCAgentConstants.SYSTEM_CPU_COUNT));
		regDataMap.put(OCAgentConstants.SYSTEM_CPU_SPEED,
		               dataMap.get(OCAgentConstants.SYSTEM_CPU_SPEED));
		regDataMap.put(OCAgentConstants.SERVER_TIMESTAMP,
		               dataMap.get(OCAgentConstants.SERVER_TIMESTAMP));
		regDataMap
				.put(OCAgentConstants.SERVER_PATCHES, dataMap.get(OCAgentConstants.SERVER_PATCHES));

		String message = null;

		try {
			message = objectMapper
					.writeValueAsString(regDataMap);

		} catch (IOException e) {
			logger.error("Failed to get JSON String from ocRegistrationRequest", e);
		}
		return message;
	}

}
