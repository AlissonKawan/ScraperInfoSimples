package com.stellarcraft;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Aplicação principal de Web Scraping para extrair dados de produtos
 * da loja fictícia Stellarcraft e salvar em formato JSON.
 */
public class ScraperApplication {

    // Representação dos dados finais do produto extraído
    public record Product(
            String title,
            String brand,
            List<String> categories,
            String description,
            String url,
            List<Sku> skus,
            @JsonProperty("specification") List<Specification> specifications,
            List<Review> reviews,
            @JsonProperty("reviews_average_score") Double reviewsAverageScore) {
    }

    public record Sku(
            String name,
            @JsonProperty("current_price") Double currentPrice,
            @JsonProperty("old_price") Double oldPrice,
            boolean available) {
    }

    public record Specification(
            String label,
            String value) {
    }

    public record Review(
            String name,
            String date,
            String text,
            Integer score) {
    }

    public static void main(String[] args) {
        try {
            // 1. Conecta e baixa o HTML da página do produto
            String targetUrl = "https://infosimples.com/vagas/desafio/stellarcraft/product.html";
            Document doc = Jsoup.connect(targetUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();
 
            // 2. Extrai as diferentes informações da página usando os métodos auxiliares
            String title = extractTitle(doc);
            String brand = extractBrand(doc);
            List<String> categories = extractCategories(doc);
            String description = extractDescription(doc);
            String url = extractUrl(doc);
            List<Sku> skus = extractSkus(doc);
            List<Specification> specs = extractSpecifications(doc);
            List<Review> reviews = extractReviews(doc);
            Double averageScore = extractAverageScore(doc);

            Product product = new Product(
                    title, brand, categories, description, url, skus, specs, reviews, averageScore);

            // 3. Converte o objeto Product em um arquivo JSON formatado
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            File outputFile = new File("produto.json");
            mapper.writeValue(outputFile, product);

            System.out.println(
                    "Extração concluída com sucesso! JSON estruturado salvo em: " + outputFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("Erro ao processar a página ou salvar o arquivo JSON: " + e.getMessage());
        }
    }

    private static String extractTitle(Document doc) {
        Element titleEl = doc.selectFirst("h1#product_title");
        return titleEl != null ? titleEl.text().trim() : null;
    }

    private static String extractBrand(Document doc) {
        Element brandEl = doc.selectFirst(".product-brand");
        return brandEl != null ? brandEl.text().trim() : null;
    }

    private static List<String> extractCategories(Document doc) {
        Elements breadcrumbs = doc.select(".breadcrumb-bar a");
        List<String> categories = new ArrayList<>();
        for (Element a : breadcrumbs) {
            categories.add(a.text().trim());
        }
        return categories;
    }

    private static String extractDescription(Document doc) {
        Elements paragraphs = doc.select("#tab-description p");
        List<String> texts = new ArrayList<>();
        for (Element p : paragraphs) {
            texts.add(p.text().trim());
        }
        return String.join("\n", texts);
    }

    private static String extractUrl(Document doc) {
        Element urlEl = doc.selectFirst("div[itemprop=offers] link[itemprop=url]");
        if (urlEl != null && urlEl.hasAttr("href")) {
            return urlEl.attr("href");
        }
        return doc.baseUri();
    }

    /**
     * Extrai as variações (SKUs) do produto, incluindo nome, preços e disponibilidade.
     */
    private static List<Sku> extractSkus(Document doc) {
        Elements variants = doc.select(".variant-list .variant-btn");
        List<Sku> skus = new ArrayList<>();

        for (Element variant : variants) {
            boolean isAvailable = !variant.hasClass("unavailable");

            Element vnameEl = variant.selectFirst(".vname");
            String name = null;
            if (vnameEl != null) {
                name = vnameEl.ownText().trim();
            }

            Double currentPrice = null;
            Double oldPrice = null;

            if (isAvailable) {
                String priceStr = variant.attr("data-price");
                if (priceStr.isBlank()) {
                    Element vpriceEl = variant.selectFirst(".vprice");
                    priceStr = vpriceEl != null ? vpriceEl.ownText() : "";
                }

                String oldPriceStr = variant.attr("data-old-price");
                if (oldPriceStr.isBlank()) {
                    Element vpriceOldEl = variant.selectFirst(".vprice-old");
                    oldPriceStr = vpriceOldEl != null ? vpriceOldEl.text() : "";
                }

                currentPrice = parsePrice(priceStr);
                oldPrice = parsePrice(oldPriceStr);
            }

            skus.add(new Sku(name, currentPrice, oldPrice, isAvailable));
        }
        return skus;
    }

    private static List<Specification> extractSpecifications(Document doc) {
        Elements rows = doc.select(".specs-table tr");
        List<Specification> specs = new ArrayList<>();

        for (Element row : rows) {
            Elements cols = row.select("td");
            if (cols.size() >= 2) {
                String label = cols.get(0).text().trim();
                String value = cols.get(1).text().trim();
                specs.add(new Specification(label, value));
            }
        }
        return specs;
    }

    /**
     * Extrai as avaliações dos clientes, incluindo nome, data, texto e nota (estrelas).
     */
    private static List<Review> extractReviews(Document doc) {
        Elements reviewCards = doc.select(".review-card");
        List<Review> reviews = new ArrayList<>();

        for (Element card : reviewCards) {
            Element nameEl = card.selectFirst(".reviewer-name");
            Element dateEl = card.selectFirst(".reviewer-date");
            Element textEl = card.selectFirst(".review-text");
            Element starsEl = card.selectFirst(".review-stars");

            String name = nameEl != null ? nameEl.text().trim() : null;
            String date = dateEl != null ? dateEl.text().trim() : null;
            String text = textEl != null ? textEl.text().trim() : null;
            Integer score = null;

            if (starsEl != null) {
                score = calculateScore(starsEl.text());
            }

            reviews.add(new Review(name, date, text, score));
        }
        return reviews;
    }

    private static Double extractAverageScore(Document doc) {
        Element avgScoreEl = doc.selectFirst(".avg-score");
        if (avgScoreEl != null) {
            try {
                return Double.parseDouble(avgScoreEl.text().replace(",", "."));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Converte uma string de preço (ex: "R$ 1.234,56") para um valor numérico do tipo Double.
     */
    private static Double parsePrice(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            return null;
        }
        String cleanStr = priceStr.replaceAll("[^\\d,-]", "");
        cleanStr = cleanStr.replace(",", ".");

        if (cleanStr.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(cleanStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer calculateScore(String starsStr) {
        int count = 0;
        for (char c : starsStr.toCharArray()) {
            if (c == '★' || c == '\u2605') {
                count++;
            }
        }
        return count;
    }
}