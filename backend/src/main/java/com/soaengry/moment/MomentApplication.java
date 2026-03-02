package com.soaengry.moment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {
        "com.soaengry.moment.domain.user.repository",
        "com.soaengry.moment.domain.wedding.repository",
        "com.soaengry.moment.domain.email.repository",
        "com.soaengry.moment.domain.guestbook.repository",
        "com.soaengry.moment.domain.feed.repository",
        "com.soaengry.moment.domain.attendance.repository",
        "com.soaengry.moment.domain.bank.repository"
})
@EnableMongoRepositories(basePackages = "com.soaengry.moment.domain.chat.repository")
public class MomentApplication {

    public static void main(String[] args) {
        SpringApplication.run(MomentApplication.class, args);
    }

}
