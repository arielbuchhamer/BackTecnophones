package com.BackTecnophones.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.BackTecnophones.model.Usuario;
import com.BackTecnophones.repository.UsuarioRepository;

@Service
@Transactional
public class UsuarioService implements GenericService<Usuario>{
	
	private final UsuarioRepository usuarioRepo;
	private final PasswordEncoder passwordEncoder;
	
	public UsuarioService(UsuarioRepository usuarioRepo, PasswordEncoder passwordEncoder) {
		this.usuarioRepo = usuarioRepo;
		this.passwordEncoder = passwordEncoder;
	}
	
	@Override
	public Optional<Usuario> findById(String id) {
		return usuarioRepo.findById(id);
	}

	@Override
	public List<Usuario> findAll() {
		return usuarioRepo.findAll();
	}

	@Override
	public Usuario save(Usuario entity) {
		if (entity.getUserName() == null || entity.getUserName().isBlank()) 
	        throw new IllegalArgumentException("El username es requerido");
		
		if (entity.getClave() != null && !entity.getClave().isBlank()) {
	        String hash = passwordEncoder.encode(entity.getClave());
	        entity.setHashClave(hash);
	    } else if (entity.getHashClave() == null || entity.getHashClave().isBlank()) 
	        throw new IllegalArgumentException("La clave es requerida para crear el usuario");
		
		return usuarioRepo.save(entity);
	}

	@Override
	public void delete(String id) {
		usuarioRepo.deleteById(id);
	}

	public Usuario updateUsuario(String id, Usuario usuarioNuevo) {
		return usuarioRepo.findById(id).map(usuarioExistente -> {
			usuarioExistente.setUserName(usuarioNuevo.getUserName());
	        usuarioExistente.setNombre(usuarioNuevo.getNombre());

	        if (usuarioNuevo.getClave() != null && !usuarioNuevo.getClave().isBlank()) {
	            String hash = passwordEncoder.encode(usuarioNuevo.getClave());
	            usuarioExistente.setHashClave(hash);
	        }

			return usuarioRepo.save(usuarioExistente);
		}).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
	}
	
	public Optional<Usuario> verificarUsuario(String username, String password) {
		Optional<Usuario> usuarioOpt = usuarioRepo.findByUserName(username);

        if (usuarioOpt.isEmpty()) 
        	return Optional.empty();
        
        Usuario usuario = usuarioOpt.get();

        if (passwordEncoder.matches(password, usuario.getHashClave())) 
            return Optional.of(usuario);
        
        return Optional.empty();
	}
}
