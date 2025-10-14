package ru.vyarus.dropwizard.guice.test.client.support;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

/**
 * @author Vyacheslav Rusakov
 * @since 13.10.2025
 */
@Path("/primitive")
public class PrimitivesResource {

    @Path("/byte")
    @GET
    public byte getByte() {
        return 1;
    }

    @Path("/bytes")
    @GET
    public byte[] getBytes() {
        return new byte[]{1};
    }

    @Path("/long")
    @GET
    public long getLong() {
        return 1;
    }

    @Path("/boolean")
    @GET
    public boolean getBoolean() {
        return false;
    }

    @Path("/short")
    @GET
    public short getShort() {
        return 1;
    }

    @Path("/float")
    @GET
    public float getFloat() {
        return 1;
    }

    @Path("/char")
    @GET
    public char getChar() {
        return 1;
    }

    @Path("/int")
    @GET
    public int getInt() {
        return 1;
    }

    @Path("/double")
    @GET
    public double getDouble() {
        return 1;
    }

}
