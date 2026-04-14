package com.soaengry.moment.wedding.repository;

import com.soaengry.moment.config.TestSchemaConfig;
import com.soaengry.moment.domain.wedding.entity.Gallery;
import com.soaengry.moment.domain.wedding.repository.GalleryRepository;
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
                "https://example.com/thumb1.jpg"
        );

        // when
        Gallery saved = galleryRepository.save(gallery);
        Gallery found = galleryRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getId()).isNotNull();
        assertThat(found.getWeddingId()).isEqualTo(1L);
        assertThat(found.getImageUrl()).isEqualTo("https://example.com/image1.jpg");
        assertThat(found.getThumbnailUrl()).isEqualTo("https://example.com/thumb1.jpg");
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Wedding ID로 갤러리 목록 조회")
    void findByWeddingId() {
        // given
        galleryRepository.save(Gallery.create(1L, "url1", "thumb1"));
        galleryRepository.save(Gallery.create(1L, "url2", "thumb2"));
        galleryRepository.save(Gallery.create(1L, "url3", "thumb3"));

        // when
        List<Gallery> galleries = galleryRepository.findByWeddingId(1L);

        // then
        assertThat(galleries).hasSize(3);
        assertThat(galleries).allMatch(g -> g.getWeddingId().equals(1L));
    }

    @Test
    @DisplayName("Gallery 업데이트 테스트 - imageUrl과 thumbnailUrl 변경")
    void updateGallery() {
        // given
        Gallery gallery = Gallery.create(1L, "url", "thumb");
        Gallery saved = galleryRepository.save(gallery);

        // when
        saved.update("new-url", "new-thumb");
        Gallery updated = galleryRepository.save(saved);

        // then
        assertThat(updated.getImageUrl()).isEqualTo("new-url");
        assertThat(updated.getThumbnailUrl()).isEqualTo("new-thumb");
    }
}
