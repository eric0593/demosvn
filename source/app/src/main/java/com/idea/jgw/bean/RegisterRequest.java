package com.idea.jgw.bean;

import java.util.HashMap;

/**
 * Created by idea on 2018/6/4.
 */

public class RegisterRequest {
    private String account;
    private String device_id;
    private String device_type = "android";
    private String ip;
    private String passwd;
    private String verifycode;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getDevice_type() {
        return device_type;
    }

    public void setDevice_type(String device_type) {
        this.device_type = device_type;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getVerifycode() {
        return verifycode;
    }

    public void setVerifycode(String verifycode) {
        this.verifycode = verifycode;
    }

    public HashMap<String, String> getQueryMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("account", account);
        map.put("passwd", passwd);
        map.put("ip", ip);
        map.put("verifycode", verifycode);
        map.put("device_type", device_type);
        map.put("device_id", device_id);
        return map;
    }
}
