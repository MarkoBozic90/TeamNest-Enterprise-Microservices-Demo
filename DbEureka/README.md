```
set VAULT_ADDR=http://127.0.0.1:8200 && set VAULT_TOKEN=root && vault kv put -mount=secret eureka EUREKA_USER=eureka EUREKA_PASS=super-strong-pass EUREKA_CLIENT_SERVICEURL_DEFAULTZONE="http://eureka:super-strong-pass@localhost:8761/eureka/"

```