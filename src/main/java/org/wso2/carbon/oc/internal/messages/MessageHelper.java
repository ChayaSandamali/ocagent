package org.wso2.carbon.oc.internal.messages;


import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.oc.internal.OperationsCenterAgentDataExtractor;
import org.wso2.carbon.oc.internal.OperationsCenterAgentDataHolder;
import org.wso2.carbon.oc.internal.exceptions.ParameterUnavailableException;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by noelyahan on 2/12/15.
 */
public class MessageHelper {
    private static Logger logger = LoggerFactory.getLogger(MessageHelper.class);
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static String BAMRegisterPayloadDef;
    private static String BAMSyncPayloadDef;




    public static String getRTRegistrationRequest() {
       String message = null;

        try {
            message = objectMapper.writeValueAsString(ocRegistrationRequestBuilder());

        } catch (IOException e) {
            logger.error("Failed to get JSON String from ocSynchronizationRequest", e);
        }
        logger.info(message);
        return message;
    }

    public static String getRTSynchronizationRequest() {
        String message = null;

        try {
            message = objectMapper.writeValueAsString(ocSynchronizationRequestBuilder());

        } catch (IOException e) {
            logger.error("Failed to get JSON String from ocSynchronizationRequest", e);
        }
        return message;
    }

    public static String getMBRegistrationRequest() {
        String message = null;

        OCEvent event = new OCEvent();
        event.setPayload(ocRegistrationRequestBuilder().getRegistrationRequest());

        try {
            message = objectMapper.writeValueAsString(event);

        } catch (IOException e) {
            logger.error("Failed to get JSON String from ocSynchronizationRequest", e);
        }
        return message;
    }

    public static String getMBSynchronizationRequest() {
        String message = null;

        OCEvent event = new OCEvent();
        event.setPayload(ocSynchronizationRequestBuilder().getSynchronizationRequest());

        try {
            message = objectMapper.writeValueAsString(event);

        } catch (IOException e) {
            logger.error("Failed to get JSON String from ocSynchronizationRequest", e);
        }

        return message;
    }

    public static Object[] getBAMRegistrationRequest() {
        _RegistrationRequest r = ocRegistrationRequestBuilder().getRegistrationRequest();
        return new Object[]{r.getIp(), r.getServerName(), r.getServerVersion(), r.getDomain(),
                            r.getSubDomain(), r.getAdminServiceUrl(), r.getStartTime(), r.getOs(),
                            r.getTotalMemory(), Double.parseDouble(""+r.getCpuCount()),
                            r.getCpuSpeed(), 12342143.53, ""};
    }

    public static Object[] getBAMSynchronizationRequest() {
        _SynchronizationRequest s = ocSynchronizationRequestBuilder().getSynchronizationRequest();
        return new Object[]{s.getFreeMemory(), s.getIdleCpuUsage(), s.getSystemCpuUsage(), s.getUserCpuUsage(),
                            s.getAdminServiceUrl(), s.getServerUpTime(), Double.parseDouble(""+s.getThreadCount()),
                            s.getSystemLoadAverage(), Double.parseDouble(""+s.getTimestamp()), ""
        };
    }

    public static String getBAMRegisterPayloadDef() {
        JsonNode root = null;
        if(BAMRegisterPayloadDef == null) {
            try {
                root = objectMapper.readTree(objectMapper.writeValueAsString(ocRegistrationRequestBuilder()));
                Map<String,String> flat = new HashMap<String, String>();
                StringBuilder streamIdBuilder = new StringBuilder();
                BAMRegisterPayloadDef = traverse(root, flat, streamIdBuilder);
                BAMRegisterPayloadDef = BAMRegisterPayloadDef.substring(0, BAMRegisterPayloadDef.length()-1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return BAMRegisterPayloadDef;
    }

    public static String getBAMSyncPayloadDef() {
        JsonNode root = null;
        if(BAMSyncPayloadDef == null) {
            try {
                root = objectMapper.readTree(objectMapper.writeValueAsString(ocSynchronizationRequestBuilder()));
                Map<String,String> flat = new HashMap<String, String>();
                StringBuilder streamIdBuilder = new StringBuilder();
                BAMSyncPayloadDef = traverse(root, flat, streamIdBuilder);
                BAMSyncPayloadDef = BAMSyncPayloadDef.substring(0, BAMSyncPayloadDef.length()-1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return BAMSyncPayloadDef;
    }


    private static String traverse(JsonNode node, Map<String,String> result, StringBuilder sb)
    {

        Iterator<Map.Entry<String,JsonNode>> it = node.getFields();
        while (it.hasNext()) {
            Map.Entry<String,JsonNode> entry = it.next();
            JsonNode n = entry.getValue();
            if (n.isObject()) { // if JSON object, traverse recursively
                traverse(n, result, sb);
            } else { // if not, just add as String
                result.put(entry.getKey(), n.asText());
                String dType = "STRING";
                if(isNumber(entry.getValue().toString())) {
                    dType = "DOUBLE";
                }
//                {'name':'ip','type':'STRING'}
                sb.append("{");
                sb.append("'name':");
                sb.append("'"+entry.getKey()+"'");
                sb.append(",");
                sb.append("'type':");
                sb.append("'"+dType+"'");
                sb.append("}");
                sb.append(",");
            }
        }

        return sb.toString();
    }

    private static boolean isNumber(String s) {
        boolean result = false;
        try {
            double d = Double.parseDouble(s);
            result = true;
        }catch (NumberFormatException e) {
            return result;
        }
        return result;
    }

    private static OCRegistrationRequest ocRegistrationRequestBuilder() {
        OCRegistrationRequest ocRegistrationRequest = new OCRegistrationRequest();

        try {
            ocRegistrationRequest.getRegistrationRequest().
                    setIp(OperationsCenterAgentDataExtractor.getInstance().getLocalIp());
            ocRegistrationRequest.getRegistrationRequest().
                    setServerName(OperationsCenterAgentDataExtractor.getInstance().getServerName());
            ocRegistrationRequest.getRegistrationRequest().
                    setServerVersion(OperationsCenterAgentDataExtractor.getInstance().getServerVersion());
            ocRegistrationRequest.getRegistrationRequest().
                    setDomain(OperationsCenterAgentDataExtractor.getInstance().getDomain());
            ocRegistrationRequest.getRegistrationRequest().
                    setSubDomain(OperationsCenterAgentDataExtractor.getInstance().getSubDomain());
            ocRegistrationRequest.getRegistrationRequest().
                    setAdminServiceUrl(OperationsCenterAgentDataExtractor.getInstance().getAdminServiceUrl());
            ocRegistrationRequest.getRegistrationRequest().
                    setStartTime(OperationsCenterAgentDataExtractor.getInstance().getServerStartTime());
            ocRegistrationRequest.getRegistrationRequest().
                    setOs(OperationsCenterAgentDataExtractor.getInstance().getOs());
            ocRegistrationRequest.getRegistrationRequest().
                    setTotalMemory(OperationsCenterAgentDataExtractor.getInstance().getTotalMemory());
            ocRegistrationRequest.getRegistrationRequest().
                    setCpuCount(OperationsCenterAgentDataExtractor.getInstance().getCpuCount());
            ocRegistrationRequest.getRegistrationRequest().
                    setCpuSpeed(OperationsCenterAgentDataExtractor.getInstance().getCpuSpeed());

            List<String> patches = OperationsCenterAgentDataExtractor.getInstance().getPatches();
            if (patches.size() > 0) {
                ocRegistrationRequest.getRegistrationRequest().setPatches(patches);
            }



        } catch (ParameterUnavailableException e) {
            logger.error("Failed to read registration parameter. ", e);
        }

        return ocRegistrationRequest;
    }

    private static OCSynchronizationRequest ocSynchronizationRequestBuilder() {
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




        } catch (ParameterUnavailableException e) {
            logger.error("Failed to read synchronization parameter. ", e);
        } catch (UserStoreException e) {
            e.printStackTrace();
        }
        return ocSynchronizationRequest;
    }
}
