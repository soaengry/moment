package com.soaengry.moment.domain.event.controller;

import com.soaengry.moment.config.TestSchemaConfig;
import com.soaengry.moment.domain.event.dto.request.EventRequest;
import com.soaengry.moment.domain.event.entity.EventType;
import com.soaengry.moment.domain.event.service.EventService;
import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestSchemaConfig.class)
class EventControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private EventService eventService;
    @Autowired private UserRepository userRepository;

    private Long ownerId;
    private String publicSlug;
    private String privateSlug;

    @BeforeEach
    void setUp() {
        User owner = userRepository.save(User.builder()
                .email("ctrl_owner_" + System.nanoTime() + "@test.com")
                .nickname("ctrl_owner_" + System.nanoTime())
                .isEmailVerified(true)
                .build());
        ownerId = owner.getId();

        publicSlug = "public-event-" + System.nanoTime();
        privateSlug = "private-event-" + System.nanoTime();

        // 공개 이벤트 생성 (location 없음 → geocoding 불필요)
        eventService.createEvent(ownerId, new EventRequest(
                "공개 모임", publicSlug, EventType.GATHERING,
                LocalDateTime.of(2026, 6, 1, 18, 0),
                null, null, null,
                true, null, null, null, null
        ));

        // 비공개 이벤트 생성
        eventService.createEvent(ownerId, new EventRequest(
                "비공개 모임", privateSlug, EventType.GATHERING,
                LocalDateTime.of(2026, 7, 1, 18, 0),
                null, null, null,
                false, null, null, null, null
        ));
    }

    @Test
    @DisplayName("GET /api/events/{slug} - 비인증 사용자가 공개 이벤트를 조회하면 200을 반환한다 (ClassCastException 없음)")
    void getEventInfo_anonymous_publicEvent_returns200() throws Exception {
        // given: Authorization 헤더 없음 (AnonymousAuthenticationToken 주입)

        // when & then
        mockMvc.perform(get("/api/events/" + publicSlug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.event.slug").value(publicSlug))
                .andExpect(jsonPath("$.data.event.isPublic").value(true));
    }

    @Test
    @DisplayName("GET /api/events/{slug} - 비인증 사용자가 비공개 이벤트를 조회하면 403을 반환한다")
    void getEventInfo_anonymous_privateEvent_returns403() throws Exception {
        // given: Authorization 헤더 없음

        // when & then: 서비스 레이어에서 EventException(EVENT_UNAUTHORIZED) 발생 → 403 FORBIDDEN
        mockMvc.perform(get("/api/events/" + privateSlug))
                .andExpect(status().isForbidden());
    }
}
