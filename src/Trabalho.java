import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

class UnitTest {
    public static void main(String[] args) throws Exception {
        Trabalho.inicializar("exemplo", 1, 16);
    }
}

class UnitTest2 {
    public static void main(String[] args) throws Exception {
        Trabalho.inicializar("exemplo2", 2, 16);
    }
}

class UnitTest3 {
    public static void main(String[] args) throws Exception {
        Trabalho.inicializar("exemplo2", 3, 16);
    }
}

public class Trabalho {

    private static Scanner scanner = new Scanner(System.in);

    private static Queue<String> instrucoes = new LinkedList<>();

    public static void main(String[] args) throws Exception {

        System.out.println("Digite o caso de teste");
        String casoTeste = scanner.nextLine();

        System.out.println("Informe o tamanho da memória principal");

        int tamanho = scanner.nextInt();

        System.out.println("Escolha a estratégia de alocação");
        System.out.println("1) partições fixas de mesmo tamanho");
        System.out.println("2) partições variáveis");
        System.out.println("3) partições definidas com o sistema buddy");

        int opcao = scanner.nextInt();

        inicializar(casoTeste, opcao, tamanho);

    }

    public static void inicializar(String casoTeste, int opcao, int tamanho) throws Exception {

        carregarArquivo(casoTeste);

        if (1 == opcao) {
            particoesFixas(tamanho);
        } else if (2 == opcao) {
            particoesVariaveis(tamanho);
        } else if (3 == opcao) {
            particoesBuddy(tamanho);
        }
    }

    private static void carregarArquivo(String fileName) throws Exception {
        final InputStream in = Trabalho.class
                .getClassLoader()
                .getResourceAsStream(fileName);

        final BufferedReader br = new BufferedReader(new InputStreamReader(in));

        while (br.ready()) {
            String linha = br.readLine();
            instrucoes.offer(linha);
        }

        br.close();
    }

    private static void particoesFixas(int tamanho) {
        System.out.println("Digite o tamanho das partições");
        int tamanhoParticoes = scanner.nextInt();


        processar(new ParticoesTamanhoFixo(tamanhoParticoes, tamanho));

    }

    private static void particoesVariaveis(int tamanho) {
        System.out.println("Selecione a política desejada");
        System.out.println("1) Best-fit");
        System.out.println("2) Worst-fit");
        int politica = scanner.nextInt();

        processar(new ParticoesTamanhoVariavel(tamanho, politica));


    }

    private static void particoesBuddy(int tamanho) {

        processar(new ParticoesBuddy(tamanho));
    }

    private static void processar(EstrategiaAlocacao estrategiaAlocacao) {
        while (!instrucoes.isEmpty()) {
            String instrucao = instrucoes.poll();

            String acao = instrucao.split("\\(")[0];
            char programa = instrucao.split("\\(")[1].charAt(0);

            if (acao.equals("IN")) {
                int tamanho = Integer.parseInt(instrucao.split("\\(")[1].split(",")[1].split("\\)")[0].trim());
                estrategiaAlocacao.alocar(programa, tamanho);
            } else if (acao.equals("OUT")) {
                estrategiaAlocacao.desalocar(programa);
            }

            System.out.printf("%-10s%-5s%-60s", instrucao, " | ", estrategiaAlocacao.imprimir());
            System.out.println();
        }
    }
}

class ParticoesBuddy implements EstrategiaAlocacao {

    private Memoria memoria;

    public ParticoesBuddy(int tamanho) {
        this.memoria = new Memoria(tamanho);
        memoria.particoes.offer(new ParticaoBuddy(tamanho));
    }

    @Override
    public void alocar(char processo, int tamanho) {
        ParticaoBuddy particao = (ParticaoBuddy) memoria.particoes.getFirst();
        ParticaoBuddy particaoBuddy = buscarParticaoIdeal(particao, tamanho);

        if (particaoBuddy!= null && particaoBuddy.getEspacoLivre() >= tamanho) {

            particaoBuddy.alocar(processo, tamanho);
            particao.tamanhoUtilizado += tamanho;


        } else {
            System.out.println("Não foi possível alocar o processo");
        }

        System.out.println(particao.toString());
    }

    @Override
    public void desalocar(char p) {

        ParticaoBuddy particao = (ParticaoBuddy) memoria.particoes.getFirst();

        ParticaoBuddy particaoParaRemover = buscarParticao(particao, p);

        particaoParaRemover.desalocar();

        ParticaoBuddy pai = particaoParaRemover.pai;

        remover(pai);

        System.out.println(particao.toString());
    }

    private void remover(ParticaoBuddy particaoBuddy) {

        if (particaoBuddy != null  && (particaoBuddy.esquerda == null || particaoBuddy.esquerda.estaCompletamenteLivre()) &&
                (particaoBuddy.direita == null || particaoBuddy.direita.estaCompletamenteLivre())) {

            particaoBuddy.esquerda = null;
            particaoBuddy.direita = null;
            remover(particaoBuddy.pai);
        }
    }

    public ParticaoBuddy buscarParticao(ParticaoBuddy particao, char processo) {
        if (particao == null || particao.status == processo) {
            return particao;
        }

        ParticaoBuddy p = buscarParticao(particao.esquerda, processo);

        if (p == null) {
            p = buscarParticao(particao.direita, processo);
        }

        return p;
    }


    @Override
    public String imprimir() {
        return null;
    }

    private ParticaoBuddy buscarParticaoIdeal(ParticaoBuddy particao, int tamanho) {
        int tamanhoProximasParticoes = particao.tamanhoTotal/2;

        //qnd encontrou
         if (tamanhoProximasParticoes < tamanho && particao.estaLivre() && particao.getEspacoLivre() >= tamanho) {
            return particao;
        } else {
             if (particao.esquerda == null && particao.estaLivre()) {
                 particao.dividir();
             }

             if (particao.esquerda.getEspacoLivre() >= tamanho  && particao.esquerda.estaLivre()) {
                 return buscarParticaoIdeal(particao.esquerda, tamanho);
             } else if (particao.direita.getEspacoLivre() >= tamanho && particao.direita.estaLivre()){
                 return buscarParticaoIdeal(particao.direita, tamanho);
             } else {
                 return null;
             }
        }

    }
}

class ParticoesTamanhoVariavel implements EstrategiaAlocacao {

    private Memoria memoria;
    private int politica;

    private static final int BEST_FIT = 1;
    private static final int WORST_FIT = 2;

    public ParticoesTamanhoVariavel(int tamanhoMemoria, int politica) {
        this.memoria = new Memoria(tamanhoMemoria);
        this.politica = politica;
    }

    @Override
    public void alocar(char processo, int tamanho) {
        int espacoLivre = memoria.buscarEspacoLivreContiguo();

        Optional<Particao> particaoLivreComEspacoSuficiente = buscarParticao(tamanho);

        int espacoSobrandoEmParticoes = memoria
                .particoes
                .stream()
                .mapToInt(Particao::getEspacoLivre)
                .sum();

        if (particaoLivreComEspacoSuficiente.isPresent()) {

            ParticaoNormal p = (ParticaoNormal) particaoLivreComEspacoSuficiente.get();

            if (WORST_FIT == politica) {
                if (p.tamanhoAlocado >= espacoLivre) {
                    memoria.alocar(processo, tamanho, p);
                } else {
                    ParticaoNormal nova = memoria.criarParticao(tamanho);

                    memoria.alocar(processo, tamanho, nova);
                }
            }

            if (BEST_FIT == politica) {
                if (p.tamanhoAlocado <= espacoLivre) {
                    memoria.alocar(processo, tamanho, p);
                } else {
                    ParticaoNormal nova = memoria.criarParticao(tamanho);

                    memoria.alocar(processo, tamanho, nova);
                }
            }

        } else if (espacoLivre >= tamanho) {
            ParticaoNormal p = memoria.criarParticao(tamanho);

            memoria.alocar(processo, tamanho, p);
        } else if ((espacoLivre + espacoSobrandoEmParticoes) >= tamanho ){
            relocarProcessos();

            memoria.alocar(processo, tamanho);
        } else {
            System.out.println("Espaço em disco insuficiente.");
        }
    }

    private Optional<Particao> buscarParticao(int tamanho) {
        if (BEST_FIT == politica) {
            return memoria.particoes
                    .stream()
                    .filter(p -> p.estaLivre() && p.getTamanhoAlocado() >= tamanho)
                    .sorted(Comparator.comparingInt(p -> p.getTamanhoAlocado()))
                    .findFirst();
        } else if (WORST_FIT == politica){
            return memoria.particoes
                    .stream()
                    .filter(p -> p.estaLivre() && p.getTamanhoAlocado() >= tamanho)
                    .sorted(Comparator.comparingInt((Particao p) -> p.getTamanhoAlocado()).reversed())
                    .findFirst();
        }

        return Optional.empty();
    }

    private void relocarProcessos() {

        Memoria memoria = new Memoria(this.memoria.tamanhoTotal);

        for (Particao p: memoria.particoes) {
            ParticaoNormal particao = (ParticaoNormal) p;
            if (!particao.estaLivre()) {
                memoria.alocar(particao.status, particao.tamanhoUtilizado);
            }
        }

        this.memoria = memoria;

    }

    @Override
    public void desalocar(char programa) {
        memoria.desalocar(programa);
    }

    @Override
    public String imprimir() {
        return ImpressaoUtils.imprimir(memoria);
    }
}


class ParticoesTamanhoFixo implements EstrategiaAlocacao {

    private final Memoria memoria;

    public ParticoesTamanhoFixo(int tamanhoParticao, int tamanhoMemoria) {

        int numParticoes = tamanhoMemoria / tamanhoParticao;

        this.memoria = new Memoria(numParticoes, tamanhoParticao);
    }

    @Override
    public void alocar(char processo, int tamanho) {
        int particao = memoria.alocar(processo, tamanho);

        if (particao == -1) {
            System.out.println("Espaço em disco insuficiente.");
        }
    }

    @Override
    public void desalocar(char programa) {
        memoria.desalocar(programa);
    }

    @Override
    public String imprimir() {
        return ImpressaoUtils.imprimir(memoria);
    }
}

interface EstrategiaAlocacao {

    void alocar(char programa, int tamanho);

    void desalocar(char programa);

    String imprimir();
}

class Memoria {
    public LinkedList<Particao> particoes;
    public int tamanhoTotal;

    public Memoria(int numParticoes, int tamanho) {
        this.particoes = new LinkedList<>();

        for (int i = 0; i < numParticoes; i++) {
            this.particoes.offer(new ParticaoNormal(i, tamanho, i * tamanho));
        }

        this.tamanhoTotal = numParticoes * tamanho;
    }

    public Memoria(int tamanhoTotal) {
        this.particoes = new LinkedList<>();
        this.tamanhoTotal = tamanhoTotal;
    }

    public ParticaoNormal criarParticao(int tamanho) {
        int tamanhoDisponivel = buscarEspacoLivreContiguo();

        if (tamanhoDisponivel >= tamanho) {
            int proximoInicio = 0;

            if (!this.particoes.isEmpty()) {

                proximoInicio = this.particoes
                        .getLast()
                        .getInicio()+ this.particoes.getLast().getTamanhoAlocado();
            }

            ParticaoNormal p = new ParticaoNormal(this.particoes.size(), tamanho, proximoInicio);
            particoes.offer(p);
            return p;
        } else {
            System.out.println("Espaço em disco insuficiente.");
            return null;
        }
    }

    public int alocar(char processo, int tamanho) {
        ParticaoNormal particaoNormal = buscarParticaoLivre(tamanho);

        if (particaoNormal != null) {
            this.alocar(processo, tamanho, particaoNormal);
            return particaoNormal.numParticao;
        } else {
            return -1;
        }
    }

    public void alocar(char processo, int tamanho, ParticaoNormal particaoNormal) {
        particaoNormal.alocar(processo, tamanho);
    }

    public void desalocar(char processo) {
        particoes.stream()
                .filter(p -> p.getStatus() == processo)
                .findFirst()
                .ifPresent(p -> p.desalocar());
    }

    public ParticaoNormal buscarParticaoLivre(int tamanhoNecessario) {
        return (ParticaoNormal) particoes
                .stream()
                .sorted(Comparator.comparingInt(q -> q.getNumParticao()))
                .filter(particao -> particao.estaLivre() && particao.getTamanhoAlocado() >= tamanhoNecessario)
                .findFirst()
                .orElse(null);
    }

    public int buscarEspacoLivreContiguo() {
        int totalAlocado = particoes
                .stream()
                .mapToInt(p -> p.getTamanhoAlocado())
                .sum();

        return tamanhoTotal - totalAlocado;
    }
}

interface Particao {
    void alocar(char processo, int tamanho);
    void desalocar();
    boolean estaLivre();
    int getTamanhoAlocado();
    int getInicio();
    int getEspacoLivre();
    char getStatus();
    int getNumParticao();
}

class ParticaoBuddy implements Particao{

    public ParticaoBuddy pai;
    public ParticaoBuddy esquerda;
    public ParticaoBuddy direita;
    public int tamanhoTotal;
    public int tamanhoUtilizado;
    public char status;

    public ParticaoBuddy(int tamanhoTotal) {
        this.tamanhoTotal = tamanhoTotal;
        this.tamanhoUtilizado = 0;
        this.status = ParticaoNormal.BURACO;
    }

    public ParticaoBuddy(ParticaoBuddy pai, int tamanhoTotal) {
        this.pai = pai;
        this.tamanhoTotal = tamanhoTotal;
        this.tamanhoUtilizado = 0;
        this.status = ParticaoNormal.BURACO;
    }

    public void incrementar(int tamanho) {
        this.tamanhoUtilizado += tamanho;

        if (this.pai != null) {
            this.pai.incrementar(tamanho);
        }
    }

    @Override
    public void alocar(char processo, int tamanho) {
        this.status = processo;
        this.tamanhoUtilizado = tamanho;

        if (pai != null) {
            pai.status += 1;
            pai.incrementar(tamanho);
        }
    }

    @Override
    public void desalocar() {
        if (pai != null) {
            des(this.tamanhoUtilizado);
        }

        this.status = ParticaoNormal.BURACO;
        this.tamanhoUtilizado = 0;
    }

    private void des(int tamanho) {
        if (pai != null) {
            pai.des(tamanho);
            if (pai.status > 0) {
                pai.status -= 1;
            }
            pai.tamanhoUtilizado -= tamanho;
        }
    }

    @Override
    public boolean estaLivre() {
        return ParticaoNormal.BURACO == this.status || (ParticaoNormal.DIVIDIDO == this.status || ParticaoNormal.PARCIALMENTE_COMPLETO == this.status);
    }

    public boolean estaCompletamenteLivre() {
        return ParticaoNormal.BURACO == this.status;
    }

    @Override
    public int getTamanhoAlocado() {
        return tamanhoTotal;
    }

    @Override
    public int getInicio() {
        return -1;
    }

    @Override
    public int getEspacoLivre() {
        return tamanhoTotal - tamanhoUtilizado;
    }

    @Override
    public char getStatus() {
        return status;
    }

    @Override
    public int getNumParticao() {
        return -1;
    }

    public void dividir() {
        if (this.tamanhoTotal > 1) {
            int novoTamanho = this.tamanhoTotal / 2;

            this.direita = new ParticaoBuddy(this, novoTamanho);
            this.esquerda = new ParticaoBuddy(this, novoTamanho);
            this.status = ParticaoNormal.DIVIDIDO;
        }
    }

    public StringBuilder toString(StringBuilder prefix, boolean isTail, StringBuilder sb) {
        if(this.direita!=null) {
            this.direita.toString(new StringBuilder().append(prefix).append(isTail ? "│   " : "    "), false, sb);
        }
        sb.append(prefix).append(isTail ? "└── " : "┌── ").append(this.status + "(" + (this.tamanhoTotal) + ")").append("\n");
        if(this.esquerda!=null) {
            this.esquerda.toString(new StringBuilder().append(prefix).append(isTail ? "    " : "│   "), true, sb);
        }
        return sb;
    }

    @Override
    public String toString() {
        return this.toString(new StringBuilder(), true, new StringBuilder()).toString();
    }
}

class ParticaoNormal implements Particao{

    public static final char BURACO = '0';
    public static final char DIVIDIDO = '1';
    public static final char PARCIALMENTE_COMPLETO = '2';
    public static final char COMPLETO = '3';

    public int numParticao;
    public int inicio;
    public int tamanhoAlocado;
    public int tamanhoUtilizado;
    public char status;

    public ParticaoNormal(int numParticao, char processo, int inicio, int tamanhoTotal, int tamanhoUtilizado) {
        this.numParticao = numParticao;
        this.inicio = inicio;
        this.tamanhoAlocado = tamanhoTotal;
        this.tamanhoUtilizado = tamanhoUtilizado;
        this.status = processo;
    }

    public ParticaoNormal(int numParticao, int tamanhoTotal, int inicio) {
        this.status = BURACO;
        this.inicio = inicio;
        this.numParticao = numParticao;
        this.tamanhoAlocado = tamanhoTotal;
        this.tamanhoUtilizado = 0;
    }

    @Override
    public void alocar(char processo, int tamanho) {
        this.status = processo;
        this.tamanhoUtilizado = tamanho;
    }

    @Override
    public void desalocar() {
        this.status = BURACO;
        this.tamanhoUtilizado = 0;
    }

    @Override
    public boolean estaLivre() {
        return BURACO == this.status;
    }

    @Override
    public int getTamanhoAlocado() {
        return tamanhoAlocado;
    }

    @Override
    public int getInicio() {
        return inicio;
    }

    @Override
    public int getEspacoLivre() {
        return tamanhoAlocado - tamanhoUtilizado;
    }

    @Override
    public char getStatus() {
        return status;
    }

    @Override
    public int getNumParticao() {
        return numParticao;
    }
}

class ImpressaoUtils {

    public static String imprimir(Memoria memoria) {
        StringBuilder str = new StringBuilder("     ");

        boolean[] b = new boolean[memoria.tamanhoTotal];

        for (Particao p : memoria.particoes) {

            for (int aux = p.getInicio(); aux < p.getInicio() + p.getTamanhoAlocado()-p.getEspacoLivre(); aux++) {
                b[aux] = true;
            }
        }

        int cont = 0;

        for (int i = 0; i<b.length; i++) {
            if (!b[i]) {
                cont++;
            } else if (cont > 0) {
                criarBloco(cont, str);
                cont = 0;
            }
        }

        if (cont > 0) {
            criarBloco(cont, str);
        }

        return str.append("|").toString();
    }

    private static void criarBloco(int uso, StringBuilder str) {
        str.append("| " + uso);
    }

}