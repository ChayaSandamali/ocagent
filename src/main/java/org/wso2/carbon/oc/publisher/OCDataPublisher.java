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

import java.util.Map;

/**
 *
 */
public interface OCDataPublisher {

	/**
	 * @param configMap - c.xml data keys can find on - OCPublisherConstants
	 */
	void init(Map<String, String> configMap);

	/**
	 * publish data to endpoint/resource
	 */
	void publish(Map<String, Object> dataMap);

	/**
	 * @return interval time mili-seconds - periodical data publishing time
	 */
	long getInterval();
}
