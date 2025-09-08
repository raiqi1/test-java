package com.example.demo.controller;

import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    private final BookRepository bookRepository;

    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    private ResponseEntity<Map<String, Object>> createResponse(HttpStatus status, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.value());
        response.put("message", message);
        response.put("data", data);
        return new ResponseEntity<>(response, status);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> createBook(@Valid @RequestBody Book book) {
        try {
            Book savedBook = bookRepository.save(book);
            return createResponse(HttpStatus.CREATED, "Book created successfully", savedBook);
        } catch (Exception e) {
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create book: " + e.getMessage(), null);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllBooks() {
        try {
            List<Book> books = bookRepository.findAll();
            if (books.isEmpty()) {
                return createResponse(HttpStatus.NO_CONTENT, "No books found", null);
            }
            return createResponse(HttpStatus.OK, "Books retrieved successfully", books);
        } catch (Exception e) {
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve books: " + e.getMessage(), null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBookById(@PathVariable("id") Long id) {
        Optional<Book> bookData = bookRepository.findById(id);
        
        if (bookData.isPresent()) {
            return createResponse(HttpStatus.OK, "Book found", bookData.get());
        } else {
            return createResponse(HttpStatus.NOT_FOUND, "Book not found with id: " + id, null);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> updateBook(@PathVariable("id") Long id, @Valid @RequestBody Book book) {
        Optional<Book> bookData = bookRepository.findById(id);
        
        if (bookData.isPresent()) {
            Book existingBook = bookData.get();
            existingBook.setTitle(book.getTitle());
            existingBook.setAuthor(book.getAuthor());
            existingBook.setIsbn(book.getIsbn());
            existingBook.setPublishedYear(book.getPublishedYear());
            existingBook.setDescription(book.getDescription());
            
            Book updatedBook = bookRepository.save(existingBook);
            return createResponse(HttpStatus.OK, "Book updated successfully", updatedBook);
        } else {
            return createResponse(HttpStatus.NOT_FOUND, "Book not found with id: " + id, null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteBook(@PathVariable("id") Long id) {
        try {
            if (bookRepository.existsById(id)) {
                bookRepository.deleteById(id);
                return createResponse(HttpStatus.OK, "Book deleted successfully", null);
            } else {
                return createResponse(HttpStatus.NOT_FOUND, "Book not found with id: " + id, null);
            }
        } catch (Exception e) {
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete book: " + e.getMessage(), null);
        }
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteAllBooks() {
        try {
            long count = bookRepository.count();
            if (count > 0) {
                bookRepository.deleteAll();
                return createResponse(HttpStatus.OK, "All books deleted successfully", null);
            } else {
                return createResponse(HttpStatus.OK, "No books to delete", null);
            }
        } catch (Exception e) {
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete all books: " + e.getMessage(), null);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> findBooksByTitle(@RequestParam("title") String title) {
        try {
            List<Book> books = bookRepository.findByTitleContainingIgnoreCase(title);
            if (books.isEmpty()) {
                return createResponse(HttpStatus.OK, "No books found with title containing: " + title, null);
            }
            return createResponse(HttpStatus.OK, "Books found with title containing: " + title, books);
        } catch (Exception e) {
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to search books: " + e.getMessage(), null);
        }
    }
}