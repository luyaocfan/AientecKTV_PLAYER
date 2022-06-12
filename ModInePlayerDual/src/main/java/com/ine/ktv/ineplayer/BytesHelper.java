package com.ine.ktv.ineplayer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BytesHelper {
    static ByteOrder Endian = ByteOrder.BIG_ENDIAN;

    public static void PutInt(int num, byte[] array, int offset, ByteOrder order) {
        if(order==ByteOrder.LITTLE_ENDIAN) {
            array[offset] =     (byte) (num & 0xff);
            array[offset + 1] = (byte) ((num >> 8) & 0xff);
            array[offset + 2] = (byte) ((num >> 16) & 0xff);
            array[offset + 3] = (byte) ((num >> 24) & 0xff);
        }
        else {
            array[offset + 3] = (byte) (num & 0xff);
            array[offset + 2] = (byte) ((num >> 8) & 0xff);
            array[offset + 1] = (byte) ((num >> 16) & 0xff);
            array[offset] =     (byte) ((num >> 24) & 0xff);
        }
    }
    public static void PutInt(int num, byte[] array, int offset) {
        if(Endian==ByteOrder.LITTLE_ENDIAN) {
            array[offset] =     (byte) (num & 0xff);
            array[offset + 1] = (byte) ((num >> 8) & 0xff);
            array[offset + 2] = (byte) ((num >> 16) & 0xff);
            array[offset + 3] = (byte) ((num >> 24) & 0xff);
        }
        else {
            array[offset + 3] = (byte) (num & 0xff);
            array[offset + 2] = (byte) ((num >> 8) & 0xff);
            array[offset + 1] = (byte) ((num >> 16) & 0xff);
            array[offset] =     (byte) ((num >> 24) & 0xff);
        }
    }

    public static void PutShort(short num, byte[] array, int offset, ByteOrder order) {
        if(order==ByteOrder.LITTLE_ENDIAN) {
            array[offset] = (byte) (num & 0xff);
            array[offset + 1] = (byte) ((num >> 8) & 0xff);
        }
        else {
            array[offset + 1] = (byte) (num & 0xff);
            array[offset] = (byte) ((num >> 8) & 0xff);
        }
    }
    public static void PutShort(short num, byte[] array, int offset) {
        if(Endian==ByteOrder.LITTLE_ENDIAN) {
            array[offset] = (byte) (num & 0xff);
            array[offset + 1] = (byte) ((num >> 8) & 0xff);
        }
        else {
            array[offset + 1] = (byte) (num & 0xff);
            array[offset] = (byte) ((num >> 8) & 0xff);
        }
    }

    public static void PutFloat(float num, byte[] array, int offset, ByteOrder order) {
        ByteBuffer byteBuffer=ByteBuffer.allocate(Float.BYTES);
        byteBuffer.order(order);
        byteBuffer.putFloat(num);
        System.arraycopy(byteBuffer.array(), 0, array, offset, Float.BYTES);
    }
    public static void PutFloat(float num, byte[] array, int offset) {
        ByteBuffer byteBuffer=ByteBuffer.allocate(Float.BYTES);
        byteBuffer.order(Endian);
        byteBuffer.putFloat(num);
        System.arraycopy(byteBuffer.array(), 0, array, offset, Float.BYTES);
    }

    public static void PutDouble(double num, byte[] array, int offset, ByteOrder order) {
        ByteBuffer byteBuffer=ByteBuffer.allocate(Double.BYTES);
        byteBuffer.order(order);
        byteBuffer.putDouble(num);
        System.arraycopy(byteBuffer.array(), 0, array, offset, Double.BYTES);
    }
    public static void PutDouble(double num, byte[] array, int offset) {
        ByteBuffer byteBuffer=ByteBuffer.allocate(Double.BYTES);
        byteBuffer.order(Endian);
        byteBuffer.putDouble(num);
        System.arraycopy(byteBuffer.array(), 0, array, offset, Double.BYTES);
    }

    public static int GetInt(byte[] array, int offset, ByteOrder order) {
        if(order==ByteOrder.LITTLE_ENDIAN)
            return ((((int)array[offset+3])&0xff)<<24)+((((int)array[offset+2])&0xff)<<16)+((((int)array[offset+1])&0xff)<<8)+(((int)array[offset])&0xff);
        else
            return ((((int)array[offset])&0xff)<<24)+((((int)array[offset+1])&0xff)<<16)+((((int)array[offset+2])&0xff)<<8)+(((int)array[offset+3])&0xff);
    }
    public static int GetInt(byte[] array, int offset) {
        if(Endian==ByteOrder.LITTLE_ENDIAN)
            return ((((int)array[offset+3])&0xff)<<24)+((((int)array[offset+2])&0xff)<<16)+((((int)array[offset+1])&0xff)<<8)+(((int)array[offset])&0xff);
        else
            return ((((int)array[offset])&0xff)<<24)+((((int)array[offset+1])&0xff)<<16)+((((int)array[offset+2])&0xff)<<8)+(((int)array[offset+3])&0xff);
    }

    public static short GetShort(byte[] array, int offset, ByteOrder order) {
        if(order==ByteOrder.LITTLE_ENDIAN)
            return (short)(((((int)array[offset+1])&0xff)<<8)+(((int)array[offset])&0xff));
        else
            return (short)(((((int)array[offset])&0xff)<<8)+(((int)array[offset+1])&0xff));
    }
    public static short GetShort(byte[] array, int offset) {
        if(Endian==ByteOrder.LITTLE_ENDIAN)
            return (short)(((((int)array[offset+1])&0xff)<<8)+(((int)array[offset])&0xff));
        else
            return (short)(((((int)array[offset])&0xff)<<8)+(((int)array[offset+1])&0xff));
    }

    public static float GetFloat(byte[] array, int offset, ByteOrder order) {
        return ByteBuffer.wrap(array,offset,Float.BYTES).order(order).getFloat();
    }
    public static float GetFloat(byte[] array, int offset) {
        return ByteBuffer.wrap(array,offset,Float.BYTES).order(Endian).getFloat();
    }

    public static double GetDouble(byte[] array, int offset, ByteOrder order) {
        return ByteBuffer.wrap(array,offset,Double.BYTES).order(order).getDouble();
    }
    public static double GetDouble(byte[] array, int offset) {
        return ByteBuffer.wrap(array,offset,Double.BYTES).order(Endian).getDouble();
    }
}
