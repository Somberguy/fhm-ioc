package org.fhm.substrate.service;

import org.fhm.substrate.annotation.Component;
import org.fhm.substrate.annotation.DemoComponent;
import org.fhm.substrate.annotation.DemoTestComponent;
import org.fhm.substrate.annotation.Setup;
import org.fhm.substrate.bean.DemoAttach;
import org.fhm.substrate.bean.IDemoTest;
import org.fhm.substrate.standard.IStarter;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Classname DemoStarter
 * @Description TODO
 * @Date 2024/2/16 21:57
 * @Created by 月光叶
 */
@Component // Inject into the IOC
public class DemoStarter implements IStarter {

    @Setup
    private DemoAttach attach;

    @Setup("Demo") // Load from the IOC
    private IDemoTest demo;

    @Setup("->test.demo.bean.name") // Specifies that the reference of the test.demo.bean.name
                                    // attribute in the configuration
                                    // file is the name of the loading object
    private IDemoTest demoAttach;

    @Setup // Mapping loads bean mechanisms
    private Map<String, IDemoTest> iDemoTestMap;

    @Override
    public List<Class<? extends Annotation>> newManageMembers() {
        ArrayList<Class<? extends Annotation>> classes = new ArrayList<>();
        classes.add(DemoComponent.class);
        classes.add(DemoTestComponent.class);
        return classes; // Returns a collection of annotations for custom injection containers
    }

    @Override
    public void manageNotify(List<?> beans, Class<? extends Annotation> clazz) {
        if (DemoComponent.class.isAssignableFrom(clazz)) { // Determines whether the bean is marked by the DemoComponent annotation
            // Beans marked with DemoComponent annotations are treated independently
        }
    }

    @Override
    public void start(String[] args) throws Exception {
        attach.test();
        demo.test(); // Runs test method of the Demo
        demoAttach.test(); // Runs test method of the DemoAttach
        iDemoTestMap.forEach((k, v) -> v.test());
    }

    @Override
    public void close() throws Exception {
        // Runs before the IOC ends
    }

}
