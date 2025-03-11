package br.com.alura.ScreenMatch.principal;

import br.com.alura.ScreenMatch.model.DadosEpisodio;
import br.com.alura.ScreenMatch.model.DadosSerie;
import br.com.alura.ScreenMatch.model.DadosTemporada;
import br.com.alura.ScreenMatch.model.Episodio;
import br.com.alura.ScreenMatch.services.ConsumoApi;
import br.com.alura.ScreenMatch.services.ConverterDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner sc = new Scanner(System.in);
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=689d4005";
    private ConsumoApi consumo = new ConsumoApi();
    private ConverterDados conversor = new ConverterDados();

    public void exibirMenu() {
        System.out.print("Digite o nome da série para a busca: ");
        var serie = sc.nextLine();
        var json = consumo.obterDados(ENDERECO + serie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();

        DadosTemporada dadosTemporada = null;
        for (int i = 1; i <= dados.totalTemporadas(); i++) {
            json = consumo.obterDados(ENDERECO + serie.replace(" ", "+").toLowerCase() + "&season=" + i + API_KEY);
            dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }
        temporadas.forEach(System.out::println);

        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());

        System.out.println();
        System.out.println("\nTop 5 episódios:");
        dadosEpisodios.stream()
                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                .limit(5)
                .forEach(e -> System.out.println(
                        "Titulo: " + e.titulo() +
                                ", Avaliação: " + e.avaliacao()
                ));

        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d))
                ).collect(Collectors.toList());

        System.out.println();
        episodios.forEach(System.out::println);

        System.out.println("Digite o nome de um episódio");
        var trechoTitulo = sc.nextLine();
        Optional<Episodio> episodioBuscado = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
                .findFirst();
        if (episodioBuscado.isPresent()) {
            System.out.println("Episódio encontrado!");
            System.out.println("Temporada: " + episodioBuscado.get().getTemporada());
        } else {
            System.out.println("Episodio não encontrado!");
        }

//        System.out.println("A partir de que ano você deseja ver os episódios?");
//        var ano = sc.nextInt();
//        sc.nextLine();
//
//        LocalDate anoBusca = LocalDate.of(ano, 1, 1);
//
//        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        episodios.stream()
//                .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(anoBusca))
//                .forEach(e -> System.out.println(
//                        "Temporada: " + e.getTemporada() +
//                        " Episódio: " + e.getTitulo() +
//                        " Data Lançamento: " + e.getDataLancamento().format(formatador)
//                ));

        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getAvaliacao)));
        System.out.println(avaliacoesPorTemporada);

        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
        System.out.println("Média: " + est.getAverage());
        System.out.println("Melhor episódio: " + est.getMax());
        System.out.println("Pior episódio: " + est.getMin());
        System.out.println("Quantidade de episódios: " + est.getCount());
    }
}
