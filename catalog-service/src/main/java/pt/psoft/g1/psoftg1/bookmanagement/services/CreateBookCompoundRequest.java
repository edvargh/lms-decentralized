package pt.psoft.g1.psoftg1.bookmanagement.services;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "Create Book + Authors + Genre in one request")
public class CreateBookCompoundRequest {

  @NotBlank
  private String title;

  @Nullable
  private String description;

  @NotBlank
  private String genre; // name

  @Nullable
  private String photoURI; // keep it simple; can add MultipartFile later

  @NotEmpty
  private List<@Valid CreateAuthorInlineRequest> authors;

  @Data
  @NoArgsConstructor
  public static class CreateAuthorInlineRequest {
    @NotBlank
    private String name;

    @Nullable
    private String bio;

    @Nullable
    private String photoURI;
  }
}
