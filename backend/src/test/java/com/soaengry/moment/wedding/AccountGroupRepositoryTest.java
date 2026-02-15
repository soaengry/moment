package com.soaengry.moment.wedding;

import com.soaengry.moment.wedding.entity.AccountGroup;
import com.soaengry.moment.wedding.repository.AccountGroupRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountGroupRepositoryTest {

    @Autowired
    private AccountGroupRepository accountGroupRepository;

    @Test
    @DisplayName("AccountGroup 저장 및 조회 테스트")
    void saveAndFindAccountGroup() {
        // given
        AccountGroup group = AccountGroup.create(
                1L,
                AccountGroup.Side.GROOM,
                "신랑측",
                1
        );

        // when
        AccountGroup saved = accountGroupRepository.save(group);
        AccountGroup found = accountGroupRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getId()).isNotNull();
        assertThat(found.getWeddingId()).isEqualTo(1L);
        assertThat(found.getSide()).isEqualTo(AccountGroup.Side.GROOM);
        assertThat(found.getGroupName()).isEqualTo("신랑측");
    }

    @Test
    @DisplayName("Wedding ID로 계좌 그룹 목록 조회 - orderIndex 순서대로")
    void findByWeddingIdOrderByOrderIndex() {
        // given
        accountGroupRepository.save(AccountGroup.create(1L, AccountGroup.Side.BRIDE, "신부측", 2));
        accountGroupRepository.save(AccountGroup.create(1L, AccountGroup.Side.GROOM, "신랑측", 1));
        accountGroupRepository.save(AccountGroup.create(1L, AccountGroup.Side.BOTH, "양가", 3));

        // when
        List<AccountGroup> groups = accountGroupRepository.findByWeddingIdOrderByOrderIndex(1L);

        // then
        assertThat(groups).hasSize(3);
        assertThat(groups.get(0).getGroupName()).isEqualTo("신랑측");
        assertThat(groups.get(1).getGroupName()).isEqualTo("신부측");
        assertThat(groups.get(2).getGroupName()).isEqualTo("양가");
    }

    @Test
    @DisplayName("Wedding ID로 계좌 그룹 개수 조회")
    void countByWeddingId() {
        // given
        accountGroupRepository.save(AccountGroup.create(1L, AccountGroup.Side.GROOM, "신랑측", 1));
        accountGroupRepository.save(AccountGroup.create(1L, AccountGroup.Side.BRIDE, "신부측", 2));
        accountGroupRepository.save(AccountGroup.create(2L, AccountGroup.Side.GROOM, "다른 웨딩", 1));

        // when
        long count = accountGroupRepository.countByWeddingId(1L);

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("AccountGroup 업데이트 테스트")
    void updateAccountGroup() {
        // given
        AccountGroup group = AccountGroup.create(1L, AccountGroup.Side.GROOM, "신랑측", 1);
        AccountGroup saved = accountGroupRepository.save(group);

        // when
        saved.update(AccountGroup.Side.BOTH, "양가 공동", 5);
        AccountGroup updated = accountGroupRepository.save(saved);

        // then
        assertThat(updated.getSide()).isEqualTo(AccountGroup.Side.BOTH);
        assertThat(updated.getGroupName()).isEqualTo("양가 공동");
        assertThat(updated.getOrderIndex()).isEqualTo(5);
    }
}
