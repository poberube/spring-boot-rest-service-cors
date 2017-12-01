package hello;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class GreetingController {
	
	@Autowired
	private RestTemplate restTemplate;

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();
    
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
    		return builder.build();
    }

    @GetMapping("/greeting")
    public Greeting greeting(@RequestParam(required=false, defaultValue="World") String name, 
    		@RequestHeader HttpHeaders headers) {
        System.out.println("==== in greeting headers:");
        for (String key : headers.keySet()) {
        		System.out.println(key + ": " + headers.get(key));
        }
        System.out.println("Calling sub greeting...");
        String echo = restTemplate.postForObject("http://echo-svc/echo", "testfromgreeting", String.class);
        return new Greeting(counter.incrementAndGet(), String.format(template, name) + " subcall: " + echo);
    }

    @GetMapping("/greeting-javaconfig")
    public Greeting greetingWithJavaconfig(@RequestParam(required=false, defaultValue="World") String name) {
        System.out.println("==== in greeting ====");
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }

}
