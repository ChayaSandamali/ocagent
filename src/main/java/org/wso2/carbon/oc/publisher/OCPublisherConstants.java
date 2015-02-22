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

package org.wso2.carbon.oc.publisher;

/**
 * Holds constants related to oc publisher
 */
public class OCPublisherConstants {

	//xpath for publisher data
	public static String OC_PUBLISHER_ROOT_XPATH = "//Publishers//Publisher";

	//access from outside as the key for config map
	public static final String IS_ENABLE = "Enable";
	public static final String CLASS_PATH = "Class";
	public static final String REPORT_URL = "ReportURL";
	public static final String REPORT_HOST_NAME = "ReportHostName";
	public static final String REPORT_PORT = "ReportHttpPort";
	public static final String THRIFT_PORT = "ThriftPort";
	public static final String THRIFT_SSL_PORT = "ThriftSSLPort";
	public static final String USERNAME = "Username";
	public static final String PASSWORD = "Password";
	public static final String INTERVAL = "Interval";

	private OCPublisherConstants() {}
}
