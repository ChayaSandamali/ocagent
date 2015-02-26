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

package org.wso2.carbon.oc.agent.publisher.mb;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.oc.agent.internal.OCAgentConstants;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * MB utility
 */
public class MBMessageUtil {
	private static Logger logger = LoggerFactory.getLogger(MBMessageUtil.class);
	private static ObjectMapper objectMapper = new ObjectMapper(); // for json conversion

	private MBMessageUtil() {
	}

	/**
	 * This method builds static event payload message
	 *
	 * @param dataMap Map<String, Object> - all server data
	 * @return String - json string
	 */
	static String getRegistrationPayload(Map<String, Object> dataMap) {

		//root map
		Map<String, Map<String, Map<String, Object>>> root =
				new HashMap<String, Map<String, Map<String, Object>>>();
		//event map
		Map<String, Map<String, Object>> event = new HashMap<String, Map<String, Object>>();
		//payload map
		Map<String, Object> payload = new HashMap<String, Object>();

		payload.put(OCAgentConstants.SYSTEM_LOCAL_IP,
		            dataMap.get(OCAgentConstants.SYSTEM_LOCAL_IP));
		payload.put(OCAgentConstants.SERVER_NAME, dataMap.get(OCAgentConstants.SERVER_NAME));
		payload.put(OCAgentConstants.SERVER_VERSION, dataMap.get(OCAgentConstants.SERVER_VERSION));
		payload.put(OCAgentConstants.SERVER_DOMAIN, dataMap.get(OCAgentConstants.SERVER_DOMAIN));
		payload.put(OCAgentConstants.SERVER_SUBDOMAIN,
		            dataMap.get(OCAgentConstants.SERVER_SUBDOMAIN));
		payload.put(OCAgentConstants.SERVER_ADMIN_SERVICE_URL,
		            dataMap.get(OCAgentConstants.SERVER_ADMIN_SERVICE_URL));
		payload.put(OCAgentConstants.SERVER_START_TIME,
		            dataMap.get(OCAgentConstants.SERVER_START_TIME));
		payload.put(OCAgentConstants.SYSTEM_OS, dataMap.get(OCAgentConstants.SYSTEM_OS));
		payload.put(OCAgentConstants.SYSTEM_TOTAL_MEMORY,
		            dataMap.get(OCAgentConstants.SYSTEM_TOTAL_MEMORY));
		payload.put(OCAgentConstants.SYSTEM_CPU_COUNT,
		            dataMap.get(OCAgentConstants.SYSTEM_CPU_COUNT));
		payload.put(OCAgentConstants.SYSTEM_CPU_SPEED,
		            dataMap.get(OCAgentConstants.SYSTEM_CPU_SPEED));
		payload.put(OCAgentConstants.SERVER_TIMESTAMP,
		            dataMap.get(OCAgentConstants.SERVER_TIMESTAMP));
		payload.put(OCAgentConstants.SERVER_PATCHES, dataMap.get(OCAgentConstants.SERVER_PATCHES));

		event.put("payload", payload);
		root.put("event", event);

		String message = null;

		try {
			message = objectMapper.writeValueAsString(root);

		} catch (IOException e) {
			logger.error("Failed to get JSON String from ocSynchronizationRequest", e);
		}
		return message;
	}

	/**
	 * This method builds dynamic event payload message
	 *
	 * @param dataMap Map<String, Object> - all server data
	 * @return String - json string
	 */
	static String getSynchronizationPayload(Map<String, Object> dataMap) {

		//root map
		Map<String, Map<String, Map<String, Object>>> root =
				new HashMap<String, Map<String, Map<String, Object>>>();
		//event map
		Map<String, Map<String, Object>> event = new HashMap<String, Map<String, Object>>();
		//payload map
		Map<String, Object> payload = new HashMap<String, Object>();

		payload.put(OCAgentConstants.SERVER_TENANTS, dataMap.get(OCAgentConstants.SERVER_TENANTS));
		payload.put(OCAgentConstants.SERVER_TIMESTAMP,
		            dataMap.get(OCAgentConstants.SERVER_TIMESTAMP));
		payload.put(OCAgentConstants.SYSTEM_LOAD_AVERAGE,
		            dataMap.get(OCAgentConstants.SYSTEM_LOAD_AVERAGE));
		payload.put(OCAgentConstants.SERVER_THREAD_COUNT,
		            dataMap.get(OCAgentConstants.SERVER_THREAD_COUNT));
		payload.put(OCAgentConstants.SERVER_UPTIME, dataMap.get(OCAgentConstants.SERVER_UPTIME));
		payload.put(OCAgentConstants.SERVER_ADMIN_SERVICE_URL,
		            dataMap.get(OCAgentConstants.SERVER_ADMIN_SERVICE_URL));
		payload.put(OCAgentConstants.SYSTEM_USER_CPU_USAGE,
		            dataMap.get(OCAgentConstants.SYSTEM_USER_CPU_USAGE));
		payload.put(OCAgentConstants.SYSTEM_SYSTEM_CPU_USAGE,
		            dataMap.get(OCAgentConstants.SYSTEM_SYSTEM_CPU_USAGE));
		payload.put(OCAgentConstants.SYSTEM_IDLE_CPU_USAGE,
		            dataMap.get(OCAgentConstants.SYSTEM_IDLE_CPU_USAGE));
		payload.put(OCAgentConstants.SYSTEM_FREE_MEMORY,
		            dataMap.get(OCAgentConstants.SYSTEM_FREE_MEMORY));

		event.put("payload", payload);
		root.put("event", event);

		String message = null;

		try {
			message = objectMapper.writeValueAsString(root);

		} catch (IOException e) {
			logger.error("Failed to get JSON String from ocSynchronizationRequest", e);
		}

		return message;
	}

}