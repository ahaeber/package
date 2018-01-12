package io.metaparticle.annotations.processor;

import com.google.auto.common.BasicAnnotationProcessor.ProcessingStep;
import com.google.auto.common.MoreElements;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import io.metaparticle.Builder;
import io.metaparticle.Metaparticle;
import io.metaparticle.annotations.Package;

import javax.lang.model.element.Element;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.util.Set;

import static io.metaparticle.Util.handleErrorExec;

class PackageAnnotationProcessingStep implements ProcessingStep {
  private final MetaparticleProcessor metaparticleProcessor;

  public PackageAnnotationProcessingStep(MetaparticleProcessor metaparticleProcessor) {
    this.metaparticleProcessor = metaparticleProcessor;
  }

  @Override
  public Set<? extends Class<? extends Annotation>> annotations() {
    return ImmutableSet.of(Package.class);
  }

  @Override
  public Set<? extends Element> process(SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
    metaparticleProcessor.log("Process Package annotation");
    for (Element element : elementsByAnnotation.values()) {
//        generateClass(processingEnv.getFiler(), element.getSimpleName() + "XYZ");

      try {
        Class clazz = MoreElements.asType(element).getSuperclass().getClass(); //Class.forName(element.asType().getKind().getDeclaringClass());
        String image = clazz.getCanonicalName().replace('.', '-').toLowerCase();
        Package p = element.getAnnotation(Package.class);

        if (p.repository().length() != 0) {
          image = p.repository() + "/" + image;
        }

        Builder builder = Metaparticle.getBuilder(p);

        OutputStream stdout = p.verbose() ? System.out : null;
        OutputStream stderr = p.quiet() ? null : System.err;

        Metaparticle.writeDockerfile(clazz.getSimpleName(), p, "metaparticle-package");

        if (p.build()) {
          handleErrorExec(new String[] {"mvn", "package"}, System.out, System.err);
          builder.build(".", image, stdout, stderr);
          if (p.publish()) {
            builder.push(image, stdout, stderr);
          }
        }
      } catch (/* NoSuchMethodException | ClassNotFoundException | */ IOException ex) {
        // This should really never happen.
        metaparticleProcessor.fatalError(ex.getMessage());
      }
    }
    return ImmutableSet.of();
  }
}
