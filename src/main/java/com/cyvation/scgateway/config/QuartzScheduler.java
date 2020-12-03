//package com.cyvation.scgateway.config;
//
////import com.cyvation.scgateway.task.AutoDiscoverEreukaServiceTask;
//import org.quartz.*;
//import org.quartz.impl.StdSchedulerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.stereotype.Component;
//
///**
// * 任务管理
// */
//@Configuration
//@Component
//public class QuartzScheduler {
//
//    @Autowired
//    private Scheduler scheduler;
//
//    /**
//     * 初始注入scheduler
//     * @return
//     * @throws SchedulerException
//     */
//    @Bean
//    public Scheduler scheduler() throws SchedulerException{
//        SchedulerFactory schedulerFactoryBean = new StdSchedulerFactory();
//        return schedulerFactoryBean.getScheduler();
//    }
//
//    /**
//     * 配置AutoReceiptDocToTyywJob
//     * @throws SchedulerException
//     */
//    public void startAutoScanServiceJob() throws SchedulerException {
//        /*
//         *  此处可以先通过任务名查询数据库，如果数据库中存在该任务，则按照ScheduleRefreshDatabase类中的方法，更新任务的配置以及触发器
//         *  如果此时数据库中没有查询到该任务，则按照下面的步骤新建一个任务，并配置初始化的参数，并将配置存到数据库中
//         */
//        JobDetail jobDetail = JobBuilder.newJob(AutoDiscoverEreukaServiceTask.class) .withIdentity("scanEreukaServiceRegist", "autoRegist").build();
//        // 每3分钟执行一次
//        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("0 0/1 * * * ?");
//        CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity("scanEreukaServiceRegist", "autoRegist") .withSchedule(scheduleBuilder).build();
//        scheduler.scheduleJob(jobDetail,cronTrigger);
//        scheduler.start();
//    }
//
//}
