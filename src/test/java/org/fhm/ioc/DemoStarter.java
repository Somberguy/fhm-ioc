package org.fhm.ioc;

import org.fhm.ioc.annotation.BeanEnable;
import org.fhm.ioc.annotation.BeanInitial;
import org.fhm.ioc.annotation.Component;
import org.fhm.ioc.annotation.Setup;
import org.fhm.ioc.standard.IStarter;

import java.lang.annotation.Annotation;
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
    private Demo demo;

    @Override
    public List<Class<? extends Annotation>> newManageMembers() {
        return Collections.singletonList(DemoComponent.class); // Returns a collection of annotations for custom injection containers
    }

    @Override
    public void manageNotify(List<?> beans, Class<? extends Annotation> clazz) {
        if (DemoComponent.class.isAssignableFrom(clazz)){ // Determines whether the bean is marked by the DemoComponent annotation
            // Beans marked with DemoComponent annotations are treated independently
        }
    }

    @Override
    public void start(String[] args) throws Exception {
        demo.test(); // Runs test method of the demo
    }

    @Override
    public void close() throws Exception {
        // Runs before the IOC ends
    }

}
