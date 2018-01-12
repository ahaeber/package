package io.metaparticle.annotations.processor;

import com.google.auto.common.BasicAnnotationProcessor.ProcessingStep;
import com.google.auto.common.MoreElements;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import io.metaparticle.Executor;
import io.metaparticle.Metaparticle;
import io.metaparticle.annotations.Runtime;

import javax.lang.model.element.Element;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Set;

import static io.metaparticle.Util.once;

class RuntimeAnnotationProcessingStep implements ProcessingStep {
  private final MetaparticleProcessor metaparticleProcessor;

  public RuntimeAnnotationProcessingStep(MetaparticleProcessor metaparticleProcessor) {
    this.metaparticleProcessor = metaparticleProcessor;
  }

  @Override
  public Set<? extends Class<? extends Annotation>> annotations() {
    return ImmutableSet.of(Runtime.class);
  }

  @Override
  public Set<? extends Element> process(SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
    metaparticleProcessor.log("Process Runtime annotation");
    for (Element element : elementsByAnnotation.values()) {
//        generateClass(processingEnv.getFiler(), element.getSimpleName() + "XYZ");

      //        Class clazz = Class.forName(className);
      Class clazz = MoreElements.asType(element).getSuperclass().getClass();
      String name = clazz.getCanonicalName().replace('.', '-').toLowerCase();
      final Runtime r = element.getAnnotation(Runtime.class);

//TODO
//        if (p.repository().length() != 0) {
//          image = p.repository() + "/" + image;
//        }

      Executor exec = Metaparticle.getExecutor(r);

      Runnable cancel = once(() -> exec.cancel(name));
      java.lang.Runtime.getRuntime().addShutdownHook(new Thread(cancel));

//TODO        exec.run(image, name, r, stdout, stderr);
      exec.logs(name, System.out, System.err);
      cancel.run();
    }
    return ImmutableSet.of();
  }
}
