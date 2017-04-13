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
