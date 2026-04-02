import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.example.demo")
@EnableScheduling
public class CrawlingIngestionApplication {
    public static void main(String[] args) {
        SpringApplication.run(CrawlingIngestionApplication.class, args);
    }

}
