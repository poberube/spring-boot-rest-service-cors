package hello;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GreetingIntegrationTests {

  @Autowired
  private TestRestTemplate restTemplate;
  
  private String token = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IkNKNlduUSJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmdvb2dsZS5jb20vaWFwIiwic3ViIjoiYWNjb3VudHMuZ29vZ2xlLmNvbToxMDIzNjcxMTc1MzA1NjI1MzY4MTYiLCJlbWFpbCI6InZpbmNlbnQuY3JlcGluQGVsYXBzZXRlY2guY29tIiwiYXVkIjoiL3Byb2plY3RzLzM1MTc0MzMyODc1MS9nbG9iYWwvYmFja2VuZFNlcnZpY2VzLzY0Mzg0MDA4NjA3ODYyMDc2ODIiLCJleHAiOjE1MTE0OTA4NzIsImlhdCI6MTUxMTQ5MDI3Mn0.ppFUBMwoT0j-kj8ALgGDqC4d9Rre0IB3AMmzr-d_Rg_FqTyu_t6mQ3zIyW_GS40fW_4lxyeUSveMMzVlh3xPFA";

 //@Test
  public void corsWithAnnotation() throws Exception {
    ResponseEntity<Greeting> entity = this.restTemplate.exchange(
        RequestEntity.get(uri("/greeting")).header(HttpHeaders.ORIGIN, "http://localhost:9000").header("x-goog-iap-jwt-assertion", token).build(),
        Greeting.class);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertEquals("http://localhost:9000", entity.getHeaders().getAccessControlAllowOrigin());
    Greeting greeting = entity.getBody();
    assertEquals("Hello, World!", greeting.getContent());
  }

  @Test
  public void corsWithJavaconfig() {
    ResponseEntity<Greeting> entity = this.restTemplate.exchange(
        RequestEntity.get(uri("/greeting-javaconfig")).header(HttpHeaders.ORIGIN, "http://localhost:9000").header("x-goog-iap-jwt-assertion", token).build(),
        Greeting.class);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertEquals("http://localhost:9000", entity.getHeaders().getAccessControlAllowOrigin());
    Greeting greeting = entity.getBody();
    assertEquals("Hello, World!", greeting.getContent());
  }

  private URI uri(String path) {
    return restTemplate.getRestTemplate().getUriTemplateHandler().expand(path);
  }

}