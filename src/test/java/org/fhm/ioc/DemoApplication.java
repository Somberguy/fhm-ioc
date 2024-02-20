package org.fhm.ioc;

import org.fhm.ioc.annotation.ScanPackageConfig;
import org.fhm.ioc.manager.Bootstrap;
import org.fhm.ioc.service.DemoStarter;

/**
 * @Classname DemoApplication
 * @Description TODO VM parameter : -Dfhm.ioc.config.file.path=./src/test/java/resource/
 * @Date 2024/2/16 21:56
 * @Created by 月光叶
 */
@ScanPackageConfig("org/fhm/ioc") // scan package name
public class DemoApplication {
    public static void main(String[] args) {
        Bootstrap.open(args, DemoStarter.class);
    }

}
