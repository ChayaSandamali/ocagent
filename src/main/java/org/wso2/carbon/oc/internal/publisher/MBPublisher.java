package org.wso2.carbon.oc.internal.publisher;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.oc.internal.OperationsCenterAgentDataExtractor;
import org.wso2.carbon.oc.internal.exceptions.ParameterUnavailableException;
import org.wso2.carbon.oc.internal.messages.OCSynchronizationRequest;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;


/**
 * Created by noelyahan on 12/22/14.
 */
public class MBPublisher implements IPublisher {


    private static Logger logger = LoggerFactory.getLogger(MBPublisher.class);
    private static MBPublisher mbQueuePublisher;

    public static final String QPID_ICF = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";
    private static final String CF_NAME_PREFIX = "connectionfactory.";
    private static final String QUEUE_NAME_PREFIX = "queue.";
    private static final String CF_NAME = "qpidConnectionfactory";
    String userName = "admin";
    String password = "admin";
    private static String CARBON_CLIENT_ID = "carbon";
    private static String CARBON_VIRTUAL_HOST_NAME = "carbon";
    private static String CARBON_DEFAULT_HOSTNAME = "localhost";
    private static String CARBON_DEFAULT_PORT = "5673";




    public static MBPublisher getInstance(){
        if(mbQueuePublisher ==  null){
            synchronized (MBPublisher.class){
                if(mbQueuePublisher == null){
                    mbQueuePublisher = new MBPublisher();
                }
            }
        }
        return mbQueuePublisher;
    }

    public void sendMessages(String queueName, String jsonMessage){

        try {
            Properties properties = new Properties();
            properties.put(Context.INITIAL_CONTEXT_FACTORY, QPID_ICF);
            properties.put(CF_NAME_PREFIX + CF_NAME, getTCPConnectionURL(userName, password));
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
        /*logger.info("======wso2-mb===========reporting");

        if (RealTimePublisher.isRegister() == false) {
            //sendMessages("RegisterRequest", null);
            sendMessages("UpdateRequest", getSyncMessage());
        } else {
            sendMessages("UpdateRequest", getSyncMessage());
        }*/
    }

    public String getSyncMessage(){
        String syncMessage = null;
        OCSynchronizationRequest ocSynchronizationRequest = new OCSynchronizationRequest();


        try {
            ocSynchronizationRequest.getSynchronizationRequest().
                    setAdminServiceUrl(OperationsCenterAgentDataExtractor.getInstance().getAdminServiceUrl());
            ocSynchronizationRequest.getSynchronizationRequest().
                    setServerUpTime(OperationsCenterAgentDataExtractor.getInstance().getServerUpTime());
            ocSynchronizationRequest.getSynchronizationRequest().
                    setThreadCount(OperationsCenterAgentDataExtractor.getInstance().getThreadCount());
            ocSynchronizationRequest.getSynchronizationRequest().
                    setFreeMemory(OperationsCenterAgentDataExtractor.getInstance().getFreeMemory());
            ocSynchronizationRequest.getSynchronizationRequest().
                    setIdleCpuUsage(OperationsCenterAgentDataExtractor.getInstance().getIdelCpuUsage());
            ocSynchronizationRequest.getSynchronizationRequest().
                    setSystemCpuUsage(OperationsCenterAgentDataExtractor.getInstance().getSystemCpuUsage());
            ocSynchronizationRequest.getSynchronizationRequest().
                    setUserCpuUsage(OperationsCenterAgentDataExtractor.getInstance().getUserCpuUsage());
            ocSynchronizationRequest.getSynchronizationRequest().
                    setSystemLoadAverage(OperationsCenterAgentDataExtractor.getInstance().getSystemLoadAverage());

            //MB

            JSONObject event = new JSONObject();
            JSONObject payloadData = new JSONObject();
            JSONObject data = new JSONObject();
            data.put("adminServiceUrl", ocSynchronizationRequest.getSynchronizationRequest().getAdminServiceUrl());
            data.put("freeMemory", ocSynchronizationRequest.getSynchronizationRequest().getFreeMemory());
            data.put("timeStamp", ocSynchronizationRequest.getSynchronizationRequest().getTimestamp());
            data.put("loadAverage", ocSynchronizationRequest.getSynchronizationRequest().getSystemLoadAverage());
            data.put("serverUpTime", ocSynchronizationRequest.getSynchronizationRequest().getServerUpTime());
            data.put("threadCount", ocSynchronizationRequest.getSynchronizationRequest().getThreadCount());
            data.put("idleUsage", ocSynchronizationRequest.getSynchronizationRequest().getIdleCpuUsage());
            data.put("systemUsage", ocSynchronizationRequest.getSynchronizationRequest().getSystemCpuUsage());
            data.put("cpuUsage", ocSynchronizationRequest.getSynchronizationRequest().getUserCpuUsage());
            payloadData.put("payloadData", data);
            event.put("event", payloadData);
            syncMessage = event.toJSONString();

        } catch (ParameterUnavailableException e) {
            logger.error("Failed to read synchronization parameter. ", e);
        }

        //logger.info(syncMessage);
        return syncMessage;
    }

    public String getTCPConnectionURL(String username, String password) {
        // amqp://{username}:{password}@carbon/carbon?brokerlist='tcp://{hostname}:{port}'
        return new StringBuffer()
                .append("amqp://").append(username).append(":").append(password)
                .append("@").append(CARBON_CLIENT_ID)
                .append("/").append(CARBON_VIRTUAL_HOST_NAME)
                .append("?brokerlist='tcp://").append(CARBON_DEFAULT_HOSTNAME).append(":").append(CARBON_DEFAULT_PORT).append("'")
                .toString();
    }
}
