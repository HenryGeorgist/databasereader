/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package databasewriter;

import databasereader.TypeEnum;

/**
 *
 * @author Will_and_Sara
 */
public abstract class AbstractWriter {
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
    public abstract void AddColumn(String ColumnName, int[] data);
    public abstract void AddColumn(String ColumnName, String[] data);
    public abstract void AddColumn(String ColumnName, double[] data);
    public abstract void AddColumn(String ColumnName, boolean[] data);
    public abstract void EditColumn(String ColumnName, int[] data);
    public abstract void EditColumn(String ColumnName, String[] data);
    public abstract void EditColumn(String ColumnName, double[] data);
    public abstract void EditColumn(String ColumnName, boolean[] data);
}
