package com.soaengry.moment;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import java.util.TimeZone;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@EnableJpaRepositories(basePackages = {
        "com.soaengry.moment.domain.user.repository",
        "com.soaengry.moment.domain.event.repository",
        "com.soaengry.moment.domain.wedding.repository",
        "com.soaengry.moment.domain.invitation.repository",
        "com.soaengry.moment.domain.rsvp.repository",
        "com.soaengry.moment.domain.email.repository",
        "com.soaengry.moment.domain.guestbook.repository",
        "com.soaengry.moment.domain.feed.repository",
        "com.soaengry.moment.domain.attendance.repository",
        "com.soaengry.moment.domain.bank.repository"
})
@EnableMongoRepositories(basePackages = "com.soaengry.moment.domain.chat.repository")
public class MomentApplication {

    @PostConstruct
    void setTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

    public static void main(String[] args) {
        SpringApplication.run(MomentApplication.class, args);
    }

}
