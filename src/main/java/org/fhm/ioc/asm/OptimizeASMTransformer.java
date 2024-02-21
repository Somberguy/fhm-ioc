package org.fhm.ioc.asm;

import org.fhm.ioc.standard.IASMTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

/**
 * @Classname ASMOptimizeTemplate
 * @Description TODO Optimize class ASM transformer
 * @Date 2023/10/15 10:56
 * @Author by 月光叶
 */
public class OptimizeASMTransformer implements IASMTransformer {


    private ClassReader cr;

    public static OptimizeASMTransformer getInstance() {
        return OptimizeASMTransformer.Instance.instance;
    }

    @Override
    public IASMTransformer init(byte[] bytes) {
        cr = new ClassReader(bytes);
        return this;
    }

    @Override
    public void create(ClassNode cn) {
        cr.accept(cn, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
    }

    private static final class Instance {
        private static final OptimizeASMTransformer instance = new OptimizeASMTransformer();
    }
}
