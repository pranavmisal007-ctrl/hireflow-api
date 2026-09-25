package com.hireflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379",
    "spring.mail.host=localhost",
    "spring.mail.port=1025",
    "app.jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
    "aws.s3.bucket-name=test-bucket",
    "aws.s3.endpoint=http://localhost:9000",
    "aws.access-key=test",
    "aws.secret-key=test",
    "aws.s3.region=us-east-1"
})
class HireFlowApplicationTest {

    @Test
    void contextLoads() {
        // Verify Spring context loads without errors
    }
}
