package ru.vyarus.dropwizard.guice.url.resource.support;

import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

/**
 * @author Vyacheslav Rusakov
 * @since 30.09.2025
 */
public class MappedBean {
    @PathParam("sm")
    private String sm;
    @QueryParam("q")
    private String q;
    @HeaderParam("HH")
    private String hh;
    @CookieParam("cc")
    private String cc;

    public String getSm() {
        return sm;
    }

    public void setSm(String sm) {
        this.sm = sm;
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public String getHh() {
        return hh;
    }

    public void setHh(String hh) {
        this.hh = hh;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }
}
