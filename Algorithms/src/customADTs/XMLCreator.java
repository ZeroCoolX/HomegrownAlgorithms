/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package customADTs;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author dewit
 */
public class XMLCreator {
    
    private File level;

    public File getLevel() {
        return level;
    }

    public void setLevel(File level) {
        level = level;
    }
    
    public static void main(String args []){
    
    }
    
    public XMLCreator(StringBuilder xml, String outputFileName, File templateXML, int levelNum){
        level = populateLevelFile(xml, outputFileName, templateXML, levelNum);
    }
    
    //create a new directory using outputFilename (exempt of the extension obviously), create new file in that dir, and write fullxml to it
    private File populateLevelFile(StringBuilder xml, String outputFileName, File templateXML, int levelNum) {
        File newLevel = null;
        try {
            String fContent = parseAndInject(xml, outputFileName, templateXML);
            if (fContent.length() == 0 || fContent.equals("")) {
                throw new IllegalStateException();
            }
            File levelsDir = new File("/Users/dewit/Documents/shift_files/level_files");
            File newLevelDir = new File(levelsDir.getAbsolutePath() + "/" + outputFileName);
            if (!levelsDir.exists()) {//it should always exist..
                throw new IOException();//directory storing all levels does not exist?! O_O
            }
            if (!newLevelDir.exists()) {
                if (!newLevelDir.mkdir()) {
                    throw new IOException();//making a directory failed
                }
            }
            while((new File(newLevelDir.getAbsolutePath() + "/" + outputFileName + (""+levelNum+"") +".xml")).exists()){
                ++levelNum;
            }
            newLevel = new File(newLevelDir.getAbsolutePath() + "/" + outputFileName + (""+levelNum+"") +".xml");
            FileWriter fw = new FileWriter(newLevel.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(fContent);
            bw.close();
        } catch (IOException io) {
            io.printStackTrace();
        } catch (IllegalStateException is) {
            is.printStackTrace();
        } catch(Exception e){
            //something ELSE went wrong..
            e.printStackTrace();
        }
        return newLevel != null ? newLevel : null;
    }
    
    //read through the given xml template until we reach the spot to add out level then continue filling in the rest and return the entire file contents
    private String parseAndInject(StringBuilder xml, String outputFileName, File templateXML) {
        StringBuilder fullFile = null;
        try {
            fullFile = new StringBuilder();
            if (templateXML == null) {
                throw new NullPointerException();//given xml template doesn't exist... -___-
            }
            try (Scanner scanner = new Scanner(templateXML)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.contains("~")) {//DELIMETER! - inject xml
                        fullFile.append(xml.toString());
                    } else {//otherwise keep writing the file as normal
                        fullFile.append((line + "\n"));
                    }
                }
                scanner.close();
            } catch (IOException ioe) {
                throw ioe;
            }
        } catch (IOException io) {
            io.printStackTrace();
        } catch (NullPointerException np) {
            np.printStackTrace();
        } catch (Exception e) {
            //something ELSE went wrong..
            e.printStackTrace();
        }
        return fullFile != null ? fullFile.toString() : "";
    }
}
