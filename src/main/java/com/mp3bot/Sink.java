/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mp3bot;

import java.util.List;

/**
 *
 * @author yfeng
 */
public interface Sink {
    boolean handle(final List<MediaInfo> medias);
}
