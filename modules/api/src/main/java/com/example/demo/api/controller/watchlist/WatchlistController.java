package com.example.demo.api.controller.watchlist;

import com.example.demo.alert.watchlist.application.port.in.AddWatchlistItemUseCase;
import com.example.demo.alert.watchlist.application.port.in.RemoveWatchlistItemUseCase;
import com.example.demo.alert.watchlist.application.port.in.SearchWatchlistUseCase;
import com.example.demo.alert.watchlist.application.usecase.WatchlistPage;
import com.example.demo.alert.watchlist.domain.domain.DuplicateWatchlistItemException;
import com.example.demo.alert.watchlist.domain.domain.WatchlistItem;
import com.example.demo.api.common.OffsetPage;
import com.example.demo.api.controller.watchlist.dto.AddWatchlistItemRequest;
import com.example.demo.api.controller.watchlist.dto.WatchlistItemResponse;
import com.example.demo.user.application.port.in.AuthenticatedAccount;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/watchlist")
@RequiredArgsConstructor
public class WatchlistController {
    private final AddWatchlistItemUseCase addWatchlistItemUseCase;
    private final RemoveWatchlistItemUseCase removeWatchlistItemUseCase;
    private final SearchWatchlistUseCase searchWatchlistUseCase;

    @GetMapping
    public OffsetPage<WatchlistItemResponse> list(
            @AuthenticationPrincipal AuthenticatedAccount auth,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String userId = auth.id().asString();
        WatchlistPage result = searchWatchlistUseCase.search(userId, keyword, page, size);
        List<WatchlistItemResponse> items = result.items().stream()
                .map(WatchlistItemResponse::from)
                .toList();
        return OffsetPage.of(items, result.page(), result.size(), result.total());
    }

    @PostMapping
    public ResponseEntity<WatchlistItemResponse> add(
            @AuthenticationPrincipal AuthenticatedAccount auth,
            @Valid @RequestBody AddWatchlistItemRequest request
    ) {
        String userId = auth.id().asString();
        WatchlistItem saved = addWatchlistItemUseCase.add(userId, request.toCommand());
        return ResponseEntity
                .created(URI.create("/api/v1/watchlist/" + saved.id()))
                .body(WatchlistItemResponse.from(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthenticatedAccount auth,
            @PathVariable long id
    ) {
        String userId = auth.id().asString();
        removeWatchlistItemUseCase.remove(userId, id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(DuplicateWatchlistItemException.class)
    public ProblemDetail handleDuplicate(DuplicateWatchlistItemException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setType(URI.create("https://docs.coindata.example/errors/watchlist/duplicate"));
        pd.setTitle("Duplicate Watchlist Item");
        pd.setDetail(ex.getMessage());
        if (request != null) {
            pd.setInstance(URI.create(request.getRequestURI()));
        }
        return pd;
    }
}
