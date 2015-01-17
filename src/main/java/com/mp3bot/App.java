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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse media info list from network.
 *
 */
public class App {

    private List<MediaInfo> medias = new ArrayList<MediaInfo>();
    private Set<String> mediaSet = new HashSet<String>();
    private CharBuffer contentBuf = CharBuffer.allocate(200 * 1024);
    private Sink sinkObject = null;

    public String readFromURL(final String url) {
        String ret = "";
        URL my = null;
        contentBuf.clear();
        try {
            my = new URL(url);
        } catch (MalformedURLException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            return ret;
        }
        InputStreamReader reader = null;
        BufferedReader breader = null;
        try {
            reader = new InputStreamReader(my.openStream());
            breader = new BufferedReader(reader);
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            return ret;
        }

        try {
            if (reader != null && breader != null) {
                int rlen = 0;
                while ((rlen = breader.read(contentBuf)) > 0) {
                    //System.out.println("read len= " + rlen + " bytes");
                }
                reader.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            return ret;
        }
        reader = null;
        my = null;

        contentBuf.flip();
        ret = contentBuf.toString().replaceAll("[\n\r]", "");
        return ret;
    }

    public void parseAndrewGlover(final String url, String sr) {
        int pos = 0;
        int endPos = 0;
        int startIndex = 0;
        final String END_TAG = "Listen now";
        MediaInfo media = null;

        //Find all titles.
        pos = sr.indexOf("Table of contents", 100);
        if (pos < 0) {
            return;
        }
        int index = 0;
        int limit = sr.indexOf("href=\"#icomments\"", pos);
        final String MARK_START = "href=\"#";
        List<String> ids = new ArrayList<String>(20);
        while ((index = sr.indexOf(MARK_START, pos)) > 0 && index < limit) {
            pos = index + MARK_START.length();
            int rpos = sr.indexOf("\"", pos);
            if (rpos > 0) {
                ids.add(sr.substring(pos, rpos));
                pos = rpos;
            }
        }

        //
        startIndex = pos;
        for (String id : ids) {
            media = null;
            String startTag = String.format("id=\"%s\"", id);
            pos = sr.indexOf(startTag, startIndex);
            if (pos > 0) {
                media = new MediaInfo();
                media.setSrc(url);

                //Find the start point.
                pos += startTag.length() + 1;
                startIndex = pos;

                //Find the title from back
                int td = sr.indexOf("</h2>", pos);
                if (td > 0) {
                    String s = sr.substring(pos, td);
                    media.setTitle(s);
                    pos = td;
                }

                //Get the string.
                final String HREF = "href=\"";
                final String HREF_END = "\">";
                int hs = sr.indexOf(HREF, pos);
                if (hs > pos) {
                    hs += HREF.length();
                    int he = sr.indexOf(HREF_END, hs);
                    if (he > hs) {
                        //Got it.
                        String mp3 = sr.substring(hs, he);
                        media.setUrl(mp3);

                        if (false == this.mediaSet.contains(media.getDigest())) {
                            this.mediaSet.add(media.getDigest());
                            this.medias.add(media);
                        }
                    } else {
                        break;
                    }
                } else {
                    break;
                }
                startIndex = hs;
            } else {
                break;
            }
        }
    }

    public void scanAndrewGlover() {
        List<String> urls = new ArrayList<String>(10);
        Collections.addAll(urls,
                "http://www.ibm.com/developerworks/library/j-gloverpodcast/", //Season1
                "http://www.ibm.com/developerworks/java/library/j-gloverpodcast2/index.html", //Season2
                "http://www.ibm.com/developerworks/java/library/j-gloverpodcast3/index.html", //Season3
                "http://www.ibm.com/developerworks/java/library/j-gloverpodcast4/index.html", //Season4
                "http://www.ibm.com/developerworks/java/library/j-gloverpodcast5/index.html" //Season5
        );

        for (String m : urls) {
            System.out.println("[INFO] Try to read from [" + m + "]");
            String str = this.readFromURL(m);
            parseAndrewGlover(m, str);
        }
    }

    public void parseChangeLog(MediaInfo media) {
        int pos = 0;
        int endPos = 0;
        int startIndex = 0;
        final String END_TAG = "</audio>";

        this.contentBuf.clear();
        String sr = this.readFromURL(media.getSrc());

        pos = sr.indexOf("<audio");
        if (pos <= 0) {
            return;
        }
        endPos = sr.indexOf(END_TAG, pos);
        if (endPos <= pos) {
            return;
        }

        String str = sr.substring(pos, endPos + END_TAG.length());

        //<audio src=\"(.*?)\" (.*?)></audio>
        String t = ("<audio src=\"(.*?)\" (.*?)></audio>");
        Pattern p = Pattern.compile(t);
        Matcher m = p.matcher(str);
        boolean found = false;
        while (m.find()) {
            String mp3 = m.group(1);
            media.setUrl(mp3);

            if (false == this.mediaSet.contains(media.getDigest())) {
                this.mediaSet.add(media.getDigest());
                this.medias.add(media);
            }
            found = true;
        }
        if (!found) {
            System.out.println("Failed to match\n");
        }
    }

    public void scanChangeLog() {
        final String url = "http://thechangelog.com/podcast/";
        this.contentBuf.clear();

        String sr = readFromURL(url);

        final String flag = "class=\"post-content\"";
        int pos = sr.indexOf(flag);
        if (pos <= 0) {
            return;
        }

        pos = sr.indexOf("<ul>", pos);
        if (pos <= 0) {
            return;
        }

        pos += 4;
        int epos = sr.indexOf("</ul>", pos);
        if (epos <= pos) {
            return;
        }
        String str = sr.substring(pos, epos);
        //Pattern p = Pattern.compile("\\s(<li>\\s<a\\shref\\s=\"([^\"]+)\"\\stitle=\"([^\"]+)\"\\s>\\s([^<]+)}\\s</a>\\s</li>)\\s");
        Pattern p = Pattern.compile("\\s(<li>(.*?)</li>)\\s");
        String t = ("<a href[\\s]+=\"(.*?)\" title=\"(.*?)\">(.*?)</a>"); //("<a href =\"(.*?)\" title=\"(.*?)\">(.*?)</a>");
        Pattern curt = Pattern.compile(t);

        Matcher m = p.matcher(str);

        boolean found = false;
        while (m.find()) {
            String cur = m.group(2).trim();
            Matcher curm = curt.matcher(cur);

            while (curm.find()) {
                MediaInfo info = new MediaInfo();
                info.setSrc(curm.group(1));
                info.setTitle(curm.group(2));
                this.parseChangeLog(info);
            }
            found = true;
        }
        if (!found) {
            System.out.println("Failed to match");
        }
    }

    public List<MediaInfo> getMedias() {
        return medias;
    }

    public Sink getSinkObject() {
        return sinkObject;
    }

    public void setSinkObject(Sink sinkObject) {
        this.sinkObject = sinkObject;
    }

    public boolean sink() {
        if (this.sinkObject != null) {
            return this.sinkObject.handle(this.medias);
        } else {
            return false;
        }
    }
    
    public static void main(String[] args) {
        int i = 0;
        App app = new App();
        app.scanAndrewGlover();
        app.scanChangeLog();
        for (MediaInfo media : app.getMedias()) {
            System.out.println(String.format("%d: [%s] -> [%s]", i, media.getTitle(), media.getUrl()));
            ++i;
        }
        //app.setSinkObject(new JsonSink());
        SqliteSink obj = new SqliteSink();
        obj.setDbName("test.dat");
        app.setSinkObject(obj);
        app.sink();
    }
}
