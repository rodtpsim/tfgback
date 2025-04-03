package es.upm.dit.isst.tfgapi.controller;

import es.upm.dit.isst.tfgapi.model.*;
import es.upm.dit.isst.tfgapi.repository.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.core.io.ByteArrayResource;
import org.slf4j.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/myApi")
public class TFGController {

    private final TFGRepository tfgRepository;
    private final SesionRepository sesionRepository;
    public static final Logger log = LoggerFactory.getLogger(TFGController.class);

    public TFGController(TFGRepository t, SesionRepository s) {
        this.tfgRepository = t;
        this.sesionRepository = s;
    }

    @GetMapping("/tfgs")
    List<TFG> readAll(@RequestParam(name = "tutor", required = false) String tutor) {
        return tutor != null && !tutor.isEmpty()
                ? (List<TFG>) tfgRepository.findByTutor(tutor)
                : (List<TFG>) tfgRepository.findAll();
    }

    @PostMapping("/tfgs")
    ResponseEntity<TFG> create(@RequestBody TFG newTFG) throws URISyntaxException {
        if (tfgRepository.findById(newTFG.getAlumno()).isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        newTFG.setEstado(Estado.SOLICITADO);
        TFG result = tfgRepository.save(newTFG);
        return ResponseEntity.created(new URI("/tfgs/" + result.getAlumno())).body(result);
    }

    @GetMapping("/tfgs/{id}")
    ResponseEntity<TFG> readOne(@PathVariable String id) {
        return tfgRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/tfgs/{id}")
    ResponseEntity<TFG> update(@RequestBody TFG newTFG, @PathVariable String id) {
        return tfgRepository.findById(id).map(tfg -> {
            tfg.setTitulo(newTFG.getTitulo());
            tfg.setResumen(newTFG.getResumen());
            tfg.setTutor(newTFG.getTutor());
            tfg.setEstado(newTFG.getEstado());
            tfg.setMemoria(newTFG.getMemoria());
            tfg.setCalificacion(newTFG.getCalificacion());
            tfg.setMatriculaHonor(newTFG.getMatriculaHonor());
            tfg.setSesion(newTFG.getSesion());
            tfgRepository.save(tfg);
            return ResponseEntity.ok().body(tfg);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PatchMapping("/tfgs/{id}")
    ResponseEntity<TFG> partialUpdate(@RequestBody TFG newTFG, @PathVariable String id) {
        return tfgRepository.findById(id).map(tfg -> {
            if (newTFG.getTitulo() != null) tfg.setTitulo(newTFG.getTitulo());
            if (newTFG.getResumen() != null) tfg.setResumen(newTFG.getResumen());
            if (newTFG.getTutor() != null) tfg.setTutor(newTFG.getTutor());
            if (newTFG.getEstado() != null) tfg.setEstado(newTFG.getEstado());
            if (newTFG.getMemoria() != null) tfg.setMemoria(newTFG.getMemoria());
            if (newTFG.getCalificacion() != null) tfg.setCalificacion(newTFG.getCalificacion());
            if (newTFG.getMatriculaHonor() != null) tfg.setMatriculaHonor(newTFG.getMatriculaHonor());
            if (newTFG.getSesion() != null) tfg.setSesion(newTFG.getSesion());
            tfgRepository.save(tfg);
            return ResponseEntity.ok().body(tfg);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/tfgs/{id}")
    ResponseEntity<?> delete(@PathVariable String id) {
        tfgRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/tfgs/{id}/estado/{estado}")
    @Transactional
    public ResponseEntity<?> actualizaEstado(@PathVariable String id, @PathVariable Estado estado) {
        return tfgRepository.findById(id).map(tfg -> {
            if (!tfg.getEstado().canTransitionTo(estado)) {
                return ResponseEntity.badRequest().body("Transición inválida");
            }
            tfg.setEstado(estado);
            tfgRepository.save(tfg);
            return ResponseEntity.ok().body(tfg);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/tfgs/{id}/memoria", consumes = "application/pdf")
    public ResponseEntity<?> subeMemoria(@PathVariable String id, @RequestBody byte[] fileContent) {
        return tfgRepository.findById(id).map(tfg -> {
            tfg.setMemoria(fileContent);
            tfgRepository.save(tfg);
            return ResponseEntity.ok("Documento subido correctamente");
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TFG no encontrado"));
    }

    @GetMapping(value = "/tfgs/{id}/memoria", produces = "application/pdf")
    public ResponseEntity<?> descargaMemoria(@PathVariable String id) {
        TFG tfg = tfgRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TFG no encontrado"));
        if (tfg.getMemoria() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"memoria_" + id + ".pdf\"")
                .body(new ByteArrayResource(tfg.getMemoria()));
    }

    @PostMapping("/sesiones")
    ResponseEntity<Sesion> createSesion(@RequestBody Sesion newSesion) throws URISyntaxException {
        Sesion result = sesionRepository.save(newSesion);
        return ResponseEntity.created(new URI("/sesiones/" + result.getId())).body(result);
    }

    @PostMapping("/sesiones/{id}/tfgs")
    ResponseEntity<?> asignaTFG(@PathVariable Long id, @RequestBody String alumno) {
        return sesionRepository.findById(id).map(sesion -> {
            TFG tfg = tfgRepository.findById(alumno).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TFG no encontrado"));
            tfg.setSesion(sesion);
            tfgRepository.save(tfg);
            return ResponseEntity.ok().body(tfg);
        }).orElse(ResponseEntity.notFound().build());
    }
}
