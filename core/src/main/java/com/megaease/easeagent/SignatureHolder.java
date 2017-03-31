package com.megaease.easeagent;

import com.google.auto.service.AutoService;

@AutoService(AppendBootstrapClassLoaderSearch.class)
public class SignatureHolder {
    public static final ThreadLocal<String> CALLER = new ThreadLocal<String>();
}
