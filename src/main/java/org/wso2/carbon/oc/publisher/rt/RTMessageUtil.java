package org.wso2.carbon.oc.publisher.rt;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.oc.internal.OCAgentDataExtractor;

import java.io.IOException;

/**
 * Real time message utility.
 */
public class RTMessageUtil {
	private static Logger logger = LoggerFactory.getLogger(RTMessageUtil.class);
	private static ObjectMapper objectMapper = new ObjectMapper();  // for json conversion

	/**
	 * This method is used to extract register message
	 * @param dataExtractor - all the server data as getters
	 * @return String - json format
	 */
	static String getRegistrationRequestMessage(OCAgentDataExtractor dataExtractor) {
		String message = null;

		try {
			message = objectMapper
					.writeValueAsString(dataExtractor.getRegistrationRequest());

		} catch (IOException e) {
			logger.error("Failed to get JSON String from ocSynchronizationRequest", e);
		}
		return message;
	}

	/**
	 * This method is used to extract sync message
	 * @param dataExtractor - all the server data as getters
	 * @return String - json format
	 */
	static String getSynchronizationRequestMessage(OCAgentDataExtractor dataExtractor) {
		String message = null;

		try {
			message = objectMapper
					.writeValueAsString(dataExtractor.getSynchronizationRequest());

		} catch (IOException e) {
			logger.error("Failed to get JSON String from ocSynchronizationRequest", e);
		}
		return message;
	}


}
