package com.soaengry.moment.wedding;

import com.soaengry.moment.wedding.entity.Gallery;
import com.soaengry.moment.wedding.repository.GalleryRepository;
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
class GalleryRepositoryTest {

    @Autowired
    private GalleryRepository galleryRepository;

    @Test
    @DisplayName("Gallery 저장 및 조회 테스트")
    void saveAndFindGallery() {
        // given
        Gallery gallery = Gallery.create(
                1L,
                "https://example.com/image1.jpg",
                "https://example.com/thumb1.jpg",
                "우리의 약혼식 사진",
                1
        );

        // when
        Gallery saved = galleryRepository.save(gallery);
        Gallery found = galleryRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getId()).isNotNull();
        assertThat(found.getWeddingId()).isEqualTo(1L);
        assertThat(found.getImageUrl()).isEqualTo("https://example.com/image1.jpg");
        assertThat(found.getThumbnailUrl()).isEqualTo("https://example.com/thumb1.jpg");
        assertThat(found.getCaption()).isEqualTo("우리의 약혼식 사진");
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Wedding ID로 갤러리 목록 조회 - orderIndex 순서대로")
    void findByWeddingIdOrderByOrderIndex() {
        // given
        galleryRepository.save(Gallery.create(1L, "url1", "thumb1", "세번째", 3));
        galleryRepository.save(Gallery.create(1L, "url2", "thumb2", "첫번째", 1));
        galleryRepository.save(Gallery.create(1L, "url3", "thumb3", "두번째", 2));

        // when
        List<Gallery> galleries = galleryRepository.findByWeddingIdOrderByOrderIndex(1L);

        // then
        assertThat(galleries).hasSize(3);
        assertThat(galleries.get(0).getCaption()).isEqualTo("첫번째");
        assertThat(galleries.get(1).getCaption()).isEqualTo("두번째");
        assertThat(galleries.get(2).getCaption()).isEqualTo("세번째");
    }

    @Test
    @DisplayName("Gallery 업데이트 테스트 - caption과 orderIndex 변경")
    void updateGallery() {
        // given
        Gallery gallery = Gallery.create(1L, "url", "thumb", "원래 설명", 1);
        Gallery saved = galleryRepository.save(gallery);

        // when
        saved.update("변경된 설명", 5);
        Gallery updated = galleryRepository.save(saved);

        // then
        assertThat(updated.getCaption()).isEqualTo("변경된 설명");
        assertThat(updated.getOrderIndex()).isEqualTo(5);
        assertThat(updated.getImageUrl()).isEqualTo("url");
        assertThat(updated.getThumbnailUrl()).isEqualTo("thumb");
    }
}