/*
 * Created on 25-Mar-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.cmt;


public class Policy {
    public static final Policy NULL = new Policy("");
    private final String name;
    
    Policy(String name) {
        this.name = name;
    }
    public String toString() {
        return name;
    }
}