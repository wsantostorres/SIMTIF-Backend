package com.api.simtif.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.simtif.models.Aluno;
import com.api.simtif.models.Vaga;
import com.api.simtif.repositories.AlunoRepository;
import com.api.simtif.repositories.VagaRepository;

import java.util.List;
import java.util.Optional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/")
public class CurriculoController {

    @Autowired
    AlunoRepository alunoRepository;

    @Autowired
    VagaRepository vagaRepository;

    @PostMapping("/baixar-curriculos/{id}/")
    public ResponseEntity<Object> baixarCurriculos(@PathVariable long id) {
        Optional<Vaga> vagaOptional = vagaRepository.findById(id);

        if (vagaOptional.isPresent()) {
            Vaga vaga = vagaOptional.get();
            List<Aluno> alunosByVaga = vaga.getAlunos();

            if (alunosByVaga.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ainda não há alunos participando desta vaga.");
            }

            // Gerar os HTMLs dos currículos
            byte[] zipBytes = generateCurriculosZip(alunosByVaga);

            String nomeArquivo = vaga.getTitulo();

            // Configurar a resposta HTTP com o arquivo ZIP contendo os HTMLs
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/zip"));
            headers.setContentDispositionFormData("attachment", nomeArquivo + ".zip");
            headers.setContentLength(zipBytes.length);

            return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vaga não encontrada.");
    }

    private byte[] generateCurriculosZip(List<Aluno> alunos) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {

            for (Aluno aluno : alunos) {
                String curriculoHtml = generateCurriculoHtml(aluno);

                // Adicionar o arquivo HTML do currículo ao arquivo ZIP
                ZipEntry entry = new ZipEntry(aluno.getNomeCompleto() + aluno.getMatricula() + ".html");
                zipOutputStream.putNextEntry(entry);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(curriculoHtml.getBytes(StandardCharsets.UTF_8));
                IOUtils.copy(inputStream, zipOutputStream);
                inputStream.close();
                zipOutputStream.closeEntry();
            }

            zipOutputStream.finish();
            zipOutputStream.close();

            return outputStream.toByteArray();
        } catch (IOException e) {
            // Lidar com erros de IO
            e.printStackTrace();
            return null;
        }
    }

    private String generateCurriculoHtml(Aluno aluno) {
        StringBuilder htmlBuilder = new StringBuilder();

        // Adicionar o cabeçalho HTML
        htmlBuilder.append("<!DOCTYPE html >\r\n" + //
                "<html lang=\"pt-br\">\r\n" + //
                "<head>\r\n" + //
                "    <meta charset=\"UTF-8\">\r\n" + //
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\r\n" + //
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n" + //
                "    <title>Curr\u00EDculo Digital</title>\r\n" + //
                "    <style>\r\n" + //
                "        *{\r\n" + //
                "            margin: 0;\r\n" + //
                "            padding: 0;\r\n" + //
                "            box-sizing: border-box;\r\n" + //
                "            font-family: Georgia, 'Times New Roman', Times, serif;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        html, body{\r\n" + //
                "            height: 100vh;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        h1, h2, h3 {\r\n" + //
                "            color: #237acc;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        h1{\r\n" + //
                "            border-bottom: 1px solid #aaa;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "\r\n" + //
                "        .main{\r\n" + //
                "            width: 670px;\r\n" + //
                "            margin: 20px auto;\r\n" + //
                "            padding: 50px;\r\n" + //
                "            border: 20px solid #237acc;\r\n" + //
                "\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        .infoContato{\r\n" + //
                "            margin-bottom: 10px;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        .infoProfisional > div > h2 {\r\n" + //
                "            padding: 0px 0px 5px 0px;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        .infoProfisional > div > h3 {\r\n" + //
                "            padding: 0px 0px 5px 0px;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "\r\n" + //
                "        .infoProfisional > div{\r\n" + //
                "            padding: 10px 0px;\r\n" + //
                "            margin-bottom: 5px;\r\n" + //
                "            border-bottom: 1px solid #aaa;\r\n" + //
                "        }\r\n" + //
                "    </style>\r\n" + //
                "</head>\r\n" + //
                "<body>\r\n" + //
                "    <div class=\"main\">\r\n" + //
                "        <div class=\"infoContato\">\r\n" + //
                "            <h1>{NOMECOMPLETO}</h1>\r\n" + //
                "            <p>{EMAIL}</p>\r\n" + //
                "            <p>{NUMTELEFONE}</p>\r\n" + //
                "            <p>{CIDADE}/{UF}</p>\r\n" + //
                "        </div>\r\n" + //
                "        <div class=\"infoProfisional\">\r\n" + //
                "            <div>\r\n" + //
                "                <h3>Objetivos</h3>\r\n" + //
                "                <div>\r\n" + //
                "                    <p>{OBJETIVOS}</p>\r\n" + //
                "                </div>\r\n" + //
                "            </div>\r\n" + //
                "            <div>\r\n" + //
                "                <h3>Habilidades</h3>\r\n" + //
                "                <div>\r\n" + //
                "                    <p>{HABILIDADES}</p>\r\n" + //
                "                </div>\r\n" + //
                "            </div>\r\n" + //
                "            <div>\r\n" + //
                "                <h3>Experi\u00EAncia</h3>\r\n" + //
                "                <div>\r\n" + //
                "                    <p>{EXPERIENCIA}</p>\r\n" + //
                "                </div>\r\n" + //
                "            </div>\r\n" + //
                "            <div>\r\n" + //
                "                <h3>Educa\u00E7\u00E3o</h3>\r\n" + //
                "                <div>\r\n" + //
                "                    <p>{EDUCACAO}</p>\r\n" + //
                "                </div>\r\n" + //
                "            </div>\r\n" + //
                "            <div>\r\n" + //
                "                <h3>Projetos</h3>\r\n" + //
                "                <div>\r\n" + //
                "                    <p>{PROJETOS}</p>\r\n" + //
                "                </div>\r\n" + //
                "            </div>\r\n" + //
                "            <div>\r\n" + //
                "                <h3>Cursos Complementares</h3>\r\n" + //
                "                <div>\r\n" + //
                "                    <p>{CURSOSCOMPLEMENTARES}</p>\r\n" + //
                "                </div>\r\n" + //
                "            </div>\r\n" + //
                "        </div>\r\n" + //
                "    </div>\r\n" + //
                "</body>\r\n" + //
                "</html>");

        String curriculo = htmlBuilder.toString();

        String nomeCompleto = aluno.getNomeCompleto();
        String email = aluno.getEmail();
        String numTelefone = aluno.getNumTelefone();
        String cidade = aluno.getCidade();
        String uf = aluno.getUf();
        String objetivos = aluno.getObjetivos();
        String habilidades = aluno.getHabilidades();
        String experiencia = aluno.getExperiencia();
        String educacao = aluno.getEducacao();
        String projetos = aluno.getProjetos();
        String cursosComplementares = aluno.getCursosComplementares();

        curriculo = curriculo.replace("{NOMECOMPLETO}", nomeCompleto != null ? nomeCompleto : "");
        curriculo = curriculo.replace("{EMAIL}", email != null ? email : "");
        curriculo = curriculo.replace("{NUMTELEFONE}", numTelefone != null ? numTelefone : "");
        curriculo = curriculo.replace("{CIDADE}", cidade != null ? cidade : "");
        curriculo = curriculo.replace("{UF}", uf != null ? uf : "");
        curriculo = curriculo.replace("{OBJETIVOS}", objetivos != null ? objetivos : "");
        curriculo = curriculo.replace("{HABILIDADES}", habilidades != null ? habilidades : "");
        curriculo = curriculo.replace("{EXPERIENCIA}", experiencia != null ? experiencia : "");
        curriculo = curriculo.replace("{EDUCACAO}", educacao != null ? educacao : "");
        curriculo = curriculo.replace("{PROJETOS}", projetos != null ? projetos : "");
        curriculo = curriculo.replace("{CURSOSCOMPLEMENTARES}", cursosComplementares != null ? cursosComplementares : "");

        return curriculo;
    }
}
