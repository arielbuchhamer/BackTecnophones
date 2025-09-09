package com.BackTecnophones.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.BackTecnophones.model.Usuario;
import com.BackTecnophones.repository.UsuarioRepository;

@Service
@Transactional
public class UsuarioService implements GenericService<Usuario>{
	
	private final UsuarioRepository usuarioRepo;
	
	public UsuarioService(UsuarioRepository usuarioRepo) {
		this.usuarioRepo = usuarioRepo;
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
		return usuarioRepo.save(entity);
	}

	@Override
	public void delete(String id) {
		usuarioRepo.deleteById(id);
	}

	public Usuario updateUsuario(String id, Usuario usuarioNuevo) {
		return usuarioRepo.findById(id).map(usuarioExistente -> {
			usuarioExistente.setUserName(usuarioNuevo.getUserName());
			usuarioExistente.setContraseña(usuarioNuevo.getContraseña());

			return usuarioRepo.save(usuarioExistente);
		}).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
	}
	
	public Optional<Usuario> verificarUsuario(String username, String password) {
//		Optional<Usuario> usuarioOpt = usuarioRepo.findByUserName(username);
//
//        if (usuarioOpt.isEmpty()) 
//        	return null;
//
//        if (usuarioOpt.get().getContraseña().equals(password)) 
//            return usuarioOpt.get();

        return usuarioRepo.findByUserName(username);
	}
}
