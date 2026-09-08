package model;

import org.pf4j.Extension;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Extension
public class GraphMaterial extends Material {

    private final List<MxNode> nodes = new ArrayList<>();
    private final List<IOPort> ports = new ArrayList<>();

    public GraphMaterial() {
    }

    public GraphMaterial(String name) {
        super(name);
    }

    public record Link(IOPort source, IOPort target) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    public class MxNode implements Serializable {

    }

    class IOPort {

    }
}
