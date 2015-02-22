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
