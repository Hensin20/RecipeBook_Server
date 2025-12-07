package org.example.recipebookserver;
import org.example.recipebookserver.model.User;
import org.example.recipebookserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

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

@RestController
@RequestMapping("/auth")
class AuthController {
   private final UserService userService;

   public AuthController(UserService userService){
        this.userService = userService;
   }

   @PostMapping("/register")
    public ResponseEntity<User>register(@RequestBody User user){
       return ResponseEntity.ok(userService.register(user));
   }
   @PostMapping("/login")
    public ResponseEntity<User>login(@RequestParam String email, @RequestParam String password){
        return ResponseEntity.ok(userService.login(email, password));
   }
}