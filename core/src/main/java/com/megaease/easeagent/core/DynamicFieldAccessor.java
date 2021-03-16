package com.megaease.easeagent.core;

import com.google.auto.service.AutoService;

@AutoService(AppendBootstrapClassLoaderSearch.class)
public interface DynamicFieldAccessor {

    void setEaseAgent$$DynamicField$$Data(Object data);

    Object getEaseAgent$$DynamicField$$Data();

}
