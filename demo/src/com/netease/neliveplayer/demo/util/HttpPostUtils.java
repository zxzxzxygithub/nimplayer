package com.netease.neliveplayer.demo.util;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 同时进行文件上传和表单数据的post类
 * Created by hzzhujinbo on 2016/11/28.
 */

public class HttpPostUtils {
    /**
     * 表单参数
     */
    private final static String LINEND = "\r\n";
    private final static String BOUNDARY = "---------------------------7df2ad12508cc"; // 数据分隔线(随机字符串)
    private final static String PREFIX = "--";
    private final static String MUTIPART_FORMDATA = "multipart/form-data";
    private final static String CHARSET = "utf-8";
    private final static String CONTENTTYPE = "application/octet-stream";

    private final static String TAG = "HttpPostData";
    /**
     * 表单数据
     */
    // 提交地址
    private String url;
    // 提交参数
    private Map<String, String> params = new HashMap<String, String>();
    // 提交的图片信息：文件名和地址
    private Map<String, String> filepaths = new HashMap<String, String>();

    /**
     * 1：上传图片 + 表单信息
     *
     * @param url        上传地址
     * @param cparams    上传表单数据：Map中的key 为 表单中的 name值 ； value 为 表单中的 value值 没有为null
     * @param cfilepaths 上传的文件地址； 单张图片上传，也需要使用 集合实现； 没有为null
     */
    public HttpPostUtils(String url, Map<String, String> cparams,
                         Map<String, String> cfilepaths) {
        // 构造函数 初始化 参数
        if (cfilepaths != null) {
            this.filepaths.putAll(cfilepaths);
        }

        this.url = url;

        if (cparams != null) {
            this.params.putAll(cparams);
        }
    }

    /**
     * 程序入口
     */
    public boolean connPost() {
        boolean isupdata = isUploadData();
        boolean isupimg = isUploadImage();
        if (!isupdata && !isupimg) {
            return false;
        } else {
            // 执行上传
            return postData(isupimg, isupdata);
        }
    }

    /**
     * 根据 params 的 数据 判断 是否 上传表单
     *
     * @return
     */
    private boolean isUploadData() {
        return params.size() > 0;
    }

    /**
     * 根据 filepaths的 数据 判断是否上传图片
     *
     * @return
     */
    private boolean isUploadImage() {
        return filepaths.size() > 0;
    }

    /**
     * HTTP上传 表单数据和图片表单数据
     *
     * @param isupimg  是否上传 表单数据
     * @param isupdata 是否上传 图片数据
     * @return
     */
    private boolean postData(boolean isupimg, boolean isupdata) {

        boolean flag = false;

//        lsLogUtil.instance().i(TAG, "upload sdk log");
        HttpURLConnection urlConn = null;
        BufferedReader br = null;
        try {
            // 新建url对象
            URL url = new URL(this.url);
            // 通过HttpURLConnection对象,向网络地址发送请求
            urlConn = (HttpURLConnection) url.openConnection();

            // 设置该连接允许读取
            urlConn.setDoOutput(true);
            // 设置该连接允许写入
            urlConn.setDoInput(true);
            // 设置不能适用缓存
            urlConn.setUseCaches(false);
            // 设置连接超时时间
            urlConn.setConnectTimeout(3000); // 设置连接超时时间
            // 设置读取时间
            urlConn.setReadTimeout(4000); // 读取超时
            // 设置连接方法post
            urlConn.setRequestMethod("POST");
            // 设置文件字符集
            urlConn.setRequestProperty("Charset", CHARSET);
            // 设置维持长连接
            urlConn.setRequestProperty("connection", "Keep-Alive");
            // 设置文件类型
            urlConn.setRequestProperty("Content-Type", MUTIPART_FORMDATA
                    + ";boundary=" + BOUNDARY);

            DataOutputStream dos = new DataOutputStream(
                    urlConn.getOutputStream());
            // 构建表单数据
            if (isupdata) {
                String entryText = GetParamsFormData();
                dos.write(entryText.getBytes());
            }
            // 构建图片表单数据
            if (isupimg) {

                Set<String> keys = filepaths.keySet();

                for (String key : keys) {
                    StringBuffer sb = new StringBuffer("");
                    String filePath = filepaths.get(key);
                    String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
//                    lsLogUtil.instance().i(TAG,"uploadLog fileName: " + fileName);
                    sb.append(PREFIX + BOUNDARY + LINEND)
                            .append("Content-Disposition: form-data;"
                                    + " name=\"" + key + "\";" + " filename=\""
                                    + fileName + "\"" + LINEND)
                            .append("Content-Type:" + CONTENTTYPE + ";"
                                    + "charset=" + CHARSET + LINEND)
                            .append(LINEND);

                    dos.write(sb.toString().getBytes());
                    FileInputStream fis = new FileInputStream(new File(filePath));
                    byte[] buffer = new byte[10000];
                    int len = 0;
                    while ((len = fis.read(buffer)) != -1) {
                        dos.write(buffer, 0, len);
                    }
                    dos.write(LINEND.getBytes());
                    fis.close();
                }
            }
            // 请求的结束标志
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
            dos.write(end_data);
            dos.flush();
            dos.close();
            // 发送请求数据结束

            // 接收返回信息
            int code = urlConn.getResponseCode();
            if (code != 200) {
                urlConn.disconnect();
            } else {
                br = new BufferedReader(new InputStreamReader(
                        urlConn.getInputStream()));
                String result = "";
                String line;
                while ((line = br.readLine()) != null) {
                    result += line;
                }
                flag = true;
//                lsLogUtil.instance().i(TAG, "uploadLog result: "+ result);

            }
        } catch (Exception e) {
            e.printStackTrace();
//            lsLogUtil.instance().e(TAG,"--------上传文件错误--------", e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (urlConn != null) {
                    urlConn.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return flag;

    }

    /**
     * 表单数据封装
     *
     * @return
     */
    private String GetParamsFormData() {
        StringBuilder buffer = new StringBuilder();

        if (isUploadData()) {
            Set<String> keys = params.keySet();
            for (String key : keys) {
                StringBuffer sb = new StringBuffer();
                sb.append(PREFIX + BOUNDARY + LINEND)
                        .append("Content-Disposition: form-data;" + " name=\""
                                + key + "\"" + LINEND).append(LINEND)
                        .append(params.get(key) + LINEND);
                buffer.append(sb.toString());
            }
        }
        return buffer.toString();

    }

}
