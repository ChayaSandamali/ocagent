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

package org.wso2.carbon.oc.publisher.bam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.exception.*;
import org.wso2.carbon.oc.internal.OCAgentDataExtractor;
import org.wso2.carbon.oc.publisher.OCDataPublisher;
import org.wso2.carbon.oc.publisher.OCPublisherConstants;
import org.wso2.carbon.utils.CarbonUtils;

import java.net.MalformedURLException;
import java.util.Map;

/**
 * Allows publish data to bam as payload data stream
 */
public class BAMPublisher implements OCDataPublisher {
    static Logger logger =  LoggerFactory.getLogger(BAMPublisher.class);

	//config attributes
    private String username;
    private String password;
    private String defaultHostName;
    private String thriftPort;
    private long interval;
	private boolean isRegistered = false;

    //stream names
    private static final String REGISTER_STREAM = "RegisterStream";
    private static final String SYNC_STREAM = "SyncStream";

	private static DataPublisher dataPublisher = null;



	@Override public void init(Map<String, String> configMap) {
        //load xml config
        this.username = configMap.get(OCPublisherConstants.USERNAME);
        this.password = configMap.get(OCPublisherConstants.PASSWORD);
        this.defaultHostName = configMap.get(OCPublisherConstants.REPORT_HOST_NAME);
        this.thriftPort = configMap.get(OCPublisherConstants.THRIFT_PORT);
        this.interval = Long.parseLong(configMap.get(OCPublisherConstants.INTERVAL));

        try {
            synchronized (BAMPublisher.class) {
                if(dataPublisher == null){
                    setTrustStoreParams();
                    dataPublisher = new DataPublisher("tcp://"+defaultHostName+":"+thriftPort, username, password);
                }

            }

        } catch (MalformedURLException e) {
           logger.info(e.getMessage(), e);
        } catch (AgentException e) {
	        logger.info("BAMPublisher connection down", e);
        } catch (AuthenticationException e) {
	        logger.info(e.getMessage(), e);
        } catch (TransportException e) {
	        logger.info(e.getMessage(), e);
        }
        logger.info("BAMPublisher init done");
    }



	/**
	 *
	 * @param streamDef - stream definition json string event > payload
	 * @return String - unique generated stream id
	 */
    private String getStreamId(String streamDef) throws AgentException{
        String streamId = null;

            try {
                streamId = dataPublisher.defineStream(streamDef);
            }  catch (MalformedStreamDefinitionException e) {
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
    private String getRegisterStreamDef(OCAgentDataExtractor dataExtractor) {
	    return "{" +
	           "  'name':'"+REGISTER_STREAM+"'," +
	           "  'description': 'Storing OC server register request'," +
	           "  'tags':['update', 'request', 'up_request']," +
	           "  'metaData':[" +
	           "               " +
	           "  ]," +
	           "  'payloadData':[" +
	           BAMMessageUtil.getBAMRegisterPayloadDef(dataExtractor) +
	           "  ]" +
	           "}";
    }

	/**
	 *
	 * @return String - synchronize message stream definition json
	 */
    private String getSynchronizeStreamDef(OCAgentDataExtractor dataExtractor) {
        return "{" +
                "  'name':'"+SYNC_STREAM+"'," +
                "  'description': 'Storing OC server update request'," +
                "  'tags':['update', 'request', 'up_request']," +
                "  'metaData':[" +
                "               " +
                "  ]," +
                "  'payloadData':[" +
                    BAMMessageUtil.getBAMSyncPayloadDef(dataExtractor) +
                "  ]" +
                "}";
    }


    private void setTrustStoreParams() {
        System.setProperty("javax.net.ssl.trustStore", CarbonUtils.getCarbonHome() + "/repository/resources/security" + "/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
    }



	@Override
    public void publish(OCAgentDataExtractor dataExtractor) {

        logger.info("==========wso2-bam==========reporting");
	    try {

	        if(!isRegistered) {
		        dataPublisher.publish(getStreamId(getRegisterStreamDef(dataExtractor)), null, null, BAMMessageUtil
				        .getBAMRegistrationRequest(dataExtractor));
		        isRegistered = true;
	        }else{
		        dataPublisher.publish(getStreamId(getSynchronizeStreamDef(dataExtractor)), null, null, BAMMessageUtil
				        .getBAMSynchronizationRequest(dataExtractor));
	        }


        } catch (AgentException e) {
	        logger.info("BAMPublisher connection down", e);
        }
    }

	/**
	 *  stop bam publisher
	 */
	public void stop(){
		dataPublisher.stop();
	}



    @Override
    public long getInterval() {
        return interval;
    }
}