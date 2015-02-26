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

package org.wso2.carbon.oc.agent.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.server.admin.service.ServerAdmin;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
    Provide access to configurations in operations-center.xml [Publisher] data
    Allows to invoke commands in server restart, shutdown
*/

public class OCAgentUtils {
	private static Logger logger = LoggerFactory.getLogger(OCAgentUtils.class);

	private static Document ocXmlDocument;

	private static final String GRACEFUL_SHUTDOWN = "GRACEFUL_SHUTDOWN";
	private static final String GRACEFUL_RESTART = "GRACEFUL_RESTART";
	private static final String SHUTDOWN = "SHUTDOWN";
	private static final String RESTART = "RESTART";

	private OCAgentUtils() {
	}

	/**
	 * This method extract active publisher's class path
	 *
	 * @return List<String> - class package path list
	 */
	public static List<Map<String, String>> getActiveOcPublishersList() {
		logger.info("++++++++++++++++++++++++++++++++++++++++++++++==");
		Document doc = OCAgentUtils.getOcXmlDocument();
		List<Map<String, String>> activePublisherList = getNodeMapList(
				eval(doc, OCAgentConstants.OC_PUBLISHER_ROOT_XPATH));
		for (Map<String, String> x: activePublisherList) {
			logger.info(x.get(OCAgentConstants.CLASS));
			logger.info(x.get(OCAgentConstants.NAME));
		}
		logger.info("++++++++++++++++++++++++++++++++++++++++++++++==");
		return activePublisherList;
	}

	/**
	 * @param publisherName - publisher name from oc xml config
	 * @return Map<String, String> - key, val pair of publisher info
	 */
	public static Map<String, String> getOcPublisherConfigMap(String publisherName) {
		Map<String, String> resultMap = null;
		Document doc = OCAgentUtils.getOcXmlDocument();
		List<Map<String, String>> publisherList = getNodeMapList(
				eval(doc, OCAgentConstants.OC_PUBLISHER_ROOT_XPATH));
		for (Map<String, String> publisher : publisherList) {
			if (publisher.get(OCAgentConstants.NAME).equalsIgnoreCase(publisherName)) {
				resultMap = publisher;
				break;
			}
		}
		return resultMap;
	}

	/**
	 * @return Document - operations-center.xml document
	 */
	private static Document getOcXmlDocument() {
		if (ocXmlDocument == null) {
			try {
				File file = new File(CarbonUtils.getCarbonHome() + "/repository/conf/" +
				                     OCAgentConstants.OC_XML);

				DocumentBuilder dBuilder =
						DocumentBuilderFactory.newInstance().newDocumentBuilder();

				ocXmlDocument = dBuilder.parse(file);

			} catch (SAXException e) {
				logger.info(e.getMessage(), e);
			} catch (IOException e) {
				logger.info(e.getMessage(), e);
			} catch (ParserConfigurationException e) {
				logger.info(e.getMessage(), e);
			}
		}
		return ocXmlDocument;
	}

	/**
	 * @param doc     - operations-center.xml document
	 * @param pathStr - xpath
	 * @return NodeList - xml attr, value
	 */
	private static NodeList eval(final Document doc, final String pathStr) {
		NodeList resultList = null;
		try {
			final XPath xpath = XPathFactory.newInstance().newXPath();
			final XPathExpression expr = xpath.compile(pathStr);
			resultList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			logger.info(e.getMessage(), e);
		}
		return resultList;
	}

	/**
	 * @param nodes - xml attr, values
	 * @return List<Map<String, String>> - operations-center.xml attr, value as list of map
	 */
	private static List<Map<String, String>> getNodeMapList(final NodeList nodes) {
		final List<Map<String, String>> out = new ArrayList<Map<String, String>>();
		int len = (nodes != null) ? nodes.getLength() : 0;
		for (int i = 0; i < len; i++) {
			NodeList children = nodes.item(i).getChildNodes();
			Map<String, String> childMap = new HashMap<String, String>();
			for (int j = 0; j < children.getLength(); j++) {
				Node child = children.item(j);
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					childMap.put(child.getNodeName(), child.getTextContent());
				}
			}
			out.add(childMap);
		}
		return out;
	}

	/**
	 * @param command "RESTART", "SHUTDOWN" etc..
	 */

	public static void performAction(String command) {
		ServerAdmin serverAdmin =
				(ServerAdmin) OCAgentDataHolder.getInstance().getServerAdmin();
		if (serverAdmin != null) {
			try {
				if (RESTART.equals(command)) {
					serverAdmin.restart();
				} else if (GRACEFUL_RESTART.equals(command)) {
					serverAdmin.restartGracefully();
				} else if (SHUTDOWN.equals(command)) {
					serverAdmin.shutdown();
				} else if (GRACEFUL_SHUTDOWN.equals(command)) {
					serverAdmin.shutdownGracefully();
				} else {
					logger.error("Unknown command received. [" + command + "]");
				}
			} catch (Exception e) {
				logger.error("Failed while executing command. [" + command + "]", e);
			}
		} else {
			logger.error("Unable to perform action, ServerAdmin is null");
		}
	}
}
