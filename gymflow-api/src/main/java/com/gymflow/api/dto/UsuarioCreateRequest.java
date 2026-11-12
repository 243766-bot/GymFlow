package com.gymflow.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioCreateRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres")
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 150, message = "El correo no puede pasar de 150 caracteres")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        String password,

        @Size(max = 20, message = "El teléfono no puede pasar de 20 caracteres")
        String telefono,

        // Opcional: si no se manda, el usuario se crea como ROLE_USER
        @Pattern(regexp = "ROLE_USER|ROLE_ADMIN", message = "El rol debe ser ROLE_USER o ROLE_ADMIN")
        String rol
) {
}