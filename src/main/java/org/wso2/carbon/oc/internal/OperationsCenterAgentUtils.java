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

package org.wso2.carbon.oc.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.server.admin.service.ServerAdmin;

import java.util.*;

/*
    Associate with carbon.xml [OperationsCenter] data
*/

public class OperationsCenterAgentUtils {
    private static Logger logger = LoggerFactory.getLogger(OperationsCenterAgentUtils.class);

    //access from outside
    public static final String IS_ENABLE = "Enable";
    public static final String CLASS_PATH = "Class";
    public static final String REPORT_URL = "ReportURL";
    public static final String REPORT_HOST_NAME = "ReportHostName";
    public static final String REPORT_PORT = "ReportHttpPort";
    public static final String THRIFT_PORT = "ThriftPort";
    public static final String THRIFT_SSL_PORT = "ThriftSSLPort";
    public static final String USERNAME = "Username";
    public static final String PASSWORD = "Password";
    public static final String DELAY = "Reporting.InitialDelay";
    public static final String INTERVAL = "Reporting.Interval";

    private static final List<String> ALLOWED_PUBLISHER_ATTRIBUTES = Arrays
            .asList(IS_ENABLE,
                    CLASS_PATH,
                    REPORT_URL,
                    REPORT_HOST_NAME,
                    REPORT_PORT,
                    THRIFT_PORT,
                    THRIFT_SSL_PORT,
                    USERNAME,
                    PASSWORD,
                    DELAY,
                    INTERVAL);

    private static final List<String> ALLOWED_CLASSES = Arrays
            .asList("org.wso2.carbon.oc.publisher.RTPublisher",
                    "org.wso2.carbon.oc.publisher.MBPublisher",
                    "org.wso2.carbon.oc.publisher.BAMPublisher");

    private static final List<String> ALLOWED_PUBLISHERS = Arrays
            .asList("RTPublisher",
                    "MBPublisher",
                    "BAMPublisher");

    private static Map<String, Map<String, String>> configurations;

    public static ServerConfigurationService getServerConfigurationService() {
        ServerConfigurationService serverConfigurationService =
                OperationsCenterAgentDataHolder.getInstance().getServerConfigurationService();
        if (serverConfigurationService == null) {
            throw new RuntimeException("ServerConfigurationService is unavailable");
        }
        return serverConfigurationService;
    }

    /**
     *
     * @return map of configuration xml data
     */
    private static Map<String, Map<String, String>> getConfigurations() {
        ServerConfigurationService serverConfigurationService = OperationsCenterAgentUtils.getServerConfigurationService();
//        Map<String, Map<String, String>> configurations = new HashMap<String, Map<String, String>>();

        if(configurations == null) {
            configurations = new HashMap<String, Map<String, String>>();
            for (String publisher : ALLOWED_PUBLISHERS) {
                    String publisherPath = "Publishers." + publisher;

                    Map<String, String> configMap = new HashMap<String, String>();
                    for (String attr : ALLOWED_PUBLISHER_ATTRIBUTES) {
                        String value = serverConfigurationService.getFirstProperty(publisherPath+"."+attr);
                        if(value != null)
                            configMap.put(attr, value);
                    }

                    configurations.put(configMap.get(CLASS_PATH), configMap);
            }
        }

        return configurations;
    }



    /**
     *
     * @return list of class path
     */
    public static List<String> getActivePublishers() {
        List<String> activePublishers = new ArrayList<String>();

        for(int i = 0; i < ALLOWED_CLASSES.size(); i++) {
            Map<String, String> configMap = OperationsCenterAgentUtils.getConfigurations().get(ALLOWED_CLASSES.get(i));
            if(Boolean.parseBoolean(configMap.get(IS_ENABLE))) {
                activePublishers.add(ALLOWED_CLASSES.get(i));
            }
        }
        return activePublishers;
    }

    /**
     *
     * @param classPath "org.wso2.carbon.oc.internal.RTPublisher"
     * @return particular xml map
     */
    public static Map<String, String> getPublisher(String classPath) {
        return OperationsCenterAgentUtils.getConfigurations().get(classPath);
    }

    /**
     *
     * @param command "RESTART", "SHUTDOWN" etc..
     */
    public static void performAction(String command) {
        ServerAdmin serverAdmin = (ServerAdmin) OperationsCenterAgentDataHolder.getInstance().getServerAdmin();
        if (serverAdmin != null) {
            try {
                if ("RESTART".equals(command)) {
                    serverAdmin.restart();
                } else if ("GRACEFUL_RESTART".equals(command)) {
                    serverAdmin.restartGracefully();
                } else if ("SHUTDOWN".equals(command)) {
                    serverAdmin.shutdown();
                } else if ("GRACEFUL_SHUTDOWN".equals(command)) {
                    serverAdmin.shutdown();
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
