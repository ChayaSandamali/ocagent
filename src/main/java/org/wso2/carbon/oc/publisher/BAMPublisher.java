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

	//config attributes
    private String username;
    private String password;
    private String defaultHostName;
    private String thriftPort;
    private long initialDelay;
    private long interval;
	private boolean isRegistered = false;

    //stream names
    private static final String REGISTER_STREAM = "RegisterStream";
    private static final String SYNC_STREAM = "SyncStream";

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
        logger.info("BAMPublisher init done");
    }

	/**
	 *
	 * @param streamDef - stream definition json string event > payload
	 * @return String - unique generated stream id
	 */
    private String getStreamId(String streamDef) {
        String streamId = null;

            try {
                streamId = dataPublisher.defineStream(streamDef);
            } catch (AgentException e) {
                logger.info(e.getMessage(), e);
            } catch (MalformedStreamDefinitionException e) {
	            logger.info(e.getMessage(), e);
            } catch (StreamDefinitionException e) {
	            logger.info(e.getMessage(), e);
            } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
	            logger.info(e.getMessage(), e);
            }


        return streamId;
    }

	/**
	 *
	 * @return String - register message stream definition json
	 */
    private String getRegisterStreamDef() {
        return "{" +
                "  'name':'"+REGISTER_STREAM+"'," +
                " 'version':'" + 1.1 + "'," +
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

	/**
	 *
	 * @return String - synchronize message stream definition json
	 */
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


    @Override
    public void publish() {

        logger.info("==========wso2-bam==========reporting");
	    try {

	        if(!isRegistered) {
		        dataPublisher.publish(getStreamId(getRegisterStreamDef()), null, null, MessageHelper.getBAMRegistrationRequest());
		        isRegistered = true;
	        }else{
		        dataPublisher.publish(getStreamId(getSynchronizeStreamDef()), null, null, MessageHelper.getBAMSynchronizationRequest());
	        }

        } catch (AgentException e) {
	        logger.info("BAM connection gone");
            logger.info(e.getMessage());
        }
    }

	/**
	 *  stop bam publisher
	 */
	public void stop(){
		dataPublisher.stop();
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
