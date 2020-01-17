// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.google.common.base.Utf8;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.google.common.base.Strings.lenientFormat;

enum RntbdTokenType {

    // All values are encoded as little endian byte sequences except for Guid
    // Guid values are serialized in Microsoft GUID byte order
    // Reference: GUID structure and System.Guid type

    Byte((byte) 0x00, RntbdByte.CODEC),                // byte => byte
    UShort((byte) 0x01, RntbdUnsignedShort.CODEC),     // short => int
    ULong((byte) 0x02, RntbdUnsignedInteger.CODEC),    // int => long
    Long((byte) 0x03, RntbdInteger.CODEC),             // int => int
    ULongLong((byte) 0x04, RntbdLong.CODEC),           // long => long
    LongLong((byte) 0x05, RntbdLong.CODEC),            // long => long

    Guid((byte) 0x06, RntbdGuid.CODEC),                // byte[16] => UUID

    SmallString((byte) 0x07, RntbdShortString.CODEC),  // (byte, byte[0..255]) => String
    String((byte) 0x08, RntbdString.CODEC),            // (short, byte[0..64KiB]) => String
    ULongString((byte) 0x09, RntbdLongString.CODEC),   // (int, byte[0..2GiB-1]) => String

    SmallBytes((byte) 0x0A, RntbdShortBytes.CODEC),    // (byte, byte[0..255]) => byte[]
    Bytes((byte) 0x0B, RntbdBytes.CODEC),              // (short, byte[0..64KiB]) => byte[]
    ULongBytes((byte) 0x0C, RntbdLongBytes.CODEC),     // (int, byte[0..2GiB-1])    => byte[]

    Float((byte) 0x0D, RntbdFloat.CODEC),              // float => float
    Double((byte) 0x0E, RntbdDouble.CODEC),            // double => double

    Invalid((byte) 0xFF, RntbdNone.CODEC);             // no data

    // region Implementation

    private Codec codec;
    private byte id;

    RntbdTokenType(final byte id, final Codec codec) {
        this.codec = codec;
        this.id = id;
    }

    public Codec codec() {
        return this.codec;
    }

    public static RntbdTokenType fromId(final byte value) {

        for (final RntbdTokenType tokenType : RntbdTokenType.values()) {
            if (value == tokenType.id) {
                return tokenType;
            }
        }
        return Invalid;
    }

    public byte id() {
        return this.id;
    }

    // endregion

    // region Types

    public interface Codec {

        int computeLength(Object value);

        Object convert(Object value);

        Object defaultValue();

        boolean isValid(Object value);

        Object read(ByteBuf in);

        ByteBuf readSlice(ByteBuf in);

        Class<?> valueType();

        void write(Object value, ByteBuf out);

        /**
         * Validates the length of a variable-length field at the reader index in a {@link ByteBuf}.
         * <p>
         * This method checks that: {@code length <= maxLength && length <= in.readableBytes()}.
         *
         * @param in an input {@linkplain ByteBuf} containing a variable-length field at {@linkplain
         * ByteBuf#readerIndex}.
         * @param length number of bytes in the variable-length field.
         * @param maxLength maximum length of the variable-length field.
         *
         * @throws IndexOutOfBoundsException if the check that {@code length <= maxLength && length <=
         * in.readableBytes()} fails.
         */
        static void checkReadableBytes(final ByteBuf in, final long length, final long maxLength) {

            final int readableBytes = in.readableBytes();

            if (length > readableBytes) {
                String reason = lenientFormat("readableBytes (%s) is less than field length (%s)",
                    readableBytes,
                    length);
                throw new IndexOutOfBoundsException(reason);
            }

            if (length > maxLength) {
                String reason = lenientFormat("field length (%s) exceeds maxLength (%s)",
                    length,
                    maxLength);
                throw new IndexOutOfBoundsException(reason);
            }
        }
    }

    private static final class RntbdByte implements Codec {

        public static final Codec CODEC = new RntbdByte();

        private RntbdByte() {
        }

        @Override
        public int computeLength(final Object value) {
            return java.lang.Byte.BYTES;
        }

        @Override
        public Object convert(final Object value) {

            assert this.isValid(value);

            if (value instanceof Number) {
                return ((Number) value).byteValue();
            }
            return (boolean) value ? (byte) 0x01 : (byte) 0x00;
        }

        @Override
        public Object defaultValue() {
            return (byte) 0;
        }

        @Override
        public boolean isValid(final Object value) {
            return value instanceof Number || value instanceof Boolean;
        }

        @Override
        public Object read(final ByteBuf in) {
            return in.readByte();
        }

        @Override
        public ByteBuf readSlice(final ByteBuf in) {
            return in.readSlice(java.lang.Byte.BYTES);
        }

        @Override
        public Class<?> valueType() {
            return java.lang.Byte.class;
        }

        @Override
        public void write(final Object value, final ByteBuf out) {
            assert this.isValid(value);
            out.writeByte(value instanceof Byte ? (byte) value : ((boolean) value ? 0x01 : 0x00));
        }
    }

    /**
     * Represents a variable-length byte sequence with maximum length of {@code 65535}.
     * <p>
     * A value of this type is referred to as a variable-length byte sequence.
     */
    private static class RntbdBytes implements Codec {

        public static final Codec CODEC = new RntbdBytes();
        private static final byte[] DEFAULT_VALUE = {};

        private RntbdBytes() {
        }

        @Override
        public int computeLength(final Object value) {
            assert this.isValid(value);
            return Short.BYTES + ((byte[]) value).length;
        }

        @Override
        public final Object convert(final Object value) {
            assert this.isValid(value);
            return value;
        }

        @Override
        public final Object defaultValue() {
            return DEFAULT_VALUE;
        }

        @Override
        public boolean isValid(final Object value) {
            return value instanceof byte[] && ((byte[]) value).length < 0xFFFF;
        }

        /**
         * Reads a variable-length byte sequence at the reader index of a {@link ByteBuf}.
         * <p>
         * This method reads two fields:
         * <table>
         * <thead><tr><th>Field</th><th>Description</th></tr></thead>
         * <tbody><tr>
         * <td>{@code length}</td><td>an unsigned 16-bit integer specifying the number of bytes in the sequence.</td>
         * </tr><tr>
         * <td>{@code value}</td><td>a sequence of {@code length} bytes.</td>
         * </tr></tbody>
         * </table>
         * <p>
         * The {@code value} is transferred to a newly created {@linkplain ByteBuf}. The input buffer's
         * {@linkplain ByteBuf#readerIndex readerIndex} is incremented by {@code length + 2}. The returned buffer's
         * {@linkplain ByteBuf#readerIndex readerIndex} and {@linkplain ByteBuf#writerIndex writerIndex} are set to
         * {@code 0} and {@code length} respectively.
         *
         * @param in the input {@linkplain ByteBuf}.
         *
         * @return a newly created {@linkplain ByteBuf} containing the transferred {@code value}.
         *
         * @throws IndexOutOfBoundsException if the input buffer's {@linkplain ByteBuf#readableBytes readableBytes}
         * count is:
         * <table>
         * <thead><tr><td>Count</td><td>Meaning</td></tr></thead>
         * <tbody>
         * <tr><td>less than {@code 2}</td><td>{@code length} cannot be read</td></tr>
         * <tr><td>less than {@code length + 2}</td><td>{@code value} cannot be read</td></tr>
         * </tbody>
         * </table>
         */
        @Override
        public Object read(final ByteBuf in) {
            final int length = in.readUnsignedShortLE();
            Codec.checkReadableBytes(in, length, 0xFFFF);
            return in.readBytes(length);
        }

        /**
         * Returns a new slice that spans a variable-length byte sequence at the reader index of a {@link ByteBuf}.
         * <p>
         * This method expects two fields:
         * <table>
         * <thead><tr><th>Field</th><th>Description</th></tr></thead>
         * <tbody><tr>
         * <td>{@code length}</td><td>an unsigned 16-bit integer specifying the number of bytes in the sequence.</td>
         * </tr><tr>
         * <td>{@code value}</td><td>a sequence of {@code length} bytes.</td>
         * </tr></tbody>
         * </table>
         * <p>
         * The newly created slice that is returned spans both fields. Its {@linkplain ByteBuf#readerIndex readerIndex}
         * and {@linkplain ByteBuf#writerIndex writerIndex} are set to {@code 0} and {@code length + 2} respectively.
         * The input buffer's {@linkplain ByteBuf#readerIndex readerIndex} is incremented by {@code length + 2}.
         *
         * @param in the input {@linkplain ByteBuf}.
         *
         * @return a newly created slice that spans a variable-length sequence of bytes.
         *
         * @throws IndexOutOfBoundsException if the input buffer's {@linkplain ByteBuf#readableBytes readableBytes}
         * count is:
         * <table>
         * <thead><tr><td>Count</td><td>Meaning</td></tr></thead>
         * <tbody>
         * <tr><td>less than {@code 2}</td><td>{@code length} cannot be read</td></tr>
         * <tr><td>less than {@code length + 2}</td><td>{@code value} cannot be read</td></tr>
         * </tbody>
         * </table>
         */
        @Override
        public ByteBuf readSlice(final ByteBuf in) {
            final int length = in.getUnsignedShortLE(in.readerIndex());
            return in.readSlice(Short.BYTES + length);
        }

        @Override
        public final Class<?> valueType() {
            return Byte[].class;
        }

        @Override
        public void write(final Object value, final ByteBuf out) {

            assert this.isValid(value);

            final byte[] bytes = (byte[]) value;
            final int length = bytes.length;

            if (length > 0xFFFF) {
                String reason = lenientFormat("value length (%s) is greater than maxLength (%s)",
                    length,
                    0xFFFFL);
                throw new EncoderException(reason);
            }

            out.writeShortLE((short) length);
            out.writeBytes(bytes);
        }
    }

    private static final class RntbdDouble implements Codec {

        public static final Codec CODEC = new RntbdDouble();

        private RntbdDouble() {
        }

        @Override
        public int computeLength(final Object value) {
            assert this.isValid(value);
            return java.lang.Double.BYTES;
        }

        @Override
        public Object convert(final Object value) {
            assert this.isValid(value);
            return ((Number) value).doubleValue();
        }

        @Override
        public Object defaultValue() {
            return 0.0D;
        }

        @Override
        public boolean isValid(final Object value) {
            return value instanceof Number;
        }

        @Override
        public Object read(final ByteBuf in) {
            return in.readDoubleLE();
        }

        @Override
        public ByteBuf readSlice(final ByteBuf in) {
            return in.readSlice(java.lang.Double.BYTES);
        }

        @Override
        public Class<?> valueType() {
            return Double.class;
        }

        @Override
        public void write(final Object value, final ByteBuf out) {
            assert this.isValid(value);
            out.writeDoubleLE(((Number) value).doubleValue());
        }
    }

    private static final class RntbdFloat implements Codec {

        public static final Codec CODEC = new RntbdFloat();

        private RntbdFloat() {
        }

        @Override
        public int computeLength(final Object value) {
            assert this.isValid(value);
            return java.lang.Float.BYTES;
        }

        @Override
        public Object convert(final Object value) {
            assert this.isValid(value);
            return ((Number) value).floatValue();
        }

        @Override
        public Object defaultValue() {
            return 0.0F;
        }

        @Override
        public boolean isValid(final Object value) {
            return value instanceof Number;
        }

        @Override
        public Object read(final ByteBuf in) {
            return in.readFloatLE();
        }

        @Override
        public ByteBuf readSlice(final ByteBuf in) {
            return in.readSlice(java.lang.Float.BYTES);
        }

        @Override
        public Class<?> valueType() {
            return Float.class;
        }

        @Override
        public void write(final Object value, final ByteBuf out) {
            assert this.isValid(value);
            out.writeFloatLE(((Number) value).floatValue());
        }
    }

    private static final class RntbdGuid implements Codec {

        public static final Codec CODEC = new RntbdGuid();

        private RntbdGuid() {
        }

        @Override
        public int computeLength(final Object value) {
            assert this.isValid(value);
            return 2 * java.lang.Long.BYTES;
        }

        @Override
        public Object convert(final Object value) {
            assert this.isValid(value);
            return value;
        }

        @Override
        public Object defaultValue() {
            return RntbdUUID.EMPTY;
        }

        @Override
        public boolean isValid(final Object value) {
            return value instanceof UUID;
        }

        @Override
        public Object read(final ByteBuf in) {
            return RntbdUUID.decode(in);
        }

        @Override
        public ByteBuf readSlice(final ByteBuf in) {
            return in.readSlice(2 * java.lang.Long.BYTES);
        }

        @Override
        public Class<?> valueType() {
            return UUID.class;
        }

        @Override
        public void write(final Object value, final ByteBuf out) {
            assert this.isValid(value);
            RntbdUUID.encode((UUID) value, out);
        }
    }

    private static final class RntbdInteger implements Codec {

        public static final Codec CODEC = new RntbdInteger();

        private RntbdInteger() {
        }

        @Override
        public int computeLength(final Object value) {
            assert this.isValid(value);
            return Integer.BYTES;
        }

        @Override
        public Object convert(final Object value) {
            assert this.isValid(value);
            return ((Number) value).intValue();
        }

        @Override
        public Object defaultValue() {
            return 0;
        }

        @Override
        public boolean isValid(final Object value) {
            return value instanceof Number;
        }

        @Override
        public Object read(final ByteBuf in) {
            return in.readIntLE();
        }

        @Override
        public ByteBuf readSlice(final ByteBuf in) {
            return in.readSlice(Integer.BYTES);
        }

        @Override
        public Class<?> valueType() {
            return Integer.class;
        }

        @Override
        public void write(final Object value, final ByteBuf out) {
            assert this.isValid(value);
            out.writeIntLE(((Number) value).intValue());
        }
    }

    private static final class RntbdLong implements Codec {

        public static final Codec CODEC = new RntbdLong();

        private RntbdLong() {
        }

        @Override
        public int computeLength(final Object value) {
            assert this.isValid(value);
            return java.lang.Long.BYTES;
        }

        @Override
        public Object convert(final Object value) {
            assert this.isValid(value);
            return ((Number) value).longValue();
        }

        @Override
        public Object defaultValue() {
            return 0L;
        }

        @Override
        public boolean isValid(final Object value) {
            return value instanceof Number;
        }

        @Override
        public Object read(final ByteBuf in) {
            return in.readLongLE();
        }

        @Override
        public ByteBuf readSlice(final ByteBuf in) {
            return in.readSlice(java.lang.Long.BYTES);
        }

        @Override
        public Class<?> valueType() {
            return Long.class;
        }

        @Override
        public void write(final Object value, final ByteBuf out) {
            assert this.isValid(value);
            out.writeLongLE(((Number) value).longValue());
        }
    }

    /**
     * Represents a variable-length byte sequence with maximum length of {@linkplain Integer#MAX_VALUE}.
     * <p>
     * A value of this type is referred to as a long variable-length byte sequence.
     */
    private static final class RntbdLongBytes extends RntbdBytes {

        public static final Codec CODEC = new RntbdLongBytes();

        private RntbdLongBytes() {
        }

        @Override
        public int computeLength(final Object value) {
            assert this.isValid(value);
            return Integer.BYTES + ((byte[]) value).length;
        }

        @Override
        public boolean isValid(final Object value) {
            return value instanceof byte[];
        }

        /**
         * Reads a long variable-length byte sequence at the reader index of a {@link ByteBuf}.
         * <p>
         * This method reads two fields:
         * <table>
         * <thead><tr><th>Field</th><th>Description</th></tr></thead>
         * <tbody><tr>
         * <td>{@code length}</td><td>a signed 32-bit integer specifying the number of bytes in the sequence.</td>
         * </tr><tr>
         * <td>{@code value}</td><td>a sequence of {@code length} bytes.</td>
         * </tr></tbody>
         * </table>
         * <p>
         * The {@code value} is transferred to a newly created {@linkplain ByteBuf}. The input buffer's
         * {@linkplain ByteBuf#readerIndex readerIndex} is incremented by {@code length + 4}. The returned buffer's
         * {@linkplain ByteBuf#readerIndex readerIndex} and {@linkplain ByteBuf#writerIndex writerIndex} are set to
         * {@code 0} and {@code length} respectively.
         *
         * @param in the input {@linkplain ByteBuf}.
         *
         * @return a newly created {@linkplain ByteBuf} containing the transferred {@code value}.
         *
         * @throws IndexOutOfBoundsException if the input buffer's {@linkplain ByteBuf#readableBytes readableBytes}
         * count is:
         * <table>
         * <thead><tr><td>Count</td><td>Meaning</td></tr></thead>
         * <tbody>
         * <tr><td>less than {@code 2}</td><td>{@code length} cannot be read</td></tr>
         * <tr><td>less than {@code length + 2}</td><td>{@code value} cannot be read</td></tr>
         * </tbody>
         * </table>
         */
        @Override
        public Object read(final ByteBuf in) {
            final long length = in.readUnsignedIntLE();
            Codec.checkReadableBytes(in, length, Integer.MAX_VALUE);
            return in.readBytes((int) length);
        }

        /**
         * Returns a new slice that spans a long variable-length byte sequence at the reader index of a {@link ByteBuf}.
         * <p>
         * This method expects two fields:
         * <table>
         * <thead><tr><th>Field</th><th>Description</th></tr></thead>
         * <tbody><tr>
         * <td>{@code length}</td><td>an unsigned 32-bit integer specifying the number of bytes in the sequence.</td>
         * </tr><tr>
         * <td>{@code value}</td><td>a sequence of {@code length} bytes.</td>
         * </tr></tbody>
         * </table>
         * <p>
         * The newly created slice that is returned spans both fields. Its {@linkplain ByteBuf#readerIndex readerIndex}
         * and {@linkplain ByteBuf#writerIndex writerIndex} are set to {@code 0} and {@code length + 4} respectively.
         * The input buffer's {@linkplain ByteBuf#readerIndex readerIndex} is incremented by {@code length + 4}.
         *
         * @param in the input {@linkplain ByteBuf}.
         *
         * @return a newly created slice that spans a variable-length sequence of bytes.
         *
         * @throws IndexOutOfBoundsException if {@code length} is greater than {@code Integer.MAX_VALUE} or the input
         * buffer's {@linkplain ByteBuf#readableBytes readableBytes} count is:
         * <table>
         * <thead><tr><td>Count</td><td>Meaning</td></tr></thead>
         * <tbody>
         * <tr><td>less than {@code 4}</td><td>{@code length} cannot be read</td></tr>
         * <tr><td>less than {@code length + 4}</td><td>{@code value} cannot be read</td></tr>
         * </tbody>
         * </table>
         */
        @Override
        public ByteBuf readSlice(final ByteBuf in) {
            final long length = in.getUnsignedIntLE(in.readerIndex());
            Codec.checkReadableBytes(in, length, Integer.MAX_VALUE);
            return in.readSlice(Integer.BYTES + (int) length);
        }

        @Override
        public void write(final Object value, final ByteBuf out) {

            assert this.isValid(value);

            final byte[] bytes = (byte[]) value;
            out.writeIntLE(bytes.length);
            out.writeBytes(bytes);
        }
    }

    /**
     * Represents a variable-length UTF-8 encoded character sequence with maximum length of {@link Integer#MAX_VALUE}.
     * <p>
     * A value of this type is referred to as a long variable-length string.
     */
    private static final class RntbdLongString extends RntbdString {

        public static final Codec CODEC = new RntbdLongString();

        private RntbdLongString() {
        }

        @Override
        public int computeLength(final Object value) {
            return Integer.BYTES + this.computeLength(value, Integer.MAX_VALUE);
        }

        /**
         * Reads a long variable-length string at the reader index of a {@link ByteBuf}.
         * <p>
         * This method reads two fields:
         * <table>
         * <thead><tr><th>Field</th><th>Description</th></tr></thead>
         * <tbody><tr>
         * <td>{@code length}</td><td>an unsigned 32-bit integer specifying the number of bytes in the string.</td>
         * </tr><tr>
         * <td>{@code value}</td><td>a sequence of {@code length} bytes.</td>
         * </tr></tbody>
         * </table>
         * <p>
         * The {@code value} is transcoded and transferred to a newly created {@linkplain String}. The input buffer's
         * {@linkplain ByteBuf#readerIndex readerIndex} is incremented by {@code length + 4}. The returned buffer's
         * {@linkplain ByteBuf#readerIndex readerIndex} and {@linkplain ByteBuf#writerIndex writerIndex} are set to
         * {@code 0} and {@code length} respectively.
         *
         * Erroneous UTF-8 code points are dropped and replaced with the Unicode REPLACEMENT CHARACTER ({@code 0xFFFD}).
         *
         * @param in the input {@linkplain ByteBuf}.
         *
         * @return a newly created {@linkplain String} containing the transcoded {@code value}.
         *
         * @throws IndexOutOfBoundsException if {@code length} is greater than {@linkplain Integer#MAX_VALUE} or the
         * input buffer's {@linkplain ByteBuf#readableBytes readableBytes} count is:
         * <table>
         * <thead><tr><td>Count</td><td>Meaning</td></tr></thead>
         * <tbody>
         * <tr><td>less than {@code 4}</td><td>{@code length} cannot be read</td></tr>
         * <tr><td>less than {@code length + 4}</td><td>{@code value} cannot be read</td></tr>
         * </tbody>
         * </table>
         */
        @Override
        public Object read(final ByteBuf in) {
            final long length = in.readUnsignedIntLE();
            Codec.checkReadableBytes(in, length, Integer.MAX_VALUE);
            return in.readCharSequence((int) length, StandardCharsets.UTF_8).toString();
        }

        @Override
        public void write(final Object value, final ByteBuf out) {
            final int length = this.computeLength(value, Integer.MAX_VALUE);
            out.writeIntLE(length);
            writeValue(out, value, length);
        }
    }

    private static final class RntbdNone implements Codec {

        public static final Codec CODEC = new RntbdNone();

        @Override
        public int computeLength(final Object value) {
            return 0;
        }

        @Override
        public Object convert(final Object value) {
            return null;
        }

        @Override
        public Object defaultValue() {
            return null;
        }

        @Override
        public boolean isValid(final Object value) {
            return true;
        }

        @Override
        public Object read(final ByteBuf in) {
            return null;
        }

        @Override
        public ByteBuf readSlice(final ByteBuf in) {
            return null;
        }

        @Override
        public Class<?> valueType() {
            return null;
        }

        @Override
        public void write(final Object value, final ByteBuf out) {
        }
    }

    /**
     * Represents a variable-length byte sequence with maximum length of {@code 255}.
     * <p>
     * A value of this type is referred to as a short variable-length byte sequence.
     */
    private static final class RntbdShortBytes extends RntbdBytes {

        public static final Codec CODEC = new RntbdShortBytes();

        private RntbdShortBytes() {
        }

        @Override
        public int computeLength(final Object value) {
            assert this.isValid(value);
            return java.lang.Byte.BYTES + ((byte[]) value).length;
        }

        @Override
        public boolean isValid(final Object value) {
            return value instanceof byte[] && ((byte[]) value).length <= 0xFF;
        }

        /**
         * Reads a short variable-length byte sequence at the reader index of a {@link ByteBuf}.
         * <p>
         * This method reads two fields:
         * <table>
         * <thead><tr><th>Field</th><th>Description</th></tr></thead>
         * <tbody><tr>
         * <td>{@code length}</td><td>an unsigned 8-bit integer specifying the number of bytes in the sequence.</td>
         * </tr><tr>
         * <td>{@code value}</td><td>a sequence of {@code length} bytes.</td>
         * </tr></tbody>
         * </table>
         * <p>
         * The {@code value} is transferred to a newly created {@linkplain ByteBuf}. The input buffer's
         * {@linkplain ByteBuf#readerIndex readerIndex} is incremented by {@code length + 2}. The returned buffer's
         * {@linkplain ByteBuf#readerIndex readerIndex} and {@linkplain ByteBuf#writerIndex writerIndex} are set to
         * {@code 0} and {@code length} respectively.
         *
         * @param in the input {@linkplain ByteBuf}.
         *
         * @return a newly created {@linkplain ByteBuf} containing the transferred {@code value}.
         *
         * @throws IndexOutOfBoundsException if the input buffer's {@linkplain ByteBuf#readableBytes readableBytes}
         * count is:
         * <table>
         * <thead><tr><td>Count</td><td>Meaning</td></tr></thead>
         * <tbody>
         * <tr><td>equal to {@code 0}</td><td>{@code length} cannot be read</td></tr>
         * <tr><td>less than {@code length + 1}</td><td>{@code value} cannot be read</td></tr>
         * </tbody>
         * </table>
         */
        @Override
        public Object read(final ByteBuf in) {

            final int length = in.readUnsignedByte();
            Codec.checkReadableBytes(in, length, 0xFF);
            final byte[] bytes = new byte[length];
            in.readBytes(bytes);

            return bytes;
        }

        @Override
        public ByteBuf readSlice(final ByteBuf in) {
            return in.readSlice(java.lang.Byte.BYTES + in.getUnsignedByte(in.readerIndex()));
        }

        @Override
        public void write(final Object value, final ByteBuf out) {

            assert this.isValid(value);

            final byte[] bytes = (byte[]) value;
            final int length = bytes.length;

            if (length > 0xFF) {
                String reason = lenientFormat("value length (%s) is greater than maxLength (%s)",
                    length,
                    0xFFL);
                throw new EncoderException(reason);
            }

            out.writeByte((byte) length);
            out.writeBytes(bytes);
        }
    }

    /**
     * Represents a variable-length UTF-8 encoded character sequence with maximum length of {@code 255}.
     * <p>
     * A value of this type is referred to as a short variable-length string.
     */
    private static final class RntbdShortString extends RntbdString {

        public static final Codec CODEC = new RntbdShortString();

        private RntbdShortString() {
        }

        @Override
        public int computeLength(final Object value) {
            return java.lang.Byte.BYTES + this.computeLength(value, 0xFF);
        }

        /**
         * Reads a short variable-length UTF-8 encoded string at the reader index of a {@link ByteBuf}.
         * <p>
         * This method reads two fields:
         * <table>
         * <thead><tr><th>Field</th><th>Description</th></tr></thead>
         * <tbody><tr>
         * <td>{@code length}</td><td>an unsigned 8-bit integer specifying the number of bytes in the string.</td>
         * </tr><tr>
         * <td>{@code value}</td><td>a sequence of {@code length} bytes.</td>
         * </tr></tbody>
         * </table>
         * <p>
         * The {@code value} is transcoded and transferred to a newly created {@linkplain String}. The input buffer's
         * {@linkplain ByteBuf#readerIndex readerIndex} is incremented by {@code length + 1}. The returned buffer's
         * {@linkplain ByteBuf#readerIndex readerIndex} and {@linkplain ByteBuf#writerIndex writerIndex} are set to
         * {@code 0} and {@code length} respectively.
         *
         * Erroneous UTF-8 code points are dropped and replaced with the Unicode REPLACEMENT CHARACTER ({@code 0xFFFD}).
         *
         * @param in the input {@linkplain ByteBuf}.
         *
         * @return a newly created {@linkplain String} containing the transcoded {@code value}.
         *
         * @throws IndexOutOfBoundsException if the input buffer's {@linkplain ByteBuf#readableBytes readableBytes}
         * count is:
         * <table>
         * <thead><tr><td>Count</td><td>Meaning</td></tr></thead>
         * <tbody>
         * <tr><td>equal to {@code 0}</td><td>{@code length} cannot be read</td></tr>
         * <tr><td>less than {@code length + 1}</td><td>{@code value} cannot be read</td></tr>
         * </tbody>
         * </table>
         */
        @Override
        public Object read(final ByteBuf in) {
            final int length = in.readUnsignedByte();
            Codec.checkReadableBytes(in, length, 0xFF);
            return in.readCharSequence(length, StandardCharsets.UTF_8).toString();
        }

        @Override
        public ByteBuf readSlice(final ByteBuf in) {
            return in.readSlice(java.lang.Byte.BYTES + in.getUnsignedByte(in.readerIndex()));
        }

        @Override
        public void write(final Object value, final ByteBuf out) {

            final int length = this.computeLength(value, 0xFF);
            out.writeByte(length);
            writeValue(out, value, length);
        }
    }

    /**
     * Represents a variable-length UTF-8 encoded character sequence with maximum length of {@link Integer#MAX_VALUE}.
     * <p>
     * A value of this type is referred to as a long variable-length string.
     */
    private static class RntbdString implements Codec {

        public static final Codec CODEC = new RntbdString();

        private RntbdString() {
        }

        @SuppressWarnings("UnstableApiUsage")
        final int computeLength(final Object value, final int maxLength) {

            assert this.isValid(value);
            final int length;

            if (value.getClass() == String.class) {

                final String string = (String) value;
                length = Utf8.encodedLength(string);

                if (length > maxLength) {
                    final String reason = lenientFormat("UTF-8 encoded string length (%s) exceeds maxLength (%s)",
                        length,
                        maxLength);
                    throw new EncoderException(reason);
                }

            } else {

                final byte[] string = (byte[]) value;

                if (!Utf8.isWellFormed(string)) {
                    final String reason = lenientFormat("UTF-8 byte string is ill-formed: %s",
                        ByteBufUtil.hexDump(string));
                    throw new DecoderException(reason);
                }

                length = string.length;

                if (length > maxLength) {
                    final String reason = lenientFormat("UTF-8 string length (%s) exceeds maxLength(%s)",
                        length,
                        maxLength);
                    throw new DecoderException(reason);
                }
            }

            return length;
        }

        @Override
        public int computeLength(final Object value) {
            return Short.BYTES + this.computeLength(value, 0xFFFF);
        }

        @Override
        public final Object convert(final Object value) {
            assert this.isValid(value);
            return value instanceof String ? value : new String((byte[]) value, StandardCharsets.UTF_8);
        }

        @Override
        public final Object defaultValue() {
            return "";
        }

        @Override
        public final boolean isValid(final Object value) {
            return value instanceof String || value instanceof byte[];
        }

        /**
         * Reads a variable-length UTF-8 encoded string at the reader index of a {@link ByteBuf}.
         * <p>
         * This method reads two fields:
         * <table>
         * <thead><tr><th>Field</th><th>Description</th></tr></thead>
         * <tbody><tr>
         * <td>{@code length}</td><td>an unsigned 16-bit integer specifying the number of bytes in the string.</td>
         * </tr><tr>
         * <td>{@code value}</td><td>a sequence of {@code length} bytes.</td>
         * </tr></tbody>
         * </table>
         * <p>
         * The {@code value} is transcoded and transferred to a newly created {@linkplain String}. The input buffer's
         * {@linkplain ByteBuf#readerIndex readerIndex} is incremented by {@code length + 2}. The returned buffer's
         * {@linkplain ByteBuf#readerIndex readerIndex} and {@linkplain ByteBuf#writerIndex writerIndex} are set to
         * {@code 0} and {@code length} respectively.
         *
         * Erroneous UTF-8 code points are dropped and replaced with the Unicode REPLACEMENT CHARACTER ({@code 0xFFFD}).
         *
         * @param in the input {@linkplain ByteBuf}.
         *
         * @return a newly created {@linkplain String} containing the transcoded {@code value}.
         *
         * @throws IndexOutOfBoundsException if the input buffer's {@linkplain ByteBuf#readableBytes readableBytes}
         * count is:
         * <table>
         * <thead><tr><td>Count</td><td>Meaning</td></tr></thead>
         * <tbody>
         * <tr><td>less than {@code 4}</td><td>{@code length} cannot be read</td></tr>
         * <tr><td>less than {@code length + 2}</td><td>{@code value} cannot be read</td></tr>
         * </tbody>
         * </table>
         */
        @Override
        public Object read(final ByteBuf in) {
            final int length = in.readUnsignedShortLE();
            Codec.checkReadableBytes(in, length, 0xFFFF);
            return in.readCharSequence(length, StandardCharsets.UTF_8).toString();
        }

        @Override
        public ByteBuf readSlice(final ByteBuf in) {
            return in.readSlice(Short.BYTES + in.getUnsignedShortLE(in.readerIndex()));
        }

        @Override
        public Class<?> valueType() {
            return String.class;
        }

        @Override
        public void write(final Object value, final ByteBuf out) {

            final int length = this.computeLength(value, 0xFFFF);
            out.writeShortLE(length);
            writeValue(out, value, length);
        }

        static void writeValue(final ByteBuf out, final Object value, final int length) {

            final int start = out.writerIndex();

            if (value instanceof String) {
                out.writeCharSequence((String) value, StandardCharsets.UTF_8);
            } else {
                out.writeBytes((byte[]) value);
            }

            assert out.writerIndex() - start == length;
        }
    }

    private static final class RntbdUnsignedInteger implements Codec {

        public static final Codec CODEC = new RntbdUnsignedInteger();

        private RntbdUnsignedInteger() {
        }

        @Override
        public int computeLength(final Object value) {
            assert this.isValid(value);
            return Integer.BYTES;
        }

        @Override
        public Object convert(final Object value) {
            assert this.isValid(value);
            return ((Number) value).longValue() & 0xFFFFFFFFL;
        }

        @Override
        public Object defaultValue() {
            return 0L;
        }

        @Override
        public boolean isValid(final Object value) {
            return value instanceof Number;
        }

        @Override
        public Object read(final ByteBuf in) {
            return in.readUnsignedIntLE();
        }

        @Override
        public ByteBuf readSlice(final ByteBuf in) {
            return in.readSlice(Integer.BYTES);
        }

        @Override
        public Class<?> valueType() {
            return Long.class;
        }

        @Override
        public void write(final Object value, final ByteBuf out) {
            assert this.isValid(value);
            out.writeIntLE(((Number) value).intValue());
        }
    }

    private static final class RntbdUnsignedShort implements Codec {

        public static final Codec CODEC = new RntbdUnsignedShort();

        private RntbdUnsignedShort() {
        }

        @Override
        public int computeLength(final Object value) {
            assert this.isValid(value);
            return Short.BYTES;
        }

        @Override
        public Object convert(final Object value) {
            assert this.isValid(value);
            return ((Number) value).intValue() & 0xFFFF;
        }

        @Override
        public Object defaultValue() {
            return 0;
        }

        @Override
        public boolean isValid(final Object value) {
            return value instanceof Number;
        }

        @Override
        public Object read(final ByteBuf in) {
            return in.readUnsignedShortLE();
        }

        @Override
        public ByteBuf readSlice(final ByteBuf in) {
            return in.readSlice(Short.BYTES);
        }

        @Override
        public Class<?> valueType() {
            return Integer.class;
        }

        @Override
        public void write(final Object value, final ByteBuf out) {
            assert this.isValid(value);
            out.writeShortLE(((Number) value).shortValue());
        }
    }

    // endregion
}
