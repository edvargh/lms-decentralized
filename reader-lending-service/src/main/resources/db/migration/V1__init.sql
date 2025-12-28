CREATE TABLE `photo` (
                         `pk` bigint NOT NULL,
                         `photo_file` varchar(255) NOT NULL,
                         PRIMARY KEY (`pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `photo_seq` (
    `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `reader_details` (
                                  `pk` bigint NOT NULL,
                                  `birth_date` date NOT NULL,
                                  `gdpr_consent` bit(1) DEFAULT NULL,
                                  `marketing_consent` bit(1) DEFAULT NULL,
                                  `phone_number` varchar(255) DEFAULT NULL,
                                  `reader_number` varchar(255) DEFAULT NULL,
                                  `third_party_sharing_consent` bit(1) DEFAULT NULL,
                                  `username` varchar(255) NOT NULL,
                                  `version` bigint DEFAULT NULL,
                                  `photo_id` bigint DEFAULT NULL,
                                  PRIMARY KEY (`pk`),
                                  UNIQUE KEY `UK_887ffii388p5i2800fvv2lrdx` (`username`),
                                  UNIQUE KEY `UK_7cbncm4p1o5bxhttg848dl5dc` (`photo_id`),
                                  CONSTRAINT `FKarvajm1nx7uca9462u5cswosy` FOREIGN KEY (`photo_id`) REFERENCES `photo` (`pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `reader_details_seq` (
    `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `lending` (
                           `pk` bigint NOT NULL,
                           `commentary` varchar(1024) DEFAULT NULL,
                           `fine_value_per_day_in_cents` int NOT NULL,
                           `isbn` varchar(20) NOT NULL,
                           `lending_number` varchar(32) DEFAULT NULL,
                           `limit_date` date NOT NULL,
                           `returned_date` date DEFAULT NULL,
                           `start_date` date NOT NULL,
                           `version` bigint NOT NULL,
                           `reader_details_pk` bigint NOT NULL,
                           PRIMARY KEY (`pk`),
                           UNIQUE KEY `UKb1kindqejxfb0yx241lm29e2n` (`lending_number`),
                           KEY `FKt045xcbmbhiyrxpucs8csbrrm` (`reader_details_pk`),
                           CONSTRAINT `FKt045xcbmbhiyrxpucs8csbrrm` FOREIGN KEY (`reader_details_pk`) REFERENCES `reader_details` (`pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `lending_seq` (
    `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `fine` (
                        `pk` bigint NOT NULL,
                        `cents_value` int NOT NULL,
                        `fine_value_per_day_in_cents` int DEFAULT NULL,
                        `lending_pk` bigint NOT NULL,
                        PRIMARY KEY (`pk`),
                        UNIQUE KEY `UK_i5vn483xq5sb1ulcusavk5hnc` (`lending_pk`),
                        CONSTRAINT `FK86ehbbpfbm1rowx2ef2npe6iu` FOREIGN KEY (`lending_pk`) REFERENCES `lending` (`pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `fine_seq` (
    `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `reader_interests` (
                                    `reader_pk` bigint NOT NULL,
                                    `genre` varchar(255) NOT NULL,
                                    KEY `FK7amrj6vfnok1ffbisr8jbrn0x` (`reader_pk`),
                                    CONSTRAINT `FK7amrj6vfnok1ffbisr8jbrn0x` FOREIGN KEY (`reader_pk`) REFERENCES `reader_details` (`pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `forbidden_name` (
                                  `pk` bigint NOT NULL,
                                  `forbidden_name` varchar(255) NOT NULL,
                                  PRIMARY KEY (`pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `forbidden_name_seq` (
    `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
