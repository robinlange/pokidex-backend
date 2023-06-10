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

