package ru.vyarus.dropwizard.guice.url.resource.support;

import javax.ws.rs.CookieParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

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
    @MatrixParam("mm")
    private String mm;

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

    public String getMm() {
        return mm;
    }

    public void setMm(String mm) {
        this.mm = mm;
    }
}
