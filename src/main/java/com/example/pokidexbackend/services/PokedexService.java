package com.example.pokidexbackend.services;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class PokedexService {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String getUrlContent(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        return response.body();
    }

    private JSONObject mapToPokedexContent(JSONObject pokemon) {
        JSONObject relevantPokemonContent = new JSONObject();
        relevantPokemonContent.put("name", pokemon.get("name"));
        relevantPokemonContent.put("img", pokemon.getJSONObject("sprites")
                .getJSONObject("other")
                .getJSONObject("official-artwork")
                .get("front_default"));
        relevantPokemonContent.put("types", pokemon.getJSONArray("types"));
        relevantPokemonContent.put("id", pokemon.get("id"));
        return relevantPokemonContent;
    }

    private JSONObject mapToPokemonDetailContent(JSONObject pokemon) {
        JSONObject pokemonDetailContent = new JSONObject();
        pokemonDetailContent.put("name", pokemon.get("name"));
        pokemonDetailContent.put("img", pokemon.getJSONObject("sprites")
                .getJSONObject("other")
                .getJSONObject("official-artwork")
                .get("front_default"));
        pokemonDetailContent.put("types", pokemon.getJSONArray("types"));
        pokemonDetailContent.put("id", pokemon.get("id"));
        pokemonDetailContent.put("height", pokemon.get("height"));
        pokemonDetailContent.put("weight", pokemon.get("weight"));

        JSONArray rawStats = pokemon.getJSONArray("stats");
        JSONArray detailedStats = new JSONArray();
        for (int i = 0; i < rawStats.length(); i++) {
            detailedStats.put(rawStats.getJSONObject(i).get("base_stat"));
        }

        pokemonDetailContent.put("stats", detailedStats);
        pokemonDetailContent.put("abilities", pokemon.getJSONArray("abilities"));
        pokemonDetailContent.put("moves", pokemon.getJSONArray("moves"));
        return pokemonDetailContent;
    }

    private JSONObject mapToEvolutionChainContent(JSONObject evolutionChain) {
        JSONObject evolutionChainContent = new JSONObject();
        JSONObject evolves_from = new JSONObject();
        JSONObject evolves_to1;
        JSONObject evolves_to2;
        JSONObject p1 = null;
        JSONObject p2 = null;
        JSONObject p3 = null;
        int zaehler = 0;
        try {
            if (!evolutionChain.getJSONObject("chain").getJSONObject("species").get("name").equals("")) {
                p1 = new JSONObject(getUrlContent("https://pokeapi.co/api/v2/pokemon/" + evolutionChain.getJSONObject("chain").getJSONObject("species").get("name")));
                zaehler++;
            }
            if (!evolutionChain.getJSONObject("chain").getJSONArray("evolves_to").isEmpty()) {
                p2 = new JSONObject(getUrlContent("https://pokeapi.co/api/v2/pokemon/" + evolutionChain.getJSONObject("chain").getJSONArray("evolves_to").getJSONObject(0).getJSONObject("species").get("name")));
                zaehler++;

                if (!evolutionChain.getJSONObject("chain").getJSONArray("evolves_to").getJSONObject(0).getJSONArray("evolves_to").isEmpty()) {
                    p3 = new JSONObject(getUrlContent("https://pokeapi.co/api/v2/pokemon/" + evolutionChain.getJSONObject("chain").getJSONArray("evolves_to").getJSONObject(0).getJSONArray("evolves_to").getJSONObject(0).getJSONObject("species").get("name")));
                    zaehler++;
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (p1 != null) {
            evolves_from.put("name", p1.get("name"));
            evolves_from.put("img", p1.getJSONObject("sprites")
                    .getJSONObject("other")
                    .getJSONObject("official-artwork")
                    .get("front_default"));
        }

        if (p2 != null) {
            evolves_to1 = new JSONObject();
            evolves_to1.put("name", p2.get("name"));
            evolves_to1.put("img", p2.getJSONObject("sprites")
                    .getJSONObject("other")
                    .getJSONObject("official-artwork")
                    .get("front_default"));
            evolves_to1.put("atLvl", evolutionChain.getJSONObject("chain").getJSONArray("evolves_to").getJSONObject(0).getJSONArray("evolution_details").getJSONObject(0).get("min_level"));
            if (p3 != null) {
                evolves_to2 = new JSONObject();
                evolves_to2.put("name", p3.get("name"));
                evolves_to2.put("img", p3.getJSONObject("sprites")
                        .getJSONObject("other")
                        .getJSONObject("official-artwork")
                        .get("front_default"));
                evolves_to2.put("atLvl", evolutionChain.getJSONObject("chain").getJSONArray("evolves_to").getJSONObject(0).getJSONArray("evolves_to").getJSONObject(0).getJSONArray("evolution_details").getJSONObject(0).get("min_level"));
                evolves_to1.put("evolvesTo", evolves_to2);
            }
            evolutionChainContent.put("evolvesTo", evolves_to1);
        }


        evolutionChainContent.put("evolvesFrom", evolves_from);
        evolutionChainContent.put("evolutionStates", zaehler);

        return evolutionChainContent;
    }

    public String getPokemonsData(int offset) throws IOException, InterruptedException {
        String url = "https://pokeapi.co/api/v2/pokemon?limit=12&offset=" + offset;
        String pokemonsJson = getUrlContent(url);

        JSONArray rawPokemonArray = new JSONObject(pokemonsJson).getJSONArray("results");
        JSONArray mappedPokemonArray = new JSONArray();

        for (int i = 0; i < rawPokemonArray.length(); i++) {
            JSONObject pokemon = rawPokemonArray.getJSONObject(i);
            String pokemonUrl = pokemon.getString("url");
            String pokemonJson = getUrlContent(pokemonUrl);
            mappedPokemonArray.put(mapToPokedexContent(new JSONObject(pokemonJson)));
        }

        return mappedPokemonArray.toString();
    }

    public String getPokemonDetailData(int id) throws IOException, InterruptedException {
        // #Name, #ID, #Types, Species, #Height, #Weight, #Abilities, Gender, Egg Groups, Egg Cycle,
        // #HP, #Attack, #Defense, #Sp. Atk, #Sp. Def, #Speed, #Evolution Informations, #Moves
        String pokemonJson = getUrlContent("https://pokeapi.co/api/v2/pokemon/" + id);
        return mapToPokemonDetailContent(new JSONObject(pokemonJson)).toString();
    }

    public String getEvolutionChainData(int id) throws IOException, InterruptedException {
        String evolutionChainJson = getUrlContent((String) (new JSONObject(getUrlContent((String) (new JSONObject(getUrlContent("https://pokeapi.co/api/v2/pokemon/" + id)).getJSONObject("species").get("url")))).getJSONObject("evolution_chain").get("url")));
        System.out.println(evolutionChainJson);
        System.out.println((String) new JSONObject(getUrlContent("https://pokeapi.co/api/v2/pokemon/" + id)).getJSONObject("species").get("url"));
        return mapToEvolutionChainContent(new JSONObject(evolutionChainJson)).toString();
    }

    public String getItemsData(int offset) throws IOException, InterruptedException {
        String url = "https://pokeapi.co/api/v2/item?limit=12&offset=" + offset;
        String itemsJson = getUrlContent(url);

        JSONArray rawItemsArray = new JSONObject(itemsJson).getJSONArray("results");
        JSONArray mappedItemsArray = new JSONArray();

        for (int i = 0; i < rawItemsArray.length(); i++) {
            JSONObject pokemon = rawItemsArray.getJSONObject(i);
            String itemUrl = pokemon.getString("url");
            String itemJson = getUrlContent(itemUrl);
            mappedItemsArray.put(mapToItemsContent(new JSONObject(itemJson)));
        }

        return mappedItemsArray.toString();
    }

    private JSONObject mapToItemsContent(JSONObject item) {
        JSONObject relevantItemContent = new JSONObject();
        relevantItemContent.put("id", item.get("id"));
        relevantItemContent.put("name", item.get("name"));
        relevantItemContent.put("img", item.getJSONObject("sprites").get("default"));
        relevantItemContent.put("category", item.getJSONObject("category").get("name"));

        return relevantItemContent;
    }

    public String getItemDetailData(int id) throws IOException, InterruptedException {
        String itemJson = getUrlContent("https://pokeapi.co/api/v2/item/" + id);

        return mapToItemDetailContent(new JSONObject(itemJson)).toString();
    }

    public JSONObject mapToItemDetailContent(JSONObject item) {
        JSONObject itemDetailContent = new JSONObject();
        itemDetailContent.put("name", item.get("name"));
        itemDetailContent.put("id", item.get("id"));
        itemDetailContent.put("img", item.getJSONObject("sprites").get("default"));

        JSONArray effectEntries = item.getJSONArray("effect_entries");
        for (int i = 0; i < effectEntries.length(); i++) {
            JSONObject effectEntry = effectEntries.getJSONObject(i);
                itemDetailContent.put("effect", effectEntry.get("effect"));
                itemDetailContent.put("short_effect", effectEntry.get("short_effect"));
                break;
        }

        itemDetailContent.put("category", item.getJSONObject("category").get("name"));
        itemDetailContent.put("cost", item.get("cost"));

        JSONArray attributes = new JSONArray();
        JSONArray rawAttributes = item.getJSONArray("attributes");
        for (int i = 0; i < rawAttributes.length(); i++) {
            attributes.put(rawAttributes.getJSONObject(i).get("name"));
        }
        itemDetailContent.put("attributes", attributes);

        return itemDetailContent;
    }
}

//      abilities: []
//      height: number
//      id: number
//      img: string
//      name: string
//      weight: number
//      types: []
// species: {} (Evolution)
//      stats: [] (HP, Attack, Defense, Sp. Atk, Sp. Def, Speed)
//      moves: []

