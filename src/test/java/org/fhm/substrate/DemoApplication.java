package org.fhm.substrate;

import org.fhm.substrate.annotation.ScanPackageConfig;
import org.fhm.substrate.manager.Bootstrap;
import org.fhm.substrate.service.DemoStarter;

/**
 * @since 2024/2/16 21:56
 * @author Somberguy
 */
@ScanPackageConfig("org.**") // scan package name
public class DemoApplication {
    public static void main(String[] args) {
        Bootstrap.open(args, DemoStarter.class);
    }

}
