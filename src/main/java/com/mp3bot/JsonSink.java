/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mp3bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yfeng
 */
public class JsonSink implements Sink {

    public boolean handle(List<MediaInfo> medias) {
        ObjectMapper om = new ObjectMapper();
        try {
            om.writeValue(new File("result.json"), medias);
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        om = null;        
        return true;
    }
    
}
