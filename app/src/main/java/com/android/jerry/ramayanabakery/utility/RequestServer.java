package com.android.jerry.ramayanabakery.utility;


public class RequestServer {
    private String server_ip = "192.168.88.2";
    private String server_url = "/jerry/";
    private String img_url = "/ramayana_bakery/foto/";

    public String getServer_url(){
        return "http://"+this.server_ip+this.server_url;
    }
    public String getImg_url(){
        return "http://"+this.server_ip+this.img_url;
    }

}
