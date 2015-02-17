package org.wso2.carbon.oc.internal.messages;

/**
 * Created by noelyahan on 2/16/15.
 */
public class OCEvent {

//    private _RegistrationRequest event;

    private OCPayload event;

    public OCEvent() {
        this.event = new OCPayload();
    }

    public OCPayload getEvent() {
        return event;
    }

    public void setPayload(IMessage payload) {
        this.event.setPayload(payload);
    }
}
