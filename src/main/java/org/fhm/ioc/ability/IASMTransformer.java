package org.fhm.ioc.ability;

import org.objectweb.asm.tree.ClassNode;

/**
 * @Classname ASMTransformerTemplate
 * @Description TODO ASM Class conversion template
 * @Date 2023/10/15 11:15
 * @Created by 月光叶
 */
public interface IASMTransformer {

    IASMTransformer init(byte[] bytes);

    /**
     * Bytecode operation
     */
    void create(ClassNode cn);


}
