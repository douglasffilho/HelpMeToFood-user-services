package br.com.douglasfernandes.UserServices.rest.api.v1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;

import br.com.douglasfernandes.UserServices.Messaging.MessageSender;
import br.com.douglasfernandes.UserServices.entities.Usuario;
import br.com.douglasfernandes.UserServices.rest.api.endpoints.ApiV1Endpoints;
import br.com.douglasfernandes.UserServices.services.UserService;
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping(ApiV1Endpoints.API_V1_USUARIOS_ROOT_ENDPOINT)
@Log4j
public class UserRestApi {

    @Autowired
    private UserService userService;

    @Autowired
    private MessageSender messageSender;

    private void sendMessageToQueue(Usuario usuario) {
        try {
            messageSender.send("UserServicesExchange", "UserServices", usuario);
        } catch (JsonProcessingException jpe) {
            log.error(
                    "M=sendMessageToQueue, E=Erro ao tentar converter objeto em json. Verifique o stacktrace seguinte:");
            jpe.printStackTrace();
        }
    }

    @GetMapping
    public List<Usuario> usuarios(HttpServletResponse response) throws IOException {
        try {
            List<Usuario> usuarios = userService.listarUsuarios();

            return usuarios;
        } catch (ServiceException ex) {
            log.error("M=usuarios, E=Erro ao tentar obter usuarios. Verifique o stacktrace seguinte:");
            ex.printStackTrace();

            response.sendError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
            return new ArrayList<>();
        }
    }

    @GetMapping(ApiV1Endpoints.API_V1_USUARIOS_BUSCA_POR_NOME_ENDPOINT)
    public Usuario obterUsuarioPorNome(@PathVariable("nome") String nome, HttpServletResponse response)
            throws IOException {
        try {
            Usuario usuario = userService.findByNome(nome);

            return usuario;
        } catch (ServiceException ex) {
            log.error("M=obterUsuarioPorNome, E=Erro ao tentar encontrar usuario. Verifique o stacktrace seguinte:");
            ex.printStackTrace();

            response.sendError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
            return null;
        }
    }

    @PostMapping(ApiV1Endpoints.API_V1_USUARIOS_SALVAR_ENDPOINT)
    public Usuario salvarUsuario(@RequestBody Usuario usuario, HttpServletResponse response) throws IOException {
        try {
            Usuario salvo = userService.salvarUsuario(usuario);

            sendMessageToQueue(salvo);

            return salvo;
        } catch (ServiceException ex) {
            log.error("M=salvarUsuario, E=Erro ao tentar salvar usuario. Verifique o stacktrace seguinte:");
            ex.printStackTrace();

            response.sendError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
            return null;
        }
    }
}
