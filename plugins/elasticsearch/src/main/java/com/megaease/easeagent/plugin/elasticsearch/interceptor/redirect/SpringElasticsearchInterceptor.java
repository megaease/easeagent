package com.megaease.easeagent.plugin.elasticsearch.interceptor.redirect;

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.middleware.MiddlewareConfigProcessor;
import com.megaease.easeagent.plugin.api.middleware.ResourceConfig;
import com.megaease.easeagent.plugin.elasticsearch.ElasticsearchRedirectPlugin;
import com.megaease.easeagent.plugin.elasticsearch.advice.SpringElasticsearchAdvice;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.utils.common.StringUtils;

import java.util.ArrayList;
import java.util.List;

@AdviceTo(value = SpringElasticsearchAdvice.class, plugin = ElasticsearchRedirectPlugin.class)
public class SpringElasticsearchInterceptor implements NonReentrantInterceptor {
    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        ResourceConfig cnf = MiddlewareConfigProcessor.INSTANCE.getData(MiddlewareConfigProcessor.ENV_ES);
        if (cnf == null) {
            return;
        }
        String method = methodInfo.getMethod();
        List<String> uris = this.formatUris(cnf.getUriList());
        if (method.equals("setUsername") && StringUtils.isNotEmpty(cnf.getUserName())) {
            methodInfo.changeArg(0, cnf.getUserName());
        } else if (method.equals("setPassword") && StringUtils.isNotEmpty(cnf.getPassword())) {
            methodInfo.changeArg(0, cnf.getPassword());
        } else if (method.equals("setEndpoints")) {
            methodInfo.changeArg(0, uris);
        } else if (method.equals("setUris")) {
            methodInfo.changeArg(0, uris);
        }

    }

    private List<String> formatUris(List<String> uriList) {
        List<String> list = new ArrayList<>();
        for (String uri : uriList) {
            if (uri.startsWith("http://") || uri.startsWith("https://")) {
                list.add(uri);
            } else {
                list.add("http://" + uri);
            }
        }
        return list;
    }

    @Override
    public String getName() {
        return Order.REDIRECT.getName();
    }

}
