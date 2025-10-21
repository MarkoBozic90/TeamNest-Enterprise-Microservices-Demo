package com.teamnest.userservice.api.request;


import com.teamnest.userservice.model.enums.InviteType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.UUID;

@Schema(description = "Admin request to issue a general invite token")
public record InviteCreateRequest(
    @NotNull
    InviteType type,
    @NotNull
    UUID clubId,
    @NotNull @Min(1)
    Integer maxUsers,
    @NotNull
    Duration ttl,

    String label
) {

}
