package org.wso2.carbon.oc.publisher.bam;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.oc.internal.OCAgentDataExtractor;
import org.wso2.carbon.oc.internal.messages.RegistrationRequest;
import org.wso2.carbon.oc.internal.messages.SynchronizationRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * BAM message utility
 */
public class BAMMessageUtil {
	private static Logger logger = LoggerFactory.getLogger(BAMMessageUtil.class);
	private static ObjectMapper objectMapper = new ObjectMapper();  // for json conversion
	private static String bamRegisterPayloadDef;// BAM registration message payload definition
	private static String bamSyncPayloadDef;// BAM synchronization message payload definition

	/**
	 * @return Object[] - BAM registration message
	 */
	public static Object[] getBAMRegistrationRequest(OCAgentDataExtractor dataExtractor) {
		RegistrationRequest r = dataExtractor.getRegistrationRequest();
		return new Object[] { r.getIp(), r.getServerName(), r.getServerVersion(), r.getDomain(),
		                      r.getSubDomain(), r.getAdminServiceUrl(), r.getStartTime(), r.getOs(),
		                      r.getTotalMemory(), Double.parseDouble("" + r.getCpuCount()),
		                      r.getCpuSpeed(), 12342143.53, "" };
	}

	/**
	 * @return Object[] - BAM synchronization message
	 */
	public static Object[] getBAMSynchronizationRequest(OCAgentDataExtractor dataExtractor) {
		SynchronizationRequest s = dataExtractor.getSynchronizationRequest();
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
	 *
	 * @return String - BAM registration payload definition
	 */
	public static String getBAMRegisterPayloadDef(OCAgentDataExtractor dataExtractor) {
		JsonNode root = null;
		if (bamRegisterPayloadDef == null) {
			try {
				root = objectMapper
						.readTree(objectMapper.writeValueAsString(
								dataExtractor.getRegistrationRequest()));
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
	 *
	 * @return String - BAM synchronization payload definition
	 */
	public static String getBAMSyncPayloadDef(OCAgentDataExtractor dataExtractor) {
		JsonNode root = null;
		if (bamSyncPayloadDef == null) {
			try {
				root = objectMapper.readTree(
						objectMapper.writeValueAsString(dataExtractor.getSynchronizationRequest()));
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
	 * This method is a recursive method to iterate through
	 * json key value pair
	 *
	 * @param node   - jackson jsoon tree node
	 * @param result - ref of json key, val
	 * @param sb     - StringBuilder ref to store payload json format
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
