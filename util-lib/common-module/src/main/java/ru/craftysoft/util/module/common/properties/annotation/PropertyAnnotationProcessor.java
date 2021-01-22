package ru.craftysoft.util.module.common.properties.annotation;

import com.squareup.javapoet.*;
import dagger.Module;
import dagger.Provides;
import ru.craftysoft.util.module.common.properties.ApplicationProperties;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

public class PropertyAnnotationProcessor extends AbstractProcessor {

    public static final String BINDER_FULL_CLASS_NAME = "ru.craftysoft.util.module.common.properties.ConfigurationPropertiesBinder";

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(Property.class.getName(), Named.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }
        var classes = new HashSet<Class<? extends Annotation>>();
        for (var className : getSupportedAnnotationTypes()) {
            try {
                var clazz = Class.forName(className);
                classes.add((Class<? extends Annotation>) clazz);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        var holders = new HashSet<MetaDataHolder>();
        elementsLoop:
        for (var element : roundEnv.getElementsAnnotatedWithAny(classes)) {
            if (element.getAnnotation(Property.class) != null && element.getAnnotation(Named.class) == null) {
                throw new IllegalStateException("Невозможно сгенерировать код без аннотации javax.inject.Named");
            }
            var annotationsOnElement = new HashSet<Annotation>();
            for (var annotationClass : classes) {
                var annotationOnElement = element.getAnnotation(annotationClass);
                if (annotationOnElement == null) {
                    continue elementsLoop;
                }
                annotationsOnElement.add(annotationOnElement);
            }
            if (annotationsOnElement.size() == getSupportedAnnotationTypes().size()) {
                holders.add(new MetaDataHolder(element, annotationsOnElement));
            }
        }
        System.out.println(holders);
        if (!holders.isEmpty()) {
            var propertyBindModuleBuilder = TypeSpec
                    .classBuilder("PropertyBindModule")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addAnnotation(Module.class);
            var allElements = processingEnv.getElementUtils();
            var applicationPropertiesTypeName = TypeName.get(allElements.getTypeElement(ApplicationProperties.class.getName()).asType());
            holders.forEach(holder -> {
                if (!isPrimitive(holder.element().asType())) {
                    throw new IllegalArgumentException("Невозможно выполнить преобразование для типа " + holder.element().asType());
                }
                var namedAnnotation = holder.annotations.stream()
                        .filter(annotation -> annotation.annotationType().equals(Named.class))
                        .findFirst()
                        .map(Named.class::cast)
                        .orElseThrow();
                var propertyAnnotation = holder.annotations.stream()
                        .filter(annotation -> annotation.annotationType().equals(Property.class))
                        .findFirst()
                        .map(Property.class::cast)
                        .orElseThrow();
                var methodSpecBuilder = MethodSpec.methodBuilder(namedAnnotation.value())
                        .addAnnotation(Provides.class)
                        .addAnnotation(Singleton.class)
                        .addAnnotation(AnnotationSpec.builder(Named.class)
                                .addMember("value", "\"" + namedAnnotation.value() + "\"")
                                .build())
                        .addParameter(ParameterSpec.builder(applicationPropertiesTypeName, "properties").build())
                        .returns(TypeName.get(holder.element.asType()));
                String extractPropertyString;
                if (!propertyAnnotation.defaultValue().equals("")) {
                    extractPropertyString = convertPrimitive(holder.element.asType(), String.format("properties.getProperty(\"%s\")", propertyAnnotation.value()), propertyAnnotation.defaultValue());
                } else {
                    extractPropertyString = convertPrimitive(holder.element.asType(), String.format("properties.getProperty(\"%s\")", propertyAnnotation.value()));
                }
                methodSpecBuilder.addStatement("var result = " + extractPropertyString);
                if (propertyAnnotation.required()) {
                    methodSpecBuilder.addStatement(String.format("""
                            if (result == null) {
                                throw new NullPointerException("Не удалось получить значение по ключу '%s'");
                            }""", propertyAnnotation.value()));
                }
                methodSpecBuilder.addStatement("return result");
                propertyBindModuleBuilder
                        .addMethod(methodSpecBuilder.build());
            });
            try {
                JavaFile.builder("ru.craftysoft.generated", propertyBindModuleBuilder.build())
                        .build()
                        .writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    private record MetaDataHolder(Element element, Set<? extends Annotation> annotations) {
    }

    private boolean isPrimitive(TypeMirror type) {
        return switch (type.toString()) {
            case "float", "java.lang.Float",
                    "double", "java.lang.Double",
                    "int", "java.lang.Integer",
                    "long", "java.lang.Long",
                    "boolean", "java.lang.Boolean",
                    "java.lang.String" -> true;
            default -> false;
        };
    }

    private String convertPrimitive(TypeMirror type, String propertyExtractExpression) {
        switch (type.toString()) {
            case "double":
            case "java.lang.Double":
                return BINDER_FULL_CLASS_NAME + ".toDouble(" + propertyExtractExpression + ")";
            case "float":
            case "java.lang.Float":
                return BINDER_FULL_CLASS_NAME + ".toFloat(" + propertyExtractExpression + ")";
            case "int":
            case "java.lang.Integer":
                return BINDER_FULL_CLASS_NAME + ".toInt(" + propertyExtractExpression + ")";
            case "long":
            case "java.lang.Long":
                return BINDER_FULL_CLASS_NAME + ".toLong(" + propertyExtractExpression + ")";
            case "boolean":
            case "java.lang.Boolean":
                return BINDER_FULL_CLASS_NAME + ".toBoolean(" + propertyExtractExpression + ")";
            case "java.lang.String":
                return propertyExtractExpression;
        }
        throw new IllegalArgumentException("Невозможно выполнить преобразование для типа " + type);
    }

    private static String convertPrimitive(TypeMirror type, String propertyExtractExpression, String defaultValue) {
        switch (type.toString()) {
            case "double":
            case "java.lang.Double":
                return BINDER_FULL_CLASS_NAME + ".toDouble(" + propertyExtractExpression + ", " + BINDER_FULL_CLASS_NAME + ".toDouble(\"" + defaultValue + "\"))";
            case "float":
            case "java.lang.Float":
                return BINDER_FULL_CLASS_NAME + ".toFloat(" + propertyExtractExpression + ", " + BINDER_FULL_CLASS_NAME + ".toFloat(\"" + defaultValue + "\"))";
            case "int":
            case "java.lang.Integer":
                return BINDER_FULL_CLASS_NAME + ".toInt(" + propertyExtractExpression + ", " + BINDER_FULL_CLASS_NAME + ".toInt(\"" + defaultValue + "\"))";
            case "long":
            case "java.lang.Long":
                return BINDER_FULL_CLASS_NAME + ".toLong(" + propertyExtractExpression + ", " + BINDER_FULL_CLASS_NAME + ".toLong(\"" + defaultValue + "\"))";
            case "boolean":
            case "java.lang.Boolean":
                return BINDER_FULL_CLASS_NAME + ".toBoolean(" + propertyExtractExpression + ", " + BINDER_FULL_CLASS_NAME + ".toBoolean(\"" + defaultValue + "\"))";
            case "java.lang.String":
                return BINDER_FULL_CLASS_NAME + ".toString(" + propertyExtractExpression + ", \"" + defaultValue + "\")";
        }
        throw new IllegalArgumentException("Невозможно выполнить преобразование для типа " + type);
    }
}
