package com.teamnest.gateway.constant;

/**
 * Centralizovane test konstante (putanje, header-i, JWT claims, poruke, statusi...).
 * Napomena: nazivi i vrednosti su zadržani kako bi ostali kompatibilni sa postojećim testovima.
 */
public final class StringConstant {

    private StringConstant() { /* no-op */ }

    // ---------------------------------------------------------------------
    // URLs / PATHS
    // ---------------------------------------------------------------------
    public static final String CB_ALWAYS_500_URL     = "/cb/always500/test";
    public static final String TEST_RLS_URL          = "/__test/rls";
    public static final String TEST_INTERNAL_URL     = "/__test/internal";
    public static final String R1_TEST_ONE           = "/rl/test/one";
    public static final String R1_TEST_TWO           = "/rl/test/two";
    public static final String FLAKY_TEST_URL        = "/flaky/test";
    public static final String RETRY_FLAKY_TEST_URL  = "/retry/flaky/test";
    public static final String PATH_LOGIN            = "/login";
    public static final String PATH_DOCS_PING        = "/docs/ping";
    public static final String PATH_FALLBACK_PING    = "/__fallback/ping";
    public static final String PATH_PROTECTED        = "/protected";
    public static final String PATH_SECURE           = "/secure";
    public static final String PATH_SVC              = "/svc";
    public static final String PATH_ANY              = "/any";
    public static final String PATH_X                = "/x";
    public static final String PATH_Y                = "/y";
    public static final String PATH_Z                = "/z";
    public static final String PATH_P                = "/z"; // zadržano kao u originalu
    public static final String PATH_RATE_LIMITED     = "/ratelimited";

    // Sample IP adrese
    public static final String PATH_LOCAL_IP         = "127.0.0.1";
    public static final String PATH_IO               = "203.0.113.10";

    // ---------------------------------------------------------------------
    // BODY PAYLOADS
    // ---------------------------------------------------------------------
    public static final String OK_AFTER_RETRIES      = "ok-after-retries";
    public static final String BODY_OK               = "ok";

    // ---------------------------------------------------------------------
    // JWT / ROLES / CLAIMS
    // ---------------------------------------------------------------------
    // JWT header
    public static final String JWT_TOKEN_VALUE       = "t";
    public static final String JWT_HEADER_ALG        = "alg";
    public static final String JWT_ALG_NONE          = "none";
    public static final String CID_1 = "cid-1";

    // Claims
    public static final String CLAIM_ROLES           = "roles";
    public static final String CLAIM_REALM_ACCESS    = "realm_access";
    public static final String CLAIM_REALM_ACCESS_ROLES = "roles";
    public static final String CLAIM_OTHER_KEY       = "club_id";
    public static final String CLAIM_OTHER_VAL       = "adasdadaas";

    // Ulazne role (iz tokena)
    public static final String ROLE_ADMIN            = "admin";        // bez prefixa
    public static final String ROLE_MANAGER          = "ROLE_manager"; // već sa prefixom
    public static final String ROLE_USER             = "user";
    public static final String ROLE_SUPER            = "super";

    // Očekivane authorities
    public static final String AUTH_ROLE_PREFIX      = "ROLE_";
    public static final String AUTH_ROLE_ADMIN       = "ROLE_admin";
    public static final String AUTH_ROLE_MANAGER     = "ROLE_manager";
    public static final String AUTH_ROLE_USER        = "ROLE_user";
    public static final String AUTH_ROLE_SUPER       = "ROLE_super";

    // ---------------------------------------------------------------------
    // REQUEST IDS
    // ---------------------------------------------------------------------
    public static final String RID_123               = "rid-123";
    public static final String RID_999               = "rid-999";
    public static final String RID_1                 = "rid-1";
    public static final String RID_X                 = "rid-x";
    public static final String RID_REQ               = "rid-req";
    public static final String RID_429               = "rid-429";
    public static final String RID_COMM              = "rid-committed";
    public static final String RID_42                = "rid-42";
    public static final String GIVEN_1               = "given-1";

    // ---------------------------------------------------------------------
    // HEADERS
    // ---------------------------------------------------------------------
    public static final String X_REQUEST_ID          = "X-Request-Id";
    public static final String X_FORWARDED_FOR       = "X-Forwarded-For";
    public static final String RETRY_AFTER_SECONDS   = "5";
    public static final String CORRELATION_ID        = "correlationId";

    // ---------------------------------------------------------------------
    // JSON PATH KEYS
    // ---------------------------------------------------------------------
    public static final String TITLE                 = "$.title";
    public static final String STATUS                = "$.status";

    // ---------------------------------------------------------------------
    // MISC / STRINGS
    // ---------------------------------------------------------------------
    public static final String ANONYMOUS             = "anonymous";
    public static final String X_DETAIL              = "x-detail";
    public static final String DETAIL_BAD            = "bad";
    public static final String NOPE                  = "nope";
    public static final String ALICE                 = "alice";
    public static final String X                     = "x";
    public static final String DETAIL_OOPS           = "oops";
    public static final String DETAIL_H2             = "h2";
    public static final String HINT                  = "hint";
    public static final String FLAKY                 = "flaky";
    public static final String STEP_2                = "step2";
    public static final String OK                    = "ok";

    // ---------------------------------------------------------------------
    // I18N / MESSAGES
    // ---------------------------------------------------------------------
    public static final String EN_LANGUAGE           = "en";
    public static final String SR_LANGUAGE           = "sr";
    public static final String SERVICE_UNAVAILABLE   = "Service Unavailable";
    public static final String DOWNSTREAM_FAILURE    = "Downstream failure";
    public static final String BOOM                  = "boom";
    public static final String TO_MANY_REQUESTS      = "Too many requests";     // zadržan naziv konstante
    public static final String TO_MANY_REQUESTS_SR   = "Previše zahteva";
    public static final String UNEXPECTED_ERROR      = "Unexpected error";
    public static final String UNEXPECTED_ERROR_SR   = "Neočekivana greška";
    public static final String RATE_LIMIT_EXCEEDED   = "Rate limit exceeded";

    // ---------------------------------------------------------------------
    // HTTP STATUS (numeric)
    // ---------------------------------------------------------------------
    public static final int HTTP_STATUS_200          = 200;
    public static final int HTTP_STATUS_429          = 429;
    public static final int HTTP_STATUS_503          = 503;
    public static final int HTTP_STATUS_500          = 500;

    // ---------------------------------------------------------------------
    // Problem JSON common properties
    // ---------------------------------------------------------------------
    public static final String ERROR_CODE            = "errorCode";
    public static final String APP                   = "app";
    public static final String GATEWAY               = "gateway";
    public static final String REQUEST_ID            = "requestId";

    // ---------------------------------------------------------------------
    // DISPLAY NAMES
    // ---------------------------------------------------------------------
    public static final String CIRCUIT_BREAKER       =
        "CircuitBreaker -> fallback returns RFC7807 503 with X-Request-Id";
}
