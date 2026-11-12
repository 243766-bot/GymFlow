package com.gymflow.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "plantillas_faciales")
public class PlantillaFacial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Un usuario tiene como máximo una plantilla facial
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    // Los 128 números del descriptor, separados por comas
    @Column(nullable = false, columnDefinition = "text")
    private String descriptor;

    // Miniatura del rostro (JPEG en base64). Es opcional: los registros anteriores no la tienen.
    @Column(columnDefinition = "text")
    private String foto;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(nullable = false)
    private boolean consentimiento;

    public PlantillaFacial() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getDescriptor() { return descriptor; }
    public void setDescriptor(String descriptor) { this.descriptor = descriptor; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public boolean isConsentimiento() { return consentimiento; }
    public void setConsentimiento(boolean consentimiento) { this.consentimiento = consentimiento; }
}