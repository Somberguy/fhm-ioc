package org.fhm.ioc.ability;

import org.fhm.ioc.asm.TransformerNode;

/**
 * @Classname Transformer
 * @Description TODO ASM class conversion function
 * @Date 2023/10/15 11:26
 * @Created by 月光叶
 */
@FunctionalInterface
public interface ITransformer {

    /**
     * Conversion
     *
     * @param transformerNode ASM conversion node
     */
    void transform(TransformerNode transformerNode);


}
