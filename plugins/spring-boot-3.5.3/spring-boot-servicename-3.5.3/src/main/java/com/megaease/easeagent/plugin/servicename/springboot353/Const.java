/*
 * Copyright (c) 2017, MegaEase
 * All rights reserved.
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

package com.megaease.easeagent.plugin.servicename.springboot353;

import com.megaease.easeagent.plugin.CodeVersion;
import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.api.config.ConfigConst;

public interface Const {
    //----------------- FeignClient begin ---------------
    /**
     * The new version has been Deprecated
     */
    String FeignLoadBalancer = "org.springframework.cloud.openfeign.ribbon.FeignLoadBalancer";
    /**
     * The new version has been Deprecated
     */
    String RetryableFeignLoadBalancer = "org.springframework.cloud.openfeign.ribbon.RetryableFeignLoadBalancer";

    /**
     * The new version has been Deprecated
     */
    String LoadBalancerFeignClient = "org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient";
    String FeignBlockingLoadBalancerClient = "org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient";
    String RetryableFeignBlockingLoadBalancerClient = "org.springframework.cloud.openfeign.loadbalancer.RetryableFeignBlockingLoadBalancerClient";
    //----------------- FeignClient end ---------------

    //----------------- RestTemplate begin ---------------
    String RetryLoadBalancerInterceptor = "org.springframework.cloud.client.loadbalancer.RetryLoadBalancerInterceptor";

    String LoadBalancerInterceptor = "org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor";
    //----------------- RestTemplate end ---------------

    //----------------- web client begin ---------------
    String ReactorLoadBalancerExchangeFilterFunction = "org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction";

    String RetryableLoadBalancerExchangeFilterFunction = "org.springframework.cloud.client.loadbalancer.reactive.RetryableLoadBalancerExchangeFilterFunction";
    //----------------- web client end ---------------

    //----------------- spring gateway begin ---------------
    String FilteringWebHandler = "org.springframework.cloud.gateway.handler.FilteringWebHandler";
    //----------------- spring gateway end ---------------
    String DEFAULT_PROPAGATE_HEAD = "X-Mesh-RPC-Service";
    String PROPAGATE_HEAD_CONFIG = "propagate.head";

    CodeVersion VERSIONS = CodeVersion.builder()
        .key(ConfigConst.CodeVersion.KEY_SPRING_BOOT)
        .add(Points.DEFAULT_VERSION)
        .add(ConfigConst.CodeVersion.VERSION_SPRING_BOOT3).build();
}
