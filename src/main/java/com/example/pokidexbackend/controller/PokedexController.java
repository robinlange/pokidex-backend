package com.example.pokidexbackend.controller;

import com.example.pokidexbackend.services.PokedexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class PokedexController {

    private final PokedexService pokedexService;

    @Autowired
    public PokedexController(PokedexService pokedexService) {
        this.pokedexService = pokedexService;
    }

    @CrossOrigin
    @GetMapping("/pokemonsData")
    public String getPokemonsData(@RequestParam(name = "offset") int offset) {
        try {
            return pokedexService.getPokemonsData(offset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @CrossOrigin
    @GetMapping("/pokemonDetailData")
    public String getPokemonDetailData(@RequestParam(name = "id") int id) {
        try {
            return pokedexService.getPokemonDetailData(id);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @CrossOrigin
    @GetMapping("/pokemonEvolutionChainData")
    public String getEvolutionChainData(@RequestParam(name = "id") int id) {
        try {
            return pokedexService.getEvolutionChainData(id);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @CrossOrigin
    @GetMapping("/items")
    public String getItemsData(@RequestParam(name = "offset") int offset) {
        try {
            return pokedexService.getItemsData(offset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
