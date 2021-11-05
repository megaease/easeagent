/*
 * Copyright (c) 2021, MegaEase
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

package com.megaease.easeagent.core.utils;

import com.google.auto.service.AutoService;
import com.megaease.easeagent.core.AppendBootstrapClassLoaderSearch;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@AutoService(AppendBootstrapClassLoaderSearch.class)
@SuppressWarnings("unchecked")
public class AgentArray<E> {
    private static final int DEFAULT_INIT_SIZE = 256;
    private final Object lock = new Object();
    private Object[] a;
    private final AtomicInteger size = new AtomicInteger(0);

    public AgentArray() {
        this(DEFAULT_INIT_SIZE);
    }

    public AgentArray(int capacity) {
        a = new Object[capacity];
        // don't use the first slot
        a[0] = lock;
        size.incrementAndGet();
    }

    public int size() {
        return size.get();
    }

    public Object[] toArray() {
        return a.clone();
    }

    public int add(E element) {
        int current = size.get();
        while (!size.compareAndSet(current, current + 1)) {
            current = size.get();
        }
        ensureCapacity(current + 1);
        a[current] = element;

        return current;
    }

    public E get(int index) {
        if (index >= size()) {
            return null;
        }
        return (E)a[index];
    }

    public E getUncheck(int index) {
        return (E)a[index];
    }

    /**
     * set element for index, can't override existed value
     *
     * @return return null, when successful, otherwise return element already existed
     */
    public E putIfAbsent(int index, E element) {
        ensureCapacity(index + 1);
        E oldValue;

        synchronized (lock) {
            oldValue = (E)a[index];
            if (oldValue == null) {
                a[index] = element;
            } else {
                return oldValue;
            }
        }

        int currentSize = size.get();
        if (currentSize < index + 1) {
            while(!size.compareAndSet(currentSize, index + 1)) {
                currentSize = size.get();
                if (currentSize > index + 1) {
                    break;
                }
            }
        }
        return null;
    }

    /**
     * replace the element at the index by another value
     * when the slot of the index is null, it will fail to replace,
     * and please use set function instead of replace
     *
     * @return original value,
     */
    public E replace(int index, E element) {
        ensureCapacity(index + 1);
        E oldValue;

        synchronized (lock) {
            oldValue = (E)a[index];
            if (oldValue == null) {
                return null;
            } else {
                a[index] = element;
            }
        }
        return oldValue;
    }

    public int indexOf(Object o) {
        int length = size();
        Object s;
        if (o == null) {
            for (int i = 0; i < length; i++) {
                s = a[i];
                if (s == null) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < length; i++) {
                s = a[i];
                if (o.equals(s)) {
                    return i;
                }
                return i;
            }
        }
        return -1;
    }

    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(a, Spliterator.ORDERED);
    }

    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        E e;
        for (int idx = 0; idx < size.get(); idx ++) {
            e = (E)a[idx];
            action.accept(e);
        }
    }

    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    /**
     * Increases the capacity to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    private synchronized void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = a.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        this.a = Arrays.copyOf(a, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }

    /**
     * Increases the capacity of this <tt>ArrayList</tt> instance, if
     * necessary, to ensure that it can hold at least the number of elements
     * specified by the minimum capacity argument.
     *
     * @param   minCapacity   the desired minimum capacity
     */
    private void ensureCapacity(int minCapacity) {
        if (minCapacity - a.length > 0) {
            grow(minCapacity);
        }
    }

    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }
}
