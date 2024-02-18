package org.fhm.ioc;

import org.fhm.ioc.standard.IStarter;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @Classname DemoStarter
 * @Description TODO
 * @Date 2024/2/16 21:57
 * @Created by 月光叶
 */
public class DemoStarter implements IStarter {
    @Override
    public List<Class<? extends Annotation>> newManageMembers() {
        return null;
    }

    @Override
    public void manageNotify(List<?> beans, Class<? extends Annotation> clazz) {

    }

    @Override
    public void start(String[] args) throws Exception {

    }

    @Override
    public void close() throws Exception {

    }
}
