package com.example.ddadang.domain.photo.repository;

import com.example.ddadang.domain.photo.entity.Photo;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoRepository extends JpaRepository<Photo, Long> {

    List<Photo> findByIdIn(Collection<Long> ids);
}
