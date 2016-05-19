package com.wise.vub.tabletpointclient.utils;

import android.view.Menu;

import java.util.UUID;

/**
 * Created by Chaun on 4/23/2016.
 */
public class Constants {
    public static final UUID MY_UUID = UUID.fromString("04c6093b-0000-1000-8000-00805f9b34fb");
    public static final int TEST = -1;
    // The following constanta are used to operate with the powerpoint application.
    public static final int PenDown = 0x20;
    public static final int PenMove = 0x21;
    public static final int PenUp = 0x22;
    public static final int GOTO_PREV_SLIDE = 0x23;
    public static final int GOTO_NEXT_SLIDE = 0x24;
    public static final int GOTO_SLIDE = 0x25;
    public static final int SAVE_INK = 0x26;
    public static final int SET_COLOR = 0x28;
    // The following constants are strings for requesting data
    public static final String IMAGE_REQEUST = "image_request";
    public static final String SLIDE_PREVIEW_REQUEST = "slide_preview_request";
    public static final String NEW_SLIDE = "new_slide_request";
    public static final String FILE_RECEIVED = "file_received";
    public static final String INKXML_REQUEST = "ink_xml_request";
    public static final String PEN = "ppSlideShowPointerPen";
    public static final String ERASER = "ppSlideShowPointerEraser";
    // The following constants are used locally
    public static final int SEND_SLIDE_PREVIEW_REQUEST = 1;
    public static final int SEND_IMAGE_REQUEST = 2;
    public static final int SEND_INKXML_REQUEST = 3;
    public static final int SEND_GOTO_SLIDE = 4;
    public static final int SEND_SAVE_INK = 5;
    public static final int SEND_GOTO_PREV = 6;
    public static final int SEND_GOTO_NEXT = 7;
    public static final int SEND_NEW_SLIDE = 8;
    public static final int SEND_PEN_DOWN = 9;
    public static final int SEND_PEN_MOVE = 10;
    public static final int SEND_PEN_UP = 11;
    public static final int SEND_ERASER_DOWN = 12;
    public static final int SEND_SET_COLOR = 13;
    // The following constants comes from the client.
    public static final int HEADER_MSB = 0x10;
    public static final int HEADER_LSB = 0x55;
    public static final int CHUNK_SIZE = 4096;
    // The following constants are for calling UI operation
    public static final int UPDATE_IMAGE = 1;
    public static final int UPDATE_PREVIEW = 2;
    public static final int UPDATE_ANNOTATIONS = 3;
    public static final int GOTO_NEW_SLIDE  =4;
    // Three types of pen stroke here.
    public static final int PENCIL_MENU_ID = Menu.FIRST;
    public static final int PEN_MENU_ID = Menu.FIRST + 1;
    public static final int ERASER_MENU_ID = Menu.FIRST + 2;
    public static final int LINE_MENU_ID = Menu.FIRST + 3;
    public static final int SQUARE_MENU_ID = Menu.FIRST + 4;
    public static final int COLOR_PICKER = Menu.FIRST + 5;
    public static final int MAGIC_WAND_ID = Menu.FIRST + 6;
    public static final int STAMP_ID = Menu.FIRST + 7;
}
