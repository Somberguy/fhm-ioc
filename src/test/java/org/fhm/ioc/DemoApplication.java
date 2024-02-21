package org.fhm.ioc;

import org.fhm.ioc.annotation.ScanPackageConfig;
import org.fhm.ioc.manager.Bootstrap;
import org.fhm.ioc.service.DemoStarter;

/**
 * @Classname DemoApplication
 * @Description TODO
 * @Date 2024/2/16 21:56
 * @Author by 月光叶
 */
@ScanPackageConfig("org/fhm/ioc") // scan package name
public class DemoApplication {
    public static void main(String[] args) {
        Bootstrap.open(args, DemoStarter.class);
    }

}
