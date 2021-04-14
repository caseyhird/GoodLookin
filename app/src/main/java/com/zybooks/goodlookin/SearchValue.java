package com.zybooks.goodlookin;

public class SearchValue {
    private String name;
    private String url;
    private String snippet;

    public SearchValue(String nameVal, String urlVal, String snippetVal){
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
