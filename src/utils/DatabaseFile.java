/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

/**
 *
 * @author Will_and_Sara
 */
public class DatabaseFile {
    private FileModeEnum _Mode;
    private boolean _IsOpen;
    private String _Path;
    public DatabaseFile(String FilePath, FileModeEnum Mode){
        _Mode = Mode;
        _Path = FilePath;
    }
    public void Open(){_IsOpen = true;}
    public void Close(){_IsOpen = false;}
    public FileModeEnum getFileMode(){return _Mode;}
    public boolean IsOpen(){return _IsOpen;}
    public String getFilePath(){return _Path;}
}
