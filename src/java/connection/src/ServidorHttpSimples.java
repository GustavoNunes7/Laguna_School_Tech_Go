import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Optional;

public class ServidorHttpSimples{
    public static void main(String[] args) throws Exception {
        // Cria servidor HTTP escutando na porta 8080
        HttpServer servidor = HttpServer.create(new InetSocketAddress(8080), 0);

        // --- HANDLERS PARA ARQUIVOS ESTÁTICOS (CSS) ---
        // O tipo MIME será detectado pela função enviarArquivo
        servidor.createContext("/styles.css", troca -> {
            enviarArquivo(troca, "src/styles/styles.css");
        });

        servidor.createContext("/global.css", troca -> {
            enviarArquivo(troca, "src/styles/global.css");
        });

        // --- HANDLER PARA SERVIR IMAGENS E OUTROS ATIVOS (assets) ---
        // Essencial para carregar a imagem de fundo do CSS
        servidor.createContext("/src/assets/", troca -> {
            // Obtém o caminho da URI de requisição (ex: /src/assets/image/foto.jpeg)
            String uriPath = troca.getRequestURI().getPath();

            // Remove a barra inicial para obter o caminho do arquivo local relativo (ex: src/assets/image/foto.jpeg)
            String caminhoArquivo = uriPath.startsWith("/") ? uriPath.substring(1) : uriPath;

            enviarArquivo(troca, caminhoArquivo);
        });
        // --------------------------------------------------------

        // --- HANDLERS PARA PÁGINAS HTML ---
        servidor.createContext("/", troca -> {
            enviarArquivo(troca, "login_e_senha.html");
        });
        servidor.createContext("/professor", troca -> {
            enviarArquivo(troca, "professor.html");
        });
        servidor.createContext("/aluno", troca -> {
            enviarArquivo(troca, "aluno.html");
        });
        servidor.createContext("/acessonegado", troca -> {
            enviarArquivo(troca, "acessonegado.html");
        });
        // --------------------------------------------------------

        /* INICIO DO CÓDIGO DE LOGIN */
        servidor.createContext("/login", troca -> {
            String query = troca.getRequestURI().getQuery();

            if (query == null || query.isEmpty()) {
                troca.getResponseHeaders().set("Location" ,"/");
                troca.sendResponseHeaders(302, -1);
                return;
            }

            String[] partes = query.split("&");

            // Assegura que temos pelo menos 3 partes (usuario, senha, perfil)
            if (partes.length < 3) {
                troca.getResponseHeaders().set("Location" ,"/acessonegado");
                troca.sendResponseHeaders(302, -1);
                return;
            }

            String usuario = partes[0].replace("usuario=","");
            String senha =  partes[1].replace("senha=","");


            // Decodificação da URL para tratar espaços (que vêm como '+')
            String perfilCodificado = partes[2].replace("selecionar+perfil=","");
            String perfil = java.net.URLDecoder.decode(perfilCodificado, StandardCharsets.UTF_8.name());

            System.out.println("Usuário: " + usuario + ", Perfil: " + perfil);


            // Lógica de autenticação
            if (usuario.equals("professor") && senha.equals("1234") &&  perfil.equals("professor")) {
                System.out.println("Acesso autorizado: Professor/Instrutor");
                troca.getResponseHeaders().set("Location" ,"/professor");
                troca.sendResponseHeaders(302, -1); //envio para a rota
            }

            if (usuario.equals("arieldias") && senha.equals("1234") &&  perfil.equals("professor")) {
                System.out.println("Acesso autorizado: Professor Ariel Dias");
                troca.getResponseHeaders().set("Location" ,"/professor");
                troca.sendResponseHeaders(302, -1); //envio para a rota
            }

            if (usuario.equals("eduardofalabella") && senha.equals("1234") &&  perfil.equals("professor")) {
                System.out.println("Acesso autorizado: Professor Eduardo Falabella");
                troca.getResponseHeaders().set("Location" ,"/professor");
                troca.sendResponseHeaders(302, -1); //envio para a rota
            }

            //Aluno
            if (usuario.equals("aluno") && senha.equals("1234") &&  perfil.equals("aluno")) {
                System.out.println("Acesso autorizado: Aluno");
                troca.getResponseHeaders().set("Location" ,"/aluno");
                troca.sendResponseHeaders(302, -1); //envio para a rota
            }

            if (usuario.equals("gustavonunes") && senha.equals("1234") &&  perfil.equals("aluno")) {
                System.out.println("Acesso autorizado: Aluno Gustavo Nunes");
                troca.getResponseHeaders().set("Location" ,"/aluno");
                troca.sendResponseHeaders(302, -1); //envio para a rota
            }

            if (usuario.equals("carinasouza") && senha.equals("1234") &&  perfil.equals("aluno")) {
                System.out.println("Acesso autorizado: Aluna Carina Souza");
                troca.getResponseHeaders().set("Location" ,"/aluno");
                troca.sendResponseHeaders(302, -1); //envio para a rota
            }

            if (usuario.equals("luizatimporini") && senha.equals("1234") &&  perfil.equals("aluno")) {
                System.out.println("Acesso autorizado: Aluna Luiza Timporini");
                troca.getResponseHeaders().set("Location" ,"/aluno");
                troca.sendResponseHeaders(302, -1); //envio para a rota
            }

            else{
                System.out.println("Acesso negado");
                troca.getResponseHeaders().set("Location" ,"/acessonegado");
                troca.sendResponseHeaders(302, -1);
            }
        });
        /* FIM DO CÓDIGO DE LOGIN */

        servidor.start();
        System.out.println("Servidor rodando em http://localhost:8080/");
    }

    // Função auxiliar para determinar o tipo MIME com base na extensão do arquivo
    private static String getMimeType(String fileName) {
        if (fileName.endsWith(".html")) return "text/html";
        if (fileName.endsWith(".css")) return "text/css";
        if (fileName.endsWith(".js")) return "application/javascript";
        if (fileName.endsWith(".json")) return "application/json";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".gif")) return "image/gif";
        if (fileName.endsWith(".svg")) return "image/svg+xml";
        // Tipo MIME padrão (se não for reconhecido)
        return "application/octet-stream";
    }

    // Envia um arquivo (HTML, CSS, Imagem, etc.), detectando o tipo MIME
    private static void enviarArquivo(HttpExchange troca, String caminho) throws IOException {
        File arquivo = new File(caminho);
        String tipo = getMimeType(caminho);

        System.out.println("Tentando servir arquivo: " + arquivo.getAbsolutePath() + " (Tipo: " + tipo + ")");

        if (!arquivo.exists() || !arquivo.isFile()) {
            System.out.println("ERRO 404: Arquivo não encontrado: " + arquivo.getAbsolutePath());
            // Envia resposta 404 (Not Found)
            String resposta = "404 - Arquivo não encontrado: " + caminho;
            byte[] bytes = resposta.getBytes(StandardCharsets.UTF_8);
            troca.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            troca.sendResponseHeaders(404, bytes.length);
            try (OutputStream os = troca.getResponseBody()) {
                os.write(bytes);
            }
            return;
        }

        try {
            byte[] bytes = Files.readAllBytes(arquivo.toPath());
            // Define o Content-Type correto
            troca.getResponseHeaders().set("Content-Type", tipo + "; charset=UTF-8");
            troca.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = troca.getResponseBody()) {
                os.write(bytes);
            }
        } catch (IOException e) {
            // Trata outros erros de I/O
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
            String resposta = "500 - Erro interno do servidor ao ler o arquivo.";
            byte[] bytes = resposta.getBytes(StandardCharsets.UTF_8);
            troca.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            troca.sendResponseHeaders(500, bytes.length);
            try (OutputStream os = troca.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // Função para enviar texto (mantida, mas não usada para arquivos)
    private static void enviarTexto(HttpExchange troca, String texto) throws IOException {
        byte[] bytes = texto.getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        troca.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = troca.getResponseBody()) {
            os.write(bytes);
        }
    }
}
