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
 * Created by noelyahan on 12/22/14.
 */
public class MBPublisher implements IPublisher {


    private static Logger logger = LoggerFactory.getLogger(MBPublisher.class);
    private static MBPublisher mbQueuePublisher;

    private static final String REG_QUEUE = "RegisterRequest";
    private static final String SYNC_QUEUE = "UpdateRequest";

    private static final String QPID_ICF = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";
    private static final String CF_NAME_PREFIX = "connectionfactory.";
    private static final String QUEUE_NAME_PREFIX = "queue.";
    private static final String CF_NAME = "qpidConnectionfactory";

    private static String CARBON_CLIENT_ID = "carbon";
    private static String CARBON_VIRTUAL_HOST_NAME = "carbon";
    private String defaultHostName;
    private String defaultPort;
    private String username;
    private String password;
    private long initialDelay;
    private long interval;



    public MBPublisher() {
        Map<String, String> configMap = OperationsCenterAgentUtils.getPublisher(MBPublisher.class.getCanonicalName());
        this.username = configMap.get(OperationsCenterAgentUtils.USERNAME).toString();
        this.password = configMap.get(OperationsCenterAgentUtils.PASSWORD).toString();
        this.defaultHostName = configMap.get(OperationsCenterAgentUtils.REPORT_HOST_NAME).toString();
        this.defaultPort = configMap.get(OperationsCenterAgentUtils.REPORT_PORT).toString();
        this.initialDelay = Long.parseLong(configMap.get(OperationsCenterAgentUtils.DELAY).toString());
        this.interval = Long.parseLong(configMap.get(OperationsCenterAgentUtils.INTERVAL).toString());
        logger.info("MBPublisher init done");
    }

    public void sendMessages(String queueName, String jsonMessage){

        try {
            Properties properties = new Properties();
            properties.put(Context.INITIAL_CONTEXT_FACTORY, QPID_ICF);
            properties.put(CF_NAME_PREFIX + CF_NAME, getTCPConnectionURL(username, password));
            properties.put(QUEUE_NAME_PREFIX + queueName, queueName);
//            System.out.println("getTCPConnectionURL(userName,password) = " + getTCPConnectionURL(userName, password));
            InitialContext ctx = new InitialContext(properties);
            // Lookup connection factory
            QueueConnectionFactory connFactory = (QueueConnectionFactory) ctx.lookup(CF_NAME);
            QueueConnection queueConnection = connFactory.createQueueConnection();
            queueConnection.start();
            QueueSession queueSession =
                    queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
            // Send message
            Queue queue = (Queue)ctx.lookup(queueName);
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
        sendMessages(SYNC_QUEUE, MessageHelper.getMBSynchronizationRequest());
//        sendMessages(REG_QUEUE, getSyncMessage());
//        logger.info(MessageHelper.getMBSynchronizationRequest());
//        logger.info(MessageHelper.getMBRegistrationRequest());
    }

    @Override
    public long getInitialDelay() {
        return initialDelay;
    }

    @Override
    public long getInterval() {
        return interval;
    }



    public String getTCPConnectionURL(String username, String password) {
        // amqp://{username}:{password}@carbon/carbon?brokerlist='tcp://{hostname}:{port}'
        return new StringBuffer()
                .append("amqp://").append(username).append(":").append(password)
                .append("@").append(CARBON_CLIENT_ID)
                .append("/").append(CARBON_VIRTUAL_HOST_NAME)
                .append("?brokerlist='tcp://").append(defaultHostName).append(":").append(defaultPort).append("'")
                .toString();
    }
}
