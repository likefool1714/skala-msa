-- MediWaste Hub 의료폐기물 수거·관리 B2B 플랫폼 초기 DDL
-- Spring JPA ddl-auto: update 로도 생성되지만
-- 명시적 DDL로 테이블 선후 관계를 문서화

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    email       VARCHAR(255)    NOT NULL UNIQUE,
    password    VARCHAR(255)    NOT NULL,
    name        VARCHAR(100)    NOT NULL,
    role        VARCHAR(20)     NOT NULL COMMENT 'STUDENT(배출 사업장) | INSTRUCTOR(수집·운반 업체)',
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 수집·운반 업체가 수거 서비스 등록
CREATE TABLE IF NOT EXISTS collection_services (
    id               BIGINT          NOT NULL AUTO_INCREMENT,
    name             VARCHAR(255)    NOT NULL,
    description      TEXT,
    waste_type       VARCHAR(50)     NOT NULL COMMENT 'GENERAL_MEDICAL | SHARPS | PATHOLOGICAL',
    price            DECIMAL(10,2)   NOT NULL,
    carrier_id       BIGINT          NOT NULL,
    request_count    INT             NOT NULL DEFAULT 0,
    status           VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE | INACTIVE',
    created_at       DATETIME(6),
    updated_at       DATETIME(6),
    PRIMARY KEY (id),
    FOREIGN KEY (carrier_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 배출 사업장이 희망 수거일과 폐기물 정보를 입력해 수거 신청
CREATE TABLE IF NOT EXISTS collection_requests (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    generator_id              BIGINT       NOT NULL,
    collection_service_id     BIGINT       NOT NULL,
    preferred_collection_date DATE         NOT NULL,
    preferred_start_time      TIME         NOT NULL,
    preferred_end_time        TIME         NOT NULL,
    waste_information         VARCHAR(500) NOT NULL,
    collection_requirements   VARCHAR(1000),
    status                    VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING | CONFIRMED | ACCEPTED | COMPLETED | REJECTED | CANCELLED',
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_collection_request_slot (generator_id, collection_service_id, preferred_collection_date, preferred_start_time),
    FOREIGN KEY (generator_id) REFERENCES users(id),
    FOREIGN KEY (collection_service_id) REFERENCES collection_services(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 수거 신청 건에 대한 수거 비용 결제
CREATE TABLE IF NOT EXISTS payments (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    collection_request_id BIGINT    NOT NULL UNIQUE,
    generator_id          BIGINT    NOT NULL,
    collection_service_id BIGINT    NOT NULL,
    amount          DECIMAL(10,2)   NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING | COMPLETED | FAILED | CANCELLED',
    transaction_id  VARCHAR(255)    UNIQUE,
    created_at      DATETIME(6),
    updated_at      DATETIME(6),
    PRIMARY KEY (id),
    FOREIGN KEY (collection_request_id) REFERENCES collection_requests(id),
    FOREIGN KEY (generator_id) REFERENCES users(id),
    FOREIGN KEY (collection_service_id) REFERENCES collection_services(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
