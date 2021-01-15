/*
 * Copyright (c) 2017, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 package com.megaease.easeagent.core;

import com.google.common.base.Objects;

class QualifiedBean {
    final String qualifier;
    final Object bean;

    QualifiedBean(String qualifier, Object bean) {
        this.qualifier = qualifier;
        this.bean = bean;
    }

    boolean matches(Class<?> aClass, String qualifier) {
        return aClass.isInstance(bean) && this.qualifier.equals(qualifier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QualifiedBean that = (QualifiedBean) o;
        return Objects.equal(qualifier, that.qualifier) &&
                Objects.equal(bean, that.bean);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(qualifier, bean);
    }

    @Override
    public String toString() {
        return "QualifiedBean{" +
                "qualifier='" + qualifier + '\'' +
                ", bean=" + bean +
                '}';
    }
}
