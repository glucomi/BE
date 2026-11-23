package com.example.ddadang.domain.photo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.domain.record.meal.entity.Food;
import com.example.ddadang.domain.record.meal.entity.Nutrients;
import com.example.ddadang.domain.record.meal.enums.FoodCategory;
import com.example.ddadang.domain.record.meal.enums.FoodUnit;
import com.example.ddadang.domain.record.meal.repository.FoodRepository;
import com.example.ddadang.support.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

class PhotoIntegrationTest extends IntegrationTest {

    @Autowired
    private FoodRepository foodRepository;

    @BeforeEach
    void setUp() {
        given(photoStorage.getUrl(anyString())).willAnswer(invocation -> "https://s3.test/" + invocation.getArgument(0));
    }

    @Test
    void 사진을_업로드하면_S3에_저장하고_photoId와_URL을_돌려준다() throws Exception {
        Member me = createMember();

        mockMvc.perform(multipart("/api/photos").file(jpeg()).header("Authorization", bearer(me)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("COMMON_201"))
            .andExpect(jsonPath("$.result.photoId").isNumber())
            .andExpect(jsonPath("$.result.url").value(org.hamcrest.Matchers.startsWith(
                "https://s3.test/photos/" + me.getId() + "/")));

        then(photoStorage).should().upload(startsWith("photos/" + me.getId() + "/"), any(), eq("image/jpeg"));
    }

    @Test
    void 이미지가_아니면_400_PHOTO_4001() throws Exception {
        MockMultipartFile pdf = new MockMultipartFile("file", "a.pdf", "application/pdf", new byte[]{1, 2});

        mockMvc.perform(multipart("/api/photos").file(pdf).header("Authorization", bearer(createMember())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("PHOTO_4001"));
        then(photoStorage).should(never()).upload(anyString(), any(), anyString());
    }

    @Test
    void 빈_파일이면_400_PHOTO_4000_파일_파트가_없으면_400() throws Exception {
        Member me = createMember();
        MockMultipartFile empty = new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/api/photos").file(empty).header("Authorization", bearer(me)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("PHOTO_4000"));
        mockMvc.perform(multipart("/api/photos").header("Authorization", bearer(me)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("COMMON_400"));
    }

    @Test
    void 사진이_10MB를_넘으면_413_PHOTO_4130() throws Exception {
        MockMultipartFile big = new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[10 * 1024 * 1024 + 1]);

        mockMvc.perform(multipart("/api/photos").file(big).header("Authorization", bearer(createMember())))
            .andExpect(status().isContentTooLarge())
            .andExpect(jsonPath("$.code").value("PHOTO_4130"));
    }

    @Test
    void 토큰_없이_업로드하면_401() throws Exception {
        mockMvc.perform(multipart("/api/photos").file(jpeg()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void 식사기록에_사진을_첨부하고_수정시_교체된다() throws Exception {
        Member me = createMember();
        Long first = upload(me);
        Long second = upload(me);
        Food rice = foodRepository.save(Food.dbFood(
            "흑미밥", null, FoodCategory.GENERAL, new BigDecimal("210"), FoodUnit.G,
            Nutrients.builder().kcal(new BigDecimal("315")).build()
        ));
        String mealBody = """
            {"eatenAt":"2026-09-28T12:30:00","items":[{"foodId":%d,"amount":210,"unit":"G"}],"photoIds":[%s]}
            """;

        String created = mockMvc.perform(post("/api/meal-records")
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mealBody.formatted(rice.getId(), second + "," + first)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.result.photos.length()").value(2))
            .andExpect(jsonPath("$.result.photos[0].photoId").value(second))
            .andExpect(jsonPath("$.result.photos[1].photoId").value(first))
            .andReturn().getResponse().getContentAsString();
        Integer mealRecordId = JsonPath.read(created, "$.result.mealRecordId");

        mockMvc.perform(put("/api/meal-records/{id}", mealRecordId)
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mealBody.formatted(rice.getId(), String.valueOf(first))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.photos.length()").value(1))
            .andExpect(jsonPath("$.result.photos[0].photoId").value(first));

        mockMvc.perform(get("/api/meal-records/{id}", mealRecordId).header("Authorization", bearer(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.photos.length()").value(1))
            .andExpect(jsonPath("$.result.photos[0].url").isNotEmpty());
    }

    @Test
    void 다른_회원의_사진이나_없는_사진을_첨부하면_404_PHOTO_4040() throws Exception {
        Member me = createMember();
        Long othersPhoto = upload(createMember());

        mockMvc.perform(post("/api/memo-records")
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"recordedAt":"2026-10-05T21:00:00","content":"메모","photoIds":[%d]}
                    """.formatted(othersPhoto)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("PHOTO_4040"));

        mockMvc.perform(post("/api/memo-records")
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"recordedAt":"2026-10-05T21:00:00","content":"메모","photoIds":[999999]}
                    """))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("PHOTO_4040"));
    }

    @Test
    void 메모기록에_사진을_첨부하고_날짜별_조회에도_나온다() throws Exception {
        Member me = createMember();
        Long photoId = upload(me);

        mockMvc.perform(post("/api/memo-records")
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"recordedAt":"2026-10-05T21:00:00","content":"메모","photoIds":[%d]}
                    """.formatted(photoId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.result.photos[0].photoId").value(photoId));

        mockMvc.perform(get("/api/memo-records").param("date", "2026-10-05").header("Authorization", bearer(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result[0].photos[0].photoId").value(photoId));
    }

    @Test
    void 사진은_10장까지만_첨부할_수_있다() throws Exception {
        Member me = createMember();
        String ids = String.join(",", java.util.Collections.nCopies(11, "1"));

        mockMvc.perform(post("/api/memo-records")
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"recordedAt":"2026-10-05T21:00:00","content":"메모","photoIds":[%s]}
                    """.formatted(ids)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("COMMON_400"));
    }

    private Long upload(Member member) throws Exception {
        String body = mockMvc.perform(multipart("/api/photos").file(jpeg()).header("Authorization", bearer(member)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.result.photoId")).longValue();
    }

    private MockMultipartFile jpeg() {
        return new MockMultipartFile("file", "meal.jpg", "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, 1});
    }
}
