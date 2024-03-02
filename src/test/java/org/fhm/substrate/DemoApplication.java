package org.fhm.substrate;

import org.fhm.substrate.annotation.ScanPackageConfig;
import org.fhm.substrate.manager.Bootstrap;
import org.fhm.substrate.service.DemoStarter;

/**
 * @Classname DemoApplication
 * @Description TODO
 * @Date 2024/2/16 21:56
 * @Author by 月光叶
 */
@ScanPackageConfig("org.**") // scan package name
public class DemoApplication {
    public static void main(String[] args) {
        Bootstrap.open(args, DemoStarter.class);
    }

}
