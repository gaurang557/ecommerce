# ecommerce

A microservices-based ecommerce platform (Java / Spring Boot 4 + React).

## Architecture

| Service       | Port | Database     | Responsibility                                   |
|---------------|------|--------------|--------------------------------------------------|
| `api-gateway` | 8080 | —            | Reverse proxy, JWT validation, admin guard, CORS |
| `product`     | 8081 | `ecommerce`  | Catalog: products, price, stock, category, search|
| `user`        | 8082 | `userdb`     | Registration, login, JWT issuance, roles         |
| `order`       | 8083 | `orderdb`    | Cart + orders + checkout orchestration           |
| `payment`     | 8084 | `paymentdb`  | Mock payment authorization                        |
| `product-ui`  | 3000 | —            | React admin catalog UI                            |
| `postgres`    | 5432 | (all)        | One instance, one database per service            |

All client traffic goes through the gateway on `:8080`. Service-to-service
calls (order → product, order → payment) are direct on the internal network.

## Run

```bash
docker compose up --build
```

UI: http://localhost:3000 · Gateway: http://localhost:8080

A bootstrap admin is seeded into the user service on first start
(`ADMIN_EMAIL` / `ADMIN_PASSWORD`, default `admin@ecommerce.local` /
`admin12345`).

## API (via gateway)

Public:
- `POST /api/auth/register` `{email,password,name}` → `{token,user}`
- `POST /api/auth/login` `{email,password}` → `{token,user}`
- `GET  /api/products/` · `GET /api/products/product/{id}` · `GET /api/products/search?q=&category=&page=&size=`

Authenticated (send `Authorization: Bearer <token>`):
- `GET /api/users/me`
- `GET /api/cart` · `POST /api/cart/items` `{productId,quantity}` · `DELETE /api/cart/items/{productId}` · `DELETE /api/cart`
- `POST /api/orders/checkout` · `GET /api/orders` · `GET /api/orders/{id}`

Admin only (ADMIN role):
- `POST /api/products/product` · `PUT /api/products/product/{id}` · `DELETE /api/products/product/{id}`

## Auth model

`user` signs HS256 JWTs with the shared `JWT_SECRET`. The gateway verifies the
token, enforces public/authenticated/admin rules, and forwards the identity
downstream as `X-User-Id` / `X-User-Role` headers. Downstream services trust
those headers (they are not internet-reachable).

The admin UI has a field to paste an admin JWT so create/update/delete work;
reads are public.
