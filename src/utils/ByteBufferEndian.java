package utils;


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Will_and_Sara
 */
public class ByteBufferEndian{
    private java.nio.channels.SeekableByteChannel _SeekableChannel;
    public ByteBufferEndian(String filePath){
        try {
            _SeekableChannel = java.nio.file.Files.newByteChannel(java.nio.file.Paths.get(filePath), java.util.EnumSet.of(java.nio.file.StandardOpenOption.READ));
        } catch (IOException ex) {
            Logger.getLogger(ByteBufferEndian.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        java.nio.ByteBuffer byteBuffer = java.nio.ByteBuffer.allocate(2);
        _SeekableChannel.read(byteBuffer);
    return (short)((byteBuffer.get(1) & 0xff) << 8 | (byteBuffer.get(0) & 0xff));
    }
    /**
     * Reads char (16-bits) type stored in little endian (see readChar() for big endian)
     * @return char value translated from little endian
     * @throws IOException if an IO error occurs
     */    
    public final char readLittleChar() throws IOException {
        java.nio.ByteBuffer byteBuffer = java.nio.ByteBuffer.allocate(2);
        _SeekableChannel.read(byteBuffer);
        return (char)((byteBuffer.get(1) & 0xff) << 8 | (byteBuffer.get(0) & 0xff));
    }    
    /**
     * Reads integer type stored in little endian (see readInt() for big endian)
     * @return integer value translated from little endian
     * @throws IOException if an IO error occurs
     */        
    public final int readLittleInt() throws IOException {
        java.nio.ByteBuffer byteBuffer = java.nio.ByteBuffer.allocate(4);
        _SeekableChannel.read(byteBuffer);
        return (byteBuffer.get(3)) << 24 | (byteBuffer.get(2) & 0xff) << 16 |
            (byteBuffer.get(1) & 0xff) << 8 | (byteBuffer.get(0) & 0xff);
    }
    /**
     * Reads long type stored in little endian (see readLong() for big endian)
     * @return long value translated from little endian
     * @throws IOException if an IO error occurs
     */        
    public final long readLittleLong() throws IOException {
        java.nio.ByteBuffer byteBuffer = java.nio.ByteBuffer.allocate(8);
        _SeekableChannel.read(byteBuffer);
        return (long)(byteBuffer.get(7)) << 56 | (long)(byteBuffer.get(6) & 0xff) << 48 |
            (long)(byteBuffer.get(5) & 0xff) << 40 | (long)(byteBuffer.get(4) & 0xff) << 32 |
            (long)(byteBuffer.get(3) & 0xff) << 24 | (long)(byteBuffer.get(2) & 0xff) << 16 |
            (long)(byteBuffer.get(2) & 0xff) <<  8 | (long)(byteBuffer.get(0) & 0xff);
    }
    /**
     * Reads unsigned short type stored in little endian (see readUnsignedShort() for big endian)
     * @return integer value representing unsigned short value translated from little endian
     * @throws IOException if an IO error occurs
     */        
    public final int readLittleUnsignedShort() throws IOException {
        java.nio.ByteBuffer byteBuffer = java.nio.ByteBuffer.allocate(2);
        _SeekableChannel.read(byteBuffer);
        return ((byteBuffer.get(1) & 0xff) << 8 | (byteBuffer.get(0) & 0xff));
    }
    /**
     * Reads a sequence of bytes based on the start position (offset from current position) and the end position, and converts the byte
     * array to a string skipping bytes of 0.
     * @param start the offset byte position from the current byte position in the RandomAccessFileStream
     * @param end the end byte position from the current byte position in the RandomAccessFileStream
     * @return a string 
     * @throws IOException
     */
    public final String ReadString(int start, int end) throws IOException{
        java.nio.ByteBuffer byteBuffer = java.nio.ByteBuffer.allocate(end-start);
        if(start!=0){seek(start);}
        _SeekableChannel.read(byteBuffer);
        final char[] result = new char[end-start];
        for(int i = start; i<end;i++){
            result[i] = ((char)byteBuffer.get(i));
        }
        return new String(result);
    }
    public void close() throws IOException{
        _SeekableChannel.close();
    }
    public int seek(int position){
        try {
            _SeekableChannel.position(position);
            return position;
        } catch (IOException ex) {
            Logger.getLogger(ByteBufferEndian.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }
    public int skipBytes(int numbytes){
        try {
            _SeekableChannel.position(_SeekableChannel.position()+numbytes);
            return numbytes;
        } catch (IOException ex) {
            Logger.getLogger(ByteBufferEndian.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }
    public byte read(){
        try {
            java.nio.ByteBuffer dst = java.nio.ByteBuffer.allocate(1);
            _SeekableChannel.read(dst);
            return dst.get(0);
        } catch (IOException ex) {
            Logger.getLogger(ByteBufferEndian.class.getName()).log(Level.SEVERE, null, ex);
            return (byte)0;
        }
    }
    public int read(byte[] bytes, int offset, int end) {
        
        try {
            java.nio.ByteBuffer dst = java.nio.ByteBuffer.allocate(bytes.length);
            _SeekableChannel.read(dst);
            bytes = dst.array();
            return bytes.length;
        } catch (IOException ex) {
            Logger.getLogger(ByteBufferEndian.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        
    }
}
