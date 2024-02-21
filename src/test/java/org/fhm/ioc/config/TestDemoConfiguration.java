package org.fhm.ioc.config;

import org.fhm.ioc.annotation.Configuration;
import org.fhm.ioc.annotation.Value;

/**
 * @Classname TestDemoConfiguration
 * @Description TODO
 * @Date 2024/2/20-3:42 PM
 * @Author tanbo
 */
@Configuration("test.demo")
public class TestDemoConfiguration extends AbstractDemoConfiguration {


    @Value("desc")
    private String desc;

    @Value("lucky.number")
    private Integer luckyNumber;

    @Value("bean.name")
    private String beanName;


    public String getDesc() {
        return desc;
    }

    public Integer getLuckyNumber() {
        return luckyNumber;
    }

    public String getBeanName() {
        return beanName;
    }
}
