package com.megaease.easeagent.report.plugin;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.report.Encoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class NoOpEncoder<S> implements Encoder<S> {
    public static final NoOpEncoder<?> INSTANCE = new NoOpEncoder<>();

    @Override
    public void init(Config config) {
        // ignored
    }

    @Override
    public String name() {
        return "noop";
    }

    @Override
    public int sizeInBytes(S input) {
        return input.toString().length();
    }

    @Override
    public byte[] encode(S input) {
        return input.toString().getBytes(StandardCharsets.US_ASCII);
    }

    @Override
    public byte[] encodeList(List<byte[]> encodedItems) {
        StringBuilder sb = new StringBuilder();
        encodedItems.forEach(sb::append);
        return sb.toString().getBytes(StandardCharsets.US_ASCII);
    }

    @Override
    public int appendSizeInBytes(List<Integer> sizes, int newMsgSize) {
        return newMsgSize;
    }

    @Override
    public int packageSizeInBytes(List<Integer> sizes) {
        return sizes.stream().mapToInt(s -> s).sum();
    }
}
