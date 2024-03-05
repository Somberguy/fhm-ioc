package org.fhm.substrate.standard;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @since 2023/10/25 18:11
 * @author Somberguy
 */
public interface IStarter {

    /**
     * used before bean production is complete
     * @return annotation tagging classes that need to be managed by the IOC
     */
    List<Class<? extends Annotation>> newManageMembers();

    /**
     * used after bean production is complete, notify which beans are being managed, and their management annotation
     *
     * @param beans which beans are being managed
     * @param clazz management annotation
     */
    void manageNotify(List<?> beans, Class<? extends Annotation> clazz);

    /**
     * enable method
     * @param args enable parameters
     * @throws Exception raise an exception for Substrate's processing.
     */
    void start(String[] args) throws Exception;

    void close() throws Exception;

}
