package com.teamnest.gateway.config;

import static com.teamnest.gateway.constant.StringConstant.DOWNSTREAM_FAILURE;
import static com.teamnest.gateway.constant.StringConstant.SERVICE_UNAVAILABLE;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
class CbFallbackHandler {

    @RequestMapping("/__cb-fallback__")
    public ResponseEntity<ProblemDetail> fallback() {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        pd.setTitle(SERVICE_UNAVAILABLE);
        pd.setDetail(DOWNSTREAM_FAILURE);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(pd);
    }
}