/*
 * Copyright 2012-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.client;

import java.util.LinkedHashMap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtils.HostInfo;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * @author Spencer Gibb
 * @author Olga Maciaszek-Sharma
 */
public class HostInfoEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

	// Before BootstrapConfigFileApplicationListener, but after
	// ConfigDataEnvironmentPostProcessor
	private static final int ORDER = Math.addExact(ConfigDataEnvironmentPostProcessor.ORDER, 1);

	@Override
	public int getOrder() {
		return ORDER;
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) { // jxh: 获取本地网络地址
		InetUtils.HostInfo hostInfo = getFirstNonLoopbackHostInfo(environment);
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		map.put("spring.cloud.client.hostname", hostInfo.getHostname());
		map.put("spring.cloud.client.ip-address", hostInfo.getIpAddress());
		MapPropertySource propertySource = new MapPropertySource("springCloudClientHostInfo", map);
		environment.getPropertySources().addLast(propertySource);
	}

	private HostInfo getFirstNonLoopbackHostInfo(ConfigurableEnvironment environment) {
		InetUtilsProperties target = new InetUtilsProperties();
		ConfigurationPropertySources.attach(environment);
		Binder.get(environment).bind(InetUtilsProperties.PREFIX, Bindable.ofInstance(target));
		try (InetUtils utils = new InetUtils(target)) {
			return utils.findFirstNonLoopbackHostInfo();
		}
	}

}
