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

import com.jezhumble.javasysmon.JavaSysMon;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.oc.internal.messages.RegistrationRequest;
import org.wso2.carbon.oc.internal.messages.RegistrationResponse;
import org.wso2.carbon.oc.internal.messages.SynchronizationRequest;
import org.wso2.carbon.oc.internal.messages.SynchronizationResponse;
import org.wso2.carbon.user.api.Tenant;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.description.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.oc.internal.exceptions.ParameterUnavailableException;
import org.wso2.carbon.server.admin.common.ServerUpTime;
import org.wso2.carbon.server.admin.service.ServerAdmin;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This class extract server data from server admin service
 */

public class OCAgentDataExtractor {
	private static OCAgentDataExtractor instance =
			new OCAgentDataExtractor();
	private static Logger logger = LoggerFactory.getLogger(OCAgentDataExtractor.class);

	private static SynchronizationRequest synchronizationRequest;
	private static RegistrationRequest registrationRequest;
	private static SynchronizationResponse synchronizationResponse;
	private static RegistrationResponse registrationResponse;

	private static final double PERCENT = 100;
	private static final double MEGA = 1000000;
	private static final double GIGA = 1000000000;

	private static final String LOCAL_IP = "carbon.local.ip";
	private static final String MGT_TRANSPORT_HTTPS_PORT = "mgt.transport.https.port";
	private static final String NAME = "Name";
	private static final String WEB_CONTEXT_ROOT = "WebContextRoot";
	private static final String DOMAIN = "domain";
	private static final String SUB_DOMAIN = "subDomain";
	private static final String PROPERTIES = "properties";
	private static final String PROPERTY_NAME = "name";
	private static final String PROPERTY_VALUE = "value";
	private static final String PATCH_PATH =
			CarbonUtils.getCarbonHome() + "/repository/components/patches";
	private static final String PATCH = "patch";

	private JavaSysMon javaSysMon = new JavaSysMon();
	private String os;
	private int cpuCount;
	private double cpuSpeed;
	private double totalMemory;

	private OCAgentDataExtractor() {
		os = javaSysMon.osName();
		cpuCount = javaSysMon.numCpus();
		cpuSpeed = javaSysMon.cpuFrequencyInHz() / GIGA;
		totalMemory = javaSysMon.physical().getTotalBytes() / MEGA;
	}

	public static OCAgentDataExtractor getInstance() {
		return instance;
	}

	public String getLocalIp() throws ParameterUnavailableException {
		String value = System.getProperty(LOCAL_IP);
		if (value == null) {
			throw new ParameterUnavailableException(LOCAL_IP + " is not available.");
		}
		return value;
	}

	public String getMgtTransportHttpsPort() throws ParameterUnavailableException {
		String value = System.getProperty(MGT_TRANSPORT_HTTPS_PORT);
		if (value == null) {
			throw new ParameterUnavailableException(
					MGT_TRANSPORT_HTTPS_PORT + " is not available.");
		}
		return value;
	}

	public String getServerName() throws ParameterUnavailableException {
		ServerConfigurationService serverConfigurationService =
				OCAgentDataHolder.getInstance().getServerConfigurationService();
		String value = serverConfigurationService.getFirstProperty(NAME);
		if (value == null) {
			throw new ParameterUnavailableException(
					MGT_TRANSPORT_HTTPS_PORT + " is not available.");
		}
		return value;
	}

	public String getServerVersion() {
		ServerAdmin serverAdmin =
				(ServerAdmin) OCAgentDataHolder.getInstance().getServerAdmin();
		if (serverAdmin != null) {
			try {
				return serverAdmin.getServerVersion();
			} catch (Exception e) {
				logger.error("Failed to retrieve server version.", e);
			}
		}
		return "Undefined";
	}

	public String getAdminServiceUrl() throws ParameterUnavailableException {
		ServerConfigurationService serverConfigurationService =
				OCAgentDataHolder.getInstance().getServerConfigurationService();
		String value = serverConfigurationService.getFirstProperty(WEB_CONTEXT_ROOT);
		if (value == null) {
			throw new ParameterUnavailableException(WEB_CONTEXT_ROOT + " is not available.");
		}
		String localIP = getLocalIp();
		String httpsPort = getMgtTransportHttpsPort();

		return "https://" + localIP + ":" + httpsPort + value;
	}

	public String getDomain() {
		String domain = "Default";
		ConfigurationContextService configurationContextService =
				OCAgentDataHolder.getInstance().getConfigurationContextService();
		if (configurationContextService != null) {
			ClusteringAgent clusteringAgent = configurationContextService.getServerConfigContext().
					getAxisConfiguration().getClusteringAgent();
			if (clusteringAgent != null) {
				Parameter domainParam = clusteringAgent.getParameter(DOMAIN);
				if (domain != null) {
					domain = domainParam.getValue().toString();
				}
			}
		}
		return domain;
	}

	public String getSubDomain() {
		String subDomain = "Default";
		ConfigurationContextService configurationContextService =
				OCAgentDataHolder.getInstance().getConfigurationContextService();
		if (configurationContextService != null) {
			ClusteringAgent clusteringAgent = configurationContextService.getServerConfigContext().
					getAxisConfiguration().getClusteringAgent();
			if (clusteringAgent != null) {
				Parameter propertiesParameter = clusteringAgent.getParameter(PROPERTIES);
				if (propertiesParameter != null) {
					OMElement omElement = propertiesParameter.getParameterElement();
					Iterator<OMElement> childElements = omElement.getChildElements();
					while (childElements.hasNext()) {
						OMElement childElement = (OMElement) childElements.next();
						if (childElement != null) {
							String propertyAttributeValue = childElement.
									                                            getAttributeValue(
											                                            childElement
													                                            .resolveQName(
															                                            PROPERTY_NAME));
							if (propertyAttributeValue != null &&
							    propertyAttributeValue.equalsIgnoreCase(SUB_DOMAIN)) {
								subDomain = (childElement.getAttributeValue(
										childElement.resolveQName(PROPERTY_VALUE)));
							}
						}
					}
				}
			}
		}
		return subDomain;
	}

	public String getOs() {
		return os;
	}

	public int getCpuCount() {
		return cpuCount;
	}

	public double getCpuSpeed() {
		return cpuSpeed;
	}

	public double getTotalMemory() {
		return totalMemory;
	}

	public double getFreeMemory() {
		return javaSysMon.physical().getFreeBytes() / MEGA;
	}

	public double getIdelCpuUsage() {
		long idle = javaSysMon.cpuTimes().getIdleMillis();
		double total = javaSysMon.cpuTimes().getTotalMillis();
		return (idle / total) * PERCENT;
	}

	public double getSystemCpuUsage() {
		long sys = javaSysMon.cpuTimes().getSystemMillis();
		double total = javaSysMon.cpuTimes().getTotalMillis();
		return (sys / total) * PERCENT;
	}

	public double getUserCpuUsage() {
		long user = javaSysMon.cpuTimes().getUserMillis();
		double total = javaSysMon.cpuTimes().getTotalMillis();
		return (user / total) * PERCENT;
	}

	public String getServerUpTime() {
		ServerAdmin serverAdmin =
				(ServerAdmin) OCAgentDataHolder.getInstance().getServerAdmin();
		if (serverAdmin != null) {
			try {
				ServerUpTime serverUptime = serverAdmin.getServerData().getServerUpTime();
				StringBuilder stringBuilder = new StringBuilder(64);
				stringBuilder.append(serverUptime.getDays());
				stringBuilder.append("d ");
				stringBuilder.append(serverUptime.getHours());
				stringBuilder.append("h ");
				stringBuilder.append(serverUptime.getMinutes());
				stringBuilder.append("m ");
				stringBuilder.append(serverUptime.getSeconds());
				stringBuilder.append("s");
				return stringBuilder.toString();
			} catch (Exception e) {
				logger.error("Failed to retrieve server up time.", e);
			}
		}
		return "Undefined";
	}

	public String getServerStartTime() {
		ServerAdmin serverAdmin =
				(ServerAdmin) OCAgentDataHolder.getInstance().getServerAdmin();
		if (serverAdmin != null) {
			try {
				return serverAdmin.getServerData().getServerStartTime();
			} catch (Exception e) {
				logger.error("Failed to retrieve server up time.", e);
			}
		}
		return "Undefined";
	}

	public int getThreadCount() {
		return Thread.activeCount();
	}

	public double getSystemLoadAverage() {
		OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
		return operatingSystemMXBean.getSystemLoadAverage();
	}

	public Tenant[] getAllTenants() {
		Tenant[] tenants = null;
		try {
			tenants =  OCAgentDataHolder.getInstance().getRealmService().getTenantManager().getAllTenants();
		} catch (UserStoreException e) {
			logger.info("problem while gettiing all tenants", e);
		}
		return tenants;
	}

	public List<String> getPatches() {
		List<String> patches = new ArrayList<String>();
		File file = new File(PATCH_PATH);
		File[] patchesList = file.listFiles();
		if (patchesList != null) {
			for (File patch : patchesList) {
				if (patch.isDirectory() && patch.getName().startsWith(PATCH)) {
					patches.add(patch.getName());
				}
			}
			Collections.sort(patches);
		}
		return patches;
	}

	public RegistrationResponse getRegistrationResponse() {
		if(registrationResponse == null) {
			registrationResponse = new RegistrationResponse();
			logger.info("new RegistrationResponse obj");
		}
		return registrationResponse;
	}

	public SynchronizationResponse getSynchronizationResponse() {
		if(synchronizationResponse == null) {
			synchronizationResponse = new SynchronizationResponse();
			logger.info("new SynchronizationResponse obj");
		}
		return synchronizationResponse;
	}

	public RegistrationRequest getRegistrationRequest() {
		if(registrationRequest == null) {
			registrationRequest = new RegistrationRequest();
			logger.info("new RegistrationRequest obj");
		}

		try {
			registrationRequest.setIp(this.getLocalIp());
			registrationRequest.setServerName(this.getServerName());
			registrationRequest.setServerVersion(this.getServerVersion());
			registrationRequest.setDomain(this.getDomain());
			registrationRequest.setSubDomain(this.getSubDomain());
			registrationRequest.setAdminServiceUrl(this.getAdminServiceUrl());
			registrationRequest.setStartTime(this.getServerStartTime());
			registrationRequest.setOs(this.getOs());
			registrationRequest.setTotalMemory(this.getTotalMemory());
			registrationRequest.setCpuCount(this.getCpuCount());
			registrationRequest.setCpuSpeed(this.getCpuSpeed());

			List<String> patches = this.getPatches();
			if (patches.size() > 0) {
				registrationRequest.setPatches(patches);
			}

		} catch (ParameterUnavailableException e) {
			logger.error("Failed to read registration parameter. ", e);
		}
		return registrationRequest;
	}

	public SynchronizationRequest getSynchronizationRequest() {
		if(synchronizationRequest == null) {
			synchronizationRequest = new SynchronizationRequest();
			logger.info("new SynchronizationRequest obj");
		}

		try {
			synchronizationRequest.setAdminServiceUrl(this.getAdminServiceUrl());
			synchronizationRequest.setServerUpTime(this.getServerUpTime());
			synchronizationRequest.setThreadCount(this.getThreadCount());
			synchronizationRequest.setFreeMemory(this.getFreeMemory());
			synchronizationRequest.setIdleCpuUsage(this.getIdelCpuUsage());
			synchronizationRequest.setSystemCpuUsage(this.getSystemCpuUsage());
			synchronizationRequest.setUserCpuUsage(this.getUserCpuUsage());
			synchronizationRequest.setSystemLoadAverage(this.getSystemLoadAverage());
			synchronizationRequest.setTenants(this.getAllTenants());

		} catch (ParameterUnavailableException e) {
			logger.error("Failed to read synchronization parameter. ", e);
		}

		return synchronizationRequest;
	}
}
