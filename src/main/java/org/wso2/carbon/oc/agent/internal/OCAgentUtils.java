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

package org.wso2.carbon.oc.agent.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.server.admin.service.ServerAdmin;

/*
    Allows to invoke commands in server restart, shutdown
*/

public class OCAgentUtils {
	private static final String FORCE_SHUTDOWN = "FORCE_SHUTDOWN";
	private static final String FORCE_RESTART = "FORCE_RESTART";
	private static final String GRACEFUL_SHUTDOWN = "GRACEFUL_SHUTDOWN";
	private static final String GRACEFUL_RESTART = "GRACEFUL_RESTART";
	private static Logger logger = LoggerFactory.getLogger(OCAgentUtils.class);

	private OCAgentUtils() {
	}

	/**
	 * @param command "RESTART", "SHUTDOWN" etc..
	 */

	public static void performAction(String command) {
		ServerAdmin serverAdmin =
				(ServerAdmin) OCAgentDataHolder.getInstance().getServerAdmin();
		if (serverAdmin != null) {
			try {
				if (FORCE_RESTART.equals(command)) {
					serverAdmin.restart();
				} else if (GRACEFUL_RESTART.equals(command)) {
					serverAdmin.restartGracefully();
				} else if (FORCE_SHUTDOWN.equals(command)) {
					serverAdmin.shutdown();
				} else if (GRACEFUL_SHUTDOWN.equals(command)) {
					serverAdmin.shutdownGracefully();
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
