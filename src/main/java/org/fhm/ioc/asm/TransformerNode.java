package org.fhm.ioc.asm;

import org.fhm.ioc.ability.ITransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

import java.util.Objects;

/**
 * @Classname TransformerNode
 * @Description TODO ASM transformer node
 * @Date 2023/10/15 10:56
 * @Created by 月光叶
 */
public class TransformerNode extends ClassNode {

    private final ITransformer tf;

    public TransformerNode(int api, ClassVisitor cv, ITransformer tf) {
        super(api);
        this.cv = cv;
        this.tf = tf;
    }

    @Override
    public void visitEnd() {
        try {
            tf.transform(this);
            if (Objects.nonNull(cv)) {
                super.accept(cv);
                super.visitEnd();
            }
        } catch (Exception ignore) {
        }
    }
}
