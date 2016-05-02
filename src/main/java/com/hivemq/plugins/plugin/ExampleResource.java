package com.hivemq.plugins.plugin;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.hivemq.spi.message.QoS;
import com.hivemq.spi.message.Topic;
import com.hivemq.spi.services.BlockingSubscriptionStore;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * @author Christoph Schäbel
 */
@Path("/example")
@Produces("application/json")
public class ExampleResource {

    private final BlockingSubscriptionStore subscriptionStore;

    @Inject
    public ExampleResource(final BlockingSubscriptionStore subscriptionStore) {
        this.subscriptionStore = subscriptionStore;
    }

    @GET
    @Path("/subscriptions")
    public Response getSubscriptions() {

        //do not get all subscriptions on large production systems, because it might consume a lot of memory
        final Multimap<String, Topic> subscriptions = subscriptionStore.getSubscriptions();
        return Response.ok(subscriptions.asMap()).build();
    }


    @POST
    @Path("subscription/{clientid}/{topic}/{qos}")
    public Response addSubscription(@PathParam("clientid") final String clientid,
                                    @PathParam("topic") final String topic,
                                    @PathParam("qos") final String qos) {

        final int qosNum;
        try {
            qosNum = Integer.parseInt(qos);
        } catch (NumberFormatException e) {
            return Response.serverError().build();
        }

        final Topic topicObject = new Topic(topic, QoS.valueOf(qosNum));
        subscriptionStore.addSubscription(clientid, topicObject);
        return Response.ok().build();
    }

}
