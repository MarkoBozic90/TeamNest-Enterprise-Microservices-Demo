package test;

import static org.junit.jupiter.api.Assertions.*;

import com.teamnest.shared.problem.ErrorCode;
import com.teamnest.shared.problem.ErrorTypeMapper;
import com.teamnest.shared.problem.ProblemTypes;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;

class ErrorCodeTest {

    @Test
    void allEnumConstantsHaveRequiredFields() {
        for (var code : ErrorCode.values()) {
            assertNotNull(code.getCode(), code.name() + " code null");
            assertTrue(code.getHttpStatus() >= 100 && code.getHttpStatus() < 600,
                () -> code.name() + " bad httpStatus=" + code.getHttpStatus());
            assertNotNull(code.getI18nKey(), code.name() + " i18nKey null");
            assertNotNull(code.getProblemType(), code.name() + " problemType null");
        }
    }

    @Test
    void specificValuesAreAsExpected() {
        assertEquals("KC-001", ErrorCode.AUTHENTICATION_FAILED.getCode());
        assertEquals(401, ErrorCode.AUTHENTICATION_FAILED.getHttpStatus());
        assertEquals("error.auth.failed", ErrorCode.AUTHENTICATION_FAILED.getI18nKey());
        assertEquals(429, ErrorCode.RATE_LIMIT_EXCEEDED.getHttpStatus());
        assertEquals("error.rateLimit", ErrorCode.RATE_LIMIT_EXCEEDED.getI18nKey());
        assertEquals("54", ErrorCode.UPSTREAM_CLIENT_ERROR.getI18nKey()); // trenutno tako definisano
    }

    @Test
    void errorTypeMapperMatchesEnumProblemType() {
        for (var ec : EnumSet.allOf(ErrorCode.class)) {
            // mapper pokriva samo eksplcitno nabrojane; za ostale očekujemo INTERNAL
            var expected = switch (ec) {
                case AUTHENTICATION_FAILED  -> ProblemTypes.AUTH;
                case ACCESS_FORBIDDEN       -> ProblemTypes.FORBIDDEN;
                case VALIDATION_ERROR       -> ProblemTypes.VALIDATION;
                case RATE_LIMIT_EXCEEDED    -> ProblemTypes.RATE_LIMIT;
                case DOWNSTREAM_UNAVAILABLE -> ProblemTypes.DOWNSTREAM;
                default                     -> ProblemTypes.INTERNAL;
            };
            assertEquals(expected, ErrorTypeMapper.toType(ec), () -> "mapper mismatch for " + ec);
        }
    }
}