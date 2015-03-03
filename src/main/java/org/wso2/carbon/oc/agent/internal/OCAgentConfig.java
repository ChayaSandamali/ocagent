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
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class helps to map operations-center.xml data
 */
public class OCAgentConfig {
	private static Logger logger = LoggerFactory.getLogger(OCAgentConfig.class);
	private static Publishers publishers;

	/**
	 * Extract all enabled publisher info
	 * @return Publishers - get all publisher objects
	 */
	public static Publishers getPublishers() {
		try {
			JAXBContext context = JAXBContext.newInstance(OCAgentConfig.OperationsCenter.class);
			Unmarshaller um = context.createUnmarshaller();
			OperationsCenter oc = (OperationsCenter) um.unmarshal(new FileReader(
					CarbonUtils.getCarbonHome() + "/repository/conf/" +
					OCAgentConstants.OC_XML));
			publishers = oc.getPublishers();
		} catch (JAXBException e) {
			logger.info(e.getMessage(), e);
		} catch (FileNotFoundException e) {
			logger.info(OCAgentConstants.OC_XML + " is missing in this path", e);
		}

		return publishers;
	}

	@XmlRootElement(name = "OperationsCenter")
	public static class OperationsCenter {
		@XmlElement(name = "Publishers")
		private Publishers pubs;

		public Publishers getPublishers() {
			return pubs;
		}

		public void setPublishers(Publishers pubs) {
			this.pubs = pubs;
		}
	}

	@XmlRootElement(name = "Publishers")
	public static class Publishers {

		@XmlElement(name = "Publisher")
		private ArrayList<Publisher> publisherList;

		public ArrayList<Publisher> getPublishersList() {
			return publisherList;
		}

		public void setPublisherList(ArrayList<Publisher> publisherList) {
			this.publisherList = publisherList;
		}

	}

	@XmlRootElement(name = "Publisher")
	@XmlType(propOrder = { "name", "classPath", "properties" })
	public static class Publisher {

		private String name;
		private String classPath;
		private Properties properties;

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
		public Properties getProperties() {
			return properties;
		}

		public void setProperties(Properties properties) {
			this.properties = properties;
		}

	}

	@XmlRootElement(name = "Properties")
	public static class Properties {

		@XmlElement(name = "Property")
		//abstract class wont work concrete class
		private ArrayList<Property> pList;

		public ArrayList<Property> getPropertyList() {
			return pList;
		}

		public void setPropertyList(ArrayList<Property> propertyList) {
			this.pList = propertyList;
		}

		public Map<String, String> getPropertyMap() {
			Map<String, String> map = new HashMap<String, String>();

			List<Property> propList = this.getPropertyList();
			for (Property p : propList) {
				map.put(p.getName(), p.getValue());
			}

			return map;
		}

	}

	@XmlRootElement(name = "Property")
	public static class Property {

		private String name;
		private String value;

		@XmlAttribute(name = "name")
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@XmlValue
		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

	}

}
