package com.soaengry.moment.domain.wedding.repository;

import com.soaengry.moment.config.TestSchemaConfig;
import com.soaengry.moment.domain.event.entity.AccountGroup;
import com.soaengry.moment.domain.event.repository.AccountGroupRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestSchemaConfig.class)
class AccountGroupRepositoryTest {

    @Autowired
    private AccountGroupRepository accountGroupRepository;

    @Test
    @DisplayName("AccountGroup 저장 및 조회 테스트")
    void saveAndFindAccountGroup() {
        // given
        AccountGroup group = AccountGroup.create(1L, "신랑측", 1);

        // when
        AccountGroup saved = accountGroupRepository.save(group);
        AccountGroup found = accountGroupRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getId()).isNotNull();
        assertThat(found.getEventId()).isEqualTo(1L);
        assertThat(found.getGroupName()).isEqualTo("신랑측");
    }

    @Test
    @DisplayName("Event ID로 계좌 그룹 목록 조회 - orderIndex 순서대로")
    void findByEventIdOrderByOrderIndex() {
        // given
        accountGroupRepository.save(AccountGroup.create(1L, "신부측", 2));
        accountGroupRepository.save(AccountGroup.create(1L, "신랑측", 1));
        accountGroupRepository.save(AccountGroup.create(1L, "신랑 가족", 3));

        // when
        List<AccountGroup> groups = accountGroupRepository.findByEventIdOrderByOrderIndex(1L);

        // then
        assertThat(groups).hasSize(3);
        assertThat(groups.get(0).getGroupName()).isEqualTo("신랑측");
        assertThat(groups.get(1).getGroupName()).isEqualTo("신부측");
        assertThat(groups.get(2).getGroupName()).isEqualTo("신랑 가족");
    }

    @Test
    @DisplayName("AccountGroup 업데이트 테스트")
    void updateAccountGroup() {
        // given
        AccountGroup group = AccountGroup.create(1L, "신랑측", 1);
        AccountGroup saved = accountGroupRepository.save(group);

        // when
        saved.update("신부 가족", 5);
        AccountGroup updated = accountGroupRepository.save(saved);

        // then
        assertThat(updated.getGroupName()).isEqualTo("신부 가족");
        assertThat(updated.getOrderIndex()).isEqualTo(5);
    }
}
