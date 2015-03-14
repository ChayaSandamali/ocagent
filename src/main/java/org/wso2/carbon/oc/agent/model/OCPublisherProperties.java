package org.wso2.carbon.oc.agent.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "Properties")
public class OCPublisherProperties {
	@XmlElement(name = "Property")
	//abstract class wont work concrete class
	private List<OCPublisherProperty> pList;

	public List<OCPublisherProperty> getPropertyList() {
		return pList;
	}

	public void setPropertyList(List<OCPublisherProperty> propertyList) {
		this.pList = propertyList;
	}

	public Map<String, String> getPropertyMap() {
		Map<String, String> map = new HashMap<String, String>();

		List<OCPublisherProperty> propList = this.getPropertyList();
		for (OCPublisherProperty p : propList) {
			map.put(p.getName(), p.getValue());
		}

		return map;
	}
}
