package dto;
import lombok.Data;
@Data
public class UserCreateRequest {
    private String email;
    private String password;
    private String name;
}