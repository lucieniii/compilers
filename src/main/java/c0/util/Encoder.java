package main.java.c0.util;

import java.nio.ByteBuffer;

public class Encoder {

    public static byte[] toBytes(Object x) {
        ByteBuffer __;
        ByteBuffer buffer = null;
        if (x instanceof Boolean) {
            buffer = ByteBuffer.allocate(Byte.BYTES);
            __ = (boolean) x ? buffer.put((byte) 1) : buffer.put((byte) 0);
        } else if (x instanceof Integer) {
            buffer = ByteBuffer.allocate(Integer.BYTES);
            buffer.putInt((int) x);
        } else if (x instanceof Long) {
            buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.putLong((long) x);
        } else if (x instanceof String) {
            String str = (String) x;
            buffer = ByteBuffer.allocate(((String) x).length());
            for (char c : ((String) x).toCharArray()) {
                buffer.put((byte)c);
            }
        } else if (x instanceof Byte) {
            buffer = ByteBuffer.allocate(Byte.BYTES);
            buffer.put((byte) x);
        } else return null;

        return buffer.array();
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte aByte : bytes) {
            hexString.append(" ");
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.substring(1);
    }

    public static String EncodeToString(Object x) {
        return bytesToHexString(toBytes(x));
    }

    public static void main(String[] args) {

    }
}
