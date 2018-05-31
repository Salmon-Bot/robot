package com.lucky.game.robot;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * conan
 * 2017/10/16 10:59
 **/
@Configuration
@EnableSwagger2
public class Swagger2 extends WebMvcConfigurerAdapter {

    @Value("${swagger.req.url:localhost:8100}")
    private String swaggerUrl;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

//        registry.addResourceHandler("/api/**").addResourceLocations("classpath:/META-INF/resources/");
        super.addResourceHandlers(registry);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addRedirectViewController("/api/v2/api-docs", "/v2/api-docs").setKeepQueryParams(true);
//        registry.addRedirectViewController("/api/swagger-resources/configuration/ui","/swagger-resources/configuration/ui");
//        registry.addRedirectViewController("/api/swagger-resources/configuration/security","/swagger-resources/configuration/security");
//        registry.addRedirectViewController("/api/swagger-resources", "/swagger-resources");
    }
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo()).host(swaggerUrl)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lucky.game.robot.controller"))
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build();
    }


    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("API 接口文定义")
                .version("1.0")
                .build();
    }

}
