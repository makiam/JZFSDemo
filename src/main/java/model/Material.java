package model;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.pf4j.ExtensionPoint;

import java.util.UUID;


@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public abstract class Material implements ExtensionPoint {

    @JsonProperty("id")
    @Getter @Setter
    private UUID id = UUID.randomUUID();

    @Getter
    @Setter
    private String name = "";

    public Material(String name) {
        this.name = name;
    }
}
