package dto;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OrderRequest {
    public Map<String, List<String>> createOrderRequest(List<String> ingredientIds) {
        return Map.of("ingredients", ingredientIds);
    }

    public Map<String, List<String>> createOrderWithoutAuthRequest(List<String> ingredientIds) {
        return Map.of("ingredients", ingredientIds);
    }

    public Map<String, List<String>> createOrderWithoutIngredientsRequest() {
        return Map.of("ingredients", Collections.emptyList());
    }
}