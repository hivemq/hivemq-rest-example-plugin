/*
 * Copyright 2015 dc-square GmbH
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

package com.hivemq.plugins.plugin;

import com.hivemq.spi.PluginEntryPoint;
import com.hivemq.spi.services.rest.RESTService;
import com.hivemq.spi.services.rest.listener.HttpListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * This is the main class of the plugin, which is instanciated during the HiveMQ start up process.
 *
 * @author Christoph Schaebel
 */
public class RestExampleEntryPoint extends PluginEntryPoint {

    Logger log = LoggerFactory.getLogger(RestExampleEntryPoint.class);

    private final RESTService restService;

    @Inject
    public RestExampleEntryPoint(final RESTService restService) {
        this.restService = restService;
    }

    /**
     * This method is executed after the instantiation of the whole class. It is used to initialize
     * the implemented callbacks and make them known to the HiveMQ core.
     */
    @PostConstruct
    public void postConstruct() {

        // add a listener on port 8888
        restService.addListener(new HttpListener("other", "0.0.0.0", 8888));

        //add the JAX-RS Resources to the RESTService
        restService.addJaxRsResources(AsyncExampleResource.class);
        restService.addJaxRsResources(ExampleResource.class);

        //add the Servlet to the RESTService
        restService.addServlet(AsyncExampleServlet.class, "/async/clients");
        restService.addServlet(ExampleServlet.class, "/clients");
    }

}
