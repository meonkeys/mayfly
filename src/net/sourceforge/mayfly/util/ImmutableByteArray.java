package net.sourceforge.mayfly.util;

import net.sourceforge.mayfly.MayflyInternalException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImmutableByteArray {
    
    private final byte[] bytes;

    public ImmutableByteArray(byte singleByte) {
        this(new byte[] { singleByte });
    }

    public ImmutableByteArray(byte[] value) {
        bytes = new byte[value.length];
        System.arraycopy(value, 0, bytes, 0, value.length);
    }
    
    /**
     * @internal
     * Read our contents from a stream (all the way until
     * end of file).  The caller is responsible for
     * closing the stream.
     */
    public ImmutableByteArray(InputStream stream) throws IOException {
        this.bytes = copy(stream);
    }

    private static byte[] copy(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }

    public InputStream asBinaryStream() {
        return new ByteArrayInputStream(bytes);
    }

    public byte[] asBytes() {
        byte[] copy = new byte[bytes.length];
        System.arraycopy(bytes, 0, copy, 0, bytes.length);
        return copy;
    }

    public String asSql() {
        StringBuilder result = new StringBuilder();
        result.append("x'");
        for (int i = 0; i < bytes.length; ++i) {
            int hi = (bytes[i] >> 4) & 0xf;
            int lo = bytes[i] & 0xf;

            result.append(toHex(hi));
            result.append(toHex(lo));
        }
        result.append("'");
        return result.toString();
    }

    private String toHex(int number) {
        String[] digits = {"0", "1", "2", "3", "4", "5", "6", "7", 
            "8", "9", "a", "b", "c", "d", "e", "f" };
        if (number >= 0 && number < 16) {
            return digits[number];
        }
        else {
            throw new MayflyInternalException(number + " is not a hex digit");
        }
    }

    public long length() {
        return bytes.length;
    }

}
