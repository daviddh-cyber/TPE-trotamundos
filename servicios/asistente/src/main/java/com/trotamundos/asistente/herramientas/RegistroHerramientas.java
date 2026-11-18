package com.trotamundos.asistente.herramientas;

import com.trotamundos.asistente.integracion.DescripcionHerramienta;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Lista cerrada de herramientas (Iteración 5, sección 5.1). El modelo no puede inventar
 * operaciones: solo existen las registradas acá. Spring registra toda clase que implemente Herramienta.
 */
@Component
public class RegistroHerramientas {

    private final Map<String, Herramienta> porNombre;

    public RegistroHerramientas(List<Herramienta> herramientas) {
        this.porNombre = herramientas.stream()
                .collect(Collectors.toUnmodifiableMap(Herramienta::nombre, Function.identity()));
    }

    public Optional<Herramienta> buscar(String nombre) {
        return Optional.ofNullable(porNombre.get(nombre));
    }

    public List<DescripcionHerramienta> descripciones() {
        return porNombre.values().stream()
                .map(h -> new DescripcionHerramienta(h.nombre(), h.descripcion()))
                .toList();
    }
}
