package com.megaease.easeagent.zipkin.kafka.brave;

public class MultiData<A, B> {
    public final A data0;
    public final B data1;
//    public final C data2;
//    public final D data3;

    public MultiData(A data0, B data1) {
        this.data0 = data0;
        this.data1 = data1;
//        this.data2 = data2;
//        this.data3 = data3;
    }
}
