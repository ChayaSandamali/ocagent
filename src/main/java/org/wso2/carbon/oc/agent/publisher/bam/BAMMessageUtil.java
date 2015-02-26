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

package org.wso2.carbon.oc.agent.publisher.bam;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.oc.agent.internal.OCAgentConstants;

import java.io.IOException;
import java.util.*;

/**
 * BAM message utility
 */
public class BAMMessageUtil {
	private static Logger logger = LoggerFactory.getLogger(BAMMessageUtil.class);
	private static ObjectMapper objectMapper = new ObjectMapper();  // for json conversion
	private static String bamRegisterPayloadDef;// BAM registration message payload definition
	private static String bamSyncPayloadDef;// BAM synchronization message payload definition

	//stream names
	private static final String REGISTER_STREAM = "RegisterStream";
	private static final String SYNC_STREAM = "SyncStream";


	private BAMMessageUtil() {}

	/**
	 * @return String - register message stream definition json
	 */
	static String getRegisterStreamDef(Map<String, Object> dataMap) {
		return "{" +
		       "  'name':'" + REGISTER_STREAM + "'," +
		       "  'description': 'Storing OC server register request'," +
		       "  'tags':['update', 'request', 'up_request']," +
		       "  'metaData':[" +
		       "               " +
		       "  ]," +
		       "  'payloadData':[" +
		       BAMMessageUtil
				       .getBAMRegPayloadDef(BAMMessageUtil.getBAMRegistrationDataMap(dataMap)) +
		       "  ]" +
		       "}";
	}

	/**
	 * @return String - synchronize message stream definition json
	 */
	static String getSynchronizeStreamDef(Map<String, Object> dataMap) {
		return "{" +
		       "  'name':'" + SYNC_STREAM + "'," +
		       "  'description': 'Storing OC server update request'," +
		       "  'tags':['update', 'request', 'up_request']," +
		       "  'metaData':[" +
		       "               " +
		       "  ]," +
		       "  'payloadData':[" +
		       BAMMessageUtil.getBAMSyncPayloadDef(
				       BAMMessageUtil.getBAMSynchronizationDataMap(dataMap)) +
		       "  ]" +
		       "}";
	}

	/**
	 * This builds the final BAM registration json message
	 * @param dataMap - all oc data map
	 * @return object array of individual values
	 */
	static Object[] getBAMRegistrationRequestMessage(Map<String, Object> dataMap) {

		Map<String, Object> map = dataMap;
		List<Object> objList = new ArrayList<Object>();
		String keySequence[] = {
				OCAgentConstants.SYSTEM_OS, OCAgentConstants.SYSTEM_CPU_COUNT,
				OCAgentConstants.SYSTEM_CPU_SPEED, OCAgentConstants.SYSTEM_TOTAL_MEMORY,
				OCAgentConstants.SERVER_SUBDOMAIN, OCAgentConstants.SERVER_VERSION,
				OCAgentConstants.SERVER_NAME, OCAgentConstants.SERVER_ADMIN_SERVICE_URL,
				OCAgentConstants.SYSTEM_LOCAL_IP, OCAgentConstants.SERVER_START_TIME,
				OCAgentConstants.SERVER_TIMESTAMP, OCAgentConstants.SERVER_PATCHES,
				OCAgentConstants.SERVER_DOMAIN };

		for (String key : keySequence) {
			if (OCAgentConstants.SERVER_PATCHES.equals(key)) {
				objList.add(" ");
				continue;
			}
			Object temp = map.get(key);
			if (isNumber(temp.toString())) {
				objList.add(Double.parseDouble(temp.toString()));
			} else {
				objList.add(temp.toString());
			}
		}
		return objList.toArray();
	}


	/**
	 * This builds the final BAM synchronize json message
	 * @param dataMap - all oc data map
	 * @return object array of individual values
	 */
	static Object[] getBAMSynchronizationRequestMessage(Map<String, Object> dataMap) {

		Map<String, Object> map = dataMap;
		List<Object> objList = new ArrayList<Object>();
		String keySequence[] = {
				OCAgentConstants.SERVER_TIMESTAMP, OCAgentConstants.SYSTEM_USER_CPU_USAGE,
				OCAgentConstants.SYSTEM_IDLE_CPU_USAGE, OCAgentConstants.SYSTEM_LOAD_AVERAGE,
				OCAgentConstants.SERVER_TENANTS, OCAgentConstants.SERVER_THREAD_COUNT,
				OCAgentConstants.SYSTEM_FREE_MEMORY, OCAgentConstants.SYSTEM_SYSTEM_CPU_USAGE,
				OCAgentConstants.SERVER_UPTIME, OCAgentConstants.SERVER_ADMIN_SERVICE_URL };

		for (String key : keySequence) {
			if (OCAgentConstants.SERVER_TENANTS.equals(key)) {
				objList.add(" ");
				continue;
			}
			Object temp = map.get(key);
			if (isNumber(temp.toString())) {
				objList.add(Double.parseDouble(temp.toString()));
			} else {
				objList.add(temp.toString());
			}
		}

		return objList.toArray();
	}

	private static String getBAMPayloadDef(Map<String, Object> syncDataMap) {
		Iterator i = syncDataMap.entrySet().iterator();

		List<Map<String, Object>> resultMapList = new ArrayList<Map<String, Object>>();

		while (i.hasNext()) {
			Map<String, Object> tempMap = new HashMap<String, Object>();
			Map.Entry<String, Object> entry = (Map.Entry<String, Object>) i.next();
			String key = entry.getKey();
			Object value = entry.getValue();
			String dataType = null;

			if (isNumber(value.toString())) {
				dataType = "DOUBLE";
			} else {
				dataType = "STRING";
			}

			tempMap.put("name", key);
			tempMap.put("type", dataType);
			resultMapList.add(tempMap);
		}

		String jsonStr = null;
		try {
			jsonStr = objectMapper.writeValueAsString(resultMapList);
		} catch (IOException e) {
			logger.info("Cannot convert the payload definition", e);
		}
		jsonStr = jsonStr.replaceAll("\\[|\\]", "");

		return jsonStr;
	}

	/**
	 * This builds the registration message from all oc data map
	 * @param dataMap - all oc data map
	 * @return Map<String, Object> - filtered static oc data map
	 */
	private static Map<String, Object> getBAMRegistrationDataMap(Map<String, Object> dataMap) {

		Map<String, Object> regDataMap = new HashMap<String, Object>();

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

		return regDataMap;
	}

	/**
	 * This builds the synchronize message from all oc data map
	 * @param dataMap - all oc data map
	 * @return Map<String, Object> - filtered dynamic oc data map
	 */
	private static Map<String, Object> getBAMSynchronizationDataMap(Map<String, Object> dataMap) {
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

		return syncDataMap;
	}

	/**
	 * This builds the registration payload structure
	 * @param regDataMap - registration data map
	 * @return String - json
	 */
	private static String getBAMRegPayloadDef(Map<String, Object> regDataMap) {
		if(bamRegisterPayloadDef == null) {
			bamRegisterPayloadDef = getBAMPayloadDef(regDataMap);
		}
		return bamRegisterPayloadDef;
	}


	/**
	 * This builds the synchronize payload structure
	 * @param syncDataMap - synchronize data map
	 * @return String - json
	 */
	private static String getBAMSyncPayloadDef(Map<String, Object> syncDataMap) {
		if(bamSyncPayloadDef == null) {
			bamSyncPayloadDef = getBAMPayloadDef(syncDataMap);
		}
		return bamSyncPayloadDef;
	}

	/**
	 * This is a helper method to identify the
	 * basic data types [STRING, DOUBLE]
	 *
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



}