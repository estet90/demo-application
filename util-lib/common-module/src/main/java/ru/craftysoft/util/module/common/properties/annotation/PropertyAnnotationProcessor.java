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
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

public class PropertyAnnotationProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return getSupportedClasses().stream()
                .map(Class::getName)
                .collect(Collectors.toSet());
    }

    private Set<Class<? extends Annotation>> getSupportedClasses() {
        return Set.of(Property.class, Named.class);
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
        var holders = resolveMetaDataHolders(roundEnv);
        if (!holders.isEmpty()) {
            var doublePropertyBindModuleClassName = "DoublePropertyBindModule.class";
            var floatPropertyBindModuleClassName = "FloatPropertyBindModule.class";
            var intPropertyBindModuleClassName = "IntPropertyBindModule.class";
            var longPropertyBindModuleClassName = "LongPropertyBindModule.class";
            var booleanPropertyBindModuleClassName = "BooleanPropertyBindModule.class";
            var stringPropertyBindModuleClassName = "StringPropertyBindModule.class";
            var bytePropertyBindModuleClassName = "BytePropertyBindModule.class";
            var shortPropertyBindModuleClassName = "ShortPropertyBindModule.class";
            var builders = Map.of(
                    stringPropertyBindModuleClassName, createModuleBuilder("StringPropertyBindModule"),
                    intPropertyBindModuleClassName, createModuleBuilder("IntPropertyBindModule"),
                    longPropertyBindModuleClassName, createModuleBuilder("LongPropertyBindModule"),
                    floatPropertyBindModuleClassName, createModuleBuilder("FloatPropertyBindModule"),
                    doublePropertyBindModuleClassName, createModuleBuilder("DoublePropertyBindModule"),
                    booleanPropertyBindModuleClassName, createModuleBuilder("BooleanPropertyBindModule"),
                    bytePropertyBindModuleClassName, createModuleBuilder("BytePropertyBindModule"),
                    shortPropertyBindModuleClassName, createModuleBuilder("ShortPropertyBindModule")
            );
            var nameValueMap = new HashMap<String, Property>();
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
                var existingPropertyAnnotation = nameValueMap.get(nameValueMapKey);
                if (existingPropertyAnnotation == null) {
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
                        case "byte", "java.lang.Byte" -> builders.get(bytePropertyBindModuleClassName);
                        case "short", "java.lang.Short" -> builders.get(shortPropertyBindModuleClassName);
                        default -> throw new IllegalArgumentException(); //этого никогда не случится
                    };
                    moduleBuilder.addMethod(methodSpec);
                } else if (!existingPropertyAnnotation.value().equals(propertyAnnotationValue)) {
                    throw new IllegalArgumentException(String.format("Невозможно передать 2 разных значения в одну и ту же переменную '%s' типа '%s'", namedAnnotationValue, returnedType));
                } else if (existingPropertyAnnotation.required() != propertyAnnotation.required()) {
                    throw new IllegalArgumentException(String.format("Невозможно создать определить, является ли значение переменной '%s' обязательным", namedAnnotationValue));
                }
                nameValueMap.put(nameValueMapKey, propertyAnnotation);
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

    private HashSet<MetaDataHolder> resolveMetaDataHolders(RoundEnvironment roundEnv) {
        var holders = new HashSet<MetaDataHolder>();
        var classes = getSupportedClasses();
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
        return switch (type.toString()) {
            case "float", "java.lang.Float",
                    "double", "java.lang.Double",
                    "int", "java.lang.Integer",
                    "long", "java.lang.Long",
                    "boolean", "java.lang.Boolean",
                    "byte", "java.lang.Byte",
                    "short", "java.lang.Short",
                    "java.lang.String" -> true;
            default -> false;
        };
    }

    private Class<?> resolveTypeForCheck(TypeMirror type) {
        return switch (type.toString()) {
            case "double", "java.lang.Double" -> Double.class;
            case "float", "java.lang.Float" -> Float.class;
            case "int", "java.lang.Integer" -> Integer.class;
            case "long", "java.lang.Long" -> Long.class;
            case "boolean", "java.lang.Boolean" -> Boolean.class;
            case "java.lang.String" -> String.class;
            case "byte", "java.lang.Byte" -> Byte.class;
            case "short", "java.lang.Short" -> Short.class;
            default -> throw new IllegalArgumentException("Невозможно выполнить преобразование для типа " + type);
        };
    }

    private String convertPrimitive(TypeMirror type, String propertyExtractExpression) {
        return switch (type.toString()) {
            case "double", "java.lang.Double" -> "toDouble(" + propertyExtractExpression + ")";
            case "float", "java.lang.Float" -> "toFloat(" + propertyExtractExpression + ")";
            case "int", "java.lang.Integer" -> "toInt(" + propertyExtractExpression + ")";
            case "long", "java.lang.Long" -> "toLong(" + propertyExtractExpression + ")";
            case "boolean", "java.lang.Boolean" -> "toBoolean(" + propertyExtractExpression + ")";
            case "java.lang.String" -> propertyExtractExpression;
            case "byte", "java.lang.Byte" -> "toByte(" + propertyExtractExpression + ")";
            case "short", "java.lang.Short" -> "toShort(" + propertyExtractExpression + ")";
            default -> throw new IllegalArgumentException("Невозможно выполнить преобразование для типа " + type);
        };
    }

    private static String convertPrimitive(TypeMirror type, String propertyExtractExpression, String defaultValue) {
        return switch (type.toString()) {
            case "double", "java.lang.Double" -> "toDouble(" + propertyExtractExpression + ", " + "toDouble(\"" + defaultValue + "\"))";
            case "float", "java.lang.Float" -> "toFloat(" + propertyExtractExpression + ", " + "toFloat(\"" + defaultValue + "\"))";
            case "int", "java.lang.Integer" -> "toInt(" + propertyExtractExpression + ", " + "toInt(\"" + defaultValue + "\"))";
            case "long", "java.lang.Long" -> "toLong(" + propertyExtractExpression + ", " + "toLong(\"" + defaultValue + "\"))";
            case "boolean", "java.lang.Boolean" -> "toBoolean(" + propertyExtractExpression + ", " + "toBoolean(\"" + defaultValue + "\"))";
            case "java.lang.String" -> "toStringWithDefaultValue(" + propertyExtractExpression + ", \"" + defaultValue + "\")";
            case "byte", "java.lang.Byte" -> "toByte(" + propertyExtractExpression + ", " + "toByte(\"" + defaultValue + "\"))";
            case "short", "java.lang.Short" -> "toShort(" + propertyExtractExpression + ", " + "toShort(\"" + defaultValue + "\"))";
            default -> throw new IllegalArgumentException("Невозможно выполнить преобразование для типа " + type);
        };
    }

    private record MetaDataHolder(Element element, Set<? extends Annotation> annotations) {
    }
}
