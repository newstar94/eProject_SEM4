package com.example.DecorEcommerceProject.Controller;

import com.example.DecorEcommerceProject.Entities.DTO.DiscountDTO;
import com.example.DecorEcommerceProject.Entities.Discount;
import com.example.DecorEcommerceProject.Service.IDiscountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DiscountController {
    private final IDiscountService discountService;
    public DiscountController(IDiscountService discountService) {
        this.discountService = discountService;
    }
    @GetMapping("/discounts")
    public ResponseEntity<?> getAllDiscount() {
        if (discountService.getAllDiscount().size() == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("List discount is empty!");
        }
        return ResponseEntity.status(HttpStatus.OK).body(discountService.getAllDiscount());
    }
    @GetMapping("/discounts/get_by_product/{id}")
    public ResponseEntity<?> getAllDiscountByProductId(@PathVariable Long id) {
        if (discountService.getAllDiscountByProductId(id).size() == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No discount for product with id: " + id);
        }
        return ResponseEntity.status(HttpStatus.OK).body(discountService.getAllDiscountByProductId(id));
    }
    @GetMapping("/discount/{id}")
    public ResponseEntity<?> getDiscountById(@PathVariable Long id) {
        if (!discountService.getDiscountById(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Discount with id " + id + " is not existed !");
        } else {
            return ResponseEntity.ok().body(discountService.getDiscountById(id));
        }
    }
    @GetMapping("/discount/product/{id}")
    public ResponseEntity<?> getAllProductById(@PathVariable Long id) {
        return ResponseEntity.ok().body(discountService.getAllProductByDiscountId(id));
    }
    @PostMapping("/discount/create")
    public ResponseEntity<?> createDiscount(@Validated @RequestBody DiscountDTO discountDTO) {
        try {
            Discount discount = discountService.createDiscount(discountDTO);
            return ResponseEntity.ok(discount);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @PutMapping("/discount/update/{id}")
    public ResponseEntity<?> createDiscount(@Validated @PathVariable Long id, @RequestBody DiscountDTO discountDTO) {
        try {
            Discount discount = discountService.updateDiscount(id, discountDTO);
            if (discount != null) {
                return ResponseEntity.ok(discount);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
