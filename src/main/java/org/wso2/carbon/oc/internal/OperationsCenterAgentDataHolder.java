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
import org.wso2.carbon.server.admin.common.IServerAdmin;
import org.wso2.carbon.statistics.services.StatisticsAdmin;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

public class OperationsCenterAgentDataHolder {
	private static OperationsCenterAgentDataHolder instance = new OperationsCenterAgentDataHolder();
	private static Logger logger = LoggerFactory.getLogger(OperationsCenterAgentDataHolder.class);

	//    private OperationsCenterConnector operationsCenterConnector;
	private ConfigurationContextService configurationContextService;
	private ServerConfigurationService serverConfigurationService;
	private IServerAdmin serverAdmin;           // server information, commands
	private RealmService realmService;          // tenant information
	private StatisticsAdmin statisticsAdmin;    // request, response count
	private int serverId;

	private OperationsCenterAgentDataHolder() {
	    /* No initializations needed for the moment. */
	}

	public static OperationsCenterAgentDataHolder getInstance() {
		return instance;
	}


	public ConfigurationContextService getConfigurationContextService() {
		return configurationContextService;
	}

	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public void setConfigurationContextService(
			ConfigurationContextService configurationContextService) {
		this.configurationContextService = configurationContextService;
	}

	public ServerConfigurationService getServerConfigurationService() {
		return serverConfigurationService;
	}

	public StatisticsAdmin getStatisticsAdmin() {
		return statisticsAdmin;
	}

	public void setStatisticsAdmin(StatisticsAdmin statisticsAdmin) {
		this.statisticsAdmin = statisticsAdmin;
	}

	public IServerAdmin getServerAdmin() {
		return serverAdmin;
	}

	public void setServerAdmin(IServerAdmin serverAdmin) {
		this.serverAdmin = serverAdmin;
	}

	public RealmService getRealmService() {
		return realmService;
	}

	public void setRealmService(RealmService realmService) {
		this.realmService = realmService;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}
}
