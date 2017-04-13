package com.megaease.easeagent.gen;

import com.google.common.io.ByteStreams;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.io.IOException;
import java.io.InputStream;

public class FileMatchers {
    static BaseMatcher<String> be(String generated) throws IOException {
        final InputStream in = FileMatchers.class.getClassLoader().getResourceAsStream(generated);
        final String expected = new String(ByteStreams.toByteArray(in));
        return new BaseMatcher<String>() {
            @Override
            public boolean matches(Object item) {
                return item.toString().equals(expected);
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(expected);
            }

        };
    }
}
