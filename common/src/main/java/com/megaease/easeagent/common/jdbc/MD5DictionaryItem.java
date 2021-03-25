package com.megaease.easeagent.common.jdbc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MD5DictionaryItem {

    //global field
    private long timestamp;

    private String category;

    @JsonProperty("host_name")
    private String hostName;

    @JsonProperty("host_ipv4")
    private String hostIpv4;

    private String gid;

    private String service;

    private String type;

    private String tags;

    private String id;

    // self field
    private String md5;

    private String sql;
}
