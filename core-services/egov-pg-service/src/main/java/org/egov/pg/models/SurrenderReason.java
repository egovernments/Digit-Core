package org.egov.pg.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SurrenderReason {
    /**
     * id is the unique Identifier of the reason
     */
    private String id;
    /**
     * name is the reason of instrument surrender. Example "Damaged cheque","Cheque to be scrapped" etc
     */
    @NotBlank
    @Size(max = 50, min = 5)
    private String name;
    /**
     * description is detailed description of the surrender of a instrument
     */
    @NotBlank
    @Size(max = 250)
    private String description;

}
