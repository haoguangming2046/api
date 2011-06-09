/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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
package javax.ws.rs.client;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpRequest;
import javax.ws.rs.core.HttpResponse;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.ClientFactory;
import javax.ws.rs.ext.Providers;

/**
 * TODO javadoc.
 *
 * @author Marek Potociar
 * @since 2.0
 */
public abstract class Client {

    public static interface Builder<T extends Client, C extends ClientConfiguration> {

        T create();

        T create(C config);
    }
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    // Factory
    public static <B extends Builder<?, ?>> B providedBy(Class<? extends ClientFactory<B>> factoryClass) {
        return getFactory(factoryClass).createClientBuilder();
    }

    private static <C extends ClientFactory<?>> C getFactory(Class<C> factoryClass) {
        try {
            return factoryClass.newInstance(); // TODO instance caching(?), injecting, setup, etc.
        } catch (InstantiationException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, "Unable to instantiate client builder factory.", ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, "Unable to instantiate client builder factory.", ex);
        }

        return null;
    }

    public static Client create() {
        // todo implement
        return null;
    }

    public static Client create(ClientConfiguration configuration) {
        // todo implement
        return null;
    }
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Protected constructor used by concrete implementations of the {@link Client}
     * class.
     * <p />
     * Use one of the static {@code Client.create(...)} factory methods to obtain
     * a new JAX-RS client instance.
     *
     * @param configuration holder for the client configuration
     */
    protected Client(final ClientConfiguration configuration) {
        // todo implement
    }

    // Clean-up
    /**
     * Closes the client. Any system resources associated with the client
     * will be cleaned up.
     * <p>
     * This method must be called when there are not responses pending otherwise
     * undefined behavior will occur.
     * <p>
     * The client must not be reused after this method is called otherwise
     * undefined behavior will occur.
     *
     * TODO implement auto-closeable from JavaSE 7?
     */
    public final void close() {
        if (closed.compareAndSet(false, true)) {
            onClose();
        }
    }

    /**
     * Called when the client gets destroyed. Any system resources associated with
     * the client instance MUST be cleaned up in this method. It is guaranteed that
     * the method will be called only once.
     *
     * @see #close()
     */
    protected abstract void onClose();

    @Override
    @SuppressWarnings("FinalizeDeclaration")
    protected void finalize() throws Throwable {
        try {
            if (closed.compareAndSet(false, true)) {
                LOGGER.log(Level.WARNING, "{0} instance leak detected.", this.getClass().getName());
                onClose();
            }
        } finally {
            super.finalize();
        }
    }

    // Getters
    /**
     * Get the {@link Providers} utilized by the client.
     *
     * @return the {@link Providers} utilized by the client.
     */
    public abstract Providers getProviders();

    /**
     * Get the immutable client property bag.
     * <p />
     * When creating new {@link ResourceUri} instances or {@link Invocation}s using
     * a {@link Client} instance, the properties and features set on the {@code Client}
     * instance are inherited by the child instances being created. Similarly,
     * when creating new {@code Invocations} or derived {@code ResourceUri}s using
     * a {@code ResourceUri} instance, the parent {@code ResourceUri} instance
     * properties and features are inherited by the child instances being created.
     * The set of inherited features and properties on the child instance reflects the
     * state of the parent set of features and properties at the time of the child
     * instance creation. Once the child instance is created its properties and features
     * are detached from the parent configuration. This means that any subsequent
     * changes in the parent configuration MUST NOT affect the configuration of
     * previously created child instances.
     * <p />
     * Once the child instance is created, it's configuration can be further customized
     * using the provided set of instance configuration mutator methods. A change
     * made in the configuration of a child instance MUST NOT affect the configuration
     * of its parent.
     *
     * @return the property bag.
     */
    public abstract Map<String, Object> getProperties();

    /**
     * Determine if a feature is enabled for the client instance.
     *
     * @param featureName the name of the feature.
     * @return {@code true} if the feature value is present in the property bag
     *     and is an instance of {@link java.lang.Boolean} and that value is
     *     {@code true}, otherwise {@code false}.
     * @see #getProperties()
     */
    public abstract boolean isEnabled(final String featureName);

    /**
     * Set the configuration property for the client instance.
     *
     * @param name property name.
     * @param value property value.
     * @return the updated client instance.
     * @see #getProperties()
     */
    public abstract Client property(String name, Object value);

    /**
     * Enable a feature for the client instance.
     *
     * @param featureName feature name.
     * @return the updated client instance.
     * @see #getProperties()
     */
    public abstract Client enable(String featureName);

    /**
     * Disable a feature for the client instance.
     *
     * @param featureName feature name.
     * @return the updated client instance.
     * @see #getProperties()
     */
    public abstract Client disable(String featureName);

    /**
     * Set new properties for the client instance (replaces everything
     * previously set).
     *
     * @param properties set of properties for the client. The content of
     *     the map will replace any existing properties set on the client.
     * @return the updated client instance.
     * @see #getProperties()
     */
    public abstract Client properties(Map<String, Object> properties);

    // Request builder methods
    public abstract HttpRequest.Builder<Invocation> request(String uri) throws IllegalArgumentException, NullPointerException;

    public abstract HttpRequest.Builder<Invocation> request(URI uri) throws NullPointerException;

    public abstract HttpRequest.Builder<Invocation> request(UriBuilder uriBuilder) throws NullPointerException;

    // Request builder methods    
    public abstract ResourceUri resourceUri(String uri) throws IllegalArgumentException, NullPointerException;

    public abstract ResourceUri resourceUri(URI uri) throws NullPointerException;

    public abstract ResourceUri resourceUri(UriBuilder uriBuilder) throws NullPointerException;

    // Invocation methods
    public abstract HttpResponse invoke(HttpRequest request) throws InvocationException;

    public abstract <T> T invoke(HttpRequest request, Class<T> responseType) throws InvocationException;

    public abstract <T> T invoke(HttpRequest request, GenericType<T> responseType) throws InvocationException;

    public abstract Future<HttpResponse> start(HttpRequest request);

    public abstract <T> Future<T> start(HttpRequest<?> request, Class<T> responseType);

    public abstract <T> Future<T> start(HttpRequest<?> request, GenericType<T> responseType);

    public abstract <T> Future<T> start(HttpRequest<?> request, InvocationCallback<T> callback);
}
