package pt.psoft.g1.psoftg1.readermanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.external.service.ApiNinjasService;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingView;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingViewMapper;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.services.LendingService;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.services.CreateReaderRequest;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderService;
import pt.psoft.g1.psoftg1.readermanagement.services.SearchReadersQuery;
import pt.psoft.g1.psoftg1.readermanagement.services.UpdateReaderRequest;
import pt.psoft.g1.psoftg1.shared.api.ListResponse;
import pt.psoft.g1.psoftg1.shared.services.ConcurrencyService;
import pt.psoft.g1.psoftg1.shared.services.FileStorageService;
import pt.psoft.g1.psoftg1.shared.services.SearchRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Tag(name = "Readers", description = "Endpoints to manage readers")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/readers")
class ReaderController {
    private final ReaderService readerService;
    private final ReaderViewMapper readerViewMapper;
    private final LendingService lendingService;
    private final LendingViewMapper lendingViewMapper;
    private final ConcurrencyService concurrencyService;
    private final FileStorageService fileStorageService;
    private final ApiNinjasService apiNinjasService;

    @Operation(summary = "Gets the reader data if authenticated as Reader or all readers if authenticated as Librarian")
    @ApiResponse(description = "Success", responseCode = "200", content = { @Content(mediaType = "application/json",
            // Use the `array` property instead of `schema`
            array = @ArraySchema(schema = @Schema(implementation = ReaderView.class))) })
    @GetMapping
    public ResponseEntity<?> getData(Authentication authentication) {
        String username = authentication.getName();

        if (!isStaff(authentication)) {
            ReaderDetails readerDetails = readerService.findByUsername(username)
                    .orElseThrow(() -> new NotFoundException(ReaderDetails.class, username));
            return ResponseEntity.ok().eTag(Long.toString(readerDetails.getVersion())).body(readerViewMapper.toReaderView(readerDetails));
        }

        return ResponseEntity.ok().body(readerViewMapper.toReaderView(readerService.findAll()));
    }

    @Operation(summary = "Gets reader by number")
    @ApiResponse(description = "Success", responseCode = "200", content = { @Content(mediaType = "application/json",
            // Use the `array` property instead of `schema`
            array = @ArraySchema(schema = @Schema(implementation = ReaderView.class))) })
    @GetMapping(value="/{year}/{seq}")
    //This is just for testing purposes, therefore admin role has been set
    //@RolesAllowed(Role.LIBRARIAN)
    public ResponseEntity<ReaderQuoteView> findByReaderNumber(@PathVariable("year")
                                                           @Parameter(description = "The year of the Reader to find")
                                                           final Integer year,
                                                       @PathVariable("seq")
                                                           @Parameter(description = "The sequencial of the Reader to find")
                                                           final Integer seq) {
        String readerNumber = year+"/"+seq;
        final var readerDetails = readerService.findByReaderNumber(readerNumber)
                .orElseThrow(() -> new NotFoundException("Could not find reader from specified reader number"));

        var readerQuoteView = readerViewMapper.toReaderQuoteView(readerDetails);

        int birthYear = readerDetails.getBirthDate().getBirthDate().getYear();
        int birhMonth = readerDetails.getBirthDate().getBirthDate().getMonthValue();

        readerQuoteView.setQuote(apiNinjasService.getRandomEventFromYearMonth(birthYear, birhMonth));

        return ResponseEntity.ok()
                .eTag(Long.toString(readerDetails.getVersion()))
                .body(readerQuoteView);
    }

    @Operation(summary = "Gets a list of Readers by phoneNumber")
    @GetMapping(params = "phoneNumber")
    public ListResponse<ReaderView> findByPhoneNumber(@RequestParam(name = "phoneNumber", required = false) final String phoneNumber) {

        List<ReaderDetails> readerDetailsList  = readerService.findByPhoneNumber(phoneNumber);

        if(readerDetailsList.isEmpty()) {
            throw new NotFoundException(ReaderDetails.class, phoneNumber);
        }

        return new ListResponse<>(readerViewMapper.toReaderView(readerDetailsList));
    }

    @GetMapping(params = "name")
    public ListResponse<ReaderView> findByReaderName(@RequestParam("name") String name) {
        throw new ResponseStatusException(
            HttpStatus.NOT_IMPLEMENTED,
            "Search readers by name depends on identity-service and is disabled during migration."
        );
    }

    @Operation(summary= "Gets a reader photo")
    @GetMapping("/{year}/{seq}/photo")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> getSpecificReaderPhoto(@PathVariable("year")
                                                     @Parameter(description = "The year of the Reader to find")
                                                     final Integer year,
                                                 @PathVariable("seq")
                                                     @Parameter(description = "The sequencial of the Reader to find")
                                                     final Integer seq,
                                                         Authentication authentication) {
        String username = authentication.getName();

        //if Librarian is logged in, skip ahead
        if (!isStaff(authentication)) {
            final var loggedReaderDetails = readerService.findByUsername(username)
                    .orElseThrow(() -> new NotFoundException(ReaderDetails.class, username));

            //if logged Reader matches the one associated with the lending, skip ahead
            if (!loggedReaderDetails.getReaderNumber().equals(year + "/" + seq)) {
                throw new AccessDeniedException("Reader does not have permission to view another reader's photo");
            }
        }


        ReaderDetails readerDetails = readerService.findByReaderNumber(year + "/" + seq).orElseThrow(() -> new NotFoundException(ReaderDetails.class, username));

        //In case the user has no photo, just return a 200 OK without body
        if(readerDetails.getPhoto() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        String photoFile = readerDetails.getPhoto().getPhotoFile();
        byte[] image = this.fileStorageService.getFile(photoFile);
        String fileFormat = this.fileStorageService.getExtension(readerDetails.getPhoto().getPhotoFile()).orElseThrow(() -> new ValidationException("Unable to get file extension"));

        if(image == null) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.ok().contentType(fileFormat.equals("png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG).body(image);
    }

    @Operation(summary= "Gets a reader photo")
    @GetMapping("/photo")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> getReaderOwnPhoto(Authentication authentication) {

        String username = authentication.getName();

        Optional<ReaderDetails> optReaderDetails = readerService.findByUsername(username);
        if(optReaderDetails.isEmpty()) {
            throw new AccessDeniedException("Could not find a valid reader from current auth");
        }

        ReaderDetails readerDetails = optReaderDetails.get();

        //In case the user has no photo, just return a 200 OK without body
        if(readerDetails.getPhoto() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        byte[] image = this.fileStorageService.getFile(readerDetails.getPhoto().getPhotoFile());

        if(image == null) {
            return ResponseEntity.ok().build();
        }

        String fileFormat = this.fileStorageService.getExtension(readerDetails.getPhoto().getPhotoFile()).orElseThrow(() -> new ValidationException("Unable to get file extension"));

        return ResponseEntity.ok().contentType(fileFormat.equals("png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG).body(image);
    }

    @Operation(summary = "Creates a reader")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ReaderView> createReader(@Valid @ModelAttribute CreateReaderRequest readerRequest) throws ValidationException {
        MultipartFile file = readerRequest.getPhoto();

        String fileName = fileStorageService.getRequestPhoto(file);

        ReaderDetails readerDetails = readerService.create(readerRequest, fileName);

        final var newReaderUri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .pathSegment(readerDetails.getReaderNumber())
                .build().toUri();

        return ResponseEntity.created(newReaderUri)
                .eTag(Long.toString(readerDetails.getVersion()))
                .body(readerViewMapper.toReaderView(readerDetails));
    }

    @Operation(summary = "Deletes a reader photo")
    @DeleteMapping("/photo")
    public ResponseEntity<Void> deleteReaderPhoto(Authentication authentication) {
        String username = authentication.getName();

        Optional<ReaderDetails> optReaderDetails = readerService.findByUsername(username);
        if(optReaderDetails.isEmpty()) {
            throw new AccessDeniedException("Could not find a valid reader from current auth");
        }

        ReaderDetails readerDetails = optReaderDetails.get();

        if(readerDetails.getPhoto() == null) {
            throw new NotFoundException("Reader has no photo to delete");
        }

        this.fileStorageService.deleteFile(readerDetails.getPhoto().getPhotoFile());
        readerService.removeReaderPhoto(readerDetails.getReaderNumber(), readerDetails.getVersion());

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Updates a reader")
    @PatchMapping
    public ResponseEntity<ReaderView> updateReader(
            @Valid UpdateReaderRequest readerRequest,
            Authentication authentication,
            final WebRequest request) {

        final String ifMatchValue = request.getHeader(ConcurrencyService.IF_MATCH);
        if (ifMatchValue == null || ifMatchValue.isEmpty() || ifMatchValue.equals("null")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You must issue a conditional PATCH using 'if-match'");
        }

        MultipartFile file = readerRequest.getPhoto();

        String fileName = this.fileStorageService.getRequestPhoto(file);

        String username = authentication.getName();
        ReaderDetails readerDetails = readerService
                .update(username, readerRequest, concurrencyService.getVersionFromIfMatchHeader(ifMatchValue), fileName);

        return ResponseEntity.ok()
                .eTag(Long.toString(readerDetails.getVersion()))
                .body(readerViewMapper.toReaderView(readerDetails));
    }

    @Operation(summary = "Gets the lendings of this reader by ISBN")
    @GetMapping(value = "/{year}/{seq}/lendings")
    public List<LendingView> getReaderLendings(
            Authentication authentication,
            @PathVariable("year")
                @Parameter(description = "The year of the Reader to find")
                final Integer year,
            @PathVariable("seq")
                @Parameter(description = "The sequencial of the Reader to find")
                final Integer seq,
            @RequestParam("isbn")
                @Parameter(description = "The ISBN of the Book to find")
                final String isbn,
            @RequestParam(value = "returned", required = false)
                @Parameter(description = "Filter by returned")
                final Optional<Boolean> returned)
    {
        String urlReaderNumber = year + "/" + seq;

        final var urlReaderDetails = readerService.findByReaderNumber(urlReaderNumber)
                .orElseThrow(() -> new NotFoundException(Lending.class, urlReaderNumber));

        String username = authentication.getName();

        //if Librarian is logged in, skip ahead
        if (!isStaff(authentication)) {
            final var loggedReaderDetails = readerService.findByUsername(username)
                    .orElseThrow(() -> new NotFoundException(ReaderDetails.class, username));

            //if logged Reader matches the one associated with the lendings, skip ahead
            if(!Objects.equals(loggedReaderDetails.getReaderNumber(), urlReaderDetails.getReaderNumber())){
                throw new AccessDeniedException("Reader does not have permission to view these lendings");
            }
        }
        final var lendings = lendingService.listByReaderNumberAndIsbn(urlReaderNumber, isbn, returned);

        if(lendings.isEmpty())
            throw new NotFoundException("No lendings found with provided ISBN");

        return lendingViewMapper.toLendingView(lendings);
    }

    @GetMapping("/top5")
    public ListResponse<ReaderView> getTop() {
        return new ListResponse<>(readerViewMapper.toReaderView(readerService.findTopReaders(5)));
    }

    @GetMapping("/top5ByGenre")
    public ListResponse<ReaderCountView> getTop5ReaderByGenre(
            @RequestParam("genre") String genre,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
    {
        final var books = readerService.findTopByGenre(genre,startDate,endDate);

        if(books.isEmpty())
            throw new NotFoundException("No lendings found with provided parameters");

        return new ListResponse<>(readerViewMapper.toReaderCountViewList(books));
    }

    @PostMapping("/search")
    public ListResponse<ReaderView> searchReaders(
            @RequestBody final SearchRequest<SearchReadersQuery> request) {
        final var readerList = readerService.searchReaders(request.getPage(), request.getQuery());
        return new ListResponse<>(readerViewMapper.toReaderView(readerList));
    }

    private boolean isStaff(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a ->
            a.getAuthority().equals("ROLE_LIBRARIAN") || a.getAuthority().equals("ROLE_ADMIN"));
    }
}
