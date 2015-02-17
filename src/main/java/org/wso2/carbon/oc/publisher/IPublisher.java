package org.wso2.carbon.oc.publisher;

/**
 * Created by noelyahan on 12/22/14.
 */
public interface IPublisher {

    void publish();

    long getInitialDelay();

    long getInterval();
}
