package com.lacapitale.cloud.templates.greeting;

import static com.google.common.collect.Lists.newArrayList;
import static springfox.documentation.schema.AlternateTypeRules.newRule;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.async.DeferredResult;

import com.fasterxml.classmate.TypeResolver;
import com.google.common.collect.Sets;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.GrantType;
import springfox.documentation.service.ImplicitGrant;
import springfox.documentation.service.LoginEndpoint;
import springfox.documentation.service.OAuth;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	
	@Value("${springfox.documentation.swagger.v2.api.name}")
	private String apiName;

	@Value("${springfox.documentation.swagger.v2.api.description}")
	private String apiDescription;

	@Value("${springfox.documentation.swagger.v2.api.version}")
	private String apiVersion;
	
	@Value("${springfox.documentation.swagger.v2.api.terms}")
	private String apiTerms;
	
	@Value("${springfox.documentation.swagger.v2.api.contact.name}")
	private String apiContactName;

	@Value("${springfox.documentation.swagger.v2.api.contact.url}")
	private String apiContactUrl;
	
	@Value("${springfox.documentation.swagger.v2.api.contact.email}")
	private String apiContactEmail;
	
	@Value("${springfox.documentation.swagger.v2.api.license.name}")
	private String apiLicenseName;
	
	@Value("${springfox.documentation.swagger.v2.api.license.url}")
	private String apiLicenseUrl;

	@Value("${springfox.documentation.swagger.v2.host}")
	private String hostName;
	
	@Value("${springfox.documentation.swagger.v2.schema}")
	private String securitySchemaOAuth2;
	
	@Value("${springfox.documentation.swagger.v2.scope.name}")
	private String authorizationScopeName;
	
	@Value("${springfox.documentation.swagger.v2.scope.desc}")
	private String authorizationScopeDesc;

	@Value("${springfox.documentation.swagger.v2.authorizationUrl}")
	private String authorizationUrl;
	
	@Autowired
	private TypeResolver typeResolver;
	
	private ApiInfo metaData() {
		ApiInfo apiInfo = new ApiInfo(apiName, apiDescription,
				apiVersion, apiTerms,
				new Contact(apiContactName, apiContactUrl, apiContactEmail),
				apiLicenseName, apiLicenseUrl, new ArrayList<VendorExtension>());
		return apiInfo;
	}

	@Bean
	public Docket greetingApi() {
		return new Docket(DocumentationType.SWAGGER_2)
				.protocols(Sets.newHashSet("https"))
				.host(hostName).select().apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.regex("/greeting")).build().apiInfo(metaData()).pathMapping("/")
				.directModelSubstitute(LocalDate.class, String.class).genericModelSubstitutes(ResponseEntity.class)
				.alternateTypeRules(newRule(
						typeResolver.resolve(DeferredResult.class,
								typeResolver.resolve(ResponseEntity.class, WildcardType.class)),
						typeResolver.resolve(WildcardType.class)))
				.useDefaultResponseMessages(false)
				.securitySchemes(newArrayList(securitySchema())).securityContexts(newArrayList(securityContext()));
	}

	private OAuth securitySchema() {
		AuthorizationScope authorizationScope = new AuthorizationScope(authorizationScopeName,
				authorizationScopeDesc);
		LoginEndpoint loginEndpoint = new LoginEndpoint(authorizationUrl);
		GrantType grantType = new ImplicitGrant(loginEndpoint, "access_token");
		return new OAuth(securitySchemaOAuth2, newArrayList(authorizationScope), newArrayList(grantType));
	}

	private SecurityContext securityContext() {
		return SecurityContext.builder().securityReferences(defaultAuth()).forPaths(PathSelectors.regex("/greeting"))
				.build();
	}

	private List<SecurityReference> defaultAuth() {
		AuthorizationScope authorizationScope = new AuthorizationScope(authorizationScopeName,
				authorizationScopeDesc);
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return newArrayList(new SecurityReference(securitySchemaOAuth2, authorizationScopes));
	}
}