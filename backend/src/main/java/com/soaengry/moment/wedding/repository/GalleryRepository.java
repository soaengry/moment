package com.soaengry.moment.wedding.repository;

import com.soaengry.moment.wedding.entity.Gallery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GalleryRepository extends JpaRepository<Gallery, Long> {

    List<Gallery> findByWeddingIdOrderByOrderIndex(Long weddingId);
}
