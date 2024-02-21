package org.fhm.ioc.standard;

import org.objectweb.asm.tree.ClassNode;

/**
 * @Classname ASMTransformerTemplate
 * @Description TODO ASM Class conversion template
 * @Date 2023/10/15 11:15
 * @Author by 月光叶
 */
public interface IASMTransformer {

    IASMTransformer init(byte[] bytes);

    /**
     * Bytecode operation
     */
    void create(ClassNode cn);


}
