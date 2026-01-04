package pt.psoft.g1.psoftg1.readermanagement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.readermanagement.services.UpdateReaderRequest;
import pt.psoft.g1.psoftg1.shared.model.EntityWithPhoto;

import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "reader_details")
public class ReaderDetails extends EntityWithPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long pk;

    /**
     * Reference to identity-service user (JWT subject).
     * This service must NOT store passwords / roles.
     */
    @Column(nullable = false, unique = true)
    @Getter
    private String username;

    @Embedded
    private ReaderNumber readerNumber;

    @Embedded
    @Getter
    private BirthDate birthDate;

    @Embedded
    private PhoneNumber phoneNumber;

    @Setter
    @Getter
    @Basic
    private boolean gdprConsent;

    @Setter
    @Getter
    @Basic
    private boolean marketingConsent;

    @Setter
    @Getter
    @Basic
    private boolean thirdPartySharingConsent;

    @Version
    @Getter
    private Long version;

    /**
     * Interest list references catalog genres.
     * Stored as strings for now (stub). Later validate via catalog-service.
     */
    @ElementCollection
    @CollectionTable(name = "READER_INTERESTS", joinColumns = @JoinColumn(name = "READER_PK"))
    @Column(name = "GENRE", nullable = false)
    @Getter
    private List<String> interestList = new ArrayList<>();

    public ReaderDetails(
        int readerNumber,
        String username,
        String birthDate,
        String phoneNumber,
        boolean gdpr,
        boolean marketing,
        boolean thirdParty,
        String photoURI,
        List<String> interestList
    ) {
        if (username == null || username.isBlank() || phoneNumber == null) {
            throw new IllegalArgumentException("Provided argument resolves to null/blank object");
        }

        if (!gdpr) {
            throw new IllegalArgumentException("Readers must agree with the GDPR rules");
        }

        this.username = username;
        setReaderNumber(new ReaderNumber(readerNumber));
        setPhoneNumber(new PhoneNumber(phoneNumber));
        setBirthDate(new BirthDate(birthDate));

        // By spec, GDPR must be true at creation time
        setGdprConsent(true);

        setMarketingConsent(marketing);
        setThirdPartySharingConsent(thirdParty);

        if (photoURI != null) {
            try {
                setPhotoInternal(photoURI);
            } catch (InvalidPathException ignored) {}
        }

        if (interestList != null) {
            this.interestList = new ArrayList<>(interestList);
        }
    }

    private void setPhoneNumber(PhoneNumber number) {
        if (number != null) {
            this.phoneNumber = number;
        }
    }

    private void setReaderNumber(ReaderNumber readerNumber) {
        if (readerNumber != null) {
            this.readerNumber = readerNumber;
        }
    }

    private void setBirthDate(BirthDate date) {
        if (date != null) {
            this.birthDate = date;
        }
    }

    /**
     * Patch only fields owned by reader-lending-service.
     * Username/password/fullName belong to identity-service (do NOT update here).
     */
    public void applyPatch(
        final long currentVersion,
        final UpdateReaderRequest request,
        String photoURI,
        List<String> interestList
    ) {
        if (!Objects.equals(currentVersion, this.version)) {
            throw new ConflictException("Provided version does not match latest version of this object");
        }

        if (request.getBirthDate() != null) {
            setBirthDate(new BirthDate(request.getBirthDate()));
        }

        if (request.getPhoneNumber() != null) {
            setPhoneNumber(new PhoneNumber(request.getPhoneNumber()));
        }

        if (request.getMarketing() != null && request.getMarketing() != this.marketingConsent) {
            setMarketingConsent(request.getMarketing());
        }

        if (request.getThirdParty() != null && request.getThirdParty() != this.thirdPartySharingConsent) {
            setThirdPartySharingConsent(request.getThirdParty());
        }


        if (photoURI != null) {
            try {
                setPhotoInternal(photoURI);
            } catch (InvalidPathException ignored) {}
        }

        if (interestList != null) {
            this.interestList = new ArrayList<>(interestList);
        }
    }

    public void removePhoto(long desiredVersion) {
        if (!Objects.equals(desiredVersion, this.version)) {
            throw new ConflictException("Provided version does not match latest version of this object");
        }
        setPhotoInternal(null);
    }

    public String getReaderNumber() {
        return this.readerNumber.toString();
    }

    public String getPhoneNumber() {
        return this.phoneNumber.toString();
    }

    /** Protected empty constructor for ORM only. */
    protected ReaderDetails() {}
}
