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
public abstract class AbstractDatabase {
    private DatabaseFile _File;
    public boolean IsOpen(){return _File.IsOpen();}
    public abstract void Open();
    public abstract void Close();
    public abstract void Initialize();
    public String getFilePath(){return _File.getFilePath();}
}
