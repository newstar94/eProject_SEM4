package com.example.DecorEcommerceProject.Controller;

import com.example.DecorEcommerceProject.Entities.Category;
import com.example.DecorEcommerceProject.Service.ICategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api")
public class CategoriesController {
    private final ICategoryService categoryService;
    public CategoriesController(ICategoryService categoryService){
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }
    @GetMapping("/category/{id}")
    public ResponseEntity<?> getCategoryByID(@PathVariable Long id){
        if(!categoryService.getCategoryByID(id).isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category with id "+id+" is not existed !");
        }else {
            return ResponseEntity.ok().body(categoryService.getCategoryByID(id));
        }
    }
    @PostMapping("/categories/add")
    public Category createCategory(@Validated @RequestBody Category category) {
        return categoryService.createCategory(category);
    }
    @DeleteMapping("/category/delete/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.deleteCategory(id));
    }


    @PutMapping("/category/save/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id,
                                                   @RequestBody Category category) {
        category = categoryService.updateCategory(id, category);
        return ResponseEntity.ok(category);
    }
}
