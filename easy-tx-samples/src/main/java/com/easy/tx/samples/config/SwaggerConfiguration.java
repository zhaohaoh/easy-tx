package com.easy.tx.samples.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.WebFluxRequestHandlerProvider;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author hzh
 * 2.6.3 springboot版本需要加配置spring.mvc.pathmatch.matching-strategy=ant_path_matcher
 */
@Profile("!prod")
@Configuration
@EnableSwagger2  //开启 Swagger2 ，可不写
// @EnableSwagger2WebMvc
public class SwaggerConfiguration {
    //不配置默认8080
    @Value("${server.port:8080}")
    public String port;
    @Value("${server.servlet.context-path:/}")
    public String path;
    @Value("${spring.application.name: 无名容器}")
    public String applicationName;

    @Bean(value = "defaultApi")
    public Docket defaultApi() {
        //DocumentationType.SWAGGER_2
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                //分组名称
//                .groupName("2.0版本")
                .select()
                //apis只有一个会生效.所以采用条件匹配器的or来匹配两个条件。
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
//               .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.any())
                .build()
        //        .globalRequestParameters(getGlobalRequestParameters())
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
        return docket;
    }


    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(applicationName)
                .description(applicationName)
                .termsOfServiceUrl("http://localhost:" + port)
                .contact(new Contact("hzh", "123", "111"))
                .version("1.0")
                .build();
    }

    private List<SecurityScheme> securitySchemes() {
        //设置请求头信息
        List<SecurityScheme> result = new ArrayList<>();
        ApiKey apiKey = new ApiKey("Authorization", "Authorization", "header");
        result.add(apiKey);
        return result;
    }

    private List<SecurityContext> securityContexts() {
        List<SecurityContext> securityContexts = new ArrayList<>();
        securityContexts.add(
                SecurityContext.builder()
                        .securityReferences(defaultAuth())
//                        对应路径下不需要权限认证
                        .forPaths(PathSelectors.regex("^((?!open|login|smscode|captcha).)*"))
//                        .forPaths(PathSelectors.regex("^(?!/open/).*"))
                        .build());
        return securityContexts;
    }

    //生成全局通用参数
//    private List<RequestParameter> getGlobalRequestParameters() {
//        List<RequestParameter> parameters = new ArrayList<>();
//        parameters.add(new RequestParameterBuilder()
//                .name("Authorization")
//                .description("header")
//                .required(true)
//                .in(ParameterType.HEADER)
//                .query(q -> q.defaultValue("Bearer ").model(m -> m.scalarModel(ScalarType.STRING)))
//                .required(false)
//                .build());
//        return parameters;
//    }
    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        List<SecurityReference> securityReferences = new ArrayList<>();
        securityReferences.add(new SecurityReference("Authorization", authorizationScopes));
        return securityReferences;
    }

    /**
     * 重写 PathProvider ,解决 context-path 重复问题 是swagger的bug
     *
     * @return
     */
//    @Bean
//    public PathProvider pathProvider() {
//        return new DefaultPathProvider() {
//            @Override
//            public String getOperationPath(String operationPath) {
//                operationPath = operationPath.replaceFirst(path, "/");
//                UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath("/");
//                return Paths.removeAdjacentForwardSlashes(uriComponentsBuilder.path(operationPath).build().toString());
//            }
//
//            @Override
//            public String getResourceListingPath(String groupName, String apiDeclaration) {
//                apiDeclaration = super.getResourceListingPath(groupName, apiDeclaration);
//                return apiDeclaration;
//            }
//        };
//    }



    // 解决springboot2.6.0和swagger不兼容的问题
    @Bean
    public static BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
        return new BeanPostProcessor() {

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof WebMvcRequestHandlerProvider || bean instanceof WebFluxRequestHandlerProvider) {
                    customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
                }
                return bean;
            }

            private <T extends RequestMappingInfoHandlerMapping> void customizeSpringfoxHandlerMappings(List<T> mappings) {
                List<T> copy = mappings.stream()
                        .filter(mapping -> mapping.getPatternParser() == null)
                        .collect(Collectors.toList());
                mappings.clear();
                mappings.addAll(copy);
            }

            @SuppressWarnings("unchecked")
            private List<RequestMappingInfoHandlerMapping> getHandlerMappings(Object bean) {
                try {
                    Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
                    field.setAccessible(true);
                    return (List<RequestMappingInfoHandlerMapping>) field.get(bean);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }
}