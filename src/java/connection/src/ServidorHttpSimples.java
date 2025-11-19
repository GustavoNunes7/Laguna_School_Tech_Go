import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ServidorHttpSimples {
    public static void main(String[] args) throws Exception {
        // Cria servidor HTTP escutando na porta 8080
        HttpServer servidor = HttpServer.create(new InetSocketAddress(8080), 0);


        servidor.createContext("/styles.css", troca -> {
            enviarArquivo(troca, "src/styles/styles.css", "text/css");
        });

        servidor.createContext("/global.css", troca -> {
            enviarArquivo(troca, "src/styles/global.css", "text/css");
        });

        /* INICIO DO CÓDIGO */
        //C:\Users\Aluno\Desktop\Dev\Laguna_School_Tech_Go\Login_e_senha.html
        servidor.createContext("/", troca -> {
            enviarArquivo(troca, "login_e_senha.html", "text/html");
        });
        servidor.createContext("/professor", troca -> {
            enviarArquivo(troca, "professor.html", "text/html");
        });
        servidor.createContext("/aluno", troca -> {
            enviarArquivo(troca, "aluno.html", "text/html");
        });
        servidor.createContext("/acessonegado", troca -> {
            enviarArquivo(troca, "acessonegado.html", "text/html");
        });




        servidor.createContext("/login", troca -> {
            String query = troca.getRequestURI().getQuery();
            //C:\Users\Aluno\Desktop\Dev\Laguna_School_Tech_Go\Login_e_senha.html
            String[] partes;
            partes = query.split("&");

            String usuario = partes[0].replace("usuario=","");
            String senha =  partes[1].replace("senha=","");
            String perfil = partes[2].replace("selecionar+perfil=","");;
            System.out.println(perfil);


            //Professor/Instrutor
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

        /* FIM DO CÓDIGO */

        servidor.start();
        System.out.println("Servidor rodando em http://localhost:8080/");
    }

    // Envia um arquivo (HTML ou CSS)
    private static void enviarArquivo(com.sun.net.httpserver.HttpExchange troca, String caminho, String tipo) throws IOException {
        File arquivo = new File(caminho);
        System.out.println(" " + arquivo.getAbsolutePath());
        if (!arquivo.exists()) {
            System.out.println("Arquivo não encontrado: " + arquivo.getAbsolutePath());
        }
        byte[] bytes = Files.readAllBytes(arquivo.toPath());
        troca.getResponseHeaders().set("Content-Type", tipo + "; charset=UTF-8");
        troca.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = troca.getResponseBody()) {
            os.write(bytes);
        }
    }

    // Envia resposta HTML gerada no código
    private static void enviarTexto(com.sun.net.httpserver.HttpExchange troca, String texto) throws IOException {
        byte[] bytes = texto.getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        troca.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = troca.getResponseBody()) {
            os.write(bytes);
        }
    }
}
