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

package org.wso2.carbon.oc.publisher.mb;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.oc.internal.OCAgentDataExtractor;
import org.wso2.carbon.oc.internal.messages.Event;

import java.io.IOException;

/**
 * MB utility
 */
public class MBMessageUtil {
	private static Logger logger = LoggerFactory.getLogger(MBMessageUtil.class);
	private static ObjectMapper objectMapper = new ObjectMapper(); // for json conversion

	/**
	 * @return String - message broker registration message
	 */
	static String getRegistrationPayload(OCAgentDataExtractor dataExtractor) {
		String message = null;

		Event event = new Event();
		event.setPayload(dataExtractor.getRegistrationRequest());

		try {
			message = objectMapper.writeValueAsString(event);

		} catch (IOException e) {
			logger.error("Failed to get JSON String from ocSynchronizationRequest", e);
		}
		return message;
	}

	/**
	 * @return String - message broker synchronization message
	 */
	static String getSynchronizationPayload(OCAgentDataExtractor dataExtractor) {
		String message = null;

		Event event = new Event();
		event.setPayload(dataExtractor.getSynchronizationRequest());

		try {
			message = objectMapper.writeValueAsString(event);

		} catch (IOException e) {
			logger.error("Failed to get JSON String from ocSynchronizationRequest", e);
		}

		return message;
	}

}