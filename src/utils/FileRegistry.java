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
    public boolean IsFileOpen(String FilePath){
        return _Files.stream().filter((f) -> (f.getFilePath().equals(FilePath))).anyMatch((f) -> (f.IsOpen()));
    }
}
