package ru.craftysoft.util.module.common.properties.annotation;

import com.squareup.javapoet.*;
import dagger.Module;
import dagger.Provides;
import ru.craftysoft.util.module.common.properties.ApplicationProperties;

import javax.annotation.Nullable;
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
import java.util.*;

import static java.util.Optional.ofNullable;

public class PropertyAnnotationProcessor extends AbstractProcessor {

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
        var classes = Set.of(Property.class, Named.class);
        var holders = resolveMetaDataHolders(roundEnv, classes);
        if (!holders.isEmpty()) {
            var doublePropertyBindModuleClassName = "DoublePropertyBindModule.class";
            var floatPropertyBindModuleClassName = "FloatPropertyBindModule.class";
            var intPropertyBindModuleClassName = "IntPropertyBindModule.class";
            var longPropertyBindModuleClassName = "LongPropertyBindModule.class";
            var booleanPropertyBindModuleClassName = "BooleanPropertyBindModule.class";
            var stringPropertyBindModuleClassName = "StringPropertyBindModule.class";
            var builders = Map.of(
                    stringPropertyBindModuleClassName, createModuleBuilder("StringPropertyBindModule"),
                    intPropertyBindModuleClassName, createModuleBuilder("IntPropertyBindModule"),
                    longPropertyBindModuleClassName, createModuleBuilder("LongPropertyBindModule"),
                    floatPropertyBindModuleClassName, createModuleBuilder("FloatPropertyBindModule"),
                    doublePropertyBindModuleClassName, createModuleBuilder("DoublePropertyBindModule"),
                    booleanPropertyBindModuleClassName, createModuleBuilder("BooleanPropertyBindModule")
            );
            var nameValueMap = new HashMap<String, String>();
            holders.forEach(holder -> {
                var returnedType = holder.element.asType();
                if (!isPrimitive(returnedType)) {
                    throw new IllegalArgumentException("Невозможно выполнить преобразование для типа " + returnedType);
                }
                var propertyAnnotation = extractAnnotationByClass(holder, Property.class);
                var propertyAnnotationValue = propertyAnnotation.value();
                if (propertyAnnotationValue.isBlank()) {
                    throw new IllegalArgumentException("Значение Property.value не может быть пустым");
                }
                var namedAnnotation = extractAnnotationByClass(holder, Named.class);
                var namedAnnotationValue = namedAnnotation.value();
                var nameValueMapKey = resolveTypeForCheck(returnedType) + namedAnnotationValue;
                var existingPropertyAnnotationValue = nameValueMap.get(nameValueMapKey);
                if (existingPropertyAnnotationValue == null) {
                    var propertyWithValuePathParts = propertyAnnotationValue.split(":", 2);
                    var valuePath = propertyWithValuePathParts[0];
                    var defaultValue = resolveDefaultValue(holder, propertyAnnotationValue, propertyWithValuePathParts);
                    var propertiesParameterName = "properties";
                    var extractPropertyString = ofNullable(defaultValue)
                            .map(dv -> convertPrimitive(returnedType, String.format("%s.getProperty(\"%s\")", propertiesParameterName, valuePath), dv))
                            .orElseGet(() -> convertPrimitive(returnedType, String.format("%s.getProperty(\"%s\")", propertiesParameterName, valuePath)));
                    var methodSpec = createMethod(returnedType, propertyAnnotation, namedAnnotationValue, extractPropertyString, propertiesParameterName);
                    var moduleBuilder = switch (returnedType.toString()) {
                        case "double", "java.lang.Double" -> builders.get(doublePropertyBindModuleClassName);
                        case "float", "java.lang.Float" -> builders.get(floatPropertyBindModuleClassName);
                        case "int", "java.lang.Integer" -> builders.get(intPropertyBindModuleClassName);
                        case "long", "java.lang.Long" -> builders.get(longPropertyBindModuleClassName);
                        case "boolean", "java.lang.Boolean" -> builders.get(booleanPropertyBindModuleClassName);
                        case "java.lang.String" -> builders.get(stringPropertyBindModuleClassName);
                        default -> throw new IllegalArgumentException(); //этого никогда не случится
                    };
                    moduleBuilder.addMethod(methodSpec);
                } else if (!existingPropertyAnnotationValue.equals(propertyAnnotationValue)) {
                    throw new IllegalArgumentException(String.format("Невозможно передать 2 разных значения в одну и ту же переменную '%s' типа '%s'", namedAnnotationValue, returnedType));
                }
                nameValueMap.put(nameValueMapKey, propertyAnnotationValue);
            });
            writeFiles(builders);
        }
        return true;
    }

    private void writeFiles(Map<String, TypeSpec.Builder> builders) {
        try {
            var includedModulesClassNames = new StringJoiner(",", "{", "}");
            for (var entry : builders.entrySet()) {
                if (!entry.getValue().methodSpecs.isEmpty()) {
                    includedModulesClassNames.add(entry.getKey());
                    writeFile(entry.getValue());
                }
            }
            var propertyBindModuleBuilder = TypeSpec
                    .classBuilder("PropertyBindModule")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addAnnotation(AnnotationSpec.builder(Module.class)
                            .addMember("includes", includedModulesClassNames.toString())
                            .build());
            writeFile(propertyBindModuleBuilder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writeFile(TypeSpec.Builder propertyBindModuleBuilder) throws IOException {
        JavaFile.builder("ru.craftysoft.generated", propertyBindModuleBuilder.build())
                .addStaticImport(ConfigurationPropertiesBinder.class, "*")
                .build()
                .writeTo(processingEnv.getFiler());
    }

    private MethodSpec createMethod(TypeMirror returnedType,
                                    Property propertyAnnotation,
                                    String namedAnnotationValue,
                                    String extractPropertyString,
                                    String propertiesParameterName) {
        var methodSpecBuilder = MethodSpec.methodBuilder(namedAnnotationValue)
                .addModifiers(Modifier.STATIC)
                .addAnnotation(Provides.class)
                .addAnnotation(Singleton.class)
                .addAnnotation(AnnotationSpec.builder(Named.class)
                        .addMember("value", "\"" + namedAnnotationValue + "\"")
                        .build())
                .addParameter(ParameterSpec.builder(ApplicationProperties.class, propertiesParameterName).build())
                .returns(TypeName.get(returnedType));
        methodSpecBuilder.addStatement("var result = " + extractPropertyString);
        if (propertyAnnotation.required()) {
            methodSpecBuilder.addStatement(String.format("""
                    if (result == null) {
                        throw new NullPointerException("Не удалось получить значение по ключу '%s'");
                    }""", propertyAnnotation.value()));
        }
        methodSpecBuilder.addStatement("return result");
        return methodSpecBuilder.build();
    }

    private <T extends Annotation> T extractAnnotationByClass(MetaDataHolder holder, Class<T> clazz) {
        return holder.annotations.stream()
                .filter(annotation -> annotation.annotationType().equals(clazz))
                .findFirst()
                .map(clazz::cast)
                .orElseThrow();
    }

    private HashSet<MetaDataHolder> resolveMetaDataHolders(RoundEnvironment roundEnv, Set<Class<? extends Annotation>> classes) {
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
        return holders;
    }

    private TypeSpec.Builder createModuleBuilder(String stringPropertyBindModule) {
        return TypeSpec
                .classBuilder(stringPropertyBindModule)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Module.class);
    }

    @Nullable
    private String resolveDefaultValue(MetaDataHolder holder, String valueWithDefaultPath, String[] propertyWithValuePathParts) {
        if (propertyWithValuePathParts.length == 2) {
            return propertyWithValuePathParts[1];
        }
        if (propertyWithValuePathParts.length == 1 && valueWithDefaultPath.contains(":")) {
            if (holder.element().asType().toString().equals("java.lang.String")) {
                return "";
            } else {
                throw new IllegalArgumentException("Пустое значение не может быть значением по умолчанию для типа " + holder.element().asType());
            }
        }
        return null;
    }

    private boolean isPrimitive(TypeMirror type) {
        return switch (type.getKind()) {
            case DOUBLE, FLOAT, INT, LONG, BOOLEAN, CHAR -> true;
            default -> false;
        };
    }

    private Class<?> resolveTypeForCheck(TypeMirror type) {
        return switch (type.getKind()) {
            case DOUBLE -> Double.class;
            case FLOAT -> Float.class;
            case INT -> Integer.class;
            case LONG -> Long.class;
            case BOOLEAN -> Boolean.class;
            case CHAR -> String.class;
            default -> throw new IllegalArgumentException("Невозможно выполнить преобразование для типа " + type);
        };
    }

    private String convertPrimitive(TypeMirror type, String propertyExtractExpression) {
        return switch (type.getKind()) {
            case DOUBLE -> "toDouble(" + propertyExtractExpression + ")";
            case FLOAT -> "toFloat(" + propertyExtractExpression + ")";
            case INT -> "toInt(" + propertyExtractExpression + ")";
            case LONG -> "toLong(" + propertyExtractExpression + ")";
            case BOOLEAN -> "toBoolean(" + propertyExtractExpression + ")";
            case CHAR -> propertyExtractExpression;
            default -> throw new IllegalArgumentException("Невозможно выполнить преобразование для типа " + type);
        };
    }

    private static String convertPrimitive(TypeMirror type, String propertyExtractExpression, String defaultValue) {
        return switch (type.getKind()) {
            case DOUBLE -> "toDouble(" + propertyExtractExpression + ", " + "toDouble(\"" + defaultValue + "\"))";
            case FLOAT -> "toFloat(" + propertyExtractExpression + ", " + "toFloat(\"" + defaultValue + "\"))";
            case INT -> "toInt(" + propertyExtractExpression + ", " + "toInt(\"" + defaultValue + "\"))";
            case LONG -> "toLong(" + propertyExtractExpression + ", " + "toLong(\"" + defaultValue + "\"))";
            case BOOLEAN -> "toBoolean(" + propertyExtractExpression + ", " + "toBoolean(\"" + defaultValue + "\"))";
            case CHAR -> "toString(" + propertyExtractExpression + ", \"" + defaultValue + "\")";
            default -> throw new IllegalArgumentException("Невозможно выполнить преобразование для типа " + type);
        };
    }

    private record MetaDataHolder(Element element, Set<? extends Annotation> annotations) {
    }
}
