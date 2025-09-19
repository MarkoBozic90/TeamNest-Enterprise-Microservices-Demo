package test;

import com.teamnest.shared.problem.ErrorCode;
import com.teamnest.shared.problem.ServiceException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceExceptionTest {

    @Test
    void testGetterWithMessageConstructor() {
        Map<String, Object> details = new HashMap<>();
        details.put("key", "value");

        ServiceException ex = ServiceException.builder()
            .code(ErrorCode.VALIDATION_ERROR)
            .message("Invalid input")
            .details(details)
            .build();

        assertEquals(ErrorCode.VALIDATION_ERROR, ex.getCode());
        assertEquals("Invalid input", ex.getMessage());
        assertEquals("value", ex.getDetails().get("key"));
        assertNull(ex.getCause());
    }

    @Test
    void testGetterWithCauseConstructor() {
        Map<String, Object> details = new HashMap<>();
        details.put("reason", "network");

        Throwable cause = new IllegalStateException("DB down");

        ServiceException ex = ServiceException.withCause()
            .cause(cause)
            .code(ErrorCode.DOWNSTREAM_UNAVAILABLE)
            .details(Map.of("reason", "network"))
            .build();

        assertEquals(ErrorCode.DOWNSTREAM_UNAVAILABLE, ex.getCode());
        assertEquals(cause, ex.getCause());
        assertEquals("network", ex.getDetails().get("reason"));
        // Message comes from RuntimeException#getMessage() (cause.toString())
        assertTrue(ex.getMessage().contains("IllegalStateException"));
    }
}