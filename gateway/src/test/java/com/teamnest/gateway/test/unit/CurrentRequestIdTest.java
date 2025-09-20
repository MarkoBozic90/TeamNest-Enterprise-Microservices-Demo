package com.teamnest.gateway.test.unit;

import static com.teamnest.gateway.constant.StringConstant.RID_42;
import static org.assertj.core.api.Assertions.assertThat;

import com.teamnest.gateway.trace.CurrentRequestId;
import org.junit.jupiter.api.Test;
import reactor.util.context.Context;

class CurrentRequestIdTest {
    @Test
    void returns_value_when_present() {
        var ctx = Context.of(CurrentRequestId.CONTEXT_KEY, RID_42);
        assertThat(CurrentRequestId.from(ctx)).isEqualTo(RID_42);
    }
    @Test
    void returns_null_when_missing() {
        assertThat(CurrentRequestId.from(Context.empty())).isNull();
    }
}