package com.hivemq.plugins.plugin;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.hivemq.spi.services.AsyncClientService;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

public class AsyncExampleServlet extends HttpServlet {

    private final AsyncClientService clientService;

    @Inject
    public AsyncExampleServlet(final AsyncClientService asyncClientService) {
        this.clientService = asyncClientService;
    }

    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        final AsyncContext asyncContext = request.startAsync(request, response);

        //since the information could be extremely large and will be collected from all cluster nodes
        //choose a very large timeout to handle worst-case delays.
        asyncContext.setTimeout(5000);

        final ListenableFuture<Set<String>> future = clientService.getConnectedClients();

        Futures.addCallback(future, new FutureCallback<Set<String>>() {

            @Override
            public void onSuccess(final Set<String> result) {
                try {
                    final PrintWriter writer = asyncContext.getResponse().getWriter();

                    writer.write("<html><body><ul>");

                    for (String clientId : result) {
                        writer.write("<li>" + clientId + "</li>");
                    }
                    writer.write("</ul></body></html>");

                    asyncContext.complete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(final Throwable t) {
                final HttpServletResponse httpResponse = (HttpServletResponse) asyncContext.getResponse();
                try {
                    httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                asyncContext.complete();
            }
        });
    }
}