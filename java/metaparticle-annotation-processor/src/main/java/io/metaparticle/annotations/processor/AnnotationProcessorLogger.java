package io.metaparticle.annotations.processor;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

public interface AnnotationProcessorLogger {
  void log(String msg);

  void error(String msg, Element element, AnnotationMirror annotation);

  void fatalError(String msg);
}
