package com.merpyzf.httpcoreserver.constant;

import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * Created by wangke on 2017/12/1.
 */

public class Constant {

    public static final int PORT = 8888;
    public static  String IP = "";
    public static final String ENCODING = "UTF-8";
    public static Hashtable theMimeTypes = new Hashtable();
    static
    {
        StringTokenizer st = new StringTokenizer(
                        "css		text/css "+
                        "js			text/javascript "+
                        "htm		text/html "+
                        "html		text/html "+
                        "txt		text/plain "+
                        "asc		text/plain "+
                        "gif		image/gif "+
                        "jpg		image/jpeg "+
                        "jpeg		image/jpeg "+
                        "png		image/png "+
                        "mp3		audio/mpeg "+
                        "m3u		audio/mpeg-url " +
                        "pdf		application/pdf "+
                        "doc		application/msword "+
                        "ogg		application/x-ogg "+
                        "zip		application/octet-stream "+
                        "exe		application/octet-stream "+
                        "class		application/octet-stream " );
        while ( st.hasMoreTokens())
            theMimeTypes.put( st.nextToken(), st.nextToken());
    }


    public static String htmlStart = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\">\n" +
            "    <title>文件服务</title>\n" +
            "</head>\n" +
            "<body>";

    public static String htmlEnd = "</body>\n" +
            "</html>";


}
