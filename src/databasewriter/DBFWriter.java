/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package databasewriter;

import databasereader.TypeEnum;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Will_and_Sara
 */
public class DBFWriter extends AbstractWriter {
    private utils.ByteBufferEndian _DBFWriter;
    private short _FirstDataRecordIndex;
    private short _RecordLength;
    private int[] _Lengths;
    private int[] _Positions;
    private String PadString(String string, int finallength, char padchar){
        java.lang.StringBuilder s = new java.lang.StringBuilder(string);
        for(int i = string.length(); i < finallength+1;i++){
            s.append(padchar);
        }
        return s.toString();
    }
    private void WriteNewHeader(String NewColumnName, int NewFieldLength, String ColumnType, int newFieldNumDecimals, java.io.RandomAccessFile Writer){
        try {
            UpdateEditDate(Writer);
            Writer.writeInt(Integer.reverseBytes(_NumberOfRows));
            Writer.writeShort(Short.reverseBytes((short)(_FirstDataRecordIndex + 32)));//needs to be little endian
            Writer.writeShort(Short.reverseBytes((short)(_RecordLength + NewFieldLength)));//needs to be little endian
            _DBFWriter.seek(0);
            _DBFWriter.seek(12);
            //the next 20 bytes are reserved.
            byte reservedBytes[] = new byte[20];
            _DBFWriter.read(reservedBytes,0,20);
            //_DBFReader.skipBytes(1);
            //System.out.println(_DBFReader.ReadString(0, 11));
            Writer.write(reservedBytes);
            //now write the new header infos.
            for(int i = 0; i < _ColumnNames.length;i++){
                Writer.writeChars(PadString(_ColumnNames[i],10,(char)0));//must be the next 11 bytes
                //Writer.skipBytes(1);
                switch(_ColumnTypes[i]){
                    case STRING:
                        Writer.writeChars("C");
                        break;
                    case INT:
                        Writer.writeChars("N");
                        break;
                    case BOOLEAN:
                        Writer.writeChars("L");
                        break;
                    case DOUBLE:
                        Writer.writeChars("F");
                        break;
                    case DATE:
                        Writer.writeChars("D");
                        break;
                    default:
                        Writer.writeChars("C");
                        break;
                }
                _DBFWriter.skipBytes(12);//columnName and column type
                reservedBytes = new byte[4];//reserved bytes...
                _DBFWriter.read(reservedBytes,0,4);//position needs to be set properly...
                Writer.write(reservedBytes, 0, 4);//next four bytes are reserved
                Writer.write(_Lengths[i]);//length (as a byte actually).
                _DBFWriter.skipBytes(1);//the length byte
                Writer.write(_DBFWriter.read());//number of decimals //directly from the bytestream
                reservedBytes = new byte[14];
                _DBFWriter.read(reservedBytes,0,14);//next fourteen bytes are reserved
                Writer.write(reservedBytes,0,14); 
            }
            //now write out the info for the new column
            Writer.writeChars(PadString(NewColumnName,10,(char)0));//must be the next 10 bytes
            Writer.writeChars(ColumnType);//must be the right string, and must be 2 bytes.
            Writer.skipBytes(4);//next four bytes are reserved
            Writer.write(NewFieldLength);//length (as a byte actually).
            Writer.write(0);//number of decimals//how do i determine number of decimals?
            Writer.skipBytes(14);//_DBFReader.read(reservedBytes,0,14);//next fourteen bytes are reserved
            System.out.println("Finished with header");
        } catch (IOException ex) {
            Logger.getLogger(DBFWriter.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    @Override
    public void AddColumn(String ColumnName, int[] data) {
        if(ColumnName.length()<=10){
            if(java.util.Arrays.asList(_ColumnNames).indexOf(ColumnName)==-1){
                if(_NumberOfRows==data.length){
                    if(!IsOpen()){Open();}//so that we can read the data.
                    java.io.RandomAccessFile Writer = null;
                    try {
                        //create a tmp file to write everythign into.
                        java.io.File TMP = java.io.File.createTempFile("TMP", ".dbf");
                        Writer = new java.io.RandomAccessFile(TMP,"rwd");
                        int newfieldlength = Integer.toString(Integer.MIN_VALUE).length();//negatives?
                        WriteNewHeader(ColumnName, newfieldlength,"C",0,Writer);
                        //write out all rows.
                        _DBFWriter.seek(0);
                        _DBFWriter.seek(_FirstDataRecordIndex + 1);
                        byte[] ExistingData = new byte[_RecordLength];
                        System.out.println("WritingData");
                        for(int i = 0; i<data.length;i++){
                            _DBFWriter.read(ExistingData,0,_RecordLength);
                            Writer.write(ExistingData);
                            Writer.writeChars(PadString(Integer.toString(data[i]),newfieldlength,(char)32));
                        }
                        if(IsOpen()){Close();}//delete old file, copy tmp file then delete old tmp file
                        Writer.close();
                        java.io.File currentFile = new java.io.File(_FilePath);
                        System.out.println("Complete,now deleting and copying.");
                        currentFile.delete();
                        java.nio.file.Files.copy(TMP.toPath(), currentFile.toPath(),java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        TMP.delete();
                        Initialize();
                    } catch (IOException ex) {
                        if(Writer!=null){
                            try {
                                Writer.close();
                            } catch (IOException ex1) {
                                Logger.getLogger(DBFWriter.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                        }
                        Close();
                        Logger.getLogger(DBFWriter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }//not the right number of rows.
            }//name already existis
        }//name too long
    }
    @Override
    public void AddColumn(String ColumnName, String[] data) {
        if(ColumnName.length()<=10){
            if(java.util.Arrays.asList(_ColumnNames).indexOf(ColumnName)==-1){
                if(_NumberOfRows==data.length){
                    if(!IsOpen()){Open();}//so that we can read the data.
                    java.io.RandomAccessFile Writer = null;
                    try {
                        //create a tmp file to write everythign into.
                        java.io.File TMP = java.io.File.createTempFile("TMP", ".dbf");
                        Writer = new java.io.RandomAccessFile(TMP,"rwd");
                        int newfieldlength = 0;
                        for(int i = 0; i<data.length;i++){
                            if(data[i].length()>newfieldlength){newfieldlength = data[i].length();}
                        }
                        WriteNewHeader(ColumnName, newfieldlength,"C",0,Writer);
                        //write out all rows.
                        _DBFWriter.seek(0);
                        _DBFWriter.seek(_FirstDataRecordIndex + 1);
                        byte[] ExistingData = new byte[_RecordLength];
                        for(int i = 0; i<data.length;i++){
                            _DBFWriter.read(ExistingData,0,_RecordLength);
                            Writer.write(ExistingData);
                            Writer.writeChars(PadString(data[i],newfieldlength,(char)32));
                        }
                        if(IsOpen()){Close();}//delete old file, copy tmp file then delete old tmp file
                        Writer.close();
                        java.io.File currentFile = new java.io.File(_FilePath);
                        currentFile.delete();
                        java.nio.file.Files.copy(TMP.toPath(), currentFile.toPath(),java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        TMP.delete();
                        Initialize();
                    } catch (IOException ex) {
                        if(Writer!=null){
                            try {
                                Writer.close();
                            } catch (IOException ex1) {
                                Logger.getLogger(DBFWriter.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                        }
                        Close();
                        Logger.getLogger(DBFWriter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }//not the right number of rows.
            }//name already existis
        }//name too long
    }
    @Override
    public void AddColumn(String ColumnName, double[] data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void AddColumn(String ColumnName, boolean[] data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    private void UpdateEditDate(java.io.RandomAccessFile writer) throws IOException{
        writer.seek(1);
        byte[] dateofedit = new byte[3];
        dateofedit[0] = (byte)(Calendar.getInstance().get(Calendar.YEAR)-1900);
        dateofedit[1] = (byte)java.time.LocalDate.now().getMonth().getValue();//will be 1 -12 i think i want 01 to 12..
        dateofedit[2] = (byte)java.time.LocalDate.now().getDayOfMonth();
        writer.write(dateofedit);
    }
    @Override
    public void EditColumn(String ColumnName, int[] data) {
        int index =java.util.Arrays.asList(_ColumnNames).indexOf(ColumnName);
        if(index!=-1){
            if(TypeEnum.INT==_ColumnTypes[index]){
                if(_NumberOfRows==data.length){
                    if(IsOpen()){Close();}//so that we can change the data.
                    java.io.RandomAccessFile writer = null;
                    try {
                        writer = new java.io.RandomAccessFile(_FilePath,"rwd");
                        UpdateEditDate(writer);
                        int rowOffset = _Positions[index];
                        writer.seek(_FirstDataRecordIndex+1);
                        for(int i = 0; i < _NumberOfRows;i++){
                            writer.skipBytes(rowOffset);
                            writer.writeChars(PadString(Integer.toString(data[i]),_Lengths[index],(char)32));//if lengths is not a maxint length, then there could ultimately be problems
                            writer.skipBytes(_RecordLength - (_Lengths[index]+rowOffset));
                        }
                        writer.close();
                    } catch (FileNotFoundException ex) {
                        //file could not be written to.
                        Logger.getLogger(DBFWriter.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        //io exception
                        Logger.getLogger(DBFWriter.class.getName()).log(Level.SEVERE, null, ex);
                        if(writer!=null){
                            try {
                                writer.close();
                            } catch (IOException ex1) {
                                Logger.getLogger(DBFWriter.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                        }
                    }
                }else{
                    //not consistent number of rows.
                }
            }else{
                //column type is not consistent with int.
            }
        }else{
            //column name is not found in the current column list, try add column
        }
    }
    @Override
    public void EditColumn(String ColumnName, String[] data) {
        int index =java.util.Arrays.asList(_ColumnNames).indexOf(ColumnName);
        if(index!=-1){
            if(TypeEnum.STRING==_ColumnTypes[index]){
                if(_NumberOfRows==data.length){
                    if(IsOpen()){Close();}//so that we can change the data.
                    java.io.RandomAccessFile writer = null;
                    try {
                        writer = new java.io.RandomAccessFile(_FilePath,"rwd");
                        UpdateEditDate(writer);
                        int rowOffset = _Positions[index];
                        writer.seek(_FirstDataRecordIndex+1);
                        for(int i = 0; i < _NumberOfRows;i++){
                            writer.skipBytes(rowOffset);
                            if(_Lengths[i]>data[i].length()){
                                writer.writeBytes(PadString(data[i],_Lengths[index],(char)32));
                            }else{
                                writer.writeBytes(data[i].substring(0, _Lengths[index]));
                            }
                            writer.skipBytes(_RecordLength - (_Lengths[index]+rowOffset));
                        }
                        writer.close();
                    } catch (FileNotFoundException ex) {
                        //file could not be written to.
                        Logger.getLogger(DBFWriter.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        //io exception
                        Logger.getLogger(DBFWriter.class.getName()).log(Level.SEVERE, null, ex);
                        if(writer!=null){
                            try {
                                writer.close();
                            } catch (IOException ex1) {
                                Logger.getLogger(DBFWriter.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                        }
                    }
                }else{
                    //not consistent number of rows.
                }
            }else{
                //column type is not consistent with string.
            }
        }else{
            //column name is not found in the current column list, try add column
        }
    }
    @Override
    public void EditColumn(String ColumnName, double[] data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void EditColumn(String ColumnName, boolean[] data) {
        int index =java.util.Arrays.asList(_ColumnNames).indexOf(ColumnName);
        if(index!=-1){
            if(TypeEnum.BOOLEAN==_ColumnTypes[index]){
                if(_NumberOfRows==data.length){
                    if(IsOpen()){Close();}//so that we can change the data.
                    java.io.RandomAccessFile writer = null;
                    try {
                        writer = new java.io.RandomAccessFile(_FilePath,"rwd");
                        UpdateEditDate(writer);
                        int rowOffset = _Positions[index];
                        writer.seek(_FirstDataRecordIndex+1);
                        for(int i = 0; i < _NumberOfRows;i++){
                            writer.skipBytes(rowOffset);
                            if(data[i]){
                                writer.writeBytes("1");//what if the lengths are not set properly?
                            }else{
                                writer.writeBytes("0");
                            }
                            writer.skipBytes(_RecordLength - (_Lengths[index]+rowOffset));
                        }
                        writer.close();
                    } catch (FileNotFoundException ex) {
                        //file could not be written to.
                        Logger.getLogger(DBFWriter.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        //io exception
                        Logger.getLogger(DBFWriter.class.getName()).log(Level.SEVERE, null, ex);
                        if(writer!=null){
                            try {
                                writer.close();
                            } catch (IOException ex1) {
                                Logger.getLogger(DBFWriter.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                        }
                    }
                }else{
                    //not consistent number of rows.
                }
            }else{
                //column type is not consistent with boolean.
            }
        }else{
            //column name is not found in the current column list, try add column
        }
    } 

    @Override
    public void Open() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void Close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void Initialize() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
