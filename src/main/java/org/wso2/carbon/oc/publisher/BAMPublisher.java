package org.wso2.carbon.oc.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.exception.*;
import org.wso2.carbon.oc.internal.OperationsCenterAgentUtils;
import org.wso2.carbon.oc.internal.messages.MessageHelper;
import org.wso2.carbon.utils.CarbonUtils;

import java.net.MalformedURLException;
import java.util.Map;

/**
 * Created by noelyahan on 11/10/14.
 */
public class BAMPublisher implements IPublisher{
    static Logger logger =  LoggerFactory.getLogger(BAMPublisher.class);

//    config attributes
    private String username;
    private String password;
    private String defaultHostName;
    private String thriftPort;
    private long initialDelay;
    private long interval;

    //
    private static final String REGISTER_STREAM = "RegisterStream";
    private static final String SYNC_STREAM = "SyncStream";

//    publishing attributes
    private static DataPublisher dataPublisher = null;



    public BAMPublisher() {
        //load xml config
        Map<String, String> configMap = OperationsCenterAgentUtils.getPublisher(BAMPublisher.class.getCanonicalName());
        this.username = configMap.get(OperationsCenterAgentUtils.USERNAME);
        this.password = configMap.get(OperationsCenterAgentUtils.PASSWORD);
        this.defaultHostName = configMap.get(OperationsCenterAgentUtils.REPORT_HOST_NAME);
        this.thriftPort = configMap.get(OperationsCenterAgentUtils.THRIFT_PORT);
        this.initialDelay = Long.parseLong(configMap.get(OperationsCenterAgentUtils.DELAY));
        this.interval = Long.parseLong(configMap.get(OperationsCenterAgentUtils.INTERVAL));

        try {
            synchronized (BAMPublisher.class) {
                if(dataPublisher == null){
                    setTrustStoreParams();
                    dataPublisher = new DataPublisher("tcp://"+defaultHostName+":"+thriftPort, username, password);
                }
            }

        } catch (MalformedURLException e) {
           logger.info(e.getMessage());
        } catch (AgentException e) {
            logger.info(e.getMessage());
        } catch (AuthenticationException e) {
            logger.info(e.getMessage());
        } catch (TransportException e) {
            logger.info(e.getMessage());
        }
        logger.info("MBPublisher init done");
    }

    public String getStreamId(String streamDef) {
        String streamId = null;

            try {
                streamId = dataPublisher.defineStream(streamDef);
            } catch (AgentException e) {
                e.printStackTrace();
            } catch (MalformedStreamDefinitionException e) {
                e.printStackTrace();
            } catch (StreamDefinitionException e) {
                e.printStackTrace();
            } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
                e.printStackTrace();
            }


        return streamId;
    }

    private String getRegisterStreamDef() {
        return "{" +
                "  'name':'"+REGISTER_STREAM+"'," +
                "  'description': 'Storing OC server registration requests'," +
                "  'tags':['register', 'request', 'reg_request']," +
                "  'metaData':[" +
                "               " +
                "  ]," +
                "  'payloadData':[" +
                MessageHelper.getBAMRegisterPayloadDef() +
                "  ]" +
                "}";
    }

    private String getSynchronizeStreamDef() {
        return "{" +
                "  'name':'"+SYNC_STREAM+"'," +
                "  'description': 'Storing OC server update request'," +
                "  'tags':['update', 'request', 'up_request']," +
                "  'metaData':[" +
                "               " +
                "  ]," +
                "  'payloadData':[" +
                    MessageHelper.getBAMSyncPayloadDef() +
                "  ]" +
                "}";
    }


    private void setTrustStoreParams() {
        System.setProperty("javax.net.ssl.trustStore", CarbonUtils.getCarbonHome() + "/repository/resources/security" + "/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
    }

    //REGISTER Request
    public void publishRegisterRequestStreamData(String ip, String serverName, String domain, String subDomain ,String adminServiceUrl, String startTime, String os, double totalMemory, int cpuCount, double cpuSpeed){
        try {
            //streamId, meta data array, correlational data array, payload data array
            dataPublisher.publish(null, null, null, new Object[]{ip, serverName, domain, subDomain,adminServiceUrl, startTime, os, totalMemory, cpuCount, cpuSpeed});

        } catch (AgentException e) {
            logger.info(e.getMessage());
        }
    }

    @Override
    public void publish() {

        logger.info("==========wso2-bam==========reporting");

//        publishRegisterRequestStreamData("12312414", "hello.noel", "wso2.com", "admin142353464323", "12:30", "32323","Windows", 124135.23, 4, 3.4);

        try {
            dataPublisher.publish(getStreamId(getRegisterStreamDef()), null, null, MessageHelper.getBAMRegistrationRequest());
            dataPublisher.publish(getStreamId(getSynchronizeStreamDef()), null, null, MessageHelper.getBAMSynchronizationRequest());



        } catch (AgentException e) {
            logger.info(e.getMessage());
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
}
