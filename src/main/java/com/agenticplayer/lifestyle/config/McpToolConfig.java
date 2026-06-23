package com.agenticplayer.lifestyle.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.agenticplayer.lifestyle.tool.LifestyleAssistantTools;

@Configuration
public class McpToolConfig {

    @Bean
    List<McpStatelessServerFeatures.SyncToolSpecification> lifestyleToolSpecifications(
            LifestyleAssistantTools tools) {
        ToolCallbackProvider provider = MethodToolCallbackProvider.builder()
                .toolObjects(tools)
                .build();

        return Arrays.stream(provider.getToolCallbacks())
                .map(callback -> McpToolUtils.toStatelessSyncToolSpecification(callback, null))
                .map(this::withPlayMcpAnnotations)
                .toList();
    }

    private McpStatelessServerFeatures.SyncToolSpecification withPlayMcpAnnotations(
            McpStatelessServerFeatures.SyncToolSpecification specification) {
        McpSchema.Tool source = specification.tool();
        McpSchema.ToolAnnotations annotations = new McpSchema.ToolAnnotations(
                toolTitle(source.name()),
                true,
                false,
                true,
                false,
                false);

        McpSchema.Tool annotatedTool = McpSchema.Tool.builder()
                .name(source.name())
                .title(toolTitle(source.name()))
                .description(source.description())
                .inputSchema(source.inputSchema())
                .outputSchema(source.outputSchema())
                .annotations(annotations)
                .meta(source.meta() == null ? Map.of() : source.meta())
                .build();

        return McpStatelessServerFeatures.SyncToolSpecification.builder()
                .tool(annotatedTool)
                .callHandler(specification.callHandler())
                .build();
    }

    private String toolTitle(String toolName) {
        return switch (toolName) {
            case "suggest_meals_from_ingredients" -> "냉장고 재료로 메뉴 추천";
            case "split_group_expenses" -> "모임 비용 정산";
            case "guide_korean_life_event" -> "한국 경조사 가이드";
            case "draft_message" -> "상황별 메시지 작성";
            default -> toolName;
        };
    }
}
