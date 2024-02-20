package org.fhm.ioc.service;

import org.fhm.ioc.annotation.DemoComponent;
import org.fhm.ioc.annotation.*;
import org.fhm.ioc.bean.IDemoTest;
import org.fhm.ioc.standard.IStarter;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Classname DemoStarter
 * @Description TODO
 * @Date 2024/2/16 21:57
 * @Created by 月光叶
 */
@Component // Inject into the IOC
public class DemoStarter implements IStarter {

    @Setup("Demo") // Load from the IOC
    private IDemoTest demo;

    @Setup("DemoAttach")
    private IDemoTest demoAttach;

    @Override
    public List<Class<? extends Annotation>> newManageMembers() {
        ArrayList<Class<? extends Annotation>> classes = new ArrayList<>();
        classes.add(DemoComponent.class);
        classes.add(DemoTestComponent.class);
        return classes; // Returns a collection of annotations for custom injection containers
    }

    @Override
    public void manageNotify(List<?> beans, Class<? extends Annotation> clazz) {
        if (DemoComponent.class.isAssignableFrom(clazz)){ // Determines whether the bean is marked by the DemoComponent annotation
            // Beans marked with DemoComponent annotations are treated independently
        }
    }

    @Override
    public void start(String[] args) throws Exception {
        demo.test(); // Runs test method of the Demo
        demoAttach.test(); // Runs test method of the DemoAttach
    }

    @Override
    public void close() throws Exception {
        // Runs before the IOC ends
    }

}
