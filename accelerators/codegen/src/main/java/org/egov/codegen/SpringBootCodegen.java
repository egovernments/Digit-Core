package org.egov.codegen;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.swagger.codegen.v3.*;
import io.swagger.codegen.v3.generators.features.BeanValidationFeatures;
import io.swagger.codegen.v3.generators.features.OptionalFeatures;
import io.swagger.codegen.v3.generators.handlebars.lambda.EscapeDoubleQuotesLambda;
import io.swagger.codegen.v3.generators.handlebars.lambda.RemoveLineBreakLambda;
import io.swagger.codegen.v3.generators.java.AbstractJavaCodegen;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.regex.Matcher;

public class SpringBootCodegen extends AbstractJavaCodegen
        implements BeanValidationFeatures, OptionalFeatures {
    public static final String DEFAULT_LIBRARY = "spring-boot";
    public static final String TITLE = "title";
    public static final String CONFIG_PACKAGE = "configPackage";
    public static final String BASE_PACKAGE = "basePackage";
    public static final String REPO_PACKAGE = "repositoryPackage";
    public static final String UTIL_PACKAGE = "utilPackage";
    public static final String MODEL_PACKAGE = "modelPackage";
    public static final String KAFKA_PACKAGE = "kafkaPackage";
    public static final String INTERFACE_ONLY = "interfaceOnly";
    public static final String DELEGATE_PATTERN = "delegatePattern";
    public static final String SINGLE_CONTENT_TYPES = "singleContentTypes";
    public static final String JAVA_8 = "java8";
    public static final String ASYNC = "async";
    public static final String RESPONSE_WRAPPER = "responseWrapper";
    public static final String USE_TAGS = "useTags";
    public static final String SPRING_MVC_LIBRARY = "spring-mvc";
    public static final String SPRING_CLOUD_LIBRARY = "spring-cloud";
    public static final String IMPLICIT_HEADERS = "implicitHeaders";
    public static final String SWAGGER_DOCKET_CONFIG = "swaggerDocketConfig";

    protected String title = "eGov App";
    protected String basePackage;
    protected String configPackage;
    protected String repositoryPackage;
    protected String utilPackage;
    protected String kafkaPackage;
    protected boolean interfaceOnly = false;
    protected boolean delegatePattern = false;
    protected boolean delegateMethod = false;
    protected boolean singleContentTypes = false;
    protected boolean java8 = true;
    protected boolean async = false;
    protected String responseWrapper = "";
    protected boolean useTags = false;
    protected boolean useBeanValidation = true;
    protected boolean implicitHeaders = false;
    protected boolean swaggerDocketConfig = false;
    protected boolean useOptional = false;

    private final Config config;

    private static Logger LOGGER = LoggerFactory.getLogger(AbstractJavaCodegen.class);

    public SpringBootCodegen(Config config, String outputFolder) {
        super();
        this.config = config;
        processConfigs();
        this.outputFolder = outputFolder;
        templateDir = "JavaSpringBoot";
        customTemplateDir = templateDir;
        embeddedTemplateDir = "JavaSpringBoot";

        supportedLibraries.put(DEFAULT_LIBRARY, "Spring-boot Server application using the SpringFox integration.");
        setLibrary(DEFAULT_LIBRARY);


//        cliOptions.add(new CliOption(CONFIG_PACKAGE, "configuration package for generated code"));
//        cliOptions.add(new CliOption(BASE_PACKAGE, "base package (invokerPackage) for generated code"));
//        cliOptions.add(CliOption.newBoolean(INTERFACE_ONLY, "Whether to generate only API interface stubs without the server files."));
//        cliOptions.add(CliOption.newBoolean(DELEGATE_PATTERN, "Whether to generate the server files using the delegate pattern"));
//        cliOptions.add(CliOption.newBoolean(SINGLE_CONTENT_TYPES, "Whether to select only one produces/consumes content-type by operation."));
//        cliOptions.add(CliOption.newBoolean(JAVA_8, "use java8 default interface"));
//        cliOptions.add(CliOption.newBoolean(ASYNC, "use async Callable controllers"));
//        cliOptions.add(new CliOption(RESPONSE_WRAPPER, "wrap the responses in given type (Future,Callable,CompletableFuture,ListenableFuture,DeferredResult,HystrixCommand,RxObservable,RxSingle or fully qualified type)"));
//        cliOptions.add(CliOption.newBoolean(USE_TAGS, "use tags for creating interface and controller classnames"));
//        cliOptions.add(CliOption.newBoolean(USE_BEANVALIDATION, "Use BeanValidation API annotations"));
//        cliOptions.add(CliOption.newBoolean(IMPLICIT_HEADERS, "Use of @ApiImplicitParams for headers."));
//        cliOptions.add(CliOption.newBoolean(SWAGGER_DOCKET_CONFIG, "Generate Spring Swagger Docket configuration class."));
//        cliOptions.add(CliOption.newBoolean(USE_OPTIONAL,
//                "Use Optional container for optional parameters"));


//        CliOption library = new CliOption(CodegenConstants.LIBRARY, "library template (sub-template) to use");
//        library.setDefault(DEFAULT_LIBRARY);
//        library.setEnum(supportedLibraries);
//        library.setDefault(DEFAULT_LIBRARY);
//        cliOptions.add(library);
    }

    private void processConfigs(){
        groupId = config.getGroupId();
        artifactId = config.getArtifactId();
        basePackage = config.getBasePackage();
        additionalProperties.put("useLombok", config.isUseLombok());
        additionalProperties.put("useTracer", config.isUseTracer());

        configPackage = basePackage + ".config";
        apiPackage = basePackage + ".web.controllers";
        modelPackage = basePackage + ".web.models";
        invokerPackage = basePackage + ".web";
        repositoryPackage = basePackage + ".repository";
        utilPackage = basePackage + ".util";
        kafkaPackage = basePackage + ".kafka";
        dateLibrary = "";

        additionalProperties.put(CONFIG_PACKAGE, configPackage);
        additionalProperties.put(BASE_PACKAGE, basePackage);
        additionalProperties.put(REPO_PACKAGE, repositoryPackage);
        additionalProperties.put(UTIL_PACKAGE, utilPackage);
        additionalProperties.put(MODEL_PACKAGE, modelPackage);
        additionalProperties.put(KAFKA_PACKAGE, kafkaPackage);
        additionalProperties.put("jackson", true);


    }

    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    @Override
    public String getName() {
        return "spring";
    }

    @Override
    public String getHelp() {
        return "Generates a Java SpringBoot Server application with eGov coding style";
    }

    @Override
    public String toApiTestFilename(String name) {
        return toApiName(name);
    }

    @Override
    public void processOpts() {

        // Process java8 option before common java ones to change the default dateLibrary to java8.
        if (additionalProperties.containsKey(JAVA_8)) {
            this.setJava8(Boolean.valueOf(additionalProperties.get(JAVA_8).toString()));
        }
        if (this.java8) {
            additionalProperties.put("javaVersion", "17");
            additionalProperties.put("jdk8", "true");
            if (!additionalProperties.containsKey(DATE_LIBRARY)) {
                setDateLibrary("java8");
            }
        }

        // set invokerPackage as basePackage
        if (additionalProperties.containsKey(CodegenConstants.INVOKER_PACKAGE)) {
            this.setBasePackage((String) additionalProperties.get(CodegenConstants.INVOKER_PACKAGE));
            additionalProperties.put(BASE_PACKAGE, basePackage);
            LOGGER.info("Set base package to invoker package (" + basePackage + ")");
        }

        super.processOpts();

        // clear model and api doc template as this codegen
        // does not support auto-generated markdown doc at the moment
        //TODO: add doc templates
        modelDocTemplateFiles.remove("model_doc.mustache");
        apiDocTemplateFiles.remove("api_doc.mustache");

        additionalProperties.put(USE_BEANVALIDATION, true);

        if (additionalProperties.containsKey(TITLE)) {
            this.setTitle((String) additionalProperties.get(TITLE));
        }

        if (additionalProperties.containsKey(CONFIG_PACKAGE)) {
            this.setConfigPackage((String) additionalProperties.get(CONFIG_PACKAGE));
        }

        if (additionalProperties.containsKey(BASE_PACKAGE)) {
            this.setBasePackage((String) additionalProperties.get(BASE_PACKAGE));
        }

        if (additionalProperties.containsKey(REPO_PACKAGE)) {
            this.setRepositoryPackage((String) additionalProperties.get(REPO_PACKAGE));
        }

        if (additionalProperties.containsKey(UTIL_PACKAGE)) {
            this.setUtilPackage((String) additionalProperties.get(UTIL_PACKAGE));
        }

        if(additionalProperties.containsKey(MODEL_PACKAGE)) {
            this.setModelPackage((String) additionalProperties.get(MODEL_PACKAGE));
        }

        if (additionalProperties.containsKey(KAFKA_PACKAGE)) {
            this.setkafkaPackage((String) additionalProperties.get(KAFKA_PACKAGE));
        }

        if (additionalProperties.containsKey(INTERFACE_ONLY)) {
            this.setInterfaceOnly(Boolean.valueOf(additionalProperties.get(INTERFACE_ONLY).toString()));
        }

        if (additionalProperties.containsKey(DELEGATE_PATTERN)) {
            this.setDelegatePattern(Boolean.valueOf(additionalProperties.get(DELEGATE_PATTERN).toString()));
        }

        if (additionalProperties.containsKey(SINGLE_CONTENT_TYPES)) {
            this.setSingleContentTypes(Boolean.valueOf(additionalProperties.get(SINGLE_CONTENT_TYPES).toString()));
        }

        if (additionalProperties.containsKey(JAVA_8)) {
            this.setJava8(Boolean.valueOf(additionalProperties.get(JAVA_8).toString()));
        }

        if (additionalProperties.containsKey(ASYNC)) {
            this.setAsync(Boolean.valueOf(additionalProperties.get(ASYNC).toString()));
        }

        if (additionalProperties.containsKey(RESPONSE_WRAPPER)) {
            this.setResponseWrapper((String) additionalProperties.get(RESPONSE_WRAPPER));
        }

        if (additionalProperties.containsKey(USE_TAGS)) {
            this.setUseTags(Boolean.valueOf(additionalProperties.get(USE_TAGS).toString()));
        }

        if (additionalProperties.containsKey(USE_BEANVALIDATION)) {
            this.setUseBeanValidation(convertPropertyToBoolean(USE_BEANVALIDATION));
        }

        if (additionalProperties.containsKey(USE_OPTIONAL)) {
            this.setUseOptional(convertPropertyToBoolean(USE_OPTIONAL));
        }

        if (useBeanValidation) {
            writePropertyBack(USE_BEANVALIDATION, useBeanValidation);
        }

        if (additionalProperties.containsKey(IMPLICIT_HEADERS)) {
            this.setImplicitHeaders(Boolean.valueOf(additionalProperties.get(IMPLICIT_HEADERS).toString()));
        }

        if (additionalProperties.containsKey(SWAGGER_DOCKET_CONFIG)) {
            this.setSwaggerDocketConfig(Boolean.valueOf(additionalProperties.get(SWAGGER_DOCKET_CONFIG).toString()));
        }

        typeMapping.put("file", "Resource");
        importMapping.put("Resource", "org.springframework.core.io.Resource");

        if (useOptional) {
            writePropertyBack(USE_OPTIONAL, useOptional);
        }

        if (this.interfaceOnly && this.delegatePattern) {
            if (this.java8) {
                this.delegateMethod = true;
                additionalProperties.put("delegate-method", true);
            } else {
                throw new IllegalArgumentException(
                        String.format("Can not generate code with `%s` and `%s` true while `%s` is false.",
                                DELEGATE_PATTERN, INTERFACE_ONLY, JAVA_8));
            }
        }

        supportingFiles.add(new SupportingFile("pom.mustache", "", "pom.xml"));
        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));


        supportingFiles.add(new SupportingFile("main.mustache",
                (sourceFolder + File.separator + basePackage).replace(".", java.io.File.separator), "Main" +
                ".java"));
        supportingFiles.add(new SupportingFile("mainConfiguration.mustache",
                (sourceFolder + File.separator + configPackage).replace(".", java.io.File.separator),
                "MainConfiguration.java"));
        supportingFiles.add(new SupportingFile("configuration.mustache",
                (sourceFolder + File.separator + configPackage).replace(".", java.io.File.separator),
                "Configuration.java"));
        supportingFiles.add(new SupportingFile("serviceConstants.mustache",
                (sourceFolder + File.separator + configPackage).replace(".", java.io.File.separator),
                "ServiceConstants.java"));
        supportingFiles.add(new SupportingFile("application.mustache",
                ("src.main.resources").replace(".", java.io.File.separator), "application.properties"));

        supportingFiles.add(new SupportingFile("start.mustache",
                ("src.main.resources").replace(".", java.io.File.separator), "start.sh"));

        supportingFiles.add(new SupportingFile("Dockerfile.mustache",
                ("src.main.resources").replace(".", java.io.File.separator), "Dockerfile"));



        supportingFiles.add(new SupportingFile("serviceRequestRepository.mustache",
                (sourceFolder + File.separator + repositoryPackage).replace(".", java.io.File.separator),
                "ServiceRequestRepository.java"));

        // Add utilities
        supportingFiles.add(new SupportingFile("userUtil.mustache",
                (sourceFolder + File.separator + utilPackage).replace(".", java.io.File.separator),
                "UserUtil.java"));

        supportingFiles.add(new SupportingFile("requestInfoWrapper.mustache",
                (sourceFolder + File.separator + modelPackage).replace(".", File.separator),
                "RequestInfoWrapper.java"));

        supportingFiles.add(new SupportingFile("workflowUtil.mustache",
                (sourceFolder + File.separator + utilPackage).replace(".", java.io.File.separator),
                "WorkflowUtil.java"));

        supportingFiles.add(new SupportingFile("urlShortenerUtil.mustache",
                (sourceFolder + File.separator + utilPackage).replace(".", java.io.File.separator),
                "UrlShortenerUtil.java"));

        supportingFiles.add(new SupportingFile("mdmsUtil.mustache",
                (sourceFolder + File.separator + utilPackage).replace(".", java.io.File.separator),
                "MdmsUtil.java"));

        supportingFiles.add(new SupportingFile("idgenUtil.mustache",
                (sourceFolder + File.separator + utilPackage).replace(".", java.io.File.separator),
                "IdgenUtil.java"));

        supportingFiles.add(new SupportingFile("responseInfoFactory.mustache",
                (sourceFolder + File.separator + utilPackage).replace(".", java.io.File.separator),
                "ResponseInfoFactory.java"));

        supportingFiles.add(new SupportingFile("consumer.mustache",
                (sourceFolder + File.separator + kafkaPackage).replace(".", java.io.File.separator),
                "Consumer.java"));

        supportingFiles.add(new SupportingFile("producer.mustache",
                (sourceFolder + File.separator + kafkaPackage).replace(".", java.io.File.separator),
                "Producer.java"));

        if(config.isUseTracer()) {
            supportingFiles.add(new SupportingFile("testConfiguration.mustache",
                    (testFolder + File.separator + basePackage).replace(".", java.io.File.separator),
                    "TestConfiguration.java"));
        }

        //Removing controller interface
        apiTemplateFiles.remove("api.mustache");

        apiTemplateFiles.put("controller.mustache", "Controller.java");

        apiTestTemplateFiles.clear();
        apiTestTemplateFiles.put("controller_test.mustache", "ControllerTest.java");


        if (this.java8) {
            additionalProperties.put("javaVersion", "17");
            additionalProperties.put("jdk8", "true");
            if (this.async) {
                additionalProperties.put(RESPONSE_WRAPPER, "CompletableFuture");
            }
        } else if (this.async) {
            additionalProperties.put(RESPONSE_WRAPPER, "Callable");
        }

        // Some well-known Spring or Spring-Cloud response wrappers
        switch (this.responseWrapper) {
            case "Future":
            case "Callable":
            case "CompletableFuture":
                additionalProperties.put(RESPONSE_WRAPPER, "java.util.concurrent" + this.responseWrapper);
                break;
            case "ListenableFuture":
                additionalProperties.put(RESPONSE_WRAPPER, "org.springframework.util.concurrent.ListenableFuture");
                break;
            case "DeferredResult":
                additionalProperties.put(RESPONSE_WRAPPER, "org.springframework.web.context.request.async.DeferredResult");
                break;
            case "HystrixCommand":
                additionalProperties.put(RESPONSE_WRAPPER, "com.netflix.hystrix.HystrixCommand");
                break;
            case "RxObservable":
                additionalProperties.put(RESPONSE_WRAPPER, "rx.Observable");
                break;
            case "RxSingle":
                additionalProperties.put(RESPONSE_WRAPPER, "rx.Single");
                break;
            default:
                break;
        }

        // add lambda for mustache templates
        additionalProperties.put("lambdaEscapeDoubleQuote", new EscapeDoubleQuotesLambda());
        additionalProperties.put("lambdaRemoveLineBreak", new RemoveLineBreakLambda());
    }

    @Override
    public void addOperationToGroup(String tag, String resourcePath, Operation operation, CodegenOperation co, Map<String, List<CodegenOperation>> operations) {
        if((library.equals(DEFAULT_LIBRARY) || library.equals(SPRING_MVC_LIBRARY)) && !useTags) {
            String basePath = resourcePath;
            if (basePath.startsWith("/")) {
                basePath = basePath.substring(1);
            }
            int pos = basePath.indexOf("/");
            if (pos > 0) {
                basePath = basePath.substring(0, pos);
            }

            if (basePath.equals("")) {
                basePath = "default";
            } else {
                co.subresourceOperation = !co.path.isEmpty();
            }
            List<CodegenOperation> opList = operations.get(basePath);
            if (opList == null) {
                opList = new ArrayList<CodegenOperation>();
                operations.put(basePath, opList);
            }
            opList.add(co);
            co.baseName = basePath;
        } else {
            super.addOperationToGroup(tag, resourcePath, operation, co, operations);
        }
    }

    @Override
    public String getDefaultTemplateDir() {
        return templateDir();
    }

    @Override
    protected String getTemplateDir() {
        return templateDir;
    }


    @Override
    public void preprocessOpenAPI(OpenAPI openAPI) {
        super.preprocessOpenAPI(openAPI);

        if(!additionalProperties.containsKey(TITLE)) {
            // From the title, compute a reasonable name for the package and the API
            String title = openAPI.getInfo().getTitle();

            // Drop any API suffix
            if (title != null) {
                title = title.trim().replace(" ", "-");
                if (title.toUpperCase().endsWith("API")) {
                    title = title.substring(0, title.length() - 3);
                }

                this.title = camelize(sanitizeName(title), true);
            }
            additionalProperties.put(TITLE, this.title);
        }

        String port = "8080";


        this.additionalProperties.put("serverPort", port);
        if (openAPI.getPaths() != null) {
            for (String pathname : openAPI.getPaths().keySet()) {
                PathItem pathItem = openAPI.getPaths().get(pathname);
                if (pathItem.readOperations() != null) {
                    for (Operation operation : pathItem.readOperations()) {
                        if (operation.getTags() != null) {
                            List<Map<String, String>> tags = new ArrayList<Map<String, String>>();
                            for (String tag : operation.getTags()) {
                                Map<String, String> value = new HashMap<String, String>();
                                value.put("tag", tag);
                                value.put("hasMore", "true");
                                tags.add(value);
                            }
                            if (tags.size() > 0) {
                                tags.get(tags.size() - 1).remove("hasMore");
                            }
                            if (operation.getTags().size() > 0) {
                                String tag = operation.getTags().get(0);
                                operation.setTags(Arrays.asList(tag));
                            }
                            operation.addExtension("x-tags", tags);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
        if (operations != null) {
            List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");
            for (final CodegenOperation operation : ops) {
                List<CodegenResponse> responses = operation.responses;
                if (responses != null) {
                    for (final CodegenResponse resp : responses) {
                        if ("0".equals(resp.code)) {
                            resp.code = "200";
                        }
                        doDataTypeAssignment(resp.dataType, new SpringBootCodegen.DataTypeAssigner() {
                            @Override
                            public void setReturnType(final String returnType) {
                                resp.dataType = returnType;
                            }

                            @Override
                            public void setReturnContainer(final String returnContainer) {
                                resp.containerType = returnContainer;
                            }
                        });
                    }
                }

                doDataTypeAssignment(operation.returnType, new SpringBootCodegen.DataTypeAssigner() {

                    @Override
                    public void setReturnType(final String returnType) {
                        operation.returnType = returnType;
                    }

                    @Override
                    public void setReturnContainer(final String returnContainer) {
                        operation.returnContainer = returnContainer;
                    }
                });

                if(implicitHeaders){
                    removeHeadersFromAllParams(operation.allParams);
                }
            }
        }

        return objs;
    }

    private interface DataTypeAssigner {
        void setReturnType(String returnType);
        void setReturnContainer(String returnContainer);
    }

    /**
     *
     * @param returnType The return type that needs to be converted
     * @param dataTypeAssigner An object that will assign the data to the respective fields in the model.
     */
    private void doDataTypeAssignment(String returnType, SpringBootCodegen.DataTypeAssigner dataTypeAssigner) {
        final String rt = returnType;
        if (rt == null) {
            dataTypeAssigner.setReturnType("Void");
        } else if (rt.startsWith("List")) {
            int end = rt.lastIndexOf(">");
            if (end > 0) {
                dataTypeAssigner.setReturnType(rt.substring("List<".length(), end).trim());
                dataTypeAssigner.setReturnContainer("List");
            }
        } else if (rt.startsWith("Map")) {
            int end = rt.lastIndexOf(">");
            if (end > 0) {
                dataTypeAssigner.setReturnType(rt.substring("Map<".length(), end).split(",")[1].trim());
                dataTypeAssigner.setReturnContainer("Map");
            }
        } else if (rt.startsWith("Set")) {
            int end = rt.lastIndexOf(">");
            if (end > 0) {
                dataTypeAssigner.setReturnType(rt.substring("Set<".length(), end).trim());
                dataTypeAssigner.setReturnContainer("Set");
            }
        }
    }

    /**
     * This method removes header parameters from the list of parameters and also
     * corrects last allParams hasMore state.
     * @param allParams list of all parameters
     */
    private void removeHeadersFromAllParams(List<CodegenParameter> allParams) {
        if(allParams.isEmpty()){
            return;
        }
        final ArrayList<CodegenParameter> copy = new ArrayList<>(allParams);
        allParams.clear();

        for(CodegenParameter p : copy){
            if(!p.getIsHeaderParam()){
                allParams.add(p);
            }
        }
//        allParams.get(allParams.size()-1).getHasMore() =false;
    }

    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
        if(library.equals(SPRING_CLOUD_LIBRARY)) {
            List<CodegenSecurity> authMethods = (List<CodegenSecurity>) objs.get("authMethods");
            if (authMethods != null) {
                for (CodegenSecurity authMethod : authMethods) {
                    authMethod.name = camelize(sanitizeName(authMethod.name), true);
                }
            }
        }
        return objs;
    }

    @Override
    public String toApiName(String name) {
        if (name.length() == 0) {
            return "DefaultApi";
        }
        name = sanitizeName(name);
        return camelize(name) + "Api";
    }

    @Override
    public void setParameterExampleValue(CodegenParameter p) {
        String type = p.baseType;
        if (type == null) {
            type = p.dataType;
        }

        if ("File".equals(type)) {
            String example;

            if (p.defaultValue == null) {
                example = p.example;
            } else {
                example = p.defaultValue;
            }

            if (example == null) {
                example = "/path/to/file";
            }
            example = "new org.springframework.core.io.FileSystemResource(new java.io.File(\"" + escapeText(example) + "\"))";
            p.example = example;
        } else {
            super.setParameterExampleValue(p);
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setConfigPackage(String configPackage) {
        this.configPackage = configPackage;
    }

    public void setRepositoryPackage(String repositoryPackage) {
        this.repositoryPackage = repositoryPackage;
    }

    public void setUtilPackage(String utilPackage) {this.utilPackage = utilPackage;}

    public void setModelPackage(String modelPackage) {this.modelPackage = modelPackage;}

    public void setkafkaPackage(String kafkaPackage) {this.kafkaPackage = kafkaPackage;}

    public void setBasePackage(String configPackage) {
        this.basePackage = configPackage;
    }

    public void setInterfaceOnly(boolean interfaceOnly) { this.interfaceOnly = interfaceOnly; }

    public void setDelegatePattern(boolean delegatePattern) { this.delegatePattern = delegatePattern; }

    public void setSingleContentTypes(boolean singleContentTypes) {
        this.singleContentTypes = singleContentTypes;
    }

    public void setJava8(boolean java8) { this.java8 = java8; }

    public void setAsync(boolean async) { this.async = async; }

    public void setResponseWrapper(String responseWrapper) { this.responseWrapper = responseWrapper; }

    public void setUseTags(boolean useTags) {
        this.useTags = useTags;
    }

    public void setImplicitHeaders(boolean implicitHeaders) {
        this.implicitHeaders = implicitHeaders;
    }

    public void setSwaggerDocketConfig(boolean swaggerDocketConfig) {
        this.swaggerDocketConfig = swaggerDocketConfig;
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);

        if ("null".equals(property.example)) {
            property.example = null;
        }

        //Add imports for Jackson
        if (!Boolean.TRUE.equals(model.getIsEnum())) {
            model.imports.add("JsonProperty");

            if (Boolean.TRUE.equals(model.getHasEnums())) {
                model.imports.add("JsonValue");
            }
        } else { // enum class
            //Needed imports for Jackson's JsonCreator
            if (additionalProperties.containsKey("jackson")) {
                model.imports.add("JsonCreator");
            }
        }
    }

    @Override
    public Map<String, Object> postProcessModelsEnum(Map<String, Object> objs) {
        objs = super.postProcessModelsEnum(objs);

        //Add imports for Jackson
        List<Map<String, String>> imports = (List<Map<String, String>>)objs.get("imports");
        List<Object> models = (List<Object>) objs.get("models");
        for (Object _mo : models) {
            Map<String, Object> mo = (Map<String, Object>) _mo;
            CodegenModel cm = (CodegenModel) mo.get("model");
            // for enum model
            if (Boolean.TRUE.equals(cm.getIsEnum()) && cm.allowableValues != null) {
                cm.imports.add(importMapping.get("JsonValue"));
                Map<String, String> item = new HashMap<String, String>();
                item.put("import", importMapping.get("JsonValue"));
                imports.add(item);
            }
        }

        return objs;
    }

    public void setUseBeanValidation(boolean useBeanValidation) {
        this.useBeanValidation = useBeanValidation;
    }

    @Override
    public void setUseOptional(boolean useOptional) {
        this.useOptional = useOptional;
    }
}

