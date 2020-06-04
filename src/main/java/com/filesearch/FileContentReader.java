package com.filesearch;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class FileContentReader {

    static HashMap<String,Boolean> txtFileExt,zipFileTypes,tikaFileTypes;
    public static String getExtension(String f){
        return f.substring(f.lastIndexOf(".")+1).toLowerCase();
    }
    public static void loadTextTypes(){

    }

    public static  void readTikaFiles(File file,FileReadListener listener) throws IOException, TikaException, SAXException {
        Parser parser = new AutoDetectParser();
        ContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();
        parser.parse(new FileInputStream(file), handler, metadata, context);
        listener.fileRead(handler.toString(),file.getAbsolutePath());
    }
    public static  boolean isTextFile(String ext){

        return true;
    }

    public static void readZipFile(File file,FileReadListener listener)   {

    }

    public static void read(File file,FileReadListener listener)  {
        try {
            String ext = getExtension(file.getName());
            if(isTextFile(ext)) {
                listener.fileRead(new String(Files.readAllBytes(file.toPath())),file.getAbsolutePath());
            }else if(zipFileTypes.containsKey(ext)){
                readZipFile(file,listener);
            }else if(tikaFileTypes.containsKey(ext)){

            }else{
                System.out.println("not text file"+file);
            }
        } catch (IOException e) {
            ;
        }

    }
}
