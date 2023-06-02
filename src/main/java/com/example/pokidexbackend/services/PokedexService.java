package com.example.pokidexbackend.services;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

@Service
public class PokedexService {

    public String getData(int offset) throws IOException {
        String pokemonsJson = getJson("https://pokeapi.co/api/v2/pokemon?limit=12&offset=${" + offset + "}");

        String pokemons = new JSONObject(pokemonsJson).get("results").toString();

        JSONArray rawPokemonArray = new JSONArray(pokemons);
        JSONArray mappedPokemonArray = new JSONArray();

        for (int i = 0; i < rawPokemonArray.length(); i++) {
            JSONObject pokemon = rawPokemonArray.getJSONObject(i);
            mappedPokemonArray.put(mapToPokdexContent(new JSONObject(getJson(pokemon.get("url").toString()))));
        }
        return mappedPokemonArray.toString();
    }

    private String getJson(String url) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openConnection().getInputStream()));
        String inputLine;
        StringBuilder erg = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            erg.append(inputLine);
        }

        in.close();

        return erg.toString();
    }

    private JSONObject mapToPokdexContent(JSONObject pokemon) {
        JSONObject relevantPokemonContent = new JSONObject();
        relevantPokemonContent.put("name", pokemon.get("name"));
        relevantPokemonContent.put("img", pokemon.getJSONObject("sprites")
                .getJSONObject("other")
                .getJSONObject("official-artwork")
                .get("front_default"));
        relevantPokemonContent.put("types", pokemon.getJSONArray("types"));
        return relevantPokemonContent;
    }
}
