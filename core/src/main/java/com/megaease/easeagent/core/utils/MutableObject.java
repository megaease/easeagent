/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.core.utils;

public interface MutableObject<T> {

    static <S> MutableObject<S> wrap(S object) {
        return new DefaultMutableObject<>(object);
    }

    static <S> MutableObject<S> nullMutableObject() {
        return new DefaultMutableObject<>(null);
    }

    T getValue();

    void setValue(T t);

    class DefaultMutableObject<T> implements MutableObject<T> {
        private T value;

        protected DefaultMutableObject(T t) {
            this.value = t;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        /**
         * <p>
         * Compares this object against the specified object. The result is {@code true} if and only if the argument
         * is not {@code null} and is a {@code MutableObject} object that contains the same {@code T}
         * value as this object.
         * </p>
         *
         * @param obj the object to compare with, {@code null} returns {@code false}
         * @return {@code true} if the objects are the same;
         * {@code true} if the objects have equivalent {@code value} fields;
         * {@code false} otherwise.
         */
        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            if (this.getClass().equals(obj.getClass())) {
                final MutableObject<?> that = (MutableObject<?>) obj;
                return this.value.equals(that.getValue());
            }
            return false;
        }

        /**
         * Returns the value's hash code or {@code 0} if the value is {@code null}.
         *
         * @return the value's hash code or {@code 0} if the value is {@code null}.
         */
        @Override
        public int hashCode() {
            return value == null ? 0 : value.hashCode();
        }


        /**
         * Returns the String value of this mutable.
         *
         * @return the mutable value as a string
         */
        @Override
        public String toString() {
            return value == null ? "null" : value.toString();
        }
    }
}
