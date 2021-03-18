/*
 * Copyright 2013-2019 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
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
