package com.zybooks.goodlookin;

public class ResultValue {
    private String name = "no value";
    private String url = "no value";
    private String snippet = "no value";

    public ResultValue(String nameVal, String urlVal, String snippetVal){
        name = nameVal;
        url = urlVal;
        snippet = snippetVal;
    }

    public String getName(){
        return name;
    }
    public String getUrl(){
        return url;
    }
    public String getSnippet(){
        return snippet;
    }
}
