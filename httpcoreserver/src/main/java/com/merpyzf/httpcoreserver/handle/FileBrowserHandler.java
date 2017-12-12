package com.merpyzf.httpcoreserver.handle;

import com.merpyzf.httpcoreserver.constant.Constant;
import com.merpyzf.httpcoreserver.util.LogUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Created by wangke on 2017/12/1.
 * 处理文件浏览
 */

public class FileBrowserHandler implements HttpRequestHandler {

    private static final String TAG = FileBrowserHandler.class.getName();
    private HttpEntity entity = null;
    private String mContentType = "text/html;charset=" + Constant.ENCODING;

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {


        String uri = URLDecoder.decode(request.getRequestLine().getUri(), Constant.ENCODING);
        final File targetFile = new File(uri);

        if (targetFile.exists()) {
            if (targetFile.canRead()) {
                if (targetFile.isDirectory()) {
                    StringBuilder htmlContent = new StringBuilder();
                    // 点击的是一个目录
                    File[] listFiles = targetFile.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            // 过滤隐藏文件
                            return !file.isHidden() && !file.getName().startsWith(".");
                        }
                    });
                    htmlContent.append(Constant.htmlStart);
                    htmlContent.append("<h1>" + targetFile.getCanonicalPath() + "</h1>\n<hr/>");
                    if (listFiles.length > 0) {

                        for (File file : listFiles) {
                            //输出字符的编码是utf-8
                            LogUtil.i(TAG, file.getName());
                            htmlContent.append("<a href=\"http://" + Constant.IP + ":" + Constant.PORT + encodeUri(file.getCanonicalPath()) + "\">" + getTitleContent(file) + "</a>");
                            htmlContent.append("<br/>");
                        }

                    } else {

                        htmlContent.append("<h1>选择的目录为空！</h1>");
                        htmlContent.append("<br/>");
                    }

                    htmlContent.append(Constant.htmlEnd);
                    entity = new StringEntity(htmlContent.toString(), Constant.ENCODING);
                    response.setHeader("Content-Type", mContentType);
                    response.setEntity(entity);

                } else {

                    // 点击的是一个文件，实现下载
                    String fileName = targetFile.getName();
                    int dotIndex = fileName.lastIndexOf(".");
                    //获取点击文件的后缀名
                    String suffixName = fileName.substring(dotIndex + 1).toLowerCase(Locale.ENGLISH);
                    String mimeType = (String) Constant.theMimeTypes.get(suffixName);
                    response.setHeader("Content-type", mimeType);
                    response.setHeader("Content-Description", "File Transfer");
                    response.setHeader("Content-Disposition", "filename=" + encodeName(targetFile));
                    response.setHeader("Accept-Ranges", "bytes");
                    response.setHeader("Accept-Length", String.valueOf(targetFile.length()));
                    response.setHeader("Content-Disposition", "attachment; filename=" + encodeName(targetFile));

                    entity = new EntityTemplate(new ContentProducer() {
                        @Override
                        public void writeTo(OutputStream outstream) throws IOException {

                            write(targetFile, outstream);

                        }
                    });


                }


            } else {

                StringBuilder sb = new StringBuilder();
                sb.append(Constant.htmlStart);
                sb.append("<p>本文件无操作权限</p>");
                sb.append(Constant.htmlEnd);
                entity = new StringEntity(sb.toString(), Constant.ENCODING);
                //没有读取的权限
                response.setHeader("Content-Type", mContentType);
            }

        } else {

            // 文件不存在
            StringBuilder sb = new StringBuilder();
            sb.append(Constant.htmlStart);
            sb.append("<p>所选文件不存在!</p>");
            sb.append(Constant.htmlEnd);
            entity = new StringEntity(sb.toString(), Constant.ENCODING);
            //没有读取的权限
            response.setHeader("Content-Type", mContentType);


        }


        response.setEntity(entity);

    }

    /**
     * a标签显示的标题
     *
     * @param file
     * @return
     */
    private String getTitleContent(File file) {

        String title;

        if (file.isDirectory()) {

            title = "/" + file.getName();

        } else {

            // 是文件类型
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            String size = decimalFormat.format(file.length() / 1024f / 1024f);
            title = file.getName() + "(" + size + "mb)";


        }

        return title;
    }

    /**
     * 将文件以流的形式写出
     *
     * @param targetFile
     * @param outstream
     */
    private void write(File targetFile, OutputStream outstream) {

        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(targetFile));

            byte[] buffer = new byte[2048];

            int len = -1;

            while ((len = bis.read(buffer, 0, buffer.length)) != -1) {


                outstream.write(buffer, 0, len);


            }


            outstream.flush();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bis.close();
                outstream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * 编码将uri编码成base64
     *
     * @param uri
     * @return
     */
    private String encodeUri(String uri) {

        StringBuilder newUri = new StringBuilder();

        StringTokenizer st = new StringTokenizer(uri, "/", true);


        while (st.hasMoreElements()) {


            String token = st.nextToken();
            try {

                if (token.equals("/")) {

                    newUri.append("/");

                } else if (token.equals(" ")) {

                    newUri.append("%20");
                } else {


                    newUri.append(URLEncoder.encode(token, Constant.ENCODING));

                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }

        return newUri.toString();
    }

    /**
     * 将文件名编码为base64
     *
     * @param file
     * @return
     */
    private String encodeName(File file) {

        String name = null;
        try {
            name = URLEncoder.encode(file.getName(), Constant.ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return name;

    }


}
