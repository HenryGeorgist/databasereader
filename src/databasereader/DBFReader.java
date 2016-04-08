/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package databasereader;

//import java.io.FileNotFoundException;
import java.io.IOException;
//import java.util.Calendar;
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
    public final void Open() {
        if(IsOpen()){
            //already open.
        }else{
//            try {
                _DBFReader = new utils.ByteBufferEndian(_FilePath);
//                _DBFReader = new utils.RandomAccessEndian(_FilePath,"r");
                _Open = true;
//            } catch (FileNotFoundException ex) {
//               System.out.println("The File: " + _FilePath + " could not be found");
//               Logger.getLogger(DBFReader.class.getName()).log(Level.SEVERE, null, ex);
//            }
        }
    }
    @Override
    public final void Close() {//consider calling this line in the destructor if(IsOpen()){Close();}
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
    public final void Initialize() {
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
                TmpColType = _DBFReader.ReadString(0,2).trim();
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
    private String PadString(String string, int finallength, char padchar){
        java.lang.StringBuilder s = new java.lang.StringBuilder(string);
        for(int i = string.length(); i < finallength+1;i++){
            s.append(padchar);
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

}
