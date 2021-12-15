package com.megaease.easeagent.plugin.kafka.interceptor.redirect;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.kafka.KafkaRedirectPlugin;
import com.megaease.easeagent.plugin.kafka.advice.KafkaConsumerConfigAdvice;

@AdviceTo(value = KafkaConsumerConfigAdvice.class, qualifier = "constructor", plugin = KafkaRedirectPlugin.class)
public class KafkaConsumerConfigConstructInterceptor extends KafkaAbstractConfigConstructInterceptor {
}
