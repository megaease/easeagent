package com.megaease.easeagent.zipkin.http.reactive;

public interface GatewayCons {

    String SPAN_KEY = GatewayCons.class.getName() + ".SPAN";
    String CHILD_SPAN_KEY = GatewayCons.class.getName() + ".CHILD_SPAN";
    String CLIENT_RECEIVE_CALLBACK_KEY = GatewayCons.class.getName() + ".CLIENT_RECEIVE_CALLBACK";
}
