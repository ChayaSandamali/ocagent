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