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

package org.wso2.carbon.oc.internal.messages;

/**
 * This is used build the json message in event, payload wise
 * using jackson
 */
public class OCEvent {

	private OCPayload event;

	public OCEvent() {
		this.event = new OCPayload();
	}

	/**
	 * Helps to get final event data with payload in it
	 * @return OCPayload - resource content
	 */
	public OCPayload getEvent() {
		return event;
	}

	public void setPayload(IMessage payload) {
		this.event.setPayload(payload);
	}
}
