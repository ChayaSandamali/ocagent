package org.wso2.carbon.oc.internal.publisher;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.oc.internal.OperationsCenterAgentDataHolder;
import org.wso2.carbon.oc.internal.OperationsCenterAgentUtils;
import org.wso2.carbon.oc.internal.messages.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by noelyahan on 12/22/14.
 */
public class RTPublisher implements IPublisher {

    private static boolean isRegister = false;


    private static Logger logger = LoggerFactory.getLogger(RTPublisher.class);
    ObjectMapper objectMapper = new ObjectMapper();

    /**
     * The http client used to connect Operations Center.
     */
    private HttpClient httpClient;

    boolean isRegistered = false;

    private String ocUrl;
    private static final String REGISTRATION_PATH = "/api/register";
    private static final String SYNCHRONIZATION_PATH = "/api/update";
    private static final String CONTENT_TYPE = "application/json";
    private static final String CHARACTER_SET = "UTF-8";

    public RTPublisher(String ocUrl, String username, String password) {
        this.ocUrl = ocUrl;
        if (StringUtils.isBlank(this.ocUrl)) {
            throw new IllegalArgumentException("Operations Center URL is unspecified.");
        }
        this.httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        this.httpClient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        this.httpClient.getParams().setAuthenticationPreemptive(true);
    }

    /**
     * @return
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    public static boolean isRegister(){
        return isRegister;
    }

    @Override
    public void publish() {
        logger.info("======real-time===========reporting");

        if (isRegistered == false) {
            register();
        } else {
            sync();
        }
    }


    private boolean register() {
        /*OCRegistrationRequest ocRegistrationRequest = new OCRegistrationRequest();
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
        } catch (ParameterUnavailableException e) {
            logger.error("Failed to read registration parameter. ", e);
            return false;
        }

        String jsonString = null;
        try {
                jsonString = objectMapper.writeValueAsString(ocRegistrationRequest);
            } catch (IOException e) {
                logger.error("Failed to get JSON String from OCRegistrationRequest", e);
                return false;
            }
*/
//        OperationsCenterAgentUtils.test(CarbonUtils.getUserMgtXMLPath());


        String jsonString = MessageHelper.getRealTimeRegistrationRequest();



        String responseBody = sendPostRequest(ocUrl + REGISTRATION_PATH, jsonString, HttpStatus.SC_CREATED);
        if (responseBody != null && responseBody.length() > 0) {
            OCRegistrationResponse ocRegistrationResponse = null;
            try {
                ocRegistrationResponse = objectMapper.readValue(responseBody, OCRegistrationResponse.class);
            } catch (IOException e) {
                logger.error("Failed to read values from OCRegistrationResponse", e);
                return false;
            }

            if (ocRegistrationResponse != null) {
                isRegistered = true;
                OperationsCenterAgentDataHolder.getInstance().
                        setServerId(Integer.parseInt(ocRegistrationResponse.getRegistrationResponse().getServerId()));
                logger.info("Registered in Operations Center successfully.");
            } else {
                logger.error("Unable receive JSON registration response.");
            }
        }
        return false;
    }

    private void sync() {
//        OCSynchronizationRequest ocSynchronizationRequest = new OCSynchronizationRequest();
//        String s[] = OperationsCenterAgentUtils.getPublishers();
        List<String> activePublishers = OperationsCenterAgentUtils.getActivePublishers();
        for (String s : activePublishers) {
            System.out.println(s);
        }





        /*logger.info("size: "+s.length);
        for (String x : s) {
            logger.info(x);
        }*/
//        logger.info(""+OperationsCenterAgentUtils.getPublishers());
//        OMElement omElement = OperationsCenterAgentUtils.getConfigurationElement("/repository/conf/oc.xml");
//        OMElement realmElement = omElement.getFirstChildWithName(new QName("Realm"));
//        logger.info(realmElement.getText());
        /*try {
            logger.info("xml = " + omElement.toStringWithConsume());
        } catch (XMLStreamException e) {
            logger.info(e.getMessage());
        }*/



        /*
        logger.info("-----STAT COUNT----");
        logger.info("req: "+OperationsCenterAgentDataExtractor.getInstance().getAllRequestCount());
        logger.info("res: "+OperationsCenterAgentDataExtractor.getInstance().getAllResponseCount());
        logger.info("-----STAT COUNT----");
        //*/


        /*try {
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
            return;
        } catch (UserStoreException e) {
            e.printStackTrace();
        }

        String jsonString = null;
        String jsonMbString = null;
        try {
            jsonString = objectMapper.writeValueAsString(ocSynchronizationRequest);


        } catch (IOException e) {
            logger.error("Failed to get JSON String from ocSynchronizationRequest", e);
            return;
        }*/

        String jsonString = MessageHelper.getRealTimeSynchronizationRequest();

        String responseBody = sendPostRequest(ocUrl + SYNCHRONIZATION_PATH, jsonString, HttpStatus.SC_OK);
        if (responseBody != null && responseBody.length() > 0) {
            OCSynchronizationResponse ocSynchronizationResponse = null;
            try {
                ocSynchronizationResponse = objectMapper.readValue(responseBody, OCSynchronizationResponse.class);
            } catch (IOException e) {
                logger.error("Failed to read values from OCSynchronizationResponse", e);
                return;
            }

            if (ocSynchronizationResponse != null) {
                if ("updated".equals(ocSynchronizationResponse.getSynchronizationResponse().getStatus())) {
                    String command = ocSynchronizationResponse.getSynchronizationResponse().getCommand();
                    logger.info("Executing command. [Command:" + command + "]");
                    OperationsCenterAgentUtils.performAction(command);
                } else if ("error".equals(ocSynchronizationResponse.getSynchronizationResponse().getStatus())) {
                    logger.error("Unable to synchronize properly.");
                    isRegistered = false;
                }

            } else {
                logger.error("Unable receive JSON synchronization response.");
            }
        }
    }


    public String sendPostRequest(String url, String request, int expected) {
        PostMethod postMethod = new PostMethod(url);
        try {
            RequestEntity entity = new StringRequestEntity(request, CONTENT_TYPE, CHARACTER_SET);
            postMethod.setRequestEntity(entity);
            if (logger.isTraceEnabled()) {
                logger.trace("Sending POST request. " + request);
            }
            try {
                int statusCode = httpClient.executeMethod(postMethod);
                if (statusCode == expected) {
                    String responseBody = postMethod.getResponseBodyAsString();
                    if (logger.isTraceEnabled()) {
                        logger.trace("Response received. " + responseBody);
                    }
                    return responseBody;
                } else {
                    logger.error("Request failed with status Code : " + statusCode);
                }
            }catch (IOException e) {
                logger.error("Failed to Execute post method", e);
                isRegistered = false;
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("Failed to register with Operations Center", e);
        } finally {
            postMethod.releaseConnection();
        }
        return null;
    }
}
