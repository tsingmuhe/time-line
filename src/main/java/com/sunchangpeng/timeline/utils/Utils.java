package com.sunchangpeng.timeline.utils;

import com.sunchangpeng.timeline.TimelineException;
import com.sunchangpeng.timeline.TimelineExceptionType;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Utils {
    public static String getLocalIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new TimelineException(TimelineExceptionType.ABORT, "Can not get local machine ip.");
        }
    }

    public static String getProcessID() {
        String value = ManagementFactory.getRuntimeMXBean().getName();
        return value.substring(0, value.indexOf("@"));
    }
}
