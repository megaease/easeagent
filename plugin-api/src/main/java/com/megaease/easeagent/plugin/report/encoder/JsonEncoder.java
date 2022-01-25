package com.megaease.easeagent.plugin.report.encoder;

import com.megaease.easeagent.plugin.report.Encoder;

import java.util.List;

/**
 * JSON Encoder
 * @param <T> abstract type
 */
public abstract class JsonEncoder<T> implements Encoder<T> {
    @Override
    public byte[] encodeList(List<byte[]> encodedItems) {
        int sizeOfArray = 2;
        int length = encodedItems.size();
        for (int i = 0; i < length; ) {
            sizeOfArray += encodedItems.get(i++).length;
            if (i < length) sizeOfArray++;
        }

        byte[] buf = new byte[sizeOfArray];
        int pos = 0;
        buf[pos++] = '[';
        for (int i = 0; i < length; ) {
            byte[] v = encodedItems.get(i++);
            System.arraycopy(v, 0, buf, pos, v.length);
            pos += v.length;
            if (i < length) buf[pos++] = ',';
        }
        buf[pos] = ']';
        return buf;
    }

    @Override
    public int packageSizeInBytes(List<Integer> sizes) {
        int sizeInBytes = 2; // brackets

        for (int i = 0, length = sizes.size(); i < length; i++) {
            sizeInBytes += sizes.get(i);
            if (i < length - 1) {
                sizeInBytes++;
            }
        }
        return sizeInBytes;
    }

    @Override
    public int appendSizeInBytes(List<Integer> sizes, int newMsgSize) {
        if (sizes.isEmpty()) {
            return newMsgSize;
        } else {
            return newMsgSize + 1;
        }
    }
}
