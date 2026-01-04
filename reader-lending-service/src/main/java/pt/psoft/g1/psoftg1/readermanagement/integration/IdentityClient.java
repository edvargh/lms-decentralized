package pt.psoft.g1.psoftg1.readermanagement.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class IdentityClient {

  private final WebClient.Builder webClientBuilder;

  @Value("${identity.base-url}")
  private String identityBaseUrl;

  public UserView createUser(CreateUserRequest req, String bearerToken) {
    return webClientBuilder
        .baseUrl(identityBaseUrl)
        .build()
        .post()
        .uri("/api/admin/users")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(req)
        .retrieve()
        // Let 4xx/5xx propagate as WebClientResponseException (we'll map in service)
        .bodyToMono(UserView.class)
        .block();
  }

  public void deleteUserById(String userId, String bearerToken) {
    webClientBuilder
        .baseUrl(identityBaseUrl)
        .build()
        .delete()
        .uri("/api/admin/users/{id}", userId)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
        .retrieve()
        .bodyToMono(Void.class)
        .onErrorResume(ex -> Mono.empty()) // best-effort compensation
        .block();
  }

  // DTOs local to reader-lending-service
  public record CreateUserRequest(String username, String password, String name, String role) {}

  public record UserView(String id, String username, String fullName) {}
}
