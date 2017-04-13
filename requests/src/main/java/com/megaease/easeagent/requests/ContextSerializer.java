package com.megaease.easeagent.requests;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;

import java.io.IOException;
import java.lang.reflect.Type;

public class ContextSerializer implements ObjectSerializer {
    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        if (object instanceof Context) {
            final Context context = (Context) object;
            serializer.writeWithFieldName(context.getChildren().isEmpty() ? "" : JSON.toJSONString(context), fieldName);
        } else {
            throw new IllegalStateException("Unsupported " + object);
        }

    }
}
