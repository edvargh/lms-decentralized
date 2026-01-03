CREATE TABLE `t_user` (
                          `dtype` varchar(31) NOT NULL,
                          `user_id` bigint NOT NULL,
                          `created_at` datetime(6) DEFAULT NULL,
                          `created_by` varchar(255) DEFAULT NULL,
                          `enabled` bit(1) NOT NULL,
                          `modified_at` datetime(6) DEFAULT NULL,
                          `modified_by` varchar(255) DEFAULT NULL,
                          `name` varchar(150) DEFAULT NULL,
                          `password` varchar(255) NOT NULL,
                          `username` varchar(255) NOT NULL,
                          `version` bigint DEFAULT NULL,
                          PRIMARY KEY (`user_id`),
                          UNIQUE KEY `UK_jhib4legehrm4yscx9t3lirqi` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `t_user_seq` (
    `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user_authorities` (
                                    `user_user_id` bigint NOT NULL,
                                    `authorities` varbinary(255) DEFAULT NULL,
                                    KEY `FKg3ms6twsd5wemq30u2bvhj7if` (`user_user_id`),
                                    CONSTRAINT `FKg3ms6twsd5wemq30u2bvhj7if` FOREIGN KEY (`user_user_id`) REFERENCES `t_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `photo` (
                         `pk` bigint NOT NULL,
                         `photo_file` varchar(255) NOT NULL,
                         PRIMARY KEY (`pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `photo_seq` (
    `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `forbidden_name` (
                                  `pk` bigint NOT NULL,
                                  `forbidden_name` varchar(255) NOT NULL,
                                  PRIMARY KEY (`pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `forbidden_name_seq` (
    `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO t_user_seq (next_val) VALUES (1);
INSERT INTO photo_seq (next_val) VALUES (1);
INSERT INTO forbidden_name_seq (next_val) VALUES (1);
