package com.example.ddadang.domain.photo.service;

import com.example.ddadang.domain.photo.dto.PhotoResponse;
import com.example.ddadang.domain.photo.entity.Photo;
import com.example.ddadang.domain.photo.repository.PhotoRepository;
import com.example.ddadang.domain.photo.status.PhotoErrorStatus;
import com.example.ddadang.domain.photo.storage.PhotoStorage;
import com.example.ddadang.global.exception.GeneralException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PhotoService {

    static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;

    private static final Map<String, String> EXTENSIONS = Map.of(
        "image/jpeg", "jpg",
        "image/png", "png",
        "image/webp", "webp",
        "image/heic", "heic",
        "image/heif", "heif"
    );

    private final PhotoRepository photoRepository;
    private final PhotoStorage photoStorage;

    @Transactional
    public PhotoResponse upload(Long memberId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new GeneralException(PhotoErrorStatus.EMPTY_FILE);
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new GeneralException(PhotoErrorStatus.FILE_TOO_LARGE);
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        String extension = EXTENSIONS.get(contentType);
        if (extension == null) {
            throw new GeneralException(PhotoErrorStatus.UNSUPPORTED_TYPE);
        }

        String key = "photos/" + memberId + "/" + UUID.randomUUID() + "." + extension;
        photoStorage.upload(key, readBytes(file), contentType);
        Photo photo = photoRepository.save(new Photo(memberId, key, contentType, file.getSize()));
        return toResponse(photo);
    }

    /**
     * 기록에 첨부할 사진 ID를 검증한다. 본인이 올린 사진이 아니거나 없는 ID가 섞여 있으면 404.
     * 중복 ID는 한 번만 남기고 요청 순서를 유지한다.
     */
    @Transactional(readOnly = true)
    public List<Long> validateOwned(Long memberId, List<Long> photoIds) {
        if (photoIds == null || photoIds.isEmpty()) {
            return List.of();
        }
        List<Long> distinct = photoIds.stream().distinct().toList();
        List<Photo> photos = photoRepository.findByIdIn(distinct);
        boolean allOwned = photos.size() == distinct.size()
            && photos.stream().allMatch(photo -> photo.getMemberId().equals(memberId));
        if (!allOwned) {
            throw new GeneralException(PhotoErrorStatus.PHOTO_NOT_FOUND);
        }
        return distinct;
    }

    /**
     * 사진 ID 순서대로 조회 URL을 만든다. 없어진 사진은 건너뛴다.
     */
    @Transactional(readOnly = true)
    public List<PhotoResponse> getPhotos(List<Long> photoIds) {
        if (photoIds == null || photoIds.isEmpty()) {
            return List.of();
        }
        Map<Long, Photo> byId = photoRepository.findByIdIn(photoIds).stream()
            .collect(Collectors.toMap(Photo::getId, Function.identity()));
        return photoIds.stream()
            .map(byId::get)
            .filter(java.util.Objects::nonNull)
            .map(this::toResponse)
            .toList();
    }

    private PhotoResponse toResponse(Photo photo) {
        return new PhotoResponse(photo.getId(), photoStorage.getUrl(photo.getStorageKey()));
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new GeneralException(PhotoErrorStatus.UPLOAD_FAILED);
        }
    }
}
