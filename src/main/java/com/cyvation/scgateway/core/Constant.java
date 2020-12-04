package com.cyvation.scgateway.core;

/**
 * 系统常量定义
 */
public class Constant {

    /**
     * 系统运行环境
     */
    public static class SysEnv{

        /**单机版*/
        public final static String STANDALONE = "standalone";

        /**连入nacos注册中心*/
        public final static String NACOS = "nacos";
    }
}
