/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package jaxrs.examples.async;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.ResumeCallback;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Asynchronous event-based request processing example.
 *
 * @author Marek Potociar
 */
@Path("/async/nextMessage")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
public class AsyncEventResource implements ResumeCallback {
    private static final BlockingQueue<String> messages = new ArrayBlockingQueue<String>(5);

    @GET
    public void readMessage(@Suspended final AsyncResponse ar) {
        ar.register(AsyncEventResource.class);
        Executors.newSingleThreadExecutor().submit(new Runnable() {

            @Override
            public void run() {
                try {
                    ar.resume(messages.take());
                } catch (InterruptedException ex) {
                    Logger.getLogger(AsyncEventResource.class.getName()).log(Level.SEVERE, null, ex);
                    ar.cancel(); // close the open connection
                }
            }
        });
    }

    @POST
    public void postMessage(final String message, @Suspended final AsyncResponse asyncResponse) {
        Executors.newSingleThreadExecutor().submit(new Runnable() {

            @Override
            public void run() {
                try {
                    messages.put(message);
                    asyncResponse.resume("Message stored.");
                } catch (InterruptedException ex) {
                    Logger.getLogger(AsyncEventResource.class.getName()).log(Level.SEVERE, null, ex);
                    asyncResponse.resume(ex); // propagate info about the problem
                }
            }
        });
    }

    @Override
    public void onResume(AsyncResponse resuming, Response response) {
        System.out.println("Resumed with a response.");
    }

    @Override
    public void onResume(AsyncResponse resuming, Throwable error) {
        System.out.println("Resumed with error.");
    }
}
