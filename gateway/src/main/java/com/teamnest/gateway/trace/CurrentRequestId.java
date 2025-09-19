package com.teamnest.gateway.trace;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import reactor.util.context.ContextView;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CurrentRequestId {
    public static final String CONTEXT_KEY = "requestId";

    /** Safe getter from Reactor ContextView; return null if missing. */
    public static String from(final ContextView ctx) {
        return ctx.hasKey(CONTEXT_KEY) ? ctx.get(CONTEXT_KEY) : null;
    }
}