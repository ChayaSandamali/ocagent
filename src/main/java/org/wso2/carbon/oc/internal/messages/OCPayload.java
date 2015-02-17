package org.wso2.carbon.oc.internal.messages;

/**
 * Created by noelyahan on 2/16/15.
 */
public class OCPayload {

    private IMessage payload;


    public IMessage getPayload() {
        return payload;
    }

    public void setPayload(IMessage payload) {
        this.payload = payload;
    }
}
