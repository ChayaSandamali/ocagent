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

package org.wso2.carbon.oc.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.oc.internal.OperationsCenterAgentUtils;
import org.wso2.carbon.oc.internal.messages.MessageHelper;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Map;
import java.util.Properties;

/**
 * intro
 */
public class MBPublisher implements IPublisher {

	private static Logger logger = LoggerFactory.getLogger(MBPublisher.class);

	// mb queue names
	private static final String REG_QUEUE = "RegisterRequest";
	private static final String SYNC_QUEUE = "UpdateRequest";

	// mb conf
	private static final String QPID_ICF =
			"org.wso2.andes.jndi.PropertiesFileInitialContextFactory";
	private static final String CF_NAME_PREFIX = "connectionfactory.";
	private static final String QUEUE_NAME_PREFIX = "queue.";
	private static final String CF_NAME = "qpidConnectionfactory";

	private static String CARBON_CLIENT_ID = "carbon";
	private static String CARBON_VIRTUAL_HOST_NAME = "carbon";

	// load from carbon.xml
	private String defaultHostName;
	private String defaultPort;
	private String username;
	private String password;
	private long initialDelay;
	private long interval;

	private boolean isRegistered = false;

	public MBPublisher() {
		//get set config
		Map<String, String> configMap =
				OperationsCenterAgentUtils.getPublisher(MBPublisher.class.getCanonicalName());
		this.username = configMap.get(OperationsCenterAgentUtils.USERNAME);
		this.password = configMap.get(OperationsCenterAgentUtils.PASSWORD);
		this.defaultHostName =
				configMap.get(OperationsCenterAgentUtils.REPORT_HOST_NAME);
		this.defaultPort = configMap.get(OperationsCenterAgentUtils.REPORT_PORT);
		this.initialDelay =
				Long.parseLong(configMap.get(OperationsCenterAgentUtils.DELAY));
		this.interval =
				Long.parseLong(configMap.get(OperationsCenterAgentUtils.INTERVAL));
		logger.info("MBPublisher init done");
	}

	/**
	 *
	 * @param queueName - String mb queue name
	 * @param jsonMessage - String mb queue message json string
	 */
	public void sendMessages(String queueName, String jsonMessage) {

		try {
			Properties properties = new Properties();
			properties.put(Context.INITIAL_CONTEXT_FACTORY, QPID_ICF);
			properties.put(CF_NAME_PREFIX + CF_NAME, getTCPConnectionURL(username, password));
			properties.put(QUEUE_NAME_PREFIX + queueName, queueName);
			InitialContext ctx = new InitialContext(properties);
			// lookup connection factory
			QueueConnectionFactory connFactory = (QueueConnectionFactory) ctx.lookup(CF_NAME);
			QueueConnection queueConnection = connFactory.createQueueConnection();
			queueConnection.start();
			QueueSession queueSession =
					queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
			// send message
			Queue queue = (Queue) ctx.lookup(queueName);
			// create the message to send

			TextMessage textMessage = queueSession.createTextMessage(jsonMessage);
			QueueSender queueSender = queueSession.createSender(queue);
			queueSender.send(textMessage);
			queueSender.close();
			queueSession.close();
			queueConnection.close();
		} catch (JMSException e) {
			logger.info("JMS error", e);
		} catch (NamingException e) {
			logger.info("Naming error", e);
		}

	}

	@Override
	public void publish() {
		logger.info("======wso2-mb===========reporting");
		if (!isRegistered) {
			sendMessages(REG_QUEUE, MessageHelper.getMBRegistrationRequest());
			isRegistered = true;
		} else {
			sendMessages(SYNC_QUEUE, MessageHelper.getMBSynchronizationRequest());
		}
	}

	@Override
	public long getInitialDelay() {
		return initialDelay;
	}

	@Override
	public long getInterval() {
		return interval;
	}

	/**
	 *
	 * @param username
	 * @param password
	 * @return String - conn url
	 */
	public String getTCPConnectionURL(String username, String password) {
		// amqp://{username}:{password}@carbon/carbon?brokerlist='tcp://{hostname}:{port}'
		return new StringBuffer()
				.append("amqp://").append(username).append(":").append(password)
				.append("@").append(CARBON_CLIENT_ID)
				.append("/").append(CARBON_VIRTUAL_HOST_NAME)
				.append("?brokerlist='tcp://").append(defaultHostName).append(":")
				.append(defaultPort).append("'")
				.toString();
	}
}
