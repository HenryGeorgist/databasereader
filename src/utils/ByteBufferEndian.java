package utils;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Will_and_Sara
 */
public class ByteBufferEndian implements java.nio.channels.SeekableByteChannel {
    private ByteBuffer _Buffer;
    //private StringBuffer bla;
    private boolean SetPosition(int position){
        try{
            _Buffer = (ByteBuffer)_Buffer.position(position);
            
            return true;
        }catch(IllegalArgumentException ex){
            //position is less than mark.
            //how do i advance the position to the place i want?
            _Buffer.rewind();
            if(position>_Buffer.remaining()){
                //must need to move mark as far forward as i can iteratively?
            }else{
                _Buffer.position(position);
                return true;
            }
                    
            return false;
        }

    }
    @Override
    public int read(ByteBuffer dst) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        //bla = new StringBuffer();
        //bla.
        _Buffer.
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long position() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long size() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isOpen() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
