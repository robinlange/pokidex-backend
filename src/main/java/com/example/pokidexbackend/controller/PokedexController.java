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

    @Autowired
    private PokedexService pokedexService;

    @CrossOrigin
    @GetMapping("/pokemonsData")
    public String pokemon(@RequestParam(required = true, name = "offset") int offset) {
        try {
            return pokedexService.getData(offset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
