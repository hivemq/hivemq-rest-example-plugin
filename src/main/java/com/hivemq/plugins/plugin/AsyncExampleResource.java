package com.hivemq.plugins.plugin;

import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.hivemq.spi.message.QoS;
import com.hivemq.spi.message.Topic;
import com.hivemq.spi.services.AsyncSubscriptionStore;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeUnit;

/**
 * @author Christoph Schäbel
 */
@Path("/example/async")
@Produces("application/json")
public class AsyncExampleResource {

    private final AsyncSubscriptionStore subscriptionStore;

    @Inject
    public AsyncExampleResource(final AsyncSubscriptionStore subscriptionStore) {
        this.subscriptionStore = subscriptionStore;
    }

    @GET
    @Path("/subscriptions")
    public void getSubscriptions(@Suspended final AsyncResponse asyncResponse) {

        //since the information could be extremely large and will be collected from all cluster nodes
        //choose a very large timeout to handle worst-case delays.
        asyncResponse.setTimeout(5, TimeUnit.SECONDS);

        //do not get all subscriptions on large production systems, because it might consume a lot of memory
        final ListenableFuture<Multimap<String, Topic>> future = subscriptionStore.getSubscriptions();

        Futures.addCallback(future, new FutureCallback<Multimap<String, Topic>>() {
            @Override
            public void onSuccess(final Multimap<String, Topic> result) {
                final Response response = Response.ok(result.asMap()).build();
                asyncResponse.resume(response);
            }

            @Override
            public void onFailure(final Throwable t) {
                asyncResponse.resume(t);
            }
        });
    }


    @POST
    @Path("subscription/{clientid}/{topic}/{qos}")
    public void addSubscription(@Suspended final AsyncResponse asyncResponse,
                                @PathParam("clientid") final String clientid,
                                @PathParam("topic") final String topic,
                                @PathParam("qos") final String qos) {

        asyncResponse.setTimeout(5, TimeUnit.SECONDS);

        //skipped checking every parameter because it is just an example

        try {

            final int qosNum;
            try {
                qosNum = Integer.parseInt(qos);
            } catch (NumberFormatException e) {
                asyncResponse.resume(e);
                return;
            }

            final Topic topicObject = new Topic(topic, QoS.valueOf(qosNum));
            final ListenableFuture<Void> future = subscriptionStore.addSubscription(clientid, topicObject);

            Futures.addCallback(future, new FutureCallback<Void>() {
                @Override
                public void onSuccess(final Void result) {
                    asyncResponse.resume(Response.ok().build());
                }

                @Override
                public void onFailure(final Throwable t) {
                    asyncResponse.resume(t);
                }
            });

        } catch (Exception e) {
            asyncResponse.resume(e);
        }

    }

}
