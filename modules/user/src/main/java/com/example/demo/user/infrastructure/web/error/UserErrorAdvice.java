package com.example.demo.user.infrastructure.web.error;

import com.example.demo.user.domain.exception.AccountNotFoundException;
import com.example.demo.user.domain.exception.ApiKeyNotFoundException;
import com.example.demo.user.domain.exception.ApiKeyOwnershipException;
import com.example.demo.user.domain.exception.ApiKeyQuotaExceededException;
import com.example.demo.user.domain.exception.ApiKeyScopeNotAllowedException;
import com.example.demo.user.domain.exception.DuplicateEmailException;
import com.example.demo.user.domain.exception.InvalidCredentialsException;
import com.example.demo.user.domain.exception.TokenInvalidException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.example.demo.user.infrastructure.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UserErrorAdvice {

    @ExceptionHandler(DuplicateEmailException.class)
    public ProblemDetail handleDuplicateEmail(DuplicateEmailException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Email Already Registered");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle("Invalid Credentials");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(TokenInvalidException.class)
    public ProblemDetail handleTokenInvalid(TokenInvalidException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle("Token Invalid");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleNotFound(AccountNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("Account Not Found");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(ApiKeyQuotaExceededException.class)
    public ProblemDetail handleApiKeyQuota(ApiKeyQuotaExceededException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("ApiKey Quota Exceeded");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(ApiKeyScopeNotAllowedException.class)
    public ProblemDetail handleApiKeyScope(ApiKeyScopeNotAllowedException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("ApiKey Scope Not Allowed");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(ApiKeyNotFoundException.class)
    public ProblemDetail handleApiKeyNotFound(ApiKeyNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("ApiKey Not Found");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(ApiKeyOwnershipException.class)
    public ProblemDetail handleApiKeyOwnership(ApiKeyOwnershipException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("ApiKey Ownership Violation");
        pd.setDetail(ex.getMessage());
        return pd;
    }
}
