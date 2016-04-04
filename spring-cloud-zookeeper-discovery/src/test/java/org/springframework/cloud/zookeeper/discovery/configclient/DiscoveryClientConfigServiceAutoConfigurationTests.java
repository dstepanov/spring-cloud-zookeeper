/*
 * Copyright 2013-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.zookeeper.discovery.configclient;

import java.util.Arrays;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.commons.util.UtilAutoConfiguration;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.config.client.DiscoveryClientConfigServiceBootstrapConfiguration;
import org.springframework.cloud.zookeeper.ZookeeperAutoConfiguration;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryClient;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryClientConfiguration;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;

/**
 * @author Dave Syer
 */
public class DiscoveryClientConfigServiceAutoConfigurationTests {

	private AnnotationConfigApplicationContext context;

	@After
	public void close() {
		if (this.context != null) {
			if (this.context.getParent() != null) {
				((AnnotationConfigApplicationContext) this.context.getParent()).close();
			}
			this.context.close();
		}
	}

	@Test
	public void onWhenRequested() throws Exception {
		setup("spring.cloud.config.discovery.enabled=true",
				"spring.cloud.zookeeper.discovery.metadata.foo:bar",
				"spring.cloud.zookeeper.discovery.port:7001",
				"spring.cloud.zookeeper.discovery.instanceHost:foo");
		assertEquals( 1, this.context
						.getBeanNamesForType(ZookeeperConfigServerAutoConfiguration.class).length);
		ZookeeperDiscoveryClient client = this.context.getParent().getBean(
				ZookeeperDiscoveryClient.class);
		Mockito.verify(client, times(2)).getInstances("configserver");
		ConfigClientProperties locator = this.context
				.getBean(ConfigClientProperties.class);
		assertEquals("http://foo:7001/", locator.getRawUri());
	}

	private void setup(String... env) {
		AnnotationConfigApplicationContext parent = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(parent, env);
		parent.register(UtilAutoConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class, EnvironmentKnobbler.class,
				ZookeeperDiscoveryClientConfigServiceBootstrapConfiguration.class,
				DiscoveryClientConfigServiceBootstrapConfiguration.class,
				ConfigClientProperties.class);
		parent.refresh();
		this.context = new AnnotationConfigApplicationContext();
		this.context.setParent(parent);
		this.context.register(PropertyPlaceholderAutoConfiguration.class,
				ZookeeperConfigServerAutoConfiguration.class, ZookeeperAutoConfiguration.class,
				ZookeeperDiscoveryClientConfiguration.class);
		this.context.refresh();
	}

	@Configuration
	protected static class EnvironmentKnobbler {

		@Bean
		public ZookeeperDiscoveryClient zookeeperDiscoveryClient(
				ZookeeperDiscoveryProperties properties) {
			ZookeeperDiscoveryClient client = Mockito.mock(ZookeeperDiscoveryClient.class);
			ServiceInstance instance = new DefaultServiceInstance("configserver",
					properties.getInstanceHost(), 7001/*properties.getPort()*/, false);
			given(client.getInstances("configserver"))
					.willReturn(Arrays.asList(instance));
			return client;
		}

	}

}
