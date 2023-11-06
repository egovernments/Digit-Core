package digit.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GenericPair<T, U> {

    private T first;

    private U second;

}
