package com.gymflow.api.service;

import com.gymflow.api.dto.ClienteResponse;
import com.gymflow.api.dto.UsuarioCreateRequest;
import com.gymflow.api.dto.UsuarioResponse;
import com.gymflow.api.dto.UsuarioUpdateRequest;
import com.gymflow.api.entity.Membresia;
import com.gymflow.api.entity.Rol;
import com.gymflow.api.entity.Usuario;
import com.gymflow.api.exception.BadRequestException;
import com.gymflow.api.exception.NotFoundException;
import com.gymflow.api.repository.MembresiaRepository;
import com.gymflow.api.repository.PlantillaFacialRepository;
import com.gymflow.api.repository.RolRepository;
import com.gymflow.api.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PlantillaFacialRepository plantillaRepository;
    private final MembresiaRepository membresiaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          PlantillaFacialRepository plantillaRepository,
                          MembresiaRepository membresiaRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.plantillaRepository = plantillaRepository;
        this.membresiaRepository = membresiaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------- Perfil propio ----------

    @Transactional(readOnly = true)
    public UsuarioResponse perfil(String email) {
        return usuarioRepository.findByEmail(email)
                .map(UsuarioResponse::from)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    // ---------- Consultas del administrador ----------

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        // Para cada usuario nos quedamos con su membresía más reciente
        Map<Long, Membresia> ultimaMembresia = new HashMap<>();
        for (Membresia m : membresiaRepository.findAll()) {
            ultimaMembresia.merge(m.getUsuario().getId(), m,
                    (a, b) -> b.getFechaVencimiento().isAfter(a.getFechaVencimiento()) ? b : a);
        }
        Set<Long> conRostro = new HashSet<>(plantillaRepository.findUsuarioIdsConRostro());

        return usuarioRepository.findAll(Sort.by("id")).stream()
                .map(u -> ClienteResponse.of(u, ultimaMembresia.get(u.getId()), conRostro.contains(u.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtener(Long id) {
        return detalle(buscar(id));
    }

    // ---------- Cambios del administrador ----------

    @Transactional
    public ClienteResponse crear(UsuarioCreateRequest request) {
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new BadRequestException("Ese correo ya está registrado");
        }

        String nombreRol = (request.rol() != null) ? request.rol() : "ROLE_USER";
        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new IllegalStateException("Falta el rol " + nombreRol + " en la base de datos"));

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setTelefono(limpiar(request.telefono()));
        usuario.setRol(rol);

        return detalle(usuarioRepository.save(usuario));
    }

    @Transactional
    public ClienteResponse actualizar(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = buscar(id);

        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmailAndIdNot(email, id)) {
            throw new BadRequestException("Ese correo ya lo usa otro usuario");
        }

        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);
        usuario.setTelefono(limpiar(request.telefono()));

        // La contraseña solo cambia si se manda una nueva
        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.password()));
        }

        return detalle(usuarioRepository.save(usuario));
    }

    @Transactional
    public ClienteResponse cambiarBan(Long id, boolean baneado, String emailAdmin) {
        Usuario usuario = buscar(id);

        if (baneado && usuario.getEmail().equalsIgnoreCase(emailAdmin)) {
            throw new BadRequestException("No puedes banearte a ti mismo");
        }

        usuario.setActivo(!baneado);
        return detalle(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id, String emailAdmin) {
        Usuario usuario = buscar(id);

        if (usuario.getEmail().equalsIgnoreCase(emailAdmin)) {
            throw new BadRequestException("No puedes eliminar tu propia cuenta");
        }
        if (membresiaRepository.existsByUsuarioId(id)) {
            throw new BadRequestException(
                    "Este usuario tiene membresías registradas. Banéalo en lugar de eliminarlo.");
        }

        plantillaRepository.deleteByUsuarioId(id); // si tenía rostro guardado, se borra
        usuarioRepository.delete(usuario);
    }

    // ---------- Utilidades ----------

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    private ClienteResponse detalle(Usuario usuario) {
        Membresia membresia = membresiaRepository
                .findFirstByUsuarioIdOrderByFechaVencimientoDesc(usuario.getId())
                .orElse(null);
        boolean rostro = plantillaRepository.existsByUsuarioId(usuario.getId());
        return ClienteResponse.of(usuario, membresia, rostro);
    }

    // Un texto vacío se guarda como "sin dato"
    private String limpiar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}