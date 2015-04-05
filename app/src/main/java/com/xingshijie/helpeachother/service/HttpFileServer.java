package com.xingshijie.helpeachother.service;

import android.os.Environment;
import android.util.Log;



import org.apache.myhttp.ConnectionClosedException;
import org.apache.myhttp.ExceptionLogger;
import org.apache.myhttp.HttpConnection;
import org.apache.myhttp.HttpEntity;
import org.apache.myhttp.HttpEntityEnclosingRequest;
import org.apache.myhttp.HttpException;
import org.apache.myhttp.HttpRequest;
import org.apache.myhttp.HttpResponse;
import org.apache.myhttp.HttpStatus;
import org.apache.myhttp.MethodNotSupportedException;
import org.apache.myhttp.config.SocketConfig;
import org.apache.myhttp.entity.ContentType;
import org.apache.myhttp.entity.FileEntity;
import org.apache.myhttp.entity.StringEntity;
import org.apache.myhttp.impl.bootstrap.HttpServer;
import org.apache.myhttp.impl.bootstrap.ServerBootstrap;
import org.apache.myhttp.protocol.HttpContext;
import org.apache.myhttp.protocol.HttpCoreContext;
import org.apache.myhttp.protocol.HttpRequestHandler;
import org.apache.myhttp.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.concurrent.TimeUnit;




/**
 *使用apache httpServer实现文件服务器功能
 */
public class HttpFileServer {

    //如果端口被占用，不会报错，但手机打不开
    int port=7373;
    public static String filePath="";


    public void startFileServer() throws Exception {
//        if (args.length < 1) {
//            System.err.println("Please specify document root directory");
//            System.exit(1);
//        }
//        // Document root directory
//        String docRoot = args[0];
//        int port = 8080;
//        if (args.length >= 2) {
//            port = Integer.parseInt(args[1]);
//        }

        /*SSLContext sslcontext = null;
        if (port == 8443) {
            // Initialize SSL context
            URL url = HttpFileServer.class.getResource("/my.keystore");
            if (url == null) {
                System.out.println("Keystore not found");
                System.exit(1);
            }
            sslcontext = SSLContexts.custom()
                    .loadKeyMaterial(url, "secret".toCharArray(), "secret".toCharArray())
                    .build();
        }*/

        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(15000)
                .setTcpNoDelay(true)
                .build();

        final HttpServer server = ServerBootstrap.bootstrap()
                .setListenerPort(port)
                .setServerInfo("Test/1.1")
                .setSocketConfig(socketConfig)
//                .setSslContext(sslcontext)
                .setExceptionLogger(new StdErrorExceptionLogger())
                .registerHandler("*", new HttpFileHandler())
                .create();
        Log.e("","本机端口"+server.toString()+"开启下载服务");

        server.start();
        server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            @Override
//            public void run() {
//                server.shutdown(5, TimeUnit.SECONDS);
//            }
//        });
    }

    static class StdErrorExceptionLogger implements ExceptionLogger {

        @Override
        public void log(final Exception ex) {
            if (ex instanceof SocketTimeoutException) {
                Log.e("", "Connection timed out");
            } else if (ex instanceof ConnectionClosedException) {
                Log.e("", ex.getMessage());
            } else {
                ex.printStackTrace();
            }
        }

    }

    static class HttpFileHandler implements HttpRequestHandler {



        public HttpFileHandler() {
            super();

        }

        public void handle(
                final HttpRequest request,
                final HttpResponse response,
                final HttpContext context) throws HttpException, IOException {
            Log.e("","正在处理下载服务");
            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ROOT);
            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
                throw new MethodNotSupportedException(method + " method not supported");
            }
            String target = request.getRequestLine().getUri();

            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                byte[] entityContent = EntityUtils.toByteArray(entity);
                Log.e("", "Incoming entity content (bytes): " + entityContent.length);
            }

            //final File file=new File(Environment.getExternalStorageDirectory().getPath()+"/fqrouter-latest.apk");
            final File file=new File(filePath);
            if (!file.exists()) {

                response.setStatusCode(HttpStatus.SC_NOT_FOUND);
                StringEntity entity = new StringEntity(
                        "<html><body><h1>File" + file.getPath() +
                                " not found</h1></body></html>",
                        "UTF-8");
//                        ContentType.create("text/html", "UTF-8"));
                response.setEntity(entity);
                Log.e("", "File " + file.getPath() + " not found");

            } else if (!file.canRead() || file.isDirectory()) {

                response.setStatusCode(HttpStatus.SC_FORBIDDEN);
                StringEntity entity = new StringEntity(
                        "<html><body><h1>Access denied</h1></body></html>",
                        "UTF-8");
//                        ContentType.create("text/html", "UTF-8"));
                response.setEntity(entity);
                Log.e("", "Cannot read file " + file.getPath());

            } else {
                HttpCoreContext coreContext = HttpCoreContext.adapt(context);
                HttpConnection conn = coreContext.getConnection(HttpConnection.class);
                response.setStatusCode(HttpStatus.SC_OK);
                FileEntity body = new FileEntity(file, ContentType.create("application/octet-stream", (Charset) null));

                //添加文件名头部,文件名头部会覆盖content。type
                response.addHeader("Content-Disposition","attachment; filename=\""+file.getName()+"\"");

                response.setEntity(body);
                Log.e("", conn + ": serving file " + file.getPath());
            }
        }

    }
}
