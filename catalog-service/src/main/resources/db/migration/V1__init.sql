CREATE TABLE `photo` (
                         `pk` bigint NOT NULL,
                         `photo_file` varchar(255) NOT NULL,
                         PRIMARY KEY (`pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `photo_seq` (
    `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `author` (
                          `author_number` bigint NOT NULL,
                          `bio` varchar(4096) NOT NULL,
                          `name` varchar(150) DEFAULT NULL,
                          `version` bigint NOT NULL,
                          `photo_id` bigint DEFAULT NULL,
                          PRIMARY KEY (`author_number`),
                          UNIQUE KEY `UK_8ohfehogmsk0lvd215had37h1` (`photo_id`),
                          CONSTRAINT `FKlcc5qir879nsu8vjhfflfl3vm` FOREIGN KEY (`photo_id`) REFERENCES `photo` (`pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `author_seq` (
    `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `genre` (
                         `pk` bigint NOT NULL,
                         `genre` varchar(100) NOT NULL,
                         PRIMARY KEY (`pk`),
                         UNIQUE KEY `UK_89b48lwadspmkos0rx7hidlty` (`genre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `genre_seq` (
    `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `book` (
                        `pk` bigint NOT NULL,
                        `description` varchar(4096) DEFAULT NULL,
                        `isbn` varchar(16) DEFAULT NULL,
                        `title` varchar(128) DEFAULT NULL,
                        `version` bigint DEFAULT NULL,
                        `photo_id` bigint DEFAULT NULL,
                        `genre_pk` bigint NOT NULL,
                        PRIMARY KEY (`pk`),
                        UNIQUE KEY `uc_book_isbn` (`isbn`),
                        UNIQUE KEY `UK_iht4edf5qjsosvs5wh329vsis` (`photo_id`),
                        KEY `FKb2b9ofh06sbwdxvvhepcec7` (`genre_pk`),
                        CONSTRAINT `FKb2b9ofh06sbwdxvvhepcec7` FOREIGN KEY (`genre_pk`) REFERENCES `genre` (`pk`),
                        CONSTRAINT `FKc4kkvusmioiqpnhuhic8wsjsv` FOREIGN KEY (`photo_id`) REFERENCES `photo` (`pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `book_seq` (
    `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `book_authors` (
                                `book_pk` bigint NOT NULL,
                                `authors_author_number` bigint NOT NULL,
                                KEY `FKkhnivov03734cv65f093rbnqk` (`authors_author_number`),
                                KEY `FKey9jk2is5wfykignfdwiqcnh3` (`book_pk`),
                                CONSTRAINT `FKey9jk2is5wfykignfdwiqcnh3` FOREIGN KEY (`book_pk`) REFERENCES `book` (`pk`),
                                CONSTRAINT `FKkhnivov03734cv65f093rbnqk` FOREIGN KEY (`authors_author_number`) REFERENCES `author` (`author_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `forbidden_name` (
                                  `pk` bigint NOT NULL,
                                  `forbidden_name` varchar(255) NOT NULL,
                                  PRIMARY KEY (`pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `forbidden_name_seq` (
    `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO genre_seq VALUES (1);
INSERT INTO book_seq VALUES (1);
INSERT INTO author_seq VALUES (1);
INSERT INTO photo_seq VALUES (1);
INSERT INTO forbidden_name_seq VALUES (1);