package com.kubeman.common.logs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestThrowableLog {

    private static Logger logger = LogManager.getLogger("com.kubeman");

    public static void main(String[] arg) {
        try {
            Integer.parseInt("abc");
        } catch (Throwable t) {
//            t.printStackTrace();
            logger.error("error: ", t);
        }

        try {
            test();
        } catch (Exception e) {
            logger.error("error2: ", e);
        }

    }

    private static void test() throws Exception {
        try {
            Integer.parseInt("def");
        } catch (Throwable t) {
            throw new Exception("error2", t);
        }
    }

}
