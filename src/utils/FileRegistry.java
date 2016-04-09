/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.ArrayList;
/**
 *
 * @author Will_and_Sara
 */
public class FileRegistry {
    private static ArrayList<DatabaseFile> _Files;
    static{
        _Files = new ArrayList<>();
    }
    static void AddFile(String NewFile, FileModeEnum Mode){
        // check if file is already in list?
        boolean addfile = true;
        for(DatabaseFile f: _Files){
            if(f.getFilePath().equals(NewFile)){
                if(f.getFileMode() == Mode){
                    //Same file same mode? what do you do?
                    addfile = false;
                }else{
                    //Same file different mode? what should one do?
                }
            }
        }
        if(!addfile){_Files.add(new DatabaseFile(NewFile,Mode));}
    }
    public boolean IsFileOpen(String FilePath){
        return _Files.stream().filter((f) -> (f.getFilePath().equals(FilePath))).anyMatch((f) -> (f.IsOpen()));
    }
}
