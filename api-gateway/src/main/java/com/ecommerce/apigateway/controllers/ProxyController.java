package com.ecommerce.apigateway.controllers;

import com.ecommerce.apigateway.security.JwtService;
import com.ecommerce.apigateway.security.JwtService.Principal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@RestController
public class ProxyController {

    private final RestClient restClient;
    private final JwtService jwtService;
    private final Map<String, String> serviceByPrefix;

    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "DELETE", "PATCH");

    public ProxyController(RestClient restClient, JwtService jwtService,
                           @Value("${services.product.url}") String productUrl,
                           @Value("${services.user.url}") String userUrl,
                           @Value("${services.order.url}") String orderUrl,
                           @Value("${services.payment.url}") String paymentUrl) {
        this.restClient = restClient;
        this.jwtService = jwtService;
        this.serviceByPrefix = Map.of(
                "/api/products", productUrl,
                "/api/auth", userUrl,
                "/api/users", userUrl,
                "/api/cart", orderUrl,
                "/api/orders", orderUrl,
                "/api/payments", paymentUrl);
    }

    @RequestMapping("/api/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest request) throws IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return ResponseEntity.ok().build();
        }

        String prefix = serviceByPrefix.keySet().stream()
                .filter(p -> path.equals(p) || path.startsWith(p + "/"))
                .findFirst()
                .orElse(null);
        if (prefix == null) {
            return ResponseEntity.notFound().build();
        }

        boolean isProduct = prefix.equals("/api/products");
        boolean isPublic = prefix.equals("/api/auth")
                || (isProduct && "GET".equalsIgnoreCase(method));

        Principal principal = jwtService.parse(request.getHeader(HttpHeaders.AUTHORIZATION));
        if (!isPublic && principal == null) {
            return ResponseEntity.status(401)
                    .body("{\"error\":\"Authentication required\"}".getBytes());
        }
        if (isProduct && WRITE_METHODS.contains(method.toUpperCase())
                && (principal == null || !"ADMIN".equals(principal.role()))) {
            return ResponseEntity.status(403)
                    .body("{\"error\":\"Admin role required\"}".getBytes());
        }

        // Product service exposes bare paths ("/", "/product/{id}"), so strip the
        // gateway prefix for it; other services already serve under /api/...
        String downstreamPath = isProduct ? remainderOrRoot(path, prefix) : path;
        String target = serviceByPrefix.get(prefix) + downstreamPath;
        if (request.getQueryString() != null) {
            target += "?" + request.getQueryString();
        }

        byte[] body = StreamUtils.copyToByteArray(request.getInputStream());

        var spec = restClient.method(HttpMethod.valueOf(method)).uri(target);
        String contentType = request.getContentType();
        if (contentType != null) {
            spec = spec.header(HttpHeaders.CONTENT_TYPE, contentType);
        }
        if (principal != null) {
            spec = spec.header("X-User-Id", principal.userId())
                    .header("X-User-Role", principal.role());
        }
        if (body.length > 0) {
            spec = spec.body(body);
        }

        return spec.exchange((req, res) -> {
            byte[] respBody = StreamUtils.copyToByteArray(res.getBody());
            MediaType ct = res.getHeaders().getContentType();
            var builder = ResponseEntity.status(res.getStatusCode());
            builder.contentType(ct != null ? ct : MediaType.APPLICATION_JSON);
            return builder.body(respBody);
        });
    }

    private String remainderOrRoot(String path, String prefix) {
        String remainder = path.substring(prefix.length());
        return remainder.isEmpty() ? "/" : remainder;
    }
}
