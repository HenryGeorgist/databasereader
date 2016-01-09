/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package databasereader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Will_and_Sara
 */
public class DBFReader extends AbstractReader{
    private utils.RandomAccessEndian _DBFReader;
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
            try {
                _DBFReader = new utils.RandomAccessEndian(_FilePath,"r");
                _Open = true;
            } catch (FileNotFoundException ex) {
                System.out.println("The File: " + _FilePath + " could not be found");
                Logger.getLogger(DBFReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    @Override
    public void Close() {
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
    @Override
    public Object getCell(int ColumnIndex, int RowIndex) {
        String resultAsString = getCellAsString(ColumnIndex, RowIndex);
        switch(_ColumnTypes[ColumnIndex]){
            case STRING:
                return resultAsString;
            case DOUBLE:
                return Double.parseDouble(resultAsString);
            case INT:
                return Integer.parseInt(resultAsString);
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
            _DBFReader.skipBytes(_FirstDataRecordIndex+1);
            Object[] result = new Object[_NumberOfRows];
            int RecordOffset = _Positions[ColumnIndex];
            int ReadLength = _Lengths[ColumnIndex];
            int RemainingOffset = _RecordLength - RecordOffset - ReadLength;
            switch(_ColumnTypes[ColumnIndex]){
            case STRING:
                for(int i = 0; i<_NumberOfRows;i++){
                    //_DBFReader.mark(_RecordLength+1);
                    _DBFReader.skipBytes(RecordOffset);
                    result[i] = _DBFReader.ReadString(0, ReadLength);
                    _DBFReader.skipBytes(RemainingOffset);
                }
                break;
            case DOUBLE:
                for(int i = 0; i<_NumberOfRows;i++){                    
                    System.out.println(i);
                    if(RecordOffset != _DBFReader.skipBytes(RecordOffset)){
                        System.out.println("Skip Failure");
                        //_DBFReader.mark(Integer.MAX_VALUE);
                        //_DBFReader.reset();
                        //_DBFReader.skipBytes(RecordOffset);
                    }else{
                        //_DBFReader.skipBytes(RecordOffset);
                    }
                    String test =_DBFReader.ReadString(0, ReadLength);
                    System.out.println(test);
                    result[i] = Double.parseDouble(test);
                    System.out.println(result[i]);
                    if(RemainingOffset != _DBFReader.skipBytes(RemainingOffset)){
                        System.out.println("Skip Failure");
                        //_DBFReader.mark(Integer.MAX_VALUE);
                        //_DBFReader.reset();
                        //_DBFReader.skip(RemainingOffset);
                    }else{
                        //_DBFReader.skip(RemainingOffset);
                    }//
                }
                break;              
            case INT:
                for(int i = 0; i<_NumberOfRows;i++){
                    //_DBFReader.mark(_RecordLength + 1);
                    _DBFReader.skipBytes(RecordOffset);
                    result[i] = Integer.parseInt(_DBFReader.ReadString(0, ReadLength));
                    _DBFReader.skipBytes(RemainingOffset);
                }
                break;               
            case BOOLEAN:
                for(int i = 0; i<_NumberOfRows;i++){
                    //_DBFReader.mark(_RecordLength + 1);
                    _DBFReader.skipBytes(RecordOffset);
                    result[i] = _DBFReader.ReadString(0, ReadLength);
                    _DBFReader.skipBytes(RemainingOffset);
                }
                break;               
            case DATE:
                for(int i = 0; i<_NumberOfRows;i++){
                    //_DBFReader.markBytes(_RecordLength + 1);
                    _DBFReader.skipBytes(RecordOffset);
                    result[i] = _DBFReader.ReadString(0, ReadLength);
                    _DBFReader.skipBytes(RemainingOffset);
                }
                break;                
            default:
                for(int i = 0; i<_NumberOfRows;i++){
                    //_DBFReader.mark(_RecordLength + 1);
                    _DBFReader.skipBytes(RecordOffset);
                    result[i] = _DBFReader.ReadString(0, ReadLength);
                    _DBFReader.skipBytes(RemainingOffset);
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

    @Override
    public void AddColumn(String ColumnName, int[] data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void AddColumn(String ColumnName, String[] data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void AddColumn(String ColumnName, double[] data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void AddColumn(String ColumnName, boolean[] data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
