package org.wso2.carbon.oc.agent.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "Publishers")
public class OCPublishers {

	@XmlElement(name = "Publisher")
	private List<OCPublisherConfiguration> publishers;

	public List<OCPublisherConfiguration> getPublishersList() {
		return publishers;
	}

	public void setPublisherList(List<OCPublisherConfiguration> publishers) {
		this.publishers = publishers;
	}
}
