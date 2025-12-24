package pt.psoft.g1.psoftg1.configuration;

import java.security.interfaces.RSAPublicKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

  @Value("${jwt.public.key}")
  private RSAPublicKey rsaPublicKey;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http = http.cors(Customizer.withDefaults()).csrf(csrf -> csrf.disable());

    http = http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    http = http.exceptionHandling(exceptions -> exceptions
        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
        .accessDeniedHandler(new BearerTokenAccessDeniedHandler()));

    http.authorizeHttpRequests(auth -> auth
        // Swagger should be public
        .requestMatchers(
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
        ).permitAll()

        // Public endpoints
        .requestMatchers("/api/public/**").permitAll()

        // Example: catalog read endpoints require authenticated user
        .requestMatchers(HttpMethod.GET, "/api/books/**").hasAnyRole("READER", "LIBRARIAN", "ADMIN")
        .requestMatchers(HttpMethod.GET, "/api/authors/**").hasAnyRole("READER", "LIBRARIAN", "ADMIN")
        .requestMatchers(HttpMethod.GET, "/api/genres/**").hasAnyRole("READER", "LIBRARIAN", "ADMIN")

        // Write endpoints restricted (adjust to match your final routes)
        .requestMatchers(HttpMethod.POST, "/api/books/**").hasAnyRole("LIBRARIAN", "ADMIN")
        .requestMatchers(HttpMethod.PUT, "/api/books/**").hasAnyRole("LIBRARIAN", "ADMIN")
        .requestMatchers(HttpMethod.PATCH, "/api/books/**").hasAnyRole("LIBRARIAN", "ADMIN")
        .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasAnyRole("LIBRARIAN", "ADMIN")

        .requestMatchers(HttpMethod.POST, "/api/authors/**").hasAnyRole("LIBRARIAN", "ADMIN")
        .requestMatchers(HttpMethod.PUT, "/api/authors/**").hasAnyRole("LIBRARIAN", "ADMIN")
        .requestMatchers(HttpMethod.PATCH, "/api/authors/**").hasAnyRole("LIBRARIAN", "ADMIN")
        .requestMatchers(HttpMethod.DELETE, "/api/authors/**").hasAnyRole("LIBRARIAN", "ADMIN")

        .requestMatchers(HttpMethod.POST, "/api/genres/**").hasAnyRole("LIBRARIAN", "ADMIN")
        .requestMatchers(HttpMethod.PUT, "/api/genres/**").hasAnyRole("LIBRARIAN", "ADMIN")
        .requestMatchers(HttpMethod.PATCH, "/api/genres/**").hasAnyRole("LIBRARIAN", "ADMIN")
        .requestMatchers(HttpMethod.DELETE, "/api/genres/**").hasAnyRole("LIBRARIAN", "ADMIN")

        // Anything else must be authenticated
        .anyRequest().authenticated()
    );

    http.oauth2ResourceServer(oauth2 -> oauth2
        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
    );

    // You do NOT need httpBasic() here
    return http.build();
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withPublicKey(this.rsaPublicKey).build();
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
    grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
    return converter;
  }

  @Bean
  public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin("*");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }
}
