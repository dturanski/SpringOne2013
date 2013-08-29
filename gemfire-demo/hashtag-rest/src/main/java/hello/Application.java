package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
@EnableAutoConfiguration
public class Application {

    public static void main(String[] args) {
//        SpringApplication app = new SpringApplication(Application.class, args);
//        app.addInitializers(new ApplicationContextInitializer<ConfigurableApplicationContext>() {
//
//			@Override
//			public void initialize(ConfigurableApplicationContext applicationContext) {
//				applicationContext.setParent(new ClassPathXmlApplicationContext("/META-INF/spring/client-cache.xml"));
//			}
//		});
 //       app.run();
    	SpringApplication.run(Application.class, args);
    }
}
