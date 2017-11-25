package com.android.jerry.ramayanabakery.utility;


public class RequestServer {
    private String server_ip = "sewabarang.ml";
    private String server_url = "/api/";
    private String img_url = "/assets/img/paket/";

    public String getServer_url(){
        return "http://"+this.server_ip+this.server_url;
    }
    public String getImg_url(){
        return "http://"+this.server_ip+this.img_url;
    }

}
