package com.hivemq.plugins.plugin;

import com.google.inject.Inject;
import com.hivemq.spi.services.BlockingClientService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

public class ExampleServlet extends HttpServlet {

    private final BlockingClientService clientService;

    @Inject
    public ExampleServlet(final BlockingClientService clientService) {
        this.clientService = clientService;
    }

    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        final Set<String> connectedClients = clientService.getConnectedClients();
        final PrintWriter writer = response.getWriter();

        writer.write("<html><body><ul>");
        for (String clientId : connectedClients) {
            writer.write("<li>" + clientId + "</li>");
        }
        writer.write("</ul></body></html>");
    }
}