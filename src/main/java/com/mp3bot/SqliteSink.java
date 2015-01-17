/*
 * Copyright (C) 2015 Ian Feng
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.mp3bot;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ian Feng
 */
public class SqliteSink implements Sink {

    private String dbName = "backup.db";
    private String tableName = "MP3BOT_MEDIAS";

    public boolean handle(List<MediaInfo> medias) {
        // Create a memory database
        Connection conn = null;
        final String CREATE_TABLE = "CREATE TABLE " + this.tableName + " ("
                + "   ID INTEGER PRIMARY KEY,"
                + "   TITLE     NVARCHAR(255)    NOT NULL,"
                + "   SRC       NVARCHAR(1024)   NOT NULL,"
                + "   URL       NVARCHAR(1024)   NOT NULL,"
                + "   DESC      NVARCHAR(1024),"
                + "   URL_DIGEST    NVARCHAR(60),"
                + "   DATA_DIGEST   NVARCHAR(60));";

        try {
            conn = DriverManager.getConnection("jdbc:sqlite:");
        } catch (SQLException ex) {
            Logger.getLogger(SqliteSink.class.getName()).log(Level.SEVERE, null, ex);
        }
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(CREATE_TABLE);
        } catch (SQLException ex) {
            Logger.getLogger(SqliteSink.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        try {
            // Do some updates
            stmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(SqliteSink.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            stmt = conn.prepareStatement(String.format("INSERT INTO %s (TITLE,SRC,URL,DESC,URL_DIGEST, DATA_DIGEST) VALUES(?,?,?,?,?,?);",
                    this.tableName));
        } catch (SQLException ex) {
            Logger.getLogger(SqliteSink.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (MediaInfo media : medias) {
            try {
                stmt.setString(1, media.getTitle());
                stmt.setString(2, media.getSrc());
                stmt.setString(3, media.getUrl());
                stmt.setString(4, media.getDescription());
                stmt.setString(5, media.getDigest());
                stmt.setString(6, "");
                stmt.executeUpdate();
                System.out.println("curMedia: " + media.getTitle());
            } catch (SQLException ex) {
                Logger.getLogger(SqliteSink.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }

        Statement st = null;
        try {
            // Dump the database contents to a file
            st = conn.createStatement();
            st.executeUpdate(String.format("backup to %s", this.dbName));
        } catch (SQLException ex) {
            Logger.getLogger(SqliteSink.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        //Restore the database from a backup file:
        // Create a memory database
        /*
         try {
         conn = DriverManager.getConnection("jdbc:sqlite:");
         } catch (SQLException ex) {
         Logger.getLogger(SqliteSink.class.getName()).log(Level.SEVERE, null, ex);
         }
         // Restore the database from a backup file
         //java.sql.Statement stat = conn.createStatement();
         //stat.executeUpdate("restore from backup.db");
         */
        return true;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
