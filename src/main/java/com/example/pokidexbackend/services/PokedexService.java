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
                System.out.println("https://pokeapi.co/api/v2/pokemon/" + evolutionChain.getJSONObject("chain").getJSONObject("species").get("name"));
                zaehler++;
            }
            if (evolutionChain.getJSONObject("chain").getJSONArray("evolves_to").length() > 0) {
                p2 = new JSONObject(getUrlContent("https://pokeapi.co/api/v2/pokemon/" + evolutionChain.getJSONObject("chain").getJSONArray("evolves_to").getJSONObject(0).getJSONObject("species").get("name")));
                System.out.println("https://pokeapi.co/api/v2/pokemon/" + evolutionChain.getJSONObject("chain").getJSONArray("evolves_to").getJSONObject(0).getJSONObject("species").get("name"));
                zaehler++;

                if (evolutionChain.getJSONObject("chain").getJSONArray("evolves_to").getJSONObject(0).getJSONArray("evolves_to").length() > 0) {
                    p3 = new JSONObject(getUrlContent("https://pokeapi.co/api/v2/pokemon/" + evolutionChain.getJSONObject("chain").getJSONArray("evolves_to").getJSONObject(0).getJSONArray("evolves_to").getJSONObject(0).getJSONObject("species").get("name")));
                    System.out.println("https://pokeapi.co/api/v2/pokemon/" + evolutionChain.getJSONObject("chain").getJSONArray("evolves_to").getJSONObject(0).getJSONArray("evolves_to").getJSONObject(0).getJSONObject("species").get("name"));
                    zaehler++;
                }
            }
            System.out.println(zaehler);
            System.out.println("----------------------------------------------------");
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

