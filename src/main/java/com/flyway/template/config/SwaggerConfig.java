package com.flyway.template.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger API 문서 설정
 * Controller 추가해도 수정 불필요!
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)  // OpenAPI 3.0
			.useDefaultResponseMessages(false)   // 기본 응답 메시지 제거
			.select()
			.apis(RequestHandlerSelectors.basePackage("com.flyway.template.controller"))  // 이 패키지 아래 자동 스캔
			.paths(PathSelectors.any())  // /api/** 경로만 문서화
			.build()
			.apiInfo(apiInfo())
			.pathMapping("/");
	}

	/**
	 * API 문서 기본 정보
	 * 프로젝트 정보만 수정하면 됨
	 */
	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
			.title("Flyway API 문서")
			.description("항공권 예약 시스템 REST API 문서")
			.version("1.0.0")
			.contact(new Contact(
				"Flyway Team",
				"https://github.com/your-org/flyway",
				"team@flyway.com"
			))
			.build();
	}
}
