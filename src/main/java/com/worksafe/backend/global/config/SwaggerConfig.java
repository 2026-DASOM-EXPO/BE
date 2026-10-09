package com.worksafe.backend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    /**
     * 운영 FE 코드에서 실제로 호출되는 API만 1페이지에 노출합니다.
     * 테스트에서만 호출되거나 api.js에 정의만 되어 있는 API는 미사용 API로 분류합니다.
     */
    private static final Set<String> FRONTEND_USED_OPERATIONS = Set.of(
            "GET /api/workers",
            "GET /api/sensor-logs/workers/{workerId}",
            "GET /api/sensor-logs/latest/workers/{workerId}",
            "GET /api/sensor-logs/equipment/{equipmentId}",
            "POST /api/auth/login",
            "GET /api/alerts",
            "PATCH /api/alerts/{alertId}/read",
            "PATCH /api/alerts/read-all",
            "GET /api/alerts/stream",
            "GET /api/events/risk",
            "PATCH /api/risk-events/{riskEventId}/status",
            "GET /api/equipment",
            "GET /api/drones"
    );

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addServersItem(new Server().url("/"))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, jwtSecurityScheme()))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    @Bean
    public GroupedOpenApi frontendUsedApis() {
        return GroupedOpenApi.builder()
                .group("1페이지 - FE 사용 API")
                .pathsToMatch("/**")
                .addOpenApiCustomizer(openApi -> filterOperations(openApi, true))
                .build();
    }

    @Bean
    public GroupedOpenApi frontendUnusedApis() {
        return GroupedOpenApi.builder()
                .group("2페이지 - FE 미사용 API")
                .pathsToMatch("/**")
                .addOpenApiCustomizer(openApi -> filterOperations(openApi, false))
                .build();
    }

    private void filterOperations(OpenAPI openApi, boolean keepFrontendUsed) {
        if (openApi.getPaths() == null) return;

        openApi.getPaths().entrySet().removeIf(pathEntry -> {
            PathItem pathItem = pathEntry.getValue();
            Map<PathItem.HttpMethod, Operation> operations = pathItem.readOperationsMap();
            new HashSet<>(operations.entrySet()).forEach(operationEntry -> {
                PathItem.HttpMethod method = operationEntry.getKey();
                String key = method.name() + " " + pathEntry.getKey();
                boolean isFrontendUsed = FRONTEND_USED_OPERATIONS.contains(key);
                if (isFrontendUsed != keepFrontendUsed) {
                    clearOperation(pathItem, method);
                }
            });
            return pathItem.readOperationsMap().isEmpty();
        });
    }

    private void clearOperation(PathItem pathItem, PathItem.HttpMethod method) {
        switch (method) {
            case GET -> pathItem.setGet(null);
            case PUT -> pathItem.setPut(null);
            case POST -> pathItem.setPost(null);
            case DELETE -> pathItem.setDelete(null);
            case OPTIONS -> pathItem.setOptions(null);
            case HEAD -> pathItem.setHead(null);
            case PATCH -> pathItem.setPatch(null);
            case TRACE -> pathItem.setTrace(null);
        }
    }

    private Info apiInfo() {
        return new Info()
                .title("WORKSAFE API")
                .description("WORKSAFE API 명세서")
                .version("v1.0.0");
    }

    private SecurityScheme jwtSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
    }
}
