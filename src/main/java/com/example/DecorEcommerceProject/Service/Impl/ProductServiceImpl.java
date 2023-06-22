package com.example.DecorEcommerceProject.Service.Impl;

import com.example.DecorEcommerceProject.Entities.Category;
import com.example.DecorEcommerceProject.Entities.DTO.ProductDto;
import com.example.DecorEcommerceProject.Entities.Enum.ProductStatus;
import com.example.DecorEcommerceProject.Entities.Product;
import com.example.DecorEcommerceProject.Repositories.CategoryRepository;
import com.example.DecorEcommerceProject.Repositories.ProductRepository;
import com.example.DecorEcommerceProject.Service.IProductService;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ProductServiceImpl implements IProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository,CategoryRepository categoryRepository){
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Product createProduct( ProductDto productDto) {
        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setInventory(productDto.getInventory());
        product.setStatus(ProductStatus.AVAILABLE);
        product.setPrice(productDto.getPrice());
        product.setCreatedAt(LocalDateTime.now());
        long categoryId = productDto.getCategory();
        if(categoryId != 0){
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(()-> new EntityNotFoundException("Category not found"));
            product.setCategory(category);
        }
        return productRepository.save(product);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getAllProductByCategoryID(Long cateID) {
        return  productRepository.getAllProductByCategoryID(cateID);
    }

    @Override
    public Optional<Product> getProductByID(long id) {
        return productRepository.findById(id);
    }

    @Override
    public String deleteProduct(Long id) {
        Optional<Product> product = productRepository.findById(id);
        if(!product.isPresent()){
            return "Not found product with id: " +id;
        }else{
            productRepository.delete(product.get());
            return "Product with id "+id+ " has been deleted!";
        }
    }

    @Override
    public Product updateProduct(Long id, ProductDto productDto) {
        Product product = productRepository.findById(id).orElse(null);
        assert product != null;
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setInventory(productDto.getInventory());
        product.setStatus(ProductStatus.AVAILABLE);
        product.setPrice(productDto.getPrice());
        product.setUpdatedAt(LocalDateTime.now());
        long categoryId = productDto.getCategory();
        if(categoryId != 0){
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(()-> new EntityNotFoundException("Category not found"));
            product.setCategory(category);
        }
        else {
            product.setCategory(null);
        }
        return productRepository.save(product);
    }

    @Override
    public List<Product> getAllProductsByKeyword(String keyword) {
        return productRepository.getAllProductsByKeyword(keyword);
    }

    @Override
    public List<Product> getAllProductByCateIDAndKeyword(long cateID, String keyword) {
        List<Product> getByKeyword = productRepository.getAllProductsByKeyword(keyword);
        List<Product> getByCateIDAndKeyword = new ArrayList<>();
        for (Product product : getByKeyword){
            if(product.getCategory().getId() == cateID){
                getByCateIDAndKeyword.add(product);
            }
        }
        return getByCateIDAndKeyword;
    }

//    @Override
//    public List<ProductTopSellerDto> getTopSellerOfBook(int topNumber) {
//        List<Tuple> getTopSeller = productRepository.getTop_Number_Product_Best_Seller(topNumber);
//    }
}
