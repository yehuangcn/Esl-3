// DO NOT MODIFY - WILL BE OVERWRITTEN DURING THE BUILD PROCESS
package com.freeswitch.netty.util;

/**
 * Provides the version information of Netty.
 *
 * @apiviz.landmark
 */
@SuppressWarnings("all")
public final class Version {
    /**
     * The version identifier.
     */
    public static final String ID = "3.10.6.Final-5f56a03";

    private Version() {
    }

    /**
     * Prints out the version identifier to stdout.
     */
    public static void main(String[] args) {
        System.out.println(ID);
    }
}
