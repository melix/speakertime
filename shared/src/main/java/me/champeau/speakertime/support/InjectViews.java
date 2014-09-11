package me.champeau.speakertime.support;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by cchampeau on 04/09/14.
 */
@GroovyASTTransformationClass("me.champeau.speakertime.support.ViewInjectionTransformation")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InjectViews {
}
