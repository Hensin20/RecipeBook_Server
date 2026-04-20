package org.example.recipebookserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class RecipeBookServerApplication {
    public static void main(String[] args){
        SpringApplication.run(RecipeBookServerApplication.class, args);
    }
}

@RestController
class TestController{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/ping-db")
    public String pingDatabase(){
        try{
            jdbcTemplate.execute("SELECT 1");
            return "Database connection OK";
        }catch (Exception e){
            return "Database connection failed: "+ e.getMessage();
        }
    }
}