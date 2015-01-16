/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mp3bot;

import java.beans.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yfeng
 */
public class SqliteSink implements Sink {

    private String dbName = "backup.db";
    
    public boolean handle(List<MediaInfo> medias) {
        // Create a memory database
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:");
        } catch (SQLException ex) {
            Logger.getLogger(SqliteSink.class.getName()).log(Level.SEVERE, null, ex);
        }
        java.sql.Statement stmt = null;
        try {
            stmt = conn.createStatement();
        } catch (SQLException ex) {
            Logger.getLogger(SqliteSink.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            // Do some updates
            stmt.executeUpdate("create table sample(id, name)");
        } catch (SQLException ex) {
            Logger.getLogger(SqliteSink.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            stmt.executeUpdate("insert into sample values(1, \"leo\")");
        } catch (SQLException ex) {
            Logger.getLogger(SqliteSink.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            stmt.executeUpdate("insert into sample values(2, \"yui\")");
        } catch (SQLException ex) {
            Logger.getLogger(SqliteSink.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            // Dump the database contents to a file
            stmt.executeUpdate("backup to backup.db");
        } catch (SQLException ex) {
            Logger.getLogger(SqliteSink.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Restore the database from a backup file:
        // Create a memory database
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:");
        } catch (SQLException ex) {
            Logger.getLogger(SqliteSink.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Restore the database from a backup file
        //java.sql.Statement stat = conn.createStatement();
        //stat.executeUpdate("restore from backup.db");
        return true;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

}
