package com.example.DecorEcommerceProject.Service.Impl;

import com.example.DecorEcommerceProject.Entities.Category;
import com.example.DecorEcommerceProject.Entities.DTO.ProductDto;
import com.example.DecorEcommerceProject.Entities.DTO.SoldDTO;
import com.example.DecorEcommerceProject.Entities.Enum.ProductStatus;
import com.example.DecorEcommerceProject.Entities.Product;
import com.example.DecorEcommerceProject.Entities.ProductImage;
import com.example.DecorEcommerceProject.Repositories.CategoryRepository;
import com.example.DecorEcommerceProject.Repositories.ProductImageRepository;
import com.example.DecorEcommerceProject.Repositories.ProductRepository;
import com.example.DecorEcommerceProject.Service.IProductService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ProductServiceImpl implements IProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final CloudinaryService cloudinary;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository,
                              ProductImageRepository productImageRepository,
                              CloudinaryService cloudinary
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
        this.cloudinary = cloudinary;
    }

    @Override
    public Product createProduct(ProductDto productDto, MultipartFile imageFile, List<MultipartFile> extraImages) {
        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setInventory(productDto.getInventory());
        product.setStatus(ProductStatus.AVAILABLE);
        product.setPrice(productDto.getPrice());
        String mainImageUrl = cloudinary.saveProductImageToCloudinary(imageFile);
        product.setMainImage(mainImageUrl);
        product.setCreatedAt(LocalDateTime.now());
        long categoryId = productDto.getCategory();
        if (categoryId != 0) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            product.setCategory(category);
        }
        productRepository.save(product);
        try {
            saveExtraImages(extraImages, product);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return product;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getAllProductByCategoryID(Long cateID) {
        return productRepository.getAllProductByCategoryID(cateID);
    }

    @Override
    public Optional<Product> getProductByID(long id) {
        return productRepository.findById(id);
    }

    @Override
    public String deleteProduct(Long id) {
        Optional<Product> product = productRepository.findById(id);
        if (!product.isPresent()) {
            return "Not found product with id: " + id;
        } else {
            productRepository.delete(product.get());
            return "Product with id " + id + " has been deleted!";
        }
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, ProductDto productDto, MultipartFile mainImageFile, List<MultipartFile> extraImages) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        existingProduct.setName(productDto.getName());
        existingProduct.setDescription(productDto.getDescription());
        existingProduct.setInventory(productDto.getInventory());
        existingProduct.setStatus(ProductStatus.AVAILABLE);
        existingProduct.setPrice(productDto.getPrice());
        existingProduct.setUpdatedAt(LocalDateTime.now());
        long categoryId = productDto.getCategory();
        if (categoryId != 0) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            existingProduct.setCategory(category);
        } else {
            existingProduct.setCategory(null);
        }
        if (mainImageFile != null && !mainImageFile.isEmpty()) {
            try {

                String mainImageUrl = cloudinary.saveProductImageToCloudinary(mainImageFile);
                deleteMainImage(existingProduct);
                existingProduct.setMainImage(mainImageUrl);
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload main image");
            }
        }
        List<ProductImage> updatedExtraImages = new ArrayList<>();
        deleteProductImages(existingProduct);
        try {
            saveExtraImages(extraImages, existingProduct);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload extra image");
        }
        existingProduct.setImages(updatedExtraImages);
        existingProduct.setUpdatedAt(LocalDateTime.now());
        productRepository.save(existingProduct);
        return existingProduct;
    }

    private void saveExtraImages(List<MultipartFile> extraImages, Product product) {
        List<String> extraImageUrls = new ArrayList<>();
        for (MultipartFile extraImageFile : extraImages) {
            String extraImageUploadResult = cloudinary.saveProductImageToCloudinary(extraImageFile);
            extraImageUrls.add(extraImageUploadResult);
        }
        for (String extraImageUrl : extraImageUrls) {
            ProductImage extraImage = new ProductImage(extraImageUrl, product);
            productImageRepository.save(extraImage);
        }
    }

    private void deleteMainImage(Product product) {
        String mainImage = product.getMainImage();
        if (mainImage != null && !mainImage.isEmpty()) {
            cloudinary.deleteProductImageFromCloudinary(mainImage);
        }
    }

    private void deleteProductImages(Product product) {
        List<ProductImage> productImages = product.getImages();
        if (productImages != null && !productImages.isEmpty()) {
            for (ProductImage productImage : productImages) {
                // Delete the image from Cloudinary
                cloudinary.deleteProductImageFromCloudinary(productImage.getImageUrl());
            }
            productImageRepository.deleteByProductId(product.getId());
        }
    }

    @Override
    public List<Product> getAllProductsByKeyword(String keyword) {
        return productRepository.getAllProductsByKeyword(keyword);
    }

    @Override
    public List<Product> getAllProductByCateIDAndKeyword(long cateID, String keyword) {
        List<Product> getByKeyword = productRepository.getAllProductsByKeyword(keyword);
        List<Product> getByCateIDAndKeyword = new ArrayList<>();
        for (Product product : getByKeyword) {
            if (product.getCategory().getId() == cateID) {
                getByCateIDAndKeyword.add(product);
            }
        }
        return getByCateIDAndKeyword;
    }

    @Override
    public List<SoldDTO> getTopSold(int top) {
        List<SoldDTO> list = productRepository.getTopSelling();
        List<SoldDTO> topSold = new ArrayList<>();
        for (SoldDTO sold : list) {
            if (sold.getProduct().getStatus() != ProductStatus.UNAVAILABLE && sold.getProduct().getStatus() != ProductStatus.OUT_OF_STOCK) {
                topSold.add(sold);
            }
        }
        return topSold.subList(0, Math.min(topSold.size(), top));
    }

    @Override
    public List<SoldDTO> getAllTopSold(int top) {
        List<SoldDTO> topSold = productRepository.getTopSelling();
        return topSold.subList(0, Math.min(topSold.size(), top));
    }
}
