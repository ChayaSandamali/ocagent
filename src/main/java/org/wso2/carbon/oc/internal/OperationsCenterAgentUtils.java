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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.mina.handler.StreamIoHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.base.ServerConfigurationException;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.server.admin.service.ServerAdmin;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

/*
    Associate with carbon.xml [OperationsCenter] data
*/

public class OperationsCenterAgentUtils {
    private static Logger logger = LoggerFactory.getLogger(OperationsCenterAgentUtils.class);
    private static final List<String> ALLOWED_CLASSES = Arrays
            .asList("org.wso2.carbon.oc.internal.publisher.RTPublisher",
                    "org.wso2.carbon.oc.internal.publisher.MBPublisher",
                    "org.wso2.carbon.oc.internal.publisher.BAMPublisher");

    private static final List<String> ALLOWED_PUBLISHERS = Arrays
            .asList("RTPublisher",
                    "MBPublisher",
                    "BAMPublisher");

    private static Map<String, Map<String, String>> configurations;


    public static final String IS_ENABLE = "Enable";
    public static final String CLASS_PATH = "Class";
    public static final String REPORT_URL = "ReportHostName";
    public static final String REPORT_HOST_NAME = "ReportHttpPort";
    public static final String REPORT_PORT = "ReportURL";
    public static final String USERNAME = "Username";
    public static final String PASSWORD = "Password";
    public static final String INTERVAL = "Reporting.Interval";
    public static final String DELAY = "Reporting.Delay";

//    private Map<String, Map<String, String>> configurations = new HashMap<String, Map<String, String>>();



    public static ServerConfigurationService getServerConfigurationService() {
        ServerConfigurationService serverConfigurationService =
                OperationsCenterAgentDataHolder.getInstance().getServerConfigurationService();
        if (serverConfigurationService == null) {
            throw new RuntimeException("ServerConfigurationService is unavailable");
        }
        return serverConfigurationService;
    }

    private static Map<String, Map<String, String>> getConfigurations() {
        ServerConfigurationService serverConfigurationService = OperationsCenterAgentUtils.getServerConfigurationService();
//        Map<String, Map<String, String>> configurations = new HashMap<String, Map<String, String>>();

        if(configurations == null) {
            configurations = new HashMap<String, Map<String, String>>();
            for (String publisher : ALLOWED_PUBLISHERS) {
                    String publisherPath = "Publishers." + publisher;

                    String isEnable = serverConfigurationService.getFirstProperty(publisherPath+"."+IS_ENABLE);
                    String classPath = serverConfigurationService.getFirstProperty(publisherPath+"."+CLASS_PATH);
                    String reportURL = serverConfigurationService.getFirstProperty(publisherPath+"."+REPORT_URL);
                    String reportHostName = serverConfigurationService.getFirstProperty(publisherPath+"."+REPORT_HOST_NAME);
                    String reportPort = serverConfigurationService.getFirstProperty(publisherPath+"."+REPORT_PORT);
                    String username = serverConfigurationService.getFirstProperty(publisherPath+"."+USERNAME);
                    String password = serverConfigurationService.getFirstProperty(publisherPath+"."+PASSWORD);
                    String interval = serverConfigurationService.getFirstProperty(publisherPath+"."+INTERVAL);
                    String delay = serverConfigurationService.getFirstProperty(publisherPath+"."+DELAY);


                    Map<String, String> configMap = new HashMap<String, String>();
                    configMap.put(IS_ENABLE, isEnable);
                    configMap.put(CLASS_PATH, classPath);
                    configMap.put(REPORT_URL, reportURL);
                    configMap.put(REPORT_HOST_NAME, reportHostName);
                    configMap.put(REPORT_PORT, reportPort);
                    configMap.put(USERNAME, username);
                    configMap.put(PASSWORD, password);
                    configMap.put(INTERVAL, interval);
                    configMap.put(DELAY, delay);

                    configurations.put(classPath, configMap);
                logger.info("init-config");
            }
        }

        return configurations;
    }

    public static String[] getPublishers() throws IllegalArgumentException {
//        ServerConfigurationService serverConfigurationService = OperationsCenterAgentUtils.getServerConfigurationService();
        logger.info(ALLOWED_CLASSES.get(0));

        Map<String, String> configMap = OperationsCenterAgentUtils.getConfigurations().get(ALLOWED_CLASSES.get(0));
        Iterator<Map.Entry<String, String>> iterator = configMap.entrySet().iterator();

        for (; iterator.hasNext();) {
            Map.Entry e = iterator.next();
            logger.info(e.getKey() +" : "+e.getValue());
        }

//        logger.info("getPub() end");

        /*String value[] = serverConfigurationService.getProperties("Publishers.Publisher.Class");
        if (value == null) {
            throw new IllegalArgumentException("OperationsCenterURL is not specified");
        }
        return value;*/
        return null;
    }

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

    public static Map<String, String> getPublisher(String classPath) {
        return OperationsCenterAgentUtils.getConfigurations().get(classPath);
    }




    public static boolean isOperationsCenterAgentEnabled() {
        ServerConfigurationService serverConfigurationService = OperationsCenterAgentUtils.getServerConfigurationService();
        String value = serverConfigurationService.getFirstProperty("OperationsCenter.Enable");
        if(Boolean.parseBoolean(value) == true) {
            return true;
        }
        return false;
    }

    public static String getOperationsCenterUrl() throws IllegalArgumentException {
        ServerConfigurationService serverConfigurationService = OperationsCenterAgentUtils.getServerConfigurationService();
        String value = serverConfigurationService.getFirstProperty("OperationsCenter.OperationsCenterURL");
        if (value == null) {
            throw new IllegalArgumentException("OperationsCenterURL is not specified");
        }
        return value;
    }

    public static String getOperationsCenterUsername() throws IllegalArgumentException {
        ServerConfigurationService serverConfigurationService = OperationsCenterAgentUtils.getServerConfigurationService();
        String value = serverConfigurationService.getFirstProperty("OperationsCenter.OperationsCenterUsername");
        if (value == null) {
            throw new IllegalArgumentException("OperationsCenterUsername is not specified");
        }
        return value;
    }

    public static long getOperationsCenterReportingInitialDelay() throws IllegalArgumentException {
        ServerConfigurationService serverConfigurationService = OperationsCenterAgentUtils.getServerConfigurationService();
        String value = serverConfigurationService.getFirstProperty("OperationsCenter.OperationsCenterReporting.InitialDelay");
        if (value == null) {
            throw new IllegalArgumentException("OperationsCenterReporting.InitialDelay is not specified");
        }
        return Long.parseLong(value);
    }

    public static long getOperationsCenterReportingInterval() throws IllegalArgumentException {
        ServerConfigurationService serverConfigurationService = OperationsCenterAgentUtils.getServerConfigurationService();
        String value = serverConfigurationService.getFirstProperty("OperationsCenter.OperationsCenterReporting.Interval");
        if (value == null) {
            throw new IllegalArgumentException("OperationsCenterReporting.Interval is not specified");
        }
        return Long.parseLong(value);
    }

    public static String getOperationsCenterPassword() throws IllegalArgumentException {
        ServerConfigurationService serverConfigurationService = OperationsCenterAgentUtils.getServerConfigurationService();
        String value = serverConfigurationService.getFirstProperty("OperationsCenter.OperationsCenterPassword");
        if (value == null) {
            throw new IllegalArgumentException("OperationsCenterPassword is not specified");
        }
        return value;
    }

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
