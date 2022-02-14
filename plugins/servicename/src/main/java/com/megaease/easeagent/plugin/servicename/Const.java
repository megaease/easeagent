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

package com.megaease.easeagent.plugin.servicename;

public interface Const {
    String FeignLoadBalancer = "org.springframework.cloud.openfeign.ribbon.FeignLoadBalancer";
    String RetryableFeignLoadBalancer = "org.springframework.cloud.openfeign.ribbon.RetryableFeignLoadBalancer";
    String LoadBalancerFeignClient = "org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient";
    String FeignBlockingLoadBalancerClient = "org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient";
    String RetryLoadBalancerInterceptor = "org.springframework.cloud.client.loadbalancer.RetryLoadBalancerInterceptor";
    String AsyncLoadBalancerInterceptor = "org.springframework.cloud.client.loadbalancer.AsyncLoadBalancerInterceptor";
    String LoadBalancerInterceptor = "org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor";
    String ReactorLoadBalancerExchangeFilterFunction = "org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction";
    String LoadBalancerExchangeFilterFunction = "org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerExchangeFilterFunction";
    String FilteringWebHandler = "org.springframework.cloud.gateway.handler.FilteringWebHandler";

    String SERVER_WEB_EXCHANGE_ROUTE_ATTRIBUTE = "org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayRoute";
    String DEFAULT_PROPAGATE_HEAD = "X-Mesh-RPC-Service";
    String PROPAGATE_HEAD_CONFIG = "propagate.head";
}
