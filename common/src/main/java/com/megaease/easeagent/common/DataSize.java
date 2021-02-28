package com.megaease.easeagent.common;

import java.util.regex.Pattern;

public class DataSize {


    /**
     * Bytes per Kilobyte.
     */
    private static final long BYTES_PER_KB = 1024;

    /**
     * Bytes per Megabyte.
     */
    private static final long BYTES_PER_MB = BYTES_PER_KB * 1024;

    /**
     * Bytes per Gigabyte.
     */
    private static final long BYTES_PER_GB = BYTES_PER_MB * 1024;

    /**
     * Bytes per Terabyte.
     */
    private static final long BYTES_PER_TB = BYTES_PER_GB * 1024;


    private final long bytes;


    private DataSize(long bytes) {
        this.bytes = bytes;
    }


    /**
     * Obtain a {@link DataSize} representing the specified number of bytes.
     *
     * @param bytes the number of bytes, positive or negative
     * @return a {@link DataSize}
     */
    public static DataSize ofBytes(long bytes) {
        return new DataSize(bytes);
    }

    /**
     * Obtain a {@link DataSize} representing the specified number of kilobytes.
     *
     * @param kilobytes the number of kilobytes, positive or negative
     * @return a {@link DataSize}
     */
    public static DataSize ofKilobytes(long kilobytes) {
        return new DataSize(Math.multiplyExact(kilobytes, BYTES_PER_KB));
    }

    /**
     * Obtain a {@link DataSize} representing the specified number of megabytes.
     *
     * @param megabytes the number of megabytes, positive or negative
     * @return a {@link DataSize}
     */
    public static DataSize ofMegabytes(long megabytes) {
        return new DataSize(Math.multiplyExact(megabytes, BYTES_PER_MB));
    }

    /**
     * Obtain a {@link DataSize} representing the specified number of gigabytes.
     *
     * @param gigabytes the number of gigabytes, positive or negative
     * @return a {@link DataSize}
     */
    public static DataSize ofGigabytes(long gigabytes) {
        return new DataSize(Math.multiplyExact(gigabytes, BYTES_PER_GB));
    }

    /**
     * Obtain a {@link DataSize} representing the specified number of terabytes.
     *
     * @param terabytes the number of terabytes, positive or negative
     * @return a {@link DataSize}
     */
    public static DataSize ofTerabytes(long terabytes) {
        return new DataSize(Math.multiplyExact(terabytes, BYTES_PER_TB));
    }

    /**
     * Obtain a {@link DataSize} representing an amount in the specified {@link DataUnit}.
     *
     * @param amount the amount of the size, measured in terms of the unit,
     *               positive or negative
     * @return a corresponding {@link DataSize}
     */
    public static DataSize of(long amount, DataUnit unit) {
        assertNotNull(unit, "Unit must not be null");
        return new DataSize(Math.multiplyExact(amount, unit.size().toBytes()));
    }

    /**
     * Checks if this size is negative, excluding zero.
     *
     * @return true if this size has a size less than zero bytes
     */
    public boolean isNegative() {
        return this.bytes < 0;
    }

    /**
     * Return the number of bytes in this instance.
     *
     * @return the number of bytes
     */
    public long toBytes() {
        return this.bytes;
    }

    /**
     * Return the number of kilobytes in this instance.
     *
     * @return the number of kilobytes
     */
    public long toKilobytes() {
        return this.bytes / BYTES_PER_KB;
    }

    /**
     * Return the number of megabytes in this instance.
     *
     * @return the number of megabytes
     */
    public long toMegabytes() {
        return this.bytes / BYTES_PER_MB;
    }

    /**
     * Return the number of gigabytes in this instance.
     *
     * @return the number of gigabytes
     */
    public long toGigabytes() {
        return this.bytes / BYTES_PER_GB;
    }

    /**
     * Return the number of terabytes in this instance.
     *
     * @return the number of terabytes
     */
    public long toTerabytes() {
        return this.bytes / BYTES_PER_TB;
    }

    @Override
    public String toString() {
        return String.format("%dB", this.bytes);
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        DataSize otherSize = (DataSize) other;
        return (this.bytes == otherSize.bytes);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.bytes);
    }

    private static void assertNotNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

}
