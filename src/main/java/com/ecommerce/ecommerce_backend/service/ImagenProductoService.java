package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.ImagenProductoDTO;
import com.ecommerce.ecommerce_backend.entity.ImagenProducto;
import com.ecommerce.ecommerce_backend.entity.Producto;
import com.ecommerce.ecommerce_backend.exception.BadRequestException;
import com.ecommerce.ecommerce_backend.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.repository.ImagenProductoRepository;
import com.ecommerce.ecommerce_backend.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ImagenProductoService {

    @Autowired
    private ImagenProductoRepository imagenProductoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    private final Path rootPath = Paths.get("uploads");

    @Transactional
    public ImagenProductoDTO subirImagen(Integer productoId, MultipartFile file) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));

        if (file.isEmpty()) {
            throw new BadRequestException("El archivo está vacío");
        }

        try {
            if (!Files.exists(rootPath)) {
                Files.createDirectories(rootPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = this.rootPath.resolve(filename);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String url = "/uploads/" + filename;

            ImagenProducto img = new ImagenProducto();
            img.setUrl(url);
            img.setProducto(producto);
            img = imagenProductoRepository.save(img);

            return new ImagenProductoDTO(img.getId(), img.getUrl(), productoId);
        } catch (IOException e) {
            throw new RuntimeException("Error al almacenar el archivo: " + e.getMessage(), e);
        }
    }

    public List<ImagenProductoDTO> listarPorProducto(Integer productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new ResourceNotFoundException("Producto no encontrado con ID: " + productoId);
        }
        return imagenProductoRepository.findByProductoId(productoId)
                .stream()
                .map(img -> new ImagenProductoDTO(img.getId(), img.getUrl(), productoId))
                .collect(Collectors.toList());
    }

    @Transactional
    public void borrarImagen(Integer id) {
        ImagenProducto img = imagenProductoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Imagen no encontrada con ID: " + id));

        String filename = img.getUrl().replace("/uploads/", "");
        Path filePath = this.rootPath.resolve(filename);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("No se pudo borrar el archivo físico: " + filePath + ". Detalle: " + e.getMessage());
        }

        imagenProductoRepository.delete(img);
    }
}
