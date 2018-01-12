package io.metaparticle.annotations.processor;

import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.google.testing.compile.JavaSourcesSubject.assertThat;

@RunWith(JUnit4.class)
public class MetaparticleProcessorTest {
  @Test
  public void simpleWebServer() {
    assertThat(
        JavaFileObjects.forResource("test/Web.java"))
        .processedWith(new MetaparticleProcessor())
        .compilesWithoutError()
//        .and().generatesFiles(
//          JavaFileObjects.forResource("META-INF/services/test.SomeService"),
//          JavaFileObjects.forResource("META-INF/services/test.AnotherService"))
        ;
  }
}