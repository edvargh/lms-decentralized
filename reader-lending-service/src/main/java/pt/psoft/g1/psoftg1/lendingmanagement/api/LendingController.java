package pt.psoft.g1.psoftg1.lendingmanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.services.CreateLendingRequest;
import pt.psoft.g1.psoftg1.lendingmanagement.services.LendingService;
import pt.psoft.g1.psoftg1.lendingmanagement.services.SearchLendingQuery;
import pt.psoft.g1.psoftg1.lendingmanagement.services.SetLendingReturnedRequest;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderService;
import pt.psoft.g1.psoftg1.shared.api.ListResponse;
import pt.psoft.g1.psoftg1.shared.services.ConcurrencyService;
import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.g1.psoftg1.shared.services.SearchRequest;

import java.util.List;
import java.util.Objects;

@Tag(name = "Lendings", description = "Endpoints for managing Lendings")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lendings")
public class LendingController {
    private final LendingService lendingService;
    private final ReaderService readerService;
    private final ConcurrencyService concurrencyService;

    private final LendingViewMapper lendingViewMapper;

    @Operation(summary = "Creates a new Lending (stubbed)")
    @PostMapping
    public ResponseEntity<LendingView> create(@Valid @RequestBody final CreateLendingRequest resource) {
        throw new ResponseStatusException(
            HttpStatus.NOT_IMPLEMENTED,
            "Create lending is temporarily disabled during migration. Will be reintroduced via cross-service communication."
        );
    }


    @Operation(summary = "Gets a specific Lending")
    @GetMapping(value = "/{year}/{seq}")
    public ResponseEntity<LendingView> findByLendingNumber(
        Authentication authentication,
        @PathVariable("year") @Parameter(description = "The year of the Lending to find") final Integer year,
        @PathVariable("seq") @Parameter(description = "The sequencial of the Lending to find") final Integer seq) {

        String ln = year + "/" + seq;

        final var lending = lendingService.findByLendingNumber(ln)
            .orElseThrow(() -> new NotFoundException(Lending.class, ln));

        // If librarian/admin: allow
        if (!isStaff(authentication)) {
            // Reader access: only if this lending belongs to them
            String username = authentication.getName();

            final var loggedReaderDetails = readerService.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(ReaderDetails.class, username));

            if (!Objects.equals(loggedReaderDetails.getReaderNumber(),
                lending.getReaderDetails().getReaderNumber())) {
                throw new AccessDeniedException(
                    "Reader does not have permission to view this lending");
            }
        }

        final var lendingUri = ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri();

        return ResponseEntity.ok().location(lendingUri)
            .contentType(MediaType.parseMediaType("application/hal+json"))
            .eTag(Long.toString(lending.getVersion()))
            .body(lendingViewMapper.toLendingView(lending));
    }


    @Operation(summary = "Sets a lending as returned")
    @PatchMapping(value = "/{year}/{seq}")
    public ResponseEntity<LendingView> setLendingReturned(
        final WebRequest request,
        final Authentication authentication,
        @PathVariable("year")
        @Parameter(description = "The year component of the Lending to find")
        final Integer year,
        @PathVariable("seq")
        @Parameter(description = "The sequential component of the Lending to find")
        final Integer seq,
        @Valid @RequestBody final SetLendingReturnedRequest resource) {

        final String ifMatchValue = request.getHeader(ConcurrencyService.IF_MATCH);
        if (ifMatchValue == null || ifMatchValue.isEmpty() || ifMatchValue.equals("null")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "You must issue a conditional PATCH using 'if-match'");
        }

        String ln = year + "/" + seq;

        final var maybeLending = lendingService.findByLendingNumber(ln)
            .orElseThrow(() -> new NotFoundException(Lending.class, ln));

        // In decentralized architecture: use JWT principal directly
        String username = authentication.getName();

        final var loggedReaderDetails = readerService.findByUsername(username)
            .orElseThrow(() -> new NotFoundException(ReaderDetails.class, username));

        // If logged Reader does not match the one associated with the lending -> deny
        if (!Objects.equals(loggedReaderDetails.getReaderNumber(), maybeLending.getReaderDetails().getReaderNumber())) {
            throw new AccessDeniedException("Reader does not have permission to edit this lending");
        }

        final var lending = lendingService.setReturned(
            ln,
            resource,
            concurrencyService.getVersionFromIfMatchHeader(ifMatchValue)
        );

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/hal+json"))
            .eTag(Long.toString(lending.getVersion()))
            .body(lendingViewMapper.toLendingView(lending));
    }


    @Operation(summary = "Get average lendings duration")
    @GetMapping(value = "/avgDuration")
    public @ResponseBody ResponseEntity<LendingsAverageDurationView> getAvgDuration() {

        return ResponseEntity.ok().body(lendingViewMapper.toLendingsAverageDurationView(lendingService.getAverageDuration()));
    }

    @Operation(summary = "Get list of overdue lendings")
    @GetMapping(value = "/overdue")
    public ListResponse<LendingView> getOverdueLendings(@Valid @RequestBody Page page) {
        final List<Lending> overdueLendings = lendingService.getOverdue(page);
        if(overdueLendings.isEmpty())
            throw new NotFoundException("No lendings to show");
        return new ListResponse<>(lendingViewMapper.toLendingView(overdueLendings));
    }

    @Operation(summary = "Search lendings (stubbed)")
    @PostMapping("/search")
    public ListResponse<LendingView> searchReaders(
        @RequestBody final SearchRequest<SearchLendingQuery> request) {

        throw new ResponseStatusException(
            HttpStatus.NOT_IMPLEMENTED,
            "Search lendings is temporarily disabled during migration. Will be reintroduced after service integration."
        );
    }


    private boolean isStaff(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(a ->
            a.getAuthority().equals("ROLE_LIBRARIAN") || a.getAuthority().equals("ROLE_ADMIN")
        );
    }
}
