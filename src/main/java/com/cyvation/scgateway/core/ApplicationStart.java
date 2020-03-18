package com.cyvation.scgateway.core;

import com.cyvation.scgateway.config.QuartzScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 应用初始化
 */
@Component
public class ApplicationStart{

    private static Logger LOGGER = LoggerFactory.getLogger(ApplicationStart.class);

    @Autowired
    private QuartzScheduler quartzScheduler;

    public void onApplicationEvent() {
        try {
            //启动任务
            this.quartzScheduler.startAutoScanServiceJob();
        }catch (Exception e){
            LOGGER.error("启动任务失败",e);
        }
    }
}
