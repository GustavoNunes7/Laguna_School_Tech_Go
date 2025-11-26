import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.net.URLDecoder;

public class ServidorHttpSimples {

    private static Connection conexao;

    public static void main(String[] args) throws Exception {
        // Conexão com SQLite (arquivo local)
        try {
            conexao = DriverManager.getConnection("jdbc:sqlite:atividades.db");
            inicializarBanco();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        HttpServer servidor = HttpServer.create(new InetSocketAddress(8080), 0);

        // Página inicial
        servidor.createContext("/", troca -> enviarArquivoHTML(troca, "professor.html"));

        // Página de cadastro (GET) e envio (POST)
        servidor.createContext("/adicionar_atividade", troca -> {
            String metodo = troca.getRequestMethod();

            if (metodo.equalsIgnoreCase("GET")) {
                enviarArquivoHTML(troca, "adicionar atividades.html");
                return;
            }

            if (metodo.equalsIgnoreCase("POST")) {
                // Lê o corpo da requisição (formulário)
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(troca.getRequestBody(), StandardCharsets.UTF_8));
                String corpo = br.readLine(); // Exemplo: nome=abc&artista=xyz&ano=2020

                // Decodifica e extrai os valores manualmente
                corpo = URLDecoder.decode(corpo, StandardCharsets.UTF_8);

                // Corpo da string url: nome=Ariel Dias&artista=Zeca Pagodinho&ano=2024
                String tarefa = corpo.substring(corpo.indexOf("tarefa=") + 5, corpo.indexOf("&materia="));
                String materia = corpo.substring(corpo.indexOf("materia=") + 8, corpo.indexOf("&dia="));
                String dia = corpo.substring(corpo.indexOf("dia=") + 4, corpo.indexOf("&mes"));
                String mes = corpo.substring(corpo.indexOf("mes=") + 4, corpo.indexOf("&observacao"));
                String obeservacao = corpo.substring(corpo.indexOf("observacao=") + 4);


                inserirAtividade(tarefa, materia, dia, mes, obeservacao);

                String resposta = """
            <html>
              <body>
                <h2>Atividade adicionada com sucesso!</h2>
                <a href='/'>Voltar</a>
              </body>
            </html>
            """;

                enviarResposta(troca, resposta);
            }
        });

        // Página de listagem (gera HTML dinâmico)
        servidor.createContext("/atividade_afazer", troca -> {
            StringBuilder html = new StringBuilder();
            html.append("<html><head><meta charset='UTF-8'><link rel='stylesheet' href='estilo.css'></head><body>");
            html.append("  <h2>Atividades para fazer</h2><table border='1'><tr><th>ID</th><th>Tarefas</th><th>Matéria</th><th>Dia</th><th>Mês</th><th>Obeservação</th></tr>");

            try (Statement st = conexao.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM atividades")) {
                while (rs.next()) {
                    html.append("<tr><td>").append(rs.getInt("id")).append("</td>")
                            .append("<td>").append(rs.getString("tarefa")).append("</td>")
                            .append("<td>").append(rs.getString("dia")).append("</td>")
                            .append("<td>").append(rs.getInt("mes")).append("</td></tr>")
                            .append("<td>").append(rs.getInt("observacao")).append("</td></tr>");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            html.append("</table><a href='/'>Voltar</a></body></html>");
            enviarResposta(troca, html.toString());
        });

        // Servir o CSS
        servidor.createContext("/estilo.css", troca -> enviarArquivoCSS(troca, "estilo.css"));

        servidor.start();
        System.out.println("Servidor rodando em http://localhost:8080/");
    }

    // 🔹 Criação da tabela, se ainda não existir
    private static void inicializarBanco() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS atividades (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "tarefa TEXT NOT NULL," +
                    "dia TEXT NOT NULL," +
                    "mes TEXT NOT NULL," +
                    "observacao INTEGER NOT NULL)";
            conexao.createStatement().execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🔹 Inserção
    private static void inserirAtividade(tarefa, materia, dia, mes, obeservacao) {
        try {
            String sql = "INSERT INTO atividades (nome, artista, ano) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setString(1, tarefa);
                ps.setString(2, materia);
                ps.setString(3, dia);
                ps.setString(4, mes);
                ps.setString(5, observacao);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🔹 Envio de arquivos HTML
    private static void enviarArquivoHTML(HttpExchange troca, String caminhoArquivo) throws IOException {
        File arquivo = new File("src/main/java/" + caminhoArquivo);
        System.out.println(arquivo);
        if (!arquivo.exists()) {
            enviarResposta(troca, "<h1>Arquivo não encontrado</h1>");
            return;
        }
        byte[] bytes = java.nio.file.Files.readAllBytes(arquivo.toPath());
        troca.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        troca.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = troca.getResponseBody()) {
            os.write(bytes);
        }
    }

    // 🔹 Envio do CSS
    private static void enviarArquivoCSS(HttpExchange troca, String caminhoArquivo) throws IOException {
        File arquivo = new File("src/main/java/" + caminhoArquivo);

        byte[] bytes = java.nio.file.Files.readAllBytes(arquivo.toPath());
        troca.getResponseHeaders().set("Content-Type", "text/css; charset=UTF-8");
        troca.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = troca.getResponseBody()) {
            os.write(bytes);
        }
    }

    // 🔹 Envio de respostas HTML simples
    private static void enviarResposta(HttpExchange troca, String resposta) throws IOException {
        byte[] bytes = resposta.getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        troca.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = troca.getResponseBody()) {
            os.write(bytes);
        }
    }
}