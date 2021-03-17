package com.megaease.easeagent.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RemoteInfo {
    private String host;
    private int port;

}
