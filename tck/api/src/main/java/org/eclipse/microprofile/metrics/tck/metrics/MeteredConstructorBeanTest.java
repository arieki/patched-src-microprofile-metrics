/**
 * Copyright © 2013 Antonin Stefanutti (antonin.stefanutti@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.microprofile.metrics.tck.metrics;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@RunWith(Arquillian.class)
public class MeteredConstructorBeanTest {

    private final static String METER_NAME = "meteredConstructor";

    private static MetricID meterMID;

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
                // Test bean
                .addClass(MeteredConstructorBean.class)
                // Bean archive deployment descriptor
                .addAsWebInfResource("beans.xml", "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private Instance<MeteredConstructorBean> instance;

    // @Test
    // @InSequence(1)
    // public void meteredConstructorNotCalledYet() {
    // assertThat("Meter is not registered correctly", registry.getMeters().keySet(), is(empty()));
    // }

    @Before
    public void instantiateTest() {
        /*
         * The MetricID relies on the MicroProfile Config API. Running a managed arquillian container will result with
         * the MetricID being created in a client process that does not contain the MPConfig impl.
         *
         * This will cause client instantiated MetricIDs to throw an exception. (i.e the global MetricIDs)
         */
        meterMID = new MetricID(METER_NAME);
    }

    @Test
    @InSequence(1)
    public void meteredConstructorCalled() {
        long count = 1L + Math.round(Math.random() * 10);
        for (int i = 0; i < count; i++) {
            instance.get();
        }

        Meter meter = registry.getMeter(meterMID);
        assertThat("Meter is not registered correctly", meter, notNullValue());

        // Make sure that the meter has been called
        assertThat("Meter count is incorrect", meter.getCount(), is(equalTo(count)));
    }
}
