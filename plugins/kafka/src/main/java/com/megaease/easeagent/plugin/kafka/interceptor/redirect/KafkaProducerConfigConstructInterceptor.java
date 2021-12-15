package com.megaease.easeagent.plugin.kafka.interceptor.redirect;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.kafka.KafkaRedirectPlugin;
import com.megaease.easeagent.plugin.kafka.advice.KafkaProducerConfigAdvice;

@AdviceTo(value = KafkaProducerConfigAdvice.class, qualifier = "constructor", plugin = KafkaRedirectPlugin.class)
public class KafkaProducerConfigConstructInterceptor extends KafkaAbstractConfigConstructInterceptor {
}
