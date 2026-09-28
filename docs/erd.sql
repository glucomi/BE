-- DDADANG ERD v0.1 (MySQL)
-- ERDCloud: 우측 상단 Import > MySQL 선택 후 이 파일 내용 붙여넣기

CREATE TABLE `member` (
    `id`                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '회원 ID',
    `provider`              VARCHAR(20)  NOT NULL COMMENT '소셜 제공자(KAKAO)',
    `provider_id`           VARCHAR(100) NOT NULL COMMENT '소셜 회원 식별자',
    `name`                  VARCHAR(50)  NULL COMMENT '이름',
    `birth_date`            DATE         NULL COMMENT '생년월일',
    `gender`                VARCHAR(10)  NULL COMMENT '성별(MALE/FEMALE)',
    `height_cm`             DECIMAL(5,1) NULL COMMENT '키(cm)',
    `diabetes_type`         VARCHAR(30)  NULL COMMENT '당뇨 유형(TYPE1/TYPE2_INSULIN/TYPE2_NO_INSULIN/PRE/GESTATIONAL/NONE)',
    `target_glucose_min`    INT          NULL COMMENT '목표 혈당 하한(mg/dL)',
    `target_glucose_max`    INT          NULL COMMENT '목표 혈당 상한(mg/dL)',
    `signup_completed`      BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '약관 동의(회원가입) 완료 여부',
    `onboarding_completed`  BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '큐레이션 온보딩 완료 여부',
    `push_glucose_enabled`  BOOLEAN      NOT NULL DEFAULT TRUE COMMENT '혈당 관련 푸시 수신 여부',
    `marketing_agreed`      BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '마케팅 수신 동의 여부',
    `status`                VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT '회원 상태(ACTIVE/WITHDRAWN)',
    `deleted_at`            DATETIME     NULL COMMENT '탈퇴 일시',
    `created_at`            DATETIME     NOT NULL COMMENT '생성 일시',
    `updated_at`            DATETIME     NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_member_provider` (`provider`, `provider_id`)
) COMMENT '회원';

CREATE TABLE `member_agreement` (
    `id`             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '약관 동의 ID',
    `member_id`      BIGINT      NOT NULL COMMENT '회원 ID',
    `terms_type`     VARCHAR(30) NOT NULL COMMENT '약관 종류(SERVICE/PRIVACY/...)',
    `terms_version`  VARCHAR(20) NOT NULL COMMENT '약관 버전',
    `agreed`         BOOLEAN     NOT NULL COMMENT '동의 여부',
    `agreed_at`      DATETIME    NOT NULL COMMENT '동의 일시',
    `created_at`     DATETIME    NOT NULL COMMENT '생성 일시',
    `updated_at`     DATETIME    NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (`id`)
) COMMENT '약관 동의';

CREATE TABLE `member_withdrawal` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '탈퇴 ID',
    `member_id`   BIGINT       NOT NULL COMMENT '회원 ID',
    `reason`      VARCHAR(50)  NOT NULL COMMENT '탈퇴 사유',
    `detail`      VARCHAR(500) NULL COMMENT '상세 사유',
    `created_at`  DATETIME     NOT NULL COMMENT '생성 일시',
    `updated_at`  DATETIME     NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (`id`)
) COMMENT '회원 탈퇴';

CREATE TABLE `refresh_token` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'refresh token ID',
    `member_id`   BIGINT       NOT NULL COMMENT '회원 ID',
    `token`       VARCHAR(500) NOT NULL COMMENT 'refresh token',
    `expires_at`  DATETIME     NOT NULL COMMENT '만료 일시',
    `created_at`  DATETIME     NOT NULL COMMENT '생성 일시',
    `updated_at`  DATETIME     NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refresh_token_member` (`member_id`)
) COMMENT 'refresh token';

CREATE TABLE `cgm_connection` (
    `id`                 BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'CGM 연결 ID',
    `member_id`          BIGINT        NOT NULL COMMENT '회원 ID',
    `provider`           VARCHAR(20)   NOT NULL COMMENT 'CGM 제공사(CARESENS_AIR/LIBRE)',
    `external_user_id`   VARCHAR(100)  NOT NULL COMMENT '제공사 회원 식별자',
    `access_token`       VARCHAR(2000) NULL COMMENT '액세스 토큰',
    `refresh_token`      VARCHAR(2000) NULL COMMENT '리프레시 토큰',
    `token_expires_at`   DATETIME      NULL COMMENT '토큰 만료 일시',
    `sensor_serial`      VARCHAR(100)  NULL COMMENT '센서 시리얼 번호',
    `sensor_started_at`  DATETIME      NULL COMMENT '센서 부착 일시(N일차 계산)',
    `status`             VARCHAR(20)   NOT NULL COMMENT '연결 상태(CONNECTED/DISCONNECTED)',
    `created_at`         DATETIME      NOT NULL COMMENT '생성 일시',
    `updated_at`         DATETIME      NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cgm_connection_provider_user` (`provider`, `external_user_id`)
) COMMENT 'CGM 연결';

CREATE TABLE `cgm_reading` (
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'CGM 혈당 ID',
    `member_id`          BIGINT       NOT NULL COMMENT '회원 ID',
    `cgm_connection_id`  BIGINT       NOT NULL COMMENT 'CGM 연결 ID',
    `serial_number`      VARCHAR(100) NOT NULL COMMENT '센서 시리얼 번호',
    `seq_number`         BIGINT       NOT NULL COMMENT '측정 순번',
    `event_at`           DATETIME     NOT NULL COMMENT '측정 일시',
    `tz_offset`          INT          NULL COMMENT '타임존 오프셋',
    `stage`              INT          NULL COMMENT '스무딩 단계(1=진행중, 2=확정)',
    `initial_value`      DOUBLE       NULL COMMENT '최초 측정값',
    `value`              DOUBLE       NULL COMMENT '혈당값(mg/dL)',
    `trend_rate`         DOUBLE       NULL COMMENT '변화율',
    `trend`              INT          NULL COMMENT '추세(0~7)',
    `error_code`         INT          NULL COMMENT '에러 코드',
    `min_max_flag`       INT          NULL COMMENT '측정 범위 플래그(0=정상, 1=40미만, 2=500초과)',
    `created_at`         DATETIME     NOT NULL COMMENT '생성 일시',
    `updated_at`         DATETIME     NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cgm_reading_conn_serial_seq` (`cgm_connection_id`, `serial_number`, `seq_number`),
    KEY `idx_cgm_reading_member_event_at` (`member_id`, `event_at`)
) COMMENT 'CGM 혈당';

CREATE TABLE `daily_glucose_score` (
    `id`               BIGINT      NOT NULL AUTO_INCREMENT COMMENT '날짜별 혈당 점수 ID',
    `member_id`        BIGINT      NOT NULL COMMENT '회원 ID',
    `score_date`       DATE        NOT NULL COMMENT '점수 날짜(KST)',
    `glucose_group`    VARCHAR(20) NOT NULL COMMENT '점수 그룹(NON_DM/PRE_DM/DM/GDM)',
    `status`           VARCHAR(20) NOT NULL COMMENT '상태(FINAL/UNAVAILABLE)',
    `score`            DOUBLE      NULL COMMENT '혈당 점수(반올림 전)',
    `average_glucose`  DOUBLE      NULL COMMENT '시간가중 평균 혈당(반올림 전)',
    `spike_count`      INT         NULL COMMENT '스파이크 횟수',
    `tir_deduction`    DOUBLE      NULL COMMENT 'TIR 감점',
    `mean_deduction`   DOUBLE      NULL COMMENT '평균혈당 감점',
    `cv_deduction`     DOUBLE      NULL COMMENT 'CV 감점',
    `spike_deduction`  DOUBLE      NULL COMMENT '스파이크 감점',
    `hypo_deduction`   DOUBLE      NULL COMMENT '저혈당 페널티',
    `finalized_at`     DATETIME    NOT NULL COMMENT '확정 일시',
    `created_at`       DATETIME    NOT NULL COMMENT '생성 일시',
    `updated_at`       DATETIME    NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_daily_glucose_score_member_date` (`member_id`, `score_date`)
) COMMENT '날짜별 혈당 점수(확정)';

CREATE TABLE `food` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '음식 ID',
    `source`          VARCHAR(20)   NOT NULL COMMENT '출처(DB/CUSTOM)',
    `member_id`       BIGINT        NULL COMMENT '등록 회원 ID(CUSTOM일 때)',
    `name`            VARCHAR(100)  NOT NULL COMMENT '음식명',
    `brand`           VARCHAR(100)  NULL COMMENT '브랜드명',
    `category`        VARCHAR(30)   NULL COMMENT '분류(GENERAL=일반 식품, PROCESSED=가공 식품)',
    `serving_amount`  DECIMAL(8,2)  NOT NULL COMMENT '기준 제공량',
    `serving_unit`    VARCHAR(10)   NOT NULL COMMENT '제공량 단위(G/ML)',
    `kcal`            DECIMAL(8,2)  NOT NULL COMMENT '열량(kcal)',
    `carbohydrate_g`  DECIMAL(8,2)  NULL COMMENT '탄수화물(g)',
    `sugars_g`        DECIMAL(8,2)  NULL COMMENT '당류(g)',
    `dietary_fiber_g` DECIMAL(8,2)  NULL COMMENT '식이섬유(g)',
    `protein_g`       DECIMAL(8,2)  NULL COMMENT '단백질(g)',
    `fat_g`           DECIMAL(8,2)  NULL COMMENT '지방(g)',
    `saturated_fat_g` DECIMAL(8,2)  NULL COMMENT '포화지방(g)',
    `trans_fat_g`     DECIMAL(8,2)  NULL COMMENT '트랜스지방(g)',
    `fatty_acid_g`    DECIMAL(8,2)  NULL COMMENT '지방산(g)',
    `unsaturated_fat_g` DECIMAL(8,2) NULL COMMENT '불포화지방산(g)',
    `cholesterol_mg`  DECIMAL(8,2)  NULL COMMENT '콜레스테롤(mg)',
    `sodium_mg`       DECIMAL(8,2)  NULL COMMENT '나트륨(mg)',
    `caffeine_mg`     DECIMAL(8,2)  NULL COMMENT '카페인(mg)',
    `glucose_grade`   VARCHAR(10)   NULL COMMENT '예상 혈당 변화 등급(A_PLUS/A/B_PLUS/B/C/F)',
    `created_at`      DATETIME      NOT NULL COMMENT '생성 일시',
    `updated_at`      DATETIME      NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (`id`)
) COMMENT '음식';

CREATE TABLE `favorite_food` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '즐겨찾기 ID',
    `member_id`   BIGINT   NOT NULL COMMENT '회원 ID',
    `food_id`     BIGINT   NOT NULL COMMENT '음식 ID',
    `created_at`  DATETIME NOT NULL COMMENT '생성 일시',
    `updated_at`  DATETIME NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_favorite_food_member_food` (`member_id`, `food_id`)
) COMMENT '음식 즐겨찾기';

CREATE TABLE `meal_record` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '식사 기록 ID',
    `member_id`   BIGINT        NOT NULL COMMENT '회원 ID',
    `eaten_at`    DATETIME      NOT NULL COMMENT '식사 일시',
    `memo`        VARCHAR(1000) NULL COMMENT '메모',
    `created_at`  DATETIME      NOT NULL COMMENT '생성 일시',
    `updated_at`  DATETIME      NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (`id`),
    KEY `idx_meal_record_member_eaten_at` (`member_id`, `eaten_at`)
) COMMENT '식사 기록';

CREATE TABLE `meal_record_item` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '식사 메뉴 ID',
    `meal_record_id`  BIGINT       NOT NULL COMMENT '식사 기록 ID',
    `food_id`         BIGINT       NOT NULL COMMENT '음식 ID',
    `amount`          DECIMAL(8,2) NOT NULL COMMENT '섭취량',
    `unit`            VARCHAR(10)  NOT NULL COMMENT '섭취 단위(G/ML/SERVING)',
    `kcal`            DECIMAL(8,2) NOT NULL COMMENT '열량 스냅샷(kcal)',
    `carbohydrate_g`  DECIMAL(8,2) NULL COMMENT '탄수화물 스냅샷(g)',
    `sugars_g`        DECIMAL(8,2) NULL COMMENT '당류 스냅샷(g)',
    `dietary_fiber_g` DECIMAL(8,2) NULL COMMENT '식이섬유 스냅샷(g)',
    `protein_g`       DECIMAL(8,2) NULL COMMENT '단백질 스냅샷(g)',
    `fat_g`           DECIMAL(8,2) NULL COMMENT '지방 스냅샷(g)',
    `saturated_fat_g` DECIMAL(8,2) NULL COMMENT '포화지방 스냅샷(g)',
    `trans_fat_g`     DECIMAL(8,2) NULL COMMENT '트랜스지방 스냅샷(g)',
    `fatty_acid_g`    DECIMAL(8,2) NULL COMMENT '지방산 스냅샷(g)',
    `unsaturated_fat_g` DECIMAL(8,2) NULL COMMENT '불포화지방산 스냅샷(g)',
    `cholesterol_mg`  DECIMAL(8,2) NULL COMMENT '콜레스테롤 스냅샷(mg)',
    `sodium_mg`       DECIMAL(8,2) NULL COMMENT '나트륨 스냅샷(mg)',
    `caffeine_mg`     DECIMAL(8,2) NULL COMMENT '카페인 스냅샷(mg)',
    `created_at`      DATETIME     NOT NULL COMMENT '생성 일시',
    `updated_at`      DATETIME     NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (`id`)
) COMMENT '식사 메뉴';

CREATE TABLE `meal_record_photo` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '식사 사진 ID',
    `meal_record_id`  BIGINT       NOT NULL COMMENT '식사 기록 ID',
    `image_url`       VARCHAR(500) NOT NULL COMMENT '이미지 URL',
    `sort_order`      INT          NOT NULL COMMENT '정렬 순서(최대 10장)',
    `created_at`      DATETIME     NOT NULL COMMENT '생성 일시',
    `updated_at`      DATETIME     NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (`id`)
) COMMENT '식사 사진';

CREATE TABLE `glucose_record` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '혈당 기록 ID',
    `member_id`    BIGINT        NOT NULL COMMENT '회원 ID',
    `measured_at`  DATETIME      NOT NULL COMMENT '측정 일시',
    `value_mg_dl`  INT           NOT NULL COMMENT '혈당값(mg/dL)',
    `memo`         VARCHAR(1000) NULL COMMENT '메모',
    `created_at`   DATETIME      NOT NULL COMMENT '생성 일시',
    `updated_at`   DATETIME      NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (`id`)
) COMMENT '혈당 수기 기록';

CREATE TABLE `weight_record` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '체중 기록 ID',
    `member_id`    BIGINT       NOT NULL COMMENT '회원 ID',
    `measured_at`  DATETIME     NOT NULL COMMENT '측정 일시',
    `weight_kg`    DECIMAL(5,1) NOT NULL COMMENT '체중(kg)',
    `created_at`   DATETIME     NOT NULL COMMENT '생성 일시',
    `updated_at`   DATETIME     NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (`id`)
) COMMENT '체중 기록';

CREATE TABLE `memo_record` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '메모 기록 ID',
    `member_id`    BIGINT        NOT NULL COMMENT '회원 ID',
    `recorded_at`  DATETIME      NOT NULL COMMENT '기록 일시',
    `content`      VARCHAR(1000) NOT NULL COMMENT '내용',
    `created_at`   DATETIME      NOT NULL COMMENT '생성 일시',
    `updated_at`   DATETIME      NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (`id`)
) COMMENT '메모 기록';

CREATE TABLE `memo_record_photo` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '메모 사진 ID',
    `memo_record_id`  BIGINT       NOT NULL COMMENT '메모 기록 ID',
    `image_url`       VARCHAR(500) NOT NULL COMMENT '이미지 URL',
    `sort_order`      INT          NOT NULL COMMENT '정렬 순서',
    `created_at`      DATETIME     NOT NULL COMMENT '생성 일시',
    `updated_at`      DATETIME     NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (`id`)
) COMMENT '메모 사진';

CREATE TABLE `insulin_product` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '인슐린 제품 ID',
    `name`         VARCHAR(100) NOT NULL COMMENT '제품명',
    `action_type`  VARCHAR(20)  NOT NULL COMMENT '작용 유형(RAPID/SHORT/INTERMEDIATE/LONG/MIXED)',
    `created_at`   DATETIME     NOT NULL COMMENT '생성 일시',
    `updated_at`   DATETIME     NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (`id`)
) COMMENT '인슐린 제품';

CREATE TABLE `member_insulin` (
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '내 인슐린 ID',
    `member_id`           BIGINT       NOT NULL COMMENT '회원 ID',
    `insulin_product_id`  BIGINT       NOT NULL COMMENT '인슐린 제품 ID',
    `default_dose_unit`   DECIMAL(5,1) NOT NULL COMMENT '기본 투여량(U)',
    `deleted_at`          DATETIME     NULL COMMENT '삭제 일시',
    `created_at`          DATETIME     NOT NULL COMMENT '생성 일시',
    `updated_at`          DATETIME     NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (`id`)
) COMMENT '내 인슐린';

CREATE TABLE `insulin_record` (
    `id`                 BIGINT        NOT NULL AUTO_INCREMENT COMMENT '인슐린 기록 ID',
    `member_id`          BIGINT        NOT NULL COMMENT '회원 ID',
    `member_insulin_id`  BIGINT        NOT NULL COMMENT '내 인슐린 ID',
    `injected_at`        DATETIME      NOT NULL COMMENT '투여 일시',
    `dose_unit`          DECIMAL(5,1)  NOT NULL COMMENT '투여량(U)',
    `memo`               VARCHAR(1000) NULL COMMENT '메모',
    `created_at`         DATETIME      NOT NULL COMMENT '생성 일시',
    `updated_at`         DATETIME      NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (`id`)
) COMMENT '인슐린 기록';

CREATE TABLE `member_medication` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '내 복용약 ID',
    `member_id`     BIGINT       NOT NULL COMMENT '회원 ID',
    `category`      VARCHAR(30)  NOT NULL COMMENT '약 종류(DIABETES/HYPERLIPIDEMIA/HYPERTENSION/SUPPLEMENT/ETC)',
    `product_name`  VARCHAR(100) NULL COMMENT '제품명(선택)',
    `deleted_at`    DATETIME     NULL COMMENT '삭제 일시',
    `created_at`    DATETIME     NOT NULL COMMENT '생성 일시',
    `updated_at`    DATETIME     NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (`id`)
) COMMENT '내 복용약';

CREATE TABLE `medication_record` (
    `id`                    BIGINT        NOT NULL AUTO_INCREMENT COMMENT '복약 기록 ID',
    `member_id`             BIGINT        NOT NULL COMMENT '회원 ID',
    `member_medication_id`  BIGINT        NOT NULL COMMENT '내 복용약 ID',
    `taken_at`              DATETIME      NOT NULL COMMENT '복용 일시',
    `memo`                  VARCHAR(1000) NULL COMMENT '메모',
    `created_at`            DATETIME      NOT NULL COMMENT '생성 일시',
    `updated_at`            DATETIME      NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (`id`)
) COMMENT '복약 기록';

CREATE TABLE `exercise` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '운동 ID',
    `name`        VARCHAR(100) NOT NULL COMMENT '운동명',
    `met`         DECIMAL(4,1) NOT NULL COMMENT 'MET(kcal 계산용)',
    `popular`     BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '인기 운동 여부',
    `created_at`  DATETIME     NOT NULL COMMENT '생성 일시',
    `updated_at`  DATETIME     NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (`id`)
) COMMENT '운동';

CREATE TABLE `exercise_record` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '운동 기록 ID',
    `member_id`     BIGINT        NOT NULL COMMENT '회원 ID',
    `exercise_id`   BIGINT        NOT NULL COMMENT '운동 ID',
    `performed_at`  DATETIME      NOT NULL COMMENT '운동 일시',
    `duration_min`  INT           NOT NULL COMMENT '운동 시간(분)',
    `kcal`          DECIMAL(8,2)  NULL COMMENT '소모 열량(kcal)',
    `memo`          VARCHAR(1000) NULL COMMENT '메모',
    `created_at`    DATETIME      NOT NULL COMMENT '생성 일시',
    `updated_at`    DATETIME      NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (`id`)
) COMMENT '운동 기록';

-- Foreign Keys
ALTER TABLE `member_agreement`   ADD CONSTRAINT `fk_member_agreement_member`   FOREIGN KEY (`member_id`) REFERENCES `member` (`id`);
ALTER TABLE `member_withdrawal`  ADD CONSTRAINT `fk_member_withdrawal_member`  FOREIGN KEY (`member_id`) REFERENCES `member` (`id`);
ALTER TABLE `refresh_token`      ADD CONSTRAINT `fk_refresh_token_member`      FOREIGN KEY (`member_id`) REFERENCES `member` (`id`);
ALTER TABLE `cgm_connection`     ADD CONSTRAINT `fk_cgm_connection_member`     FOREIGN KEY (`member_id`) REFERENCES `member` (`id`);
ALTER TABLE `cgm_reading`        ADD CONSTRAINT `fk_cgm_reading_member`        FOREIGN KEY (`member_id`) REFERENCES `member` (`id`);
ALTER TABLE `cgm_reading`        ADD CONSTRAINT `fk_cgm_reading_connection`    FOREIGN KEY (`cgm_connection_id`) REFERENCES `cgm_connection` (`id`);
ALTER TABLE `daily_glucose_score` ADD CONSTRAINT `fk_daily_glucose_score_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`);
ALTER TABLE `food`               ADD CONSTRAINT `fk_food_member`               FOREIGN KEY (`member_id`) REFERENCES `member` (`id`);
ALTER TABLE `favorite_food`      ADD CONSTRAINT `fk_favorite_food_member`      FOREIGN KEY (`member_id`) REFERENCES `member` (`id`);
ALTER TABLE `favorite_food`      ADD CONSTRAINT `fk_favorite_food_food`        FOREIGN KEY (`food_id`) REFERENCES `food` (`id`);
ALTER TABLE `meal_record`        ADD CONSTRAINT `fk_meal_record_member`        FOREIGN KEY (`member_id`) REFERENCES `member` (`id`);
ALTER TABLE `meal_record_item`   ADD CONSTRAINT `fk_meal_record_item_record`   FOREIGN KEY (`meal_record_id`) REFERENCES `meal_record` (`id`);
ALTER TABLE `meal_record_item`   ADD CONSTRAINT `fk_meal_record_item_food`     FOREIGN KEY (`food_id`) REFERENCES `food` (`id`);
ALTER TABLE `meal_record_photo`  ADD CONSTRAINT `fk_meal_record_photo_record`  FOREIGN KEY (`meal_record_id`) REFERENCES `meal_record` (`id`);
ALTER TABLE `glucose_record`     ADD CONSTRAINT `fk_glucose_record_member`     FOREIGN KEY (`member_id`) REFERENCES `member` (`id`);
ALTER TABLE `weight_record`      ADD CONSTRAINT `fk_weight_record_member`      FOREIGN KEY (`member_id`) REFERENCES `member` (`id`);
ALTER TABLE `memo_record`        ADD CONSTRAINT `fk_memo_record_member`        FOREIGN KEY (`member_id`) REFERENCES `member` (`id`);
ALTER TABLE `memo_record_photo`  ADD CONSTRAINT `fk_memo_record_photo_record`  FOREIGN KEY (`memo_record_id`) REFERENCES `memo_record` (`id`);
ALTER TABLE `member_insulin`     ADD CONSTRAINT `fk_member_insulin_member`     FOREIGN KEY (`member_id`) REFERENCES `member` (`id`);
ALTER TABLE `member_insulin`     ADD CONSTRAINT `fk_member_insulin_product`    FOREIGN KEY (`insulin_product_id`) REFERENCES `insulin_product` (`id`);
ALTER TABLE `insulin_record`     ADD CONSTRAINT `fk_insulin_record_member`     FOREIGN KEY (`member_id`) REFERENCES `member` (`id`);
ALTER TABLE `insulin_record`     ADD CONSTRAINT `fk_insulin_record_my_insulin` FOREIGN KEY (`member_insulin_id`) REFERENCES `member_insulin` (`id`);
ALTER TABLE `member_medication`  ADD CONSTRAINT `fk_member_medication_member`  FOREIGN KEY (`member_id`) REFERENCES `member` (`id`);
ALTER TABLE `medication_record`  ADD CONSTRAINT `fk_medication_record_member`  FOREIGN KEY (`member_id`) REFERENCES `member` (`id`);
ALTER TABLE `medication_record`  ADD CONSTRAINT `fk_medication_record_my_drug` FOREIGN KEY (`member_medication_id`) REFERENCES `member_medication` (`id`);
ALTER TABLE `exercise_record`    ADD CONSTRAINT `fk_exercise_record_member`    FOREIGN KEY (`member_id`) REFERENCES `member` (`id`);
ALTER TABLE `exercise_record`    ADD CONSTRAINT `fk_exercise_record_exercise`  FOREIGN KEY (`exercise_id`) REFERENCES `exercise` (`id`);
