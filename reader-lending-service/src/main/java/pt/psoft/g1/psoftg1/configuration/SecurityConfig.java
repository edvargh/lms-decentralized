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

        // Public endpoints (if you have any)
        .requestMatchers("/api/public/**").permitAll()

        /*
         * Reader endpoints
         * Adjust paths if your controllers differ.
         */
        // Usually only librarians/admin create or manage readers
        .requestMatchers(HttpMethod.POST, "/api/readers/**").hasAnyRole("LIBRARIAN", "ADMIN")
        .requestMatchers(HttpMethod.PUT, "/api/readers/**").hasAnyRole("LIBRARIAN", "ADMIN")
        .requestMatchers(HttpMethod.PATCH, "/api/readers/**").hasAnyRole("LIBRARIAN", "ADMIN")
        .requestMatchers(HttpMethod.DELETE, "/api/readers/**").hasAnyRole("LIBRARIAN", "ADMIN")

        // Read endpoints: reader + librarian + admin
        .requestMatchers(HttpMethod.GET, "/api/readers/**").hasAnyRole("READER", "LIBRARIAN", "ADMIN")

        /*
         * Lending endpoints
         * Typical rules:
         * - Librarian/Admin can create lendings and see operational views like overdue
         * - Reader can view own lendings and request return/extend (if you support that)
         */
        .requestMatchers(HttpMethod.POST, "/api/lendings/**").hasAnyRole("LIBRARIAN", "ADMIN")
        .requestMatchers(HttpMethod.PUT, "/api/lendings/**").hasAnyRole("LIBRARIAN", "ADMIN")

        // PATCH often used for "return book" / "extend lending" etc.
        .requestMatchers(HttpMethod.PATCH, "/api/lendings/**").hasAnyRole("READER", "LIBRARIAN", "ADMIN")

        // Overdue/analytics endpoints: librarian/admin
        .requestMatchers(HttpMethod.GET, "/api/lendings/overdue").hasAnyRole("LIBRARIAN", "ADMIN")
        .requestMatchers(HttpMethod.GET, "/api/lendings/avgDuration").hasAnyRole("LIBRARIAN", "ADMIN")

        // General lending reads: reader + librarian + admin
        .requestMatchers(HttpMethod.GET, "/api/lendings/**").hasAnyRole("READER", "LIBRARIAN", "ADMIN")

        // Anything else must be authenticated
        .anyRequest().authenticated()
    );

    http.oauth2ResourceServer(oauth2 -> oauth2
        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
    );

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
