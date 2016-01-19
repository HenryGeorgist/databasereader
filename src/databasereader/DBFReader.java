/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package databasereader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Will_and_Sara
 */
public class DBFReader extends AbstractReader{
    private utils.ByteBufferEndian _DBFReader;
    private short _FirstDataRecordIndex;
    private short _RecordLength;
    private int[] _Lengths;
    private int[] _Positions;
    public DBFReader(String FilePath){
        String ext;
        ext = FilePath.substring(FilePath.lastIndexOf(".")).toLowerCase();
        if(!ext.equals(".dbf")){//not a dbf
            System.out.println("The File: " + FilePath + " was not a .dbf file");
        }else{
           _FilePath = FilePath;
           Open();
           Initialize();
           Close();
        }
    }
    @Override
    public void Open() {
        if(IsOpen()){
            //already open.
        }else{
//            try {
                _DBFReader = new utils.ByteBufferEndian(_FilePath);
                //_DBFReader = new utils.RandomAccessEndian(_FilePath,"r");
                _Open = true;
//            } catch (FileNotFoundException ex) {
//                System.out.println("The File: " + _FilePath + " could not be found");
//                Logger.getLogger(DBFReader.class.getName()).log(Level.SEVERE, null, ex);
//            }
        }
    }
    @Override
    public void Close() {//consider calling this line in the destructor if(IsOpen()){Close();}
        if(IsOpen()){
            try {
                _DBFReader.close();
                _Open = false;
            } catch (IOException ex) {
                Logger.getLogger(DBFReader.class.getName()).log(Level.SEVERE, null, ex);
            }            
        }//else it is already closed, so dont try.
    }
    //http://www.oocities.org/geoff_wass/dBASE/GaryWhite/dBASE/FAQ/qformt.htm   //dbase IV spec, has the first 32 bits right        
    //http://www.dbase.com/KnowledgeBase/int/db7_file_fmt.htm //info on other field types that might be supported.
    /**
     *Initializes the dbf by reading the header, which contains information that describes the columns and rows of the dbf.
     */
    @Override
    public void Initialize() {
        if(!IsOpen()) Open();
        try {
            _DBFReader.seek(0);
            _DBFReader.skipBytes(4);//byte 0 determines file type and presence of memo fields, 1-3 determine last edited date.
            _NumberOfRows = _DBFReader.readLittleInt();
            _FirstDataRecordIndex = _DBFReader.readLittleShort();
            _RecordLength = _DBFReader.readLittleShort();
            _NumberOfColumns = (_FirstDataRecordIndex - 32 -1) / 32;
            _ColumnNames = new String[_NumberOfColumns];
            _ColumnTypes = new TypeEnum[_NumberOfColumns];
            _Lengths = new int[_NumberOfColumns];
            _Positions = new int [_NumberOfColumns];
            String TmpColName;
            String TmpColType;
            _DBFReader.skipBytes(3);//reserved
            _DBFReader.skipBytes(13);//reserved
            _DBFReader.skipBytes(4);//reserved
            int NumberOfDecimals = 0;
            for(int i = 0; i<_NumberOfColumns;i++){
                TmpColName = _DBFReader.ReadString(0,10).trim();
                _ColumnNames[i] = TmpColName;
                TmpColType = _DBFReader.ReadString(0,2);
                _DBFReader.skipBytes(4);//advance to 16
                _Lengths[i] = _DBFReader.read();//advance to 17
                NumberOfDecimals = _DBFReader.read();//adance to 18
                _DBFReader.skipBytes(14);//advance to 31 end of field record.
                switch(TmpColType){
                    case "I":
                        _ColumnTypes[i] = TypeEnum.INT;
                        break;
                    case "N":
                        if(NumberOfDecimals>0){
                            _ColumnTypes[i] = TypeEnum.DOUBLE;
                        }else{
                            _ColumnTypes[i] = TypeEnum.INT;
                        }
                        break;
                    case "F":
                        _ColumnTypes[i] = TypeEnum.DOUBLE;
                        break;
                    case "B":
                        _ColumnTypes[i] = TypeEnum.STRING;
                        break;
                    case "M":
                        _ColumnTypes[i] = TypeEnum.STRING;
                        break;
                    case "L":
                        _ColumnTypes[i] = TypeEnum.BOOLEAN;
                        break;
                    case "C":
                        _ColumnTypes[i] = TypeEnum.STRING;
                        break;
                    default:
                        _ColumnTypes[i] = TypeEnum.STRING;
                }
                if(i==0){
                    _Positions[i] = 0;
                }else{
                    _Positions[i] = _Positions[i-1] + _Lengths[i-1];
                }
            } 
        } catch (IOException ex) {
            System.out.println(ex.toString());
            Logger.getLogger(DBFReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private String getCellAsString(int ColumnIndex, int RowIndex){
        if(!IsOpen()){Open();}
        try {
            _DBFReader.seek(0);
            _DBFReader.skipBytes(_FirstDataRecordIndex+1+(RowIndex*_RecordLength)+_Positions[ColumnIndex]);
            return _DBFReader.ReadString(0, _Lengths[ColumnIndex]);
        } catch (IOException ex) {
            System.out.println("Cannot reset");
            Logger.getLogger(DBFReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;        
    }
    private String RemoveSpaces(String Input){
       java.lang.StringBuilder s = new java.lang.StringBuilder();
       String character = "";
       for(int i = 0; i<Input.length();i++){
           character = Character.toString(Input.charAt(i));
           if(!character.equals(" ")){
           s.append(character);
           }
       }
       return s.toString();
    }
    private String RemoveLeadingSpaces(String Input){
       java.lang.StringBuilder s = new java.lang.StringBuilder();
       boolean FirstCharacter = false;
       String character = "";
       for(int i = 0; i<Input.length();i++){
           character = Character.toString(Input.charAt(i));
           if(!FirstCharacter){
                if(!character.equals(" ")){
                    FirstCharacter = true;
                    s.append(character);
                }
           }else{
               s.append(character);
           }
       }
       return s.toString();
    }
    private String PadString(String string, int finallength){
        java.lang.StringBuilder s = new java.lang.StringBuilder(string);
        for(int i = string.length(); i < finallength+1;i++){
            s.append((char)32);
        }
        return s.toString();
    }
    @Override
    public Object getCell(int ColumnIndex, int RowIndex) {
        String resultAsString = getCellAsString(ColumnIndex, RowIndex);
        switch(_ColumnTypes[ColumnIndex]){
            case STRING:
                return resultAsString;
            case DOUBLE:
                return Double.parseDouble(resultAsString);
            case INT:
                return Integer.parseInt(RemoveSpaces(resultAsString));
            case BOOLEAN:
                return Boolean.parseBoolean(resultAsString);
            case DATE:
                return resultAsString;
            default:
                return resultAsString;           
        }
    }
    @Override
    public Object getCell(String ColumnName, int RowIndex) {
        return getCell(java.util.Arrays.asList(_ColumnNames).indexOf(ColumnName),RowIndex);
    }
    @Override
    public Object[] getRow(int RowIndex) {
        if(_NumberOfRows>=RowIndex){
            if(!IsOpen()){Open();}
            try {
                _DBFReader.seek(_FirstDataRecordIndex+1+(RowIndex * _RecordLength));
                Object[] result = new Object[_NumberOfColumns];
                for(int ColumnIndex = 0; ColumnIndex<_NumberOfColumns; ColumnIndex++){
                    switch(_ColumnTypes[ColumnIndex]){
                        case STRING:
                            result[ColumnIndex] = _DBFReader.ReadString(0, _Lengths[ColumnIndex]);
                            break;
                        case DOUBLE:
                            result[ColumnIndex] = Double.parseDouble(_DBFReader.ReadString(0, _Lengths[ColumnIndex]));
                            break;
                        case INT:
                            result[ColumnIndex] = Integer.parseInt(RemoveSpaces(_DBFReader.ReadString(0, _Lengths[ColumnIndex])));//will be slow.
                            break;
                        case BOOLEAN:
                            result[ColumnIndex] = Boolean.parseBoolean(_DBFReader.ReadString(0, _Lengths[ColumnIndex]));
                            break;
                        case DATE:
                            result[ColumnIndex] = _DBFReader.ReadString(0, _Lengths[ColumnIndex]);
                            break;
                        default:
                            result[ColumnIndex] = _DBFReader.ReadString(0, _Lengths[ColumnIndex]);
                            break;
                    }
                }
                return result;
            } catch (IOException ex) {
                Logger.getLogger(DBFReader.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }else{
            //not that many rows.
            return null;
        }
    }
    @Override
    public Object[] getRowForColumns(int RowIndex, int[] ColumnIndices) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public Object[] getColumn(String ColumnName) {
        int index =java.util.Arrays.asList(_ColumnNames).indexOf(ColumnName);
        if(index!=-1){return getColumn(index);}
        return null;
    }
    @Override
    public Object[] getColumn(int ColumnIndex) {
        if(_ColumnNames.length< ColumnIndex){return null;}
        if(!IsOpen()){Open();}
        try {
            _DBFReader.seek(_FirstDataRecordIndex+1);
            Object[] result = new Object[_NumberOfRows];
            int RecordOffset = _Positions[ColumnIndex];
            int ReadLength = _Lengths[ColumnIndex];
            int RemainingOffset = _RecordLength - RecordOffset - ReadLength;
            switch(_ColumnTypes[ColumnIndex]){
            case STRING:
                for(int i = 0; i<_NumberOfRows;i++){
                    if(RecordOffset != _DBFReader.skipBytes(RecordOffset)){System.out.println("Skip Failure");}
                    result[i] = _DBFReader.ReadString(0, ReadLength);
                    if(RemainingOffset != _DBFReader.skipBytes(RemainingOffset)){System.out.println("Skip Failure");}
                }
                break;
            case DOUBLE:
                for(int i = 0; i<_NumberOfRows;i++){                    
                    if(RecordOffset != _DBFReader.skipBytes(RecordOffset)){System.out.println("Skip Failure");}
                    result[i] = Double.parseDouble(_DBFReader.ReadString(0, ReadLength));
                    if(RemainingOffset != _DBFReader.skipBytes(RemainingOffset)){System.out.println("Skip Failure");}
                }
                break;              
            case INT:
                for(int i = 0; i<_NumberOfRows;i++){
                    if(RecordOffset != _DBFReader.skipBytes(RecordOffset)){System.out.println("Skip Failure");}
                    result[i] = Integer.parseInt(RemoveSpaces(_DBFReader.ReadString(0, ReadLength)));
                    if(RemainingOffset != _DBFReader.skipBytes(RemainingOffset)){System.out.println("Skip Failure");}
                }
                break;               
            case BOOLEAN:
                for(int i = 0; i<_NumberOfRows;i++){
                    if(RecordOffset != _DBFReader.skipBytes(RecordOffset)){System.out.println("Skip Failure");}
                    result[i] = _DBFReader.ReadString(0, ReadLength);
                    if(RemainingOffset != _DBFReader.skipBytes(RemainingOffset)){System.out.println("Skip Failure");}
                }
                break;               
            case DATE:
                for(int i = 0; i<_NumberOfRows;i++){
                    if(RecordOffset != _DBFReader.skipBytes(RecordOffset)){System.out.println("Skip Failure");}
                    result[i] = _DBFReader.ReadString(0, ReadLength);
                    if(RemainingOffset != _DBFReader.skipBytes(RemainingOffset)){System.out.println("Skip Failure");}
                }
                break;                
            default:
                for(int i = 0; i<_NumberOfRows;i++){
                    if(RecordOffset != _DBFReader.skipBytes(RecordOffset)){System.out.println("Skip Failure");}
                    result[i] = _DBFReader.ReadString(0, ReadLength);
                    if(RemainingOffset != _DBFReader.skipBytes(RemainingOffset)){System.out.println("Skip Failure");}
                }
                break;                                 
            }
            Close();
            return result;
        } catch (IOException ex) {
            Logger.getLogger(DBFReader.class.getName()).log(Level.SEVERE, null, ex);
            Close();
            return null;
        }
    }
    private void WriteNewHeader(String NewColumnName, int NewFieldLength, String ColumnType, int newFieldNumDecimals, java.io.RandomAccessFile Writer){
//        try {
//            UpdateEditDate(Writer);
//            Writer.writeInt(Integer.reverseBytes(_NumberOfRows));
//            Writer.writeShort(Short.reverseBytes((short)(_FirstDataRecordIndex + 32)));//needs to be little endian
//            Writer.writeShort(Short.reverseBytes((short)(_RecordLength + NewFieldLength)));//needs to be little endian
//            _DBFReader.seek(0);
//            _DBFReader.seek(12);
//            //the next 20 bytes are reserved.
//            byte reservedBytes[] = new byte[20];
//            _DBFReader.read(reservedBytes,0,20);
//            //_DBFReader.skipBytes(1);
//            //System.out.println(_DBFReader.ReadString(0, 11));
//            Writer.write(reservedBytes);
//            //now write the new header infos.
//            for(int i = 0; i < _ColumnNames.length;i++){
//                Writer.writeChars(PadString(_ColumnNames[i],10));//must be the next 11 bytes
//                //Writer.skipBytes(1);
//                switch(_ColumnTypes[i]){
//                    case STRING:
//                        Writer.writeChars("C");
//                        break;
//                    case INT:
//                        Writer.writeChars("N");
//                        break;
//                    case BOOLEAN:
//                        Writer.writeChars("L");
//                        break;
//                    case DOUBLE:
//                        Writer.writeChars("F");
//                        break;
//                    case DATE:
//                        Writer.writeChars("D");
//                        break;
//                    default:
//                        Writer.writeChars("C");
//                        break;
//                }
//                _DBFReader.skipBytes(12);//columnName and column type
//                reservedBytes = new byte[4];//reserved bytes...
//                _DBFReader.read(reservedBytes,0,4);//position needs to be set properly...
//                Writer.write(reservedBytes, 0, 4);//next four bytes are reserved
//                Writer.write(_Lengths[i]);//length (as a byte actually).
//                _DBFReader.skipBytes(1);//the length byte
//                Writer.write(_DBFReader.read());//number of decimals //directly from the bytestream
//                reservedBytes = new byte[14];
//                _DBFReader.read(reservedBytes,0,14);//next fourteen bytes are reserved
//                Writer.write(reservedBytes,0,14); 
//            }
//            //now write out the info for the new column
//            Writer.writeChars(PadString(NewColumnName,10));//must be the next 10 bytes
//            Writer.writeChars(ColumnType);//must be the right string, and must be 2 bytes.
//            Writer.skipBytes(4);//next four bytes are reserved
//            Writer.write(NewFieldLength);//length (as a byte actually).
//            Writer.write(0);//number of decimals//how do i determine number of decimals?
//            Writer.skipBytes(14);//_DBFReader.read(reservedBytes,0,14);//next fourteen bytes are reserved
//            System.out.println("Finished with header");
//        } catch (IOException ex) {
//            Logger.getLogger(DBFReader.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
    }
    @Override
    public void AddColumn(String ColumnName, int[] data) {
//        if(ColumnName.length()<=10){
//            if(java.util.Arrays.asList(_ColumnNames).indexOf(ColumnName)==-1){
//                if(_NumberOfRows==data.length){
//                    if(!IsOpen()){Open();}//so that we can read the data.
//                    java.io.RandomAccessFile Writer = null;
//                    try {
//                        //create a tmp file to write everythign into.
//                        java.io.File TMP = java.io.File.createTempFile("TMP", ".dbf");
//                        Writer = new java.io.RandomAccessFile(TMP,"rwd");
//                        int newfieldlength = Integer.toString(Integer.MIN_VALUE).length();//negatives?
//                        WriteNewHeader(ColumnName, newfieldlength,"C",0,Writer);
//                        //write out all rows.
//                        _DBFReader.seek(0);
//                        _DBFReader.seek(_FirstDataRecordIndex + 1);
//                        byte[] ExistingData = new byte[_RecordLength];
//                        System.out.println("WritingData");
//                        for(int i = 0; i<data.length;i++){
//                            _DBFReader.read(ExistingData,0,_RecordLength);
//                            Writer.write(ExistingData);
//                            Writer.writeChars(PadString(Integer.toString(data[i]),newfieldlength));
//                        }
//                        if(IsOpen()){Close();}//delete old file, copy tmp file then delete old tmp file
//                        Writer.close();
//                        java.io.File currentFile = new java.io.File(_FilePath);
//                        System.out.println("Complete,now deleting and copying.");
//                        currentFile.delete();
//                        java.nio.file.Files.copy(TMP.toPath(), currentFile.toPath(),java.nio.file.StandardCopyOption.REPLACE_EXISTING);
//                        TMP.delete();
//                        Initialize();
//                    } catch (IOException ex) {
//                        if(Writer!=null){
//                            try {
//                                Writer.close();
//                            } catch (IOException ex1) {
//                                Logger.getLogger(DBFReader.class.getName()).log(Level.SEVERE, null, ex1);
//                            }
//                        }
//                        Close();
//                        Logger.getLogger(DBFReader.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }//not the right number of rows.
//            }//name already existis
//        }//name too long
    }
    @Override
    
    public void AddColumn(String ColumnName, String[] data) {
//        if(ColumnName.length()<=10){
//            if(java.util.Arrays.asList(_ColumnNames).indexOf(ColumnName)==-1){
//                if(_NumberOfRows==data.length){
//                    if(!IsOpen()){Open();}//so that we can read the data.
//                    java.io.RandomAccessFile Writer = null;
//                    try {
//                        //create a tmp file to write everythign into.
//                        java.io.File TMP = java.io.File.createTempFile("TMP", ".dbf");
//                        Writer = new java.io.RandomAccessFile(TMP,"rwd");
//                        int newfieldlength = 0;
//                        for(int i = 0; i<data.length;i++){
//                            if(data[i].length()>newfieldlength){newfieldlength = data[i].length();}
//                        }
//                        WriteNewHeader(ColumnName, newfieldlength,"C",0,Writer);
//                        //write out all rows.
//                        _DBFReader.seek(0);
//                        _DBFReader.seek(_FirstDataRecordIndex + 1);
//                        byte[] ExistingData = new byte[_RecordLength];
//                        for(int i = 0; i<data.length;i++){
//                            _DBFReader.read(ExistingData,0,_RecordLength);
//                            Writer.write(ExistingData);
//                            Writer.writeChars(PadString(data[i],newfieldlength));
//                        }
//                        if(IsOpen()){Close();}//delete old file, copy tmp file then delete old tmp file
//                        Writer.close();
//                        java.io.File currentFile = new java.io.File(_FilePath);
//                        currentFile.delete();
//                        java.nio.file.Files.copy(TMP.toPath(), currentFile.toPath(),java.nio.file.StandardCopyOption.REPLACE_EXISTING);
//                        TMP.delete();
//                        Initialize();
//                    } catch (IOException ex) {
//                        if(Writer!=null){
//                            try {
//                                Writer.close();
//                            } catch (IOException ex1) {
//                                Logger.getLogger(DBFReader.class.getName()).log(Level.SEVERE, null, ex1);
//                            }
//                        }
//                        Close();
//                        Logger.getLogger(DBFReader.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }//not the right number of rows.
//            }//name already existis
//        }//name too long
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
                            writer.writeChars(PadString(Integer.toString(data[i]),_Lengths[index]));//if lengths is not a maxint length, then there could ultimately be problems
                            writer.skipBytes(_RecordLength - (_Lengths[index]+rowOffset));
                        }
                        writer.close();
                    } catch (FileNotFoundException ex) {
                        //file could not be written to.
                        Logger.getLogger(DBFReader.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        //io exception
                        Logger.getLogger(DBFReader.class.getName()).log(Level.SEVERE, null, ex);
                        if(writer!=null){
                            try {
                                writer.close();
                            } catch (IOException ex1) {
                                Logger.getLogger(DBFReader.class.getName()).log(Level.SEVERE, null, ex1);
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
                                writer.writeBytes(PadString(data[i],_Lengths[index]));
                            }else{
                                writer.writeBytes(data[i].substring(0, _Lengths[index]));
                            }
                            writer.skipBytes(_RecordLength - (_Lengths[index]+rowOffset));
                        }
                        writer.close();
                    } catch (FileNotFoundException ex) {
                        //file could not be written to.
                        Logger.getLogger(DBFReader.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        //io exception
                        Logger.getLogger(DBFReader.class.getName()).log(Level.SEVERE, null, ex);
                        if(writer!=null){
                            try {
                                writer.close();
                            } catch (IOException ex1) {
                                Logger.getLogger(DBFReader.class.getName()).log(Level.SEVERE, null, ex1);
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
                        Logger.getLogger(DBFReader.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        //io exception
                        Logger.getLogger(DBFReader.class.getName()).log(Level.SEVERE, null, ex);
                        if(writer!=null){
                            try {
                                writer.close();
                            } catch (IOException ex1) {
                                Logger.getLogger(DBFReader.class.getName()).log(Level.SEVERE, null, ex1);
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
}
