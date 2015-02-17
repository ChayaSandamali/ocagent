package org.wso2.carbon.oc.publisher;

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

    private boolean isRegistered = false;

    private String ocUrl;
    private long initialDelay;
    private long interval;

    private static final String REGISTRATION_PATH = "/api/register";
    private static final String SYNCHRONIZATION_PATH = "/api/update";
    private static final String CONTENT_TYPE = "application/json";
    private static final String CHARACTER_SET = "UTF-8";

    public RTPublisher() {
        Map<String, String> configMap = OperationsCenterAgentUtils.getPublisher(RTPublisher.class.getCanonicalName());
        String username = configMap.get(OperationsCenterAgentUtils.USERNAME);
        String password = configMap.get(OperationsCenterAgentUtils.PASSWORD);


        this.ocUrl = configMap.get(OperationsCenterAgentUtils.REPORT_URL);

        this.initialDelay = Long.parseLong(configMap.get(OperationsCenterAgentUtils.DELAY).toString());
        this.interval = Long.parseLong(configMap.get(OperationsCenterAgentUtils.INTERVAL).toString());


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

        String jsonString = MessageHelper.getRTRegistrationRequest();



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

        /*List<String> activePublishers = OperationsCenterAgentUtils.getActivePublishers();
        for (String s : activePublishers) {
            System.out.println(s);
        }


*/
        String jsonString = MessageHelper.getRTSynchronizationRequest();

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

    public long getInitialDelay() {
        return initialDelay;
    }

    public long getInterval() {
        return interval;
    }
}
