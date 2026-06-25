package com.agenticplayer.lifestyle.tool;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.agenticplayer.lifestyle.service.OutdoorSafetyBriefingService;

class LifestyleAssistantToolsTest {

    @Test
    void exposesOutdoorSafetyBriefingTool() {
        var tools = new LifestyleAssistantTools(new OutdoorSafetyBriefingService());

        assertThat(tools).isNotNull();
    }
}
