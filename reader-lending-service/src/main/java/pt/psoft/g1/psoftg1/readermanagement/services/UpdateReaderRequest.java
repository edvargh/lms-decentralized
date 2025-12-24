package pt.psoft.g1.psoftg1.readermanagement.services;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UpdateReaderRequest {

    @Nullable
    private String number;

    /**
     * Belongs to identity-service.
     * Keep for now (migration compatibility), but reader-lending MUST ignore it.
     */
    @Nullable
    private String username;

    /**
     * Belongs to identity-service.
     * Keep for now (migration compatibility), but reader-lending MUST ignore it.
     */
    @Nullable
    private String password;

    /**
     * Belongs to identity-service.
     * Keep for now (migration compatibility), but reader-lending MUST ignore it.
     */
    @Nullable
    private String fullName;

    @Nullable
    private String birthDate;

    @Nullable
    private String phoneNumber;

    /**
     * Use Boolean so PATCH can distinguish:
     * - null  => not provided (don’t change)
     * - true/false => update
     */
    @Nullable
    private Boolean marketing;

    @Nullable
    private Boolean thirdParty;

    @Nullable
    private List<String> interestList;

    @Nullable
    private MultipartFile photo;
}
