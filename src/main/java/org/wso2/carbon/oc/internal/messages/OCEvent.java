package org.wso2.carbon.oc.internal.messages;

/**
 * Created by noelyahan on 12/23/14.
 */
public class OCEvent {
    private _SynchronizationRequest payload;

    public OCEvent() {
        this.payload = new _SynchronizationRequest();
        this.payload.setTimestamp(System.currentTimeMillis());
    }

    public _SynchronizationRequest getPayload() {
        return payload;
    }

}
