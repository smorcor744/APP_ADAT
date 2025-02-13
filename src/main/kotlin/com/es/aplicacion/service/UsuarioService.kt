package com.es.aplicacion.service

import com.es.aplicacion.dto.UsuarioDTO
import com.es.aplicacion.dto.UsuarioRegisterDTO
import com.es.aplicacion.error.exception.BadRequestException
import com.es.aplicacion.error.exception.UnauthorizedException
import com.es.aplicacion.model.Usuario
import com.es.aplicacion.repository.UsuarioRepository
import com.mongodb.DuplicateKeyException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class UsuarioService : UserDetailsService {

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder


    override fun loadUserByUsername(username: String?): UserDetails {
        var usuario: Usuario = usuarioRepository
            .findByUsername(username!!)
            .orElseThrow {
                UnauthorizedException("$username no existente")
            }

        return User.builder()
            .username(usuario.username)
            .password(usuario.password)
            .roles(usuario.roles)
            .build()
    }

    fun insertUser(usuarioInsertadoDTO: UsuarioRegisterDTO) : UsuarioDTO? {


            if (usuarioInsertadoDTO.username.isBlank() || usuarioInsertadoDTO.password.isBlank() || usuarioInsertadoDTO.passwordRepeat.isBlank()) throw BadRequestException("Uno o más campos vacios")

            if (usuarioRepository.findByUsername(usuarioInsertadoDTO.username).isEmpty) {
                throw Exception("Usuario ${usuarioInsertadoDTO.username} existente")
            }

            if (usuarioInsertadoDTO.password != usuarioInsertadoDTO.passwordRepeat) {
                throw BadRequestException("Las contraseñas no coinciden")
            }

            if (usuarioInsertadoDTO.rol != null && usuarioInsertadoDTO.rol != "USER" && usuarioInsertadoDTO.rol != "ADMIN") {
                throw BadRequestException("Rol: ${usuarioInsertadoDTO.rol} incorrecto")
            }

            //Comprobar email

            val usuario = Usuario(
                null,
                usuarioInsertadoDTO.username,
                passwordEncoder.encode(usuarioInsertadoDTO.password),
                usuarioInsertadoDTO.passwordRepeat,
                usuarioInsertadoDTO.rol
            )
        usuarioRepository.insert(usuario)
        return UsuarioDTO(usuario.username, usuario.password,usuario.roles)

    }
}