package org.wso2.carbon.oc.internal.messages;


import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.oc.internal.OperationsCenterAgentDataExtractor;
import org.wso2.carbon.oc.internal.OperationsCenterAgentDataHolder;
import org.wso2.carbon.oc.internal.exceptions.ParameterUnavailableException;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.IOException;
import java.util.List;

/**
 * Created by noelyahan on 2/12/15.
 */
public class MessageHelper {
    private static Logger logger = LoggerFactory.getLogger(MessageHelper.class);
    private static ObjectMapper objectMapper = new ObjectMapper();


    public static String getRealTimeRegistrationRequest() {
//        logger.info("++++++++++++++++++++++++++++++++++++++++");
//        logger.info("++++++++++++++++++++++++++++++++++++++++");
//        logger.info("++++++++++++++++++++++++++++++++++++++++");
        String message = null;
        OCRegistrationRequest ocRegistrationRequest = new OCRegistrationRequest();

        try {
            ocRegistrationRequest.getRegistrationRequest().setIp(OperationsCenterAgentDataExtractor.getInstance().getLocalIp());
            ocRegistrationRequest.getRegistrationRequest().setServerName(OperationsCenterAgentDataExtractor.getInstance().getServerName());
            ocRegistrationRequest.getRegistrationRequest().setServerVersion(OperationsCenterAgentDataExtractor.getInstance().getServerVersion());
            ocRegistrationRequest.getRegistrationRequest().setDomain(OperationsCenterAgentDataExtractor.getInstance().getDomain());
            ocRegistrationRequest.getRegistrationRequest().setSubDomain(OperationsCenterAgentDataExtractor.getInstance().getSubDomain());
            ocRegistrationRequest.getRegistrationRequest().setAdminServiceUrl(OperationsCenterAgentDataExtractor.getInstance().getAdminServiceUrl());
            ocRegistrationRequest.getRegistrationRequest().setStartTime(OperationsCenterAgentDataExtractor.getInstance().getServerStartTime());
            ocRegistrationRequest.getRegistrationRequest().setOs(OperationsCenterAgentDataExtractor.getInstance().getOs());
            ocRegistrationRequest.getRegistrationRequest().setTotalMemory(OperationsCenterAgentDataExtractor.getInstance().getTotalMemory());
            ocRegistrationRequest.getRegistrationRequest().setCpuCount(OperationsCenterAgentDataExtractor.getInstance().getCpuCount());
            ocRegistrationRequest.getRegistrationRequest().setCpuSpeed(OperationsCenterAgentDataExtractor.getInstance().getCpuSpeed());
            List<String> patches = OperationsCenterAgentDataExtractor.getInstance().getPatches();
            if (patches.size() > 0) {
                ocRegistrationRequest.getRegistrationRequest().setPatches(patches);
            }

            try {
                message = objectMapper.writeValueAsString(ocRegistrationRequest);

            } catch (IOException e) {
                logger.error("Failed to get JSON String from ocSynchronizationRequest", e);
            }

        } catch (ParameterUnavailableException e) {
            logger.error("Failed to read registration parameter. ", e);
        }

//        logger.info("----------------------------------------");
//        logger.info("----------------------------------------");
//        logger.info("----------------------------------------");
//        logger.info(message);
        return message;
    }

    public static String getRealTimeSynchronizationRequest() {
//        logger.info("++++++++++++++++++++++++++++++++++++++++");
//        logger.info("++++++++++++++++++++++++++++++++++++++++");
//        logger.info("++++++++++++++++++++++++++++++++++++++++");

        String message = null;
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
            ocSynchronizationRequest.getSynchronizationRequest().
                    setTenants(OperationsCenterAgentDataHolder.getInstance().getRealmService().getTenantManager().getAllTenants());

            try {
                message = objectMapper.writeValueAsString(ocSynchronizationRequest);

            } catch (IOException e) {
                logger.error("Failed to get JSON String from ocSynchronizationRequest", e);
            }


        } catch (ParameterUnavailableException e) {
            logger.error("Failed to read synchronization parameter. ", e);
        } catch (UserStoreException e) {
            e.printStackTrace();
        }

//        logger.info("----------------------------------------");
//        logger.info("----------------------------------------");
//        logger.info("----------------------------------------");
//        logger.info(message);
        return message;
    }
}
