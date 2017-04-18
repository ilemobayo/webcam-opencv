package com.face;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Owner
 */
public class NewClass {
    
    public static void hello() throws MalformedURLException{
        try {
            URL url = new URL("http://localhost:9001/in/q?name=bayo");
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            StringBuffer stringBuffer;
            try ( //http.setDoInput(true);
                BufferedReader buffer = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                stringBuffer = new StringBuffer();
                String line;
                while((line = buffer.readLine()) != null) {
                    stringBuffer.append(line);
                }
            }
            System.out.println(stringBuffer.toString());
            //OutputStream os = conn.getOutputStream();    
        } catch (IOException ex) {
            Logger.getLogger(NewClass.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args){
        try {
            hello();
        } catch (MalformedURLException ex) {
            Logger.getLogger(NewClass.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
