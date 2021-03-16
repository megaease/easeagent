package com.megaease.easeagent.core;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.megaease.easeagent.config.Configs;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.jolokia.jvmagent.JvmAgent;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.UUID;

public class BootstrapTest {
    ObjectMapper json = new ObjectMapper();

    @Test
    public void should_work() throws Exception {
        HashMap<String, String> source = new HashMap<>();
        String text = UUID.randomUUID().toString();
        source.put("key", text);
        Configs configs = new Configs(source);
        final Instrumentation inst = Mockito.mock(Instrumentation.class);
        Bootstrap.registerMBeans(configs, inst);
        JvmAgent.premain("", inst);
        String baseUrl = "http://localhost:8778/jolokia/";
        RestTemplate client = new RestTemplate();

        {
            String actual = getConfigValue(baseUrl, client, "key");
            MatcherAssert.assertThat(actual, CoreMatchers.equalTo(text));
        }

        {
            String helloValue = UUID.randomUUID().toString();
            ResponseEntity<String> resp = client.postForEntity(baseUrl, "{\n" +
                    "\t\"type\":\"exec\",\n" +
                    "\t\"mbean\":\"com.megaease.easeagent:type=ConfigManager\",\n" +
                    "\t\"operation\":\"updateConfigs\",\n" +
                    "\t\"arguments\":[{\"hello\":\"" + helloValue + "\"}]\n" +
                    "}", String.class);
            MatcherAssert.assertThat(resp.getStatusCodeValue(), CoreMatchers.equalTo(200));
            MatcherAssert.assertThat(getConfigValue(baseUrl, client, "hello"), CoreMatchers.equalTo(helloValue));
            MatcherAssert.assertThat(getConfigValue(baseUrl, client, "key"), CoreMatchers.equalTo(text));
        }

    }

    private String getConfigValue(String baseUrl, RestTemplate client, String name) throws JsonProcessingException {
        String resp = client.getForObject(baseUrl + "read/com.megaease.easeagent:type=ConfigManager/Configs/" + name, String.class);
        String actual = json.readTree(resp).path("value").asText();
        return actual;
    }

}