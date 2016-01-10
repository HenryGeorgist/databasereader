/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package databasereader;
/**
 * An abstract class that defines standard entry points for reading databases, this can be extended for any standard database.
 * @author Will_and_Sara influenced greatly by the readers built by Woody Fields in vb.net.
 */
public abstract class AbstractReader {
    protected boolean _Open = false;
    protected String _FilePath;
    protected String[] _ColumnNames;
    protected TypeEnum[] _ColumnTypes;
    protected int _NumberOfRows;
    protected int _NumberOfColumns;
    public String getFilePath(){return _FilePath;}
    public String[] getColumnNames(){return _ColumnNames;}
    public TypeEnum[] getColumnTypes(){return _ColumnTypes;}
    public int getNumberOfRows(){return _NumberOfRows;}
    public int getNumberOfColumns(){return _NumberOfColumns;}
    public boolean IsOpen(){return _Open;}
    public abstract void Open();
    public abstract void Close();
    public abstract void Initialize();
    public abstract Object getCell(int ColumnIndex, int RowIndex);
    public abstract Object getCell(String ColumnName, int RowIndex);
    public abstract Object[] getRow(int RowIndex);
    public abstract Object[] getRowForColumns(int RowIndex, int[] ColumnIndices);
    public abstract Object[] getColumn(String ColumnName);
    public abstract Object[] getColumn(int ColumnIndex);
    public abstract void AddColumn(String ColumnName, int[] data);
    public abstract void AddColumn(String ColumnName, String[] data);
    public abstract void AddColumn(String ColumnName, double[] data);
    public abstract void AddColumn(String ColumnName, boolean[] data);
}
