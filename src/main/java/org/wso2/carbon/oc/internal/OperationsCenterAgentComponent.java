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

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.oc.publisher.IPublisher;
import org.wso2.carbon.server.admin.common.IServerAdmin;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @scr.component name="org.wso2.carbon.oc.operationscenteragentcomponent" immediate="true"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"
 * bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="server.configuration"
 * interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1" policy="dynamic"
 * bind="setServerConfigurationService"
 * unbind="unsetServerConfigurationService"
 * @scr.reference name="org.wso2.carbon.server.admin.common"
 * interface="org.wso2.carbon.server.admin.common.IServerAdmin"
 * cardinality="1..1" policy="dynamic"
 * bind="setServerAdminService"
 * unbind="unsetServerAdminService"
 * @scr.reference name="org.wso2.carbon.user.core.service"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 */

public class OperationsCenterAgentComponent {
	private static Logger logger = LoggerFactory.getLogger(OperationsCenterAgentComponent.class);
	private static final ScheduledExecutorService reporterTaskExecuter =
			Executors.newScheduledThreadPool(1);

	protected void activate(ComponentContext componentContext) {
		try {
			logger.info("Activating Operations Center Agent component.");

			// get active publishers class paths
			List<String> activeClasses = OperationsCenterAgentUtils.getActivePublishers();
			for (String s : activeClasses)

				for (String activeClass : activeClasses) {
					IPublisher publisher = null;
					Class publisherClass = Class.forName(activeClass);

					publisher = (IPublisher) publisherClass.newInstance();

					//Start reporting task as scheduled task
					if (publisher != null) {

						OperationsCenterAgentReporterTask operationsCenterAgentReporterTask
								= new OperationsCenterAgentReporterTask(publisher);
						reporterTaskExecuter.scheduleAtFixedRate(operationsCenterAgentReporterTask,
						                                         publisher.getInitialDelay(),
						                                         publisher.getInterval(),
						                                         TimeUnit.MILLISECONDS);
					}
				}

		} catch (Throwable throwable) {
			logger.error("Failed to activate OperationsCenterAgentComponent", throwable);
			reporterTaskExecuter.shutdown();
		}
	}

	protected void deactivate(ComponentContext componentContext) {
		logger.info("Deactivating Operations Center Agent component.");
		unsetConfigurationContextService(null);
		unsetRealmService(null);
		unsetServerAdminService(null);
		unsetServerConfigurationService(null);
	}

	protected void unsetConfigurationContextService(
			ConfigurationContextService configurationContextService) {
		OperationsCenterAgentDataHolder.getInstance().setConfigurationContextService(null);
	}

	protected void setConfigurationContextService(
			ConfigurationContextService configurationContextService) {
		OperationsCenterAgentDataHolder.getInstance()
		                               .setConfigurationContextService(configurationContextService);
	}

	protected void unsetServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		OperationsCenterAgentDataHolder.getInstance().setServerConfigurationService(null);
	}

	protected void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		OperationsCenterAgentDataHolder.getInstance()
		                               .setServerConfigurationService(serverConfigurationService);
	}

	protected void unsetServerAdminService(IServerAdmin serverAdmin) {
		OperationsCenterAgentDataHolder.getInstance().setServerAdmin(null);
	}

	protected void setServerAdminService(IServerAdmin serverAdmin) {
		OperationsCenterAgentDataHolder.getInstance().setServerAdmin(serverAdmin);
	}

	protected void setRealmService(RealmService realmService) {
		OperationsCenterAgentDataHolder.getInstance().setRealmService(realmService);
	}

	protected void unsetRealmService(RealmService realmService) {
		OperationsCenterAgentDataHolder.getInstance().setRealmService(null);
	}

}
