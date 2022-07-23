package com.pig.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pig.dashboard.enums.ResponseCodeEnum;
import com.pig.dashboard.exception.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class AlertUtils {
    private static Logger logger = LoggerFactory.getLogger(AlertUtils.class);

    public static boolean dingAlert(String dingRobotUrl, String phoneNumbers, String content) {
        try {
            doDingAlert(dingRobotUrl, phoneNumbers, content);
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    public static void doDingAlert(String dingRobotUrl, String phoneNumbers, String content) {
        JSONObject params = buildDingRequestParams(phoneNumbers, content);
        logger.info("ding alert request:{}", params.toJSONString());
        JSONObject repsonse = doPost(dingRobotUrl, params);
        logger.info("ding alert response:{}", repsonse.toJSONString());
        Integer errcode = repsonse.getInteger("errcode");
        String errmsg = repsonse.getString("errmsg");
        if (errcode != null && errcode.intValue() != 0) {
            logger.error("errcode:{} errmsg:{}", errcode, errmsg);
            throw new TransactionException(ResponseCodeEnum.ALERT_DING_ERROR.getCode(), errmsg);
        }
    }


    private static JSONObject buildDingRequestParams(String phoneNumbers, String content) {
        JSONObject params = new JSONObject();

        params.put("msgtype", "text");

        JSONObject textJSON = new JSONObject();
        textJSON.put("content", content);
        params.put("text", textJSON);

        JSONObject atJSON = new JSONObject();
        atJSON.put("atMobiles", phoneNumbers.split(","));
        atJSON.put("isAtAll", false);
        params.put("at", atJSON);

        return params;

    }

    private static JSONObject doPost(String url, JSONObject params) {
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try {
            URLConnection conn = new URL(url).openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            out.print(params.toJSONString());
            out.flush();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            logger.error("doPost url:{} error", url);
            throw new TransactionException(ResponseCodeEnum.ALERT_DING_ERROR);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                logger.error("doPost url:{} connect close error", url);
            }
        }
        return JSON.parseObject(result.toString());

    }

    public static void main(String[] args) {
        dingAlert(
                "",
                "",
                ""
        );
    }

}
