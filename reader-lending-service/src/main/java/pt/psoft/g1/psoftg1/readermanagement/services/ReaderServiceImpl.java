package pt.psoft.g1.psoftg1.readermanagement.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import pt.psoft.g1.psoftg1.CreateUserCommand;
import pt.psoft.g1.psoftg1.CreateUserReply;
import pt.psoft.g1.psoftg1.DeleteUserCommand;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.readermanagement.integration.IdentityRpcClient;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class ReaderServiceImpl implements ReaderService {
    private final ReaderRepository readerRepo;
    private final PhotoRepository photoRepository;
    private final IdentityRpcClient identityRpcClient;


    @Override
    public ReaderDetails create(CreateReaderRequest request, String photoURI) {

        if (readerRepo.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reader already exists for username");
        }

        CreateUserReply reply;
        try {
            reply = identityRpcClient.createUser(new CreateUserCommand(
                request.getUsername(),
                request.getPassword(),
                request.getFullName(),
                "READER"
            ));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to create user in identity-service", e);
        }

        if (!reply.success()) {
            // map errorCode to status like you already do
            HttpStatus status = switch (reply.errorCode()) {
                case "USERNAME_EXISTS" -> HttpStatus.CONFLICT;
                case "VALIDATION" -> HttpStatus.BAD_REQUEST;
                case "FORBIDDEN" -> HttpStatus.FORBIDDEN;
                default -> HttpStatus.BAD_GATEWAY;
            };
            throw new ResponseStatusException(status, reply.errorMessage());
        }

        Long createdUserId = reply.userId();

        try {
            int nextSeq = readerRepo.getCountFromCurrentYear() + 1;

            ReaderDetails reader = new ReaderDetails(
                nextSeq,
                request.getUsername(),
                request.getBirthDate(),
                request.getPhoneNumber(),
                request.getGdpr(),
                request.getMarketing(),
                request.getThirdParty(),
                photoURI,
                request.getInterestList()
            );

            return readerRepo.save(reader);

        } catch (Exception failure) {
            // compensate
            if (createdUserId != null) {
                identityRpcClient.deleteUser(new DeleteUserCommand(createdUserId));
            }
            throw failure instanceof ResponseStatusException rse
                ? rse
                : new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to create reader locally; user rollback requested", failure);
        }
    }

    @Override
    public List<ReaderBookCountDTO> findTopByGenre(String genre, LocalDate startDate, LocalDate endDate) {
        throw new ResponseStatusException(
            HttpStatus.NOT_IMPLEMENTED,
            "Top readers by genre is disabled during migration (depends on catalog-service)."
        );
    }

    @Override
    public ReaderDetails update(final String username, final UpdateReaderRequest request, final long desiredVersion, String photoURI){
        final ReaderDetails readerDetails = readerRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Cannot find reader"));

        List<String> interestList = request.getInterestList();

         /*
         * Since photos can be null (no photo uploaded) that means the URI can be null as well.
         * To avoid the client sending false data, photoURI has to be set to any value / null
         * according to the MultipartFile photo object
         *
         * That means:
         * - photo = null && photoURI = null -> photo is removed
         * - photo = null && photoURI = validString -> ignored
         * - photo = validFile && photoURI = null -> ignored
         * - photo = validFile && photoURI = validString -> photo is set
         * */

        MultipartFile photo = request.getPhoto();
        if(photo == null && photoURI != null || photo != null && photoURI == null) {
            request.setPhoto(null);
        }

        readerDetails.applyPatch(desiredVersion, request, photoURI, interestList);
        return readerRepo.save(readerDetails);
    }


    @Override
    public Optional<ReaderDetails> findByReaderNumber(String readerNumber) {
        return this.readerRepo.findByReaderNumber(readerNumber);
    }

    @Override
    public List<ReaderDetails> findByPhoneNumber(String phoneNumber) {
        return this.readerRepo.findByPhoneNumber(phoneNumber);
    }

    @Override
    public Optional<ReaderDetails> findByUsername(final String username) {
        return this.readerRepo.findByUsername(username);
    }


    @Override
    public Iterable<ReaderDetails> findAll() {
        return this.readerRepo.findAll();
    }

    @Override
    public List<ReaderDetails> findTopReaders(int minTop) {
        if(minTop < 1) {
            throw new IllegalArgumentException("Minimum top reader must be greater than 0");
        }

        Pageable pageableRules = PageRequest.of(0,minTop);
        Page<ReaderDetails> page = readerRepo.findTopReaders(pageableRules);
        return page.getContent();
    }

    @Override
    public Optional<ReaderDetails> removeReaderPhoto(String readerNumber, long desiredVersion) {
        ReaderDetails readerDetails = readerRepo.findByReaderNumber(readerNumber)
                .orElseThrow(() -> new NotFoundException("Cannot find reader"));

        String photoFile = readerDetails.getPhoto().getPhotoFile();
        readerDetails.removePhoto(desiredVersion);
        Optional<ReaderDetails> updatedReader = Optional.of(readerRepo.save(readerDetails));
        photoRepository.deleteByPhotoFile(photoFile);
        return updatedReader;
    }

    @Override
    public List<ReaderDetails> searchReaders(pt.psoft.g1.psoftg1.shared.services.Page page, SearchReadersQuery query) {
        if (page == null)
            page = new pt.psoft.g1.psoftg1.shared.services.Page(1, 10);

        if (query == null)
            query = new SearchReadersQuery("", "","");

        final var list = readerRepo.searchReaderDetails(page, query);

        if(list.isEmpty())
            throw new NotFoundException("No results match the search query");

        return list;
    }
}
