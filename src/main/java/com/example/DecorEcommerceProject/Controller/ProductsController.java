package com.example.DecorEcommerceProject.Controller;

import com.example.DecorEcommerceProject.Entities.DTO.ProductDto;
import com.example.DecorEcommerceProject.Entities.Product;
import com.example.DecorEcommerceProject.Service.ICategoryService;
import com.example.DecorEcommerceProject.Service.IProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductsController {
    private final IProductService productService;
    public ProductsController(IProductService productService){
        this.productService = productService;
    }
    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        if (productService.getAllProducts().size()==0){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("List product is empty!");
        }
        return ResponseEntity.status(HttpStatus.OK).body(productService.getAllProducts());
    }
    @GetMapping("/search")
    public ResponseEntity<?> getAllProductsByKeyword(@RequestParam("keyword") String keyword) {
        if(productService.getAllProductsByKeyword(keyword).size() == 0){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("List product is empty!");
        }else{
            return ResponseEntity.ok().body(productService.getAllProductsByKeyword(keyword));
        }
    }
    @GetMapping("/product/{id}")
    public ResponseEntity<?> getProductByID(@PathVariable Long id){
        if(!productService.getProductByID(id).isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product with id "+id+" is not existed !");
        }else {
            return ResponseEntity.ok().body(productService.getProductByID(id));
        }
    }
    @GetMapping("/category/{cateId}")
    public ResponseEntity<?> getProductByCateID(@PathVariable Long cateId){
        if(productService.getAllProductByCategoryID(cateId) == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("List products is empty!");
        }else{
            return ResponseEntity.ok().body(productService.getAllProductByCategoryID(cateId));
        }
    }
    @GetMapping("/cateID-search")
    public ResponseEntity<?> getProductsByCateIDAndKeyword(@RequestParam("cateID") Long cateID,
                                                       @RequestParam("keyword") String keyword){
        if(productService.getAllProductByCateIDAndKeyword(cateID, keyword) == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("List products is empty!");
        }else{
            return ResponseEntity.ok().body(productService.getAllProductByCateIDAndKeyword(cateID, keyword));
        }
    }
    @PostMapping
        public ResponseEntity<?> createProduct(@ModelAttribute  ProductDto productDto,
                                               @RequestParam("imageFile") MultipartFile mainImageMultipart,
                                               @RequestParam("extraImages") List<MultipartFile> extraImagesMultipart
                                                ){
        try {
            Product newProduct = productService.createProduct(productDto, mainImageMultipart,extraImagesMultipart);
            return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.deleteProduct(id));
    }
    @PutMapping("/save/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Long id,
                                        @ModelAttribute  ProductDto productDto,
                                        @RequestParam(value = "imageFile", required = false) MultipartFile mainImageFile,
                                        @RequestParam(value = "extraImages", required = false) List<MultipartFile> extraImages) {
        try {
            Product updatedProduct = productService.updateProduct(id, productDto, mainImageFile, extraImages);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
