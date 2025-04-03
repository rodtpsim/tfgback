package es.upm.dit.isst.tfgapi.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class Sesion {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Future
    private Date fecha;

    private String lugar;

    @Size(min = 3, max = 3)
    private List<@Email @NotEmpty String> tribunal;

    @JsonIgnore
    @OneToMany(mappedBy = "sesion")
    private List<@Valid TFG> tfgs;

    public Sesion() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }

    public List<String> getTribunal() { return tribunal; }
    public void setTribunal(List<String> tribunal) { this.tribunal = tribunal; }

    public List<TFG> getTfgs() { return tfgs; }

    @JsonGetter("tfgs")
    public String[] getEmailsTfgs() {
        return tfgs != null ? tfgs.stream().map(TFG::getAlumno).toArray(String[]::new) : new String[0];
    }

    @JsonProperty("tfgs")
    public void setTfgs(List<TFG> tfgs) {
        this.tfgs = tfgs;
    }
}
