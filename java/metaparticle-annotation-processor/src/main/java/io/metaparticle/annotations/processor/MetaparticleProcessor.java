package io.metaparticle.annotations.processor;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.ImmutableSet;
import io.metaparticle.Builder;
import io.metaparticle.DockerImpl;
import io.metaparticle.annotations.Package;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SupportedAnnotationTypes("io.metaparticle.annotations.*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(MetaparticleProcessor.OPTION_DEBUG)
//TODO(ahaeber) Fails to run tests with @AutoService so disable that until have a fix for that.
// @AutoService(Processor.class)
public class MetaparticleProcessor extends BasicAnnotationProcessor implements AnnotationProcessorLogger {
  static final String OPTION_DEBUG = "debug";

  @Override
  protected Iterable<? extends ProcessingStep> initSteps() {
    return ImmutableSet.of(
        new RuntimeAnnotationProcessingStep(this),
        new PackageAnnotationProcessingStep(this)
    );
  }

  @Override
  public void log(String msg) {
    if (processingEnv.getOptions().containsKey(MetaparticleProcessor.OPTION_DEBUG)) {
      processingEnv.getMessager().printMessage(Kind.NOTE, msg);
    }
  }

  @Override
  public void error(String msg, Element element, AnnotationMirror annotation) {
    processingEnv.getMessager().printMessage(Kind.ERROR, msg, element, annotation);
  }

  @Override
  public void fatalError(String msg) {
    processingEnv.getMessager().printMessage(Kind.ERROR, "FATAL ERROR: " + msg);
  }

  static Builder getBuilder(Package pkg) {
    if (pkg == null) {
      return new DockerImpl();
    }
    switch (pkg.builder()) {
      case "docker":
        return new DockerImpl();
      default:
        throw new IllegalStateException("Unknown builder: " + pkg.builder());
    }
  }

  static void writeDockerfile(String className, Package p, String projectName) throws IOException {
    byte [] output;
    if (p.dockerfile() == null || p.dockerfile().length() == 0) {
      String contents =
          "FROM openjdk:8-jre-alpine\n" +
              "COPY %s /main.jar\n" +
              "CMD java -classpath /main.jar %s";
      output = String.format(contents, p.jarFile(), className).getBytes();
    } else {
      output = Files.readAllBytes(Paths.get(p.dockerfile()));
    }
    Files.write(Paths.get("Dockerfile"), output);
  }

}
