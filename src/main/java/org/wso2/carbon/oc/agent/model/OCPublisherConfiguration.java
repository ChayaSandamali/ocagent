package org.wso2.carbon.oc.agent.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Publisher")
public class OCPublisherConfiguration {

	private String name;
	private String classPath;
	private OCPublisherProperties ocPublisherProperties;

	@XmlElement(name = "Name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(name = "Class")
	public String getClassPath() {
		return classPath;
	}

	public void setClassPath(String classPath) {
		this.classPath = classPath;
	}

	@XmlElement(name = "Properties")
	public OCPublisherProperties getOcPublisherProperties() {
		return ocPublisherProperties;
	}

	public void setOcPublisherProperties(OCPublisherProperties ocPublisherProperties) {
		this.ocPublisherProperties = ocPublisherProperties;
	}
}
