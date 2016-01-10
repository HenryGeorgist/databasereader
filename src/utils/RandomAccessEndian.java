/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;
import java.io.FileNotFoundException;
import java.io.IOException;
/**
 * A simple extention to the RandomAccessFile that allows the user to read big endian (through the existing calls) and little endian
 * (through the extended calls).
 * @author Will_and_Sara informed by: Bill unspecified.specification@gmail. http://stackoverflow.com/questions/8028094/java-datainputstream-replacement-for-endianness
 */
public class RandomAccessEndian extends java.io.RandomAccessFile {
    public RandomAccessEndian(String name, String mode) throws FileNotFoundException {
        super(name, mode);
    }
     /**
     * Reads floating point type stored in little endian (see readFloat() for big endian)
     * @return float value translated from little endian
     * @throws IOException if an IO error occurs
     */
    public final float readLittleFloat() throws IOException {
        return Float.intBitsToFloat(readLittleInt());
    }    
    /**
     * Reads double precision floating point type stored in little endian (see readDouble() for big endian)
     * @return double precision float value translated from little endian
     * @throws IOException if an IO error occurs
     */    
    public final double readLittleDouble() throws IOException {
        return Double.longBitsToDouble(readLittleLong());
    }
    /**
     * Reads short type stored in little endian (see readShort() for big endian)
     * @return short value translated from little endian
     * @throws IOException if an IO error occurs
     */    
    public final short readLittleShort() throws IOException {
        byte[] byteBuffer = new byte[2];
        read(byteBuffer, 0, 2);
    return (short)((byteBuffer[1] & 0xff) << 8 | (byteBuffer[0] & 0xff));
    }
    /**
     * Reads char (16-bits) type stored in little endian (see readChar() for big endian)
     * @return char value translated from little endian
     * @throws IOException if an IO error occurs
     */    
    public final char readLittleChar() throws IOException {
        byte[] byteBuffer = new byte[2];
        read(byteBuffer, 0, 2);
        return (char)((byteBuffer[1] & 0xff) << 8 | (byteBuffer[0] & 0xff));
    }    
    /**
     * Reads integer type stored in little endian (see readInt() for big endian)
     * @return integer value translated from little endian
     * @throws IOException if an IO error occurs
     */        
    public final int readLittleInt() throws IOException {
        byte[] byteBuffer = new byte[4];
        read(byteBuffer, 0, 4);
        return (byteBuffer[3]) << 24 | (byteBuffer[2] & 0xff) << 16 |
            (byteBuffer[1] & 0xff) << 8 | (byteBuffer[0] & 0xff);
    }
    /**
     * Reads long type stored in little endian (see readLong() for big endian)
     * @return long value translated from little endian
     * @throws IOException if an IO error occurs
     */        
    public final long readLittleLong() throws IOException {
        byte[] byteBuffer = new byte[8];
        read(byteBuffer, 0, 8);
        return (long)(byteBuffer[7]) << 56 | (long)(byteBuffer[6]&0xff) << 48 |
            (long)(byteBuffer[5] & 0xff) << 40 | (long)(byteBuffer[4] & 0xff) << 32 |
            (long)(byteBuffer[3] & 0xff) << 24 | (long)(byteBuffer[2] & 0xff) << 16 |
            (long)(byteBuffer[1] & 0xff) <<  8 | (long)(byteBuffer[0] & 0xff);
    }
    /**
     * Reads unsigned short type stored in little endian (see readUnsignedShort() for big endian)
     * @return integer value representing unsigned short value translated from little endian
     * @throws IOException if an IO error occurs
     */        
    public final int readLittleUnsignedShort() throws IOException {
        byte[] byteBuffer = new byte[2];
        read(byteBuffer, 0, 2);
        return ((byteBuffer[1] & 0xff) << 8 | (byteBuffer[0] & 0xff));
    }
    /**
     * Reads a sequence of bytes based on the start position (offset from current position) and the end position, and converts the byte
     * array to a string skipping bytes of 0.
     * @param start the offset byte position from the current byte position in the RandomAccessFileStream
     * @param end the end byte position from the current byte position in the RandomAccessFileStream
     * @return a string 
     * @throws IOException
     */
    public String ReadString(int start, int end) throws IOException{
        byte[] bytes = new byte[end-start];
        if(start!=0){skipBytes(start);}
        read(bytes,0,end-start);
        java.lang.StringBuilder result = new java.lang.StringBuilder();
        for(int i = start; i<end;i++){
            if(bytes[i] == 0){
                //trimming all 0 bytes.
            }else{
                result.append((char)bytes[i]);
            }
        }
        return result.toString();
    }
}
