package org.wso2.carbon.oc.agent.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "OperationsCenter")
public class OCConfiguration {

	@XmlElement(name = "Publishers")
	private OCPublishers publishers;

	public OCPublishers getOcPublishers() {
		return publishers;
	}

	public void setOcPublishers(OCPublishers publishers) {
		this.publishers = publishers;
	}
}
