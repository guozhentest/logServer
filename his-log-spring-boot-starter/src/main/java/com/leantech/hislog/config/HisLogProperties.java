package com.leantech.hislog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "his.log")
public class HisLogProperties {

    private boolean enabled = true;
    private boolean compressBody = false;
    private String tablePrefix = "";

    // getter / setter...
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isCompressBody() { return compressBody; }
    public void setCompressBody(boolean compressBody) { this.compressBody = compressBody; }
    public String getTablePrefix() { return tablePrefix; }
    public void setTablePrefix(String tablePrefix) { this.tablePrefix = tablePrefix; }
}